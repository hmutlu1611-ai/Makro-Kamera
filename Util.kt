package com.makrokamera.app

import android.widget.SeekBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class SimpleSeekListener : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(s: SeekBar, p: Int, fromUser: Boolean) {}
    override fun onStartTrackingTouch(s: SeekBar) {}
    override fun onStopTrackingTouch(s: SeekBar) {}
}

fun timestamp(): String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
