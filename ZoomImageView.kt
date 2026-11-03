package com.makrokamera.app

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min

/** İki parmakla 50 kata kadar büyütme, kaydırma ve çift dokunma ile yakınlaştırma. */
class ZoomImageView @JvmOverloads constructor(
    ctx: Context, attrs: AttributeSet? = null
) : AppCompatImageView(ctx, attrs) {

    private val m = Matrix()
    private val v = FloatArray(9)
    private var baseScale = 1f
    private var bw = 0
    private var bh = 0

    private val scaleDetector = ScaleGestureDetector(ctx,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(d: ScaleGestureDetector): Boolean {
                val cur = currentScale()
                val target = (cur * d.scaleFactor).coerceIn(baseScale, baseScale * 50f)
                val f = target / cur
                m.postScale(f, f, d.focusX, d.focusY)
                apply()
                return true
            }
        })

    private val gestures = GestureDetector(ctx, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
            m.postTranslate(-dx, -dy)
            apply()
            return true
        }
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (currentScale() > baseScale * 1.1f) fit() else {
                m.postScale(4f, 4f, e.x, e.y)
                apply()
            }
            return true
        }
    })

    init { scaleType = ScaleType.MATRIX }

    fun setImageBitmapKeepZoom(b: Bitmap) {
        val same = b.width == bw && b.height == bh
        super.setImageBitmap(b)
        bw = b.width; bh = b.height
        if (same) apply() else fit()
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        fit()
    }

    private fun fit() {
        if (bw == 0 || width == 0 || height == 0) return
        baseScale = min(width / bw.toFloat(), height / bh.toFloat())
        m.reset()
        m.postScale(baseScale, baseScale)
        m.postTranslate((width - bw * baseScale) / 2f, (height - bh * baseScale) / 2f)
        apply()
    }

    private fun currentScale(): Float { m.getValues(v); return v[Matrix.MSCALE_X] }

    private fun apply() {
        m.getValues(v)
        val s = v[Matrix.MSCALE_X]
        val iw = bw * s
        val ih = bh * s
        v[Matrix.MTRANS_X] = if (iw <= width) (width - iw) / 2f else v[Matrix.MTRANS_X].coerceIn(width - iw, 0f)
        v[Matrix.MTRANS_Y] = if (ih <= height) (height - ih) / 2f else v[Matrix.MTRANS_Y].coerceIn(height - ih, 0f)
        m.setValues(v)
        // Çok büyütünce gerçek pikselleri bulanıklaştırmadan göster
        (drawable as? BitmapDrawable)?.let { it.isFilterBitmap = s < 2f }
        imageMatrix = m
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(e)
        gestures.onTouchEvent(e)
        return true
    }
}
