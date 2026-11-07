package com.example.fontboyutu

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*

class MainActivity : Activity() {

    private lateinit var info: TextView
    private lateinit var seek: SeekBar
    private lateinit var applyBtn: Button
    private lateinit var permBtn: Button

    private fun toScale(p: Int) = 0.5f + p * 0.05f
    private fun toProgress(s: Float) = ((s - 0.5f) / 0.05f).toInt().coerceIn(0, 70)

    private fun currentScale(): Float =
        Settings.System.getFloat(contentResolver, Settings.System.FONT_SCALE, 1.0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
            gravity = Gravity.CENTER_HORIZONTAL
        }
        info = TextView(this).apply { textSize = 18f }
        seek = SeekBar(this).apply { max = 70 }
        applyBtn = Button(this).apply { text = "Uygula" }
        val resetBtn = Button(this).apply { text = "Sıfırla (1.0)" }
        permBtn = Button(this).apply { text = "İzin ver (Sistem ayarlarını değiştir)" }
        val preview = TextView(this).apply {
            text = "Örnek yazı: Bu yazı seçtiğiniz boyutta görünür."
            textSize = 16f
        }

        val lp = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = pad }
        listOf(info, seek, preview, applyBtn, resetBtn, permBtn).forEach { root.addView(it, lp) }
        setContentView(ScrollView(this).apply { addView(root) })

        seek.progress = toProgress(currentScale())
        updateInfo(toScale(seek.progress))

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                val v = toScale(p)
                updateInfo(v)
                preview.textSize = 16f * v / currentScale().coerceAtLeast(0.1f)
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        applyBtn.setOnClickListener { apply(toScale(seek.progress)) }
        resetBtn.setOnClickListener { seek.progress = toProgress(1.0f); apply(1.0f) }
        permBtn.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName"))
            )
        }
    }

    override fun onResume() {
        super.onResume()
        permBtn.isEnabled = !Settings.System.canWrite(this)
    }

    private fun updateInfo(v: Float) {
        info.text = "Yazı ölçeği: %.2f".format(v)
    }

    private fun apply(v: Float) {
        if (!Settings.System.canWrite(this)) {
            Toast.makeText(this, "Önce izin vermelisiniz", Toast.LENGTH_LONG).show()
            return
        }
        try {
            Settings.System.putFloat(contentResolver, Settings.System.FONT_SCALE, v)
        } catch (e: Exception) {
            Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
