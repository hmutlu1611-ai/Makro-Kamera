package com.makrokamera.app

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ClickableViewAccessibility", "SetTextI18n")
class EditorActivity : AppCompatActivity() {

    companion object {
        private const val PREVIEW_MAX = 3000    // ekranda incelemek için
        private const val SAVE_MAX = 16000      // kayıt: tam çözünürlük
    }

    private lateinit var imageView: ZoomImageView
    private lateinit var progress: ProgressBar
    private lateinit var btnSave: Button

    private var sourceUri: Uri? = null
    private var original: Bitmap? = null
    private var processed: Bitmap? = null
    private var renderJob: Job? = null
    private var adjust = Adjust()
    private val sliders = mutableListOf<Pair<SeekBar, Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        imageView = findViewById(R.id.imageView)
        progress = findViewById(R.id.progress)
        btnSave = findViewById(R.id.btnSave)

        sourceUri = intent.data
        if (sourceUri == null) { finish(); return }

        slider(R.id.seekBrightness, R.id.labelBrightness, "Parlaklık", 100, { "${it - 100}" }) {
            adjust = adjust.copy(brightness = (it - 100).toFloat())
        }
        slider(R.id.seekContrast, R.id.labelContrast, "Kontrast", 100, { "${it - 100}" }) {
            adjust = adjust.copy(contrast = it / 100f)
        }
        slider(R.id.seekSaturation, R.id.labelSaturation, "Doygunluk", 100, { "${it - 100}" }) {
            adjust = adjust.copy(saturation = it / 100f)
        }
        slider(R.id.seekWarmth, R.id.labelWarmth, "Sıcaklık", 100, { "${it - 100}" }) {
            adjust = adjust.copy(warmth = (it - 100) * 0.4f)
        }
        slider(R.id.seekSharpness, R.id.labelSharpness, "Keskinlik", 0, { "$it" }) {
            adjust = adjust.copy(sharpness = it / 100f)
        }

        findViewById<Button>(R.id.btnRotate).setOnClickListener {
            adjust = adjust.copy(rotation = (adjust.rotation + 90) % 360)
            scheduleRender()
        }
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            adjust = Adjust()
            sliders.forEach { (s, init) -> s.progress = init }
            scheduleRender()
        }
        findViewById<Button>(R.id.btnCompare).setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> original?.let { imageView.setImageBitmapKeepZoom(it) }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                    processed?.let { imageView.setImageBitmapKeepZoom(it) }
            }
            true
        }
        btnSave.setOnClickListener { save() }

        loadPreview()
    }

    private fun slider(
        seekId: Int, labelId: Int, title: String, initial: Int,
        display: (Int) -> String, onValue: (Int) -> Unit
    ) {
        val seek = findViewById<SeekBar>(seekId)
        val label = findViewById<TextView>(labelId)
        seek.max = 200
        seek.progress = initial
        label.text = "$title: ${display(initial)}"
        sliders += seek to initial
        seek.setOnSeekBarChangeListener(object : SimpleSeekListener() {
            override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {
                label.text = "$title: ${display(p)}"
                onValue(p)
                scheduleRender()
            }
        })
    }

    private fun loadPreview() {
        val uri = sourceUri ?: return
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val bmp = withContext(Dispatchers.IO) {
                runCatching { ImageProcessor.decode(contentResolver, uri, PREVIEW_MAX) }.getOrNull()
            }
            progress.visibility = View.GONE
            if (bmp == null) {
                toast("Resim açılamadı"); finish(); return@launch
            }
            original = bmp
            processed = bmp
            imageView.setImageBitmapKeepZoom(bmp)
        }
    }

    private fun scheduleRender() {
        val src = original ?: return
        val a = adjust
        renderJob?.cancel()
        renderJob = lifecycleScope.launch {
            delay(80)
            val out = withContext(Dispatchers.Default) {
                ImageProcessor.process(src, a, recycleSource = false)
            }
            processed = out
            imageView.setImageBitmapKeepZoom(out)
        }
    }

    private fun save() {
        val uri = sourceUri ?: return
        val a = adjust
        btnSave.isEnabled = false
        progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            val saved = withContext(Dispatchers.Default) {
                var maxSide = SAVE_MAX
                var result: Uri? = null
                var done = false
                while (!done && maxSide >= 1024) {
                    try {
                        val full = ImageProcessor.decode(contentResolver, uri, maxSide)
                        val out = ImageProcessor.process(full, a, recycleSource = true)
                        result = saveJpeg(out)
                        out.recycle()
                        done = true
                    } catch (e: OutOfMemoryError) {
                        System.gc()
                        maxSide /= 2   // bellek yetmezse çözünürlüğü düşürüp tekrar dene
                    } catch (e: Exception) {
                        done = true
                    }
                }
                result
            }
            progress.visibility = View.GONE
            btnSave.isEnabled = true
            toast(if (saved != null) "Kaydedildi: Resimler/MakroKamera" else "Kaydedilemedi")
        }
    }

    private fun saveJpeg(bmp: Bitmap): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "DUZENLENMIS_${timestamp()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MakroKamera")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
        contentResolver.openOutputStream(uri)?.use { bmp.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        contentResolver.update(uri, values, null, null)
        return uri
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}
