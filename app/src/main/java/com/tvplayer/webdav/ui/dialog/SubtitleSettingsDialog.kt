package com.tvplayer.webdav.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.*
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.SubtitleConfig

/**
 * 字幕设置对话框
 */
class SubtitleSettingsDialog(
    context: Context,
    private val currentConfig: SubtitleConfig,
    private val onConfigChanged: (SubtitleConfig) -> Unit
) : Dialog(context) {

    private lateinit var fontSizeSeekBar: SeekBar
    private lateinit var fontSizeText: TextView
    private lateinit var timeOffsetSeekBar: SeekBar
    private lateinit var timeOffsetText: TextView
    private lateinit var fontColorSpinner: Spinner
    private lateinit var backgroundColorSpinner: Spinner
    private lateinit var positionSpinner: Spinner
    private lateinit var previewText: TextView
    
    private var tempConfig = currentConfig.copy()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_subtitle_settings)
        
        initViews()
        setupControls()
        updatePreview()
    }

    private fun initViews() {
        fontSizeSeekBar = findViewById(R.id.seekbar_font_size)
        fontSizeText = findViewById(R.id.text_font_size)
        timeOffsetSeekBar = findViewById(R.id.seekbar_time_offset)
        timeOffsetText = findViewById(R.id.text_time_offset)
        fontColorSpinner = findViewById(R.id.spinner_font_color)
        backgroundColorSpinner = findViewById(R.id.spinner_background_color)
        positionSpinner = findViewById(R.id.spinner_position)
        previewText = findViewById(R.id.text_preview)
        
        findViewById<Button>(R.id.btn_reset).setOnClickListener {
            resetToDefault()
        }
        
        findViewById<Button>(R.id.btn_cancel).setOnClickListener {
            dismiss()
        }
        
        findViewById<Button>(R.id.btn_confirm).setOnClickListener {
            onConfigChanged(tempConfig)
            dismiss()
        }
    }

    private fun setupControls() {
        // 字体大小设置
        fontSizeSeekBar.max = 40
        fontSizeSeekBar.progress = (tempConfig.textSize.toInt() - 10).coerceIn(0, 40)
        fontSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    tempConfig = tempConfig.copy(textSize = (progress + 10).toFloat())
                    updateFontSizeText()
                    updatePreview()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // 时间偏移设置
        timeOffsetSeekBar.max = 600 // -30s 到 +30s，精度0.1s
        val offsetSeconds = tempConfig.globalOffsetMs / 1000.0f
        timeOffsetSeekBar.progress = ((offsetSeconds + 30) * 10).toInt().coerceIn(0, 600)
        timeOffsetSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val newOffsetSeconds = (progress / 10.0f) - 30f
                    tempConfig = tempConfig.copy(globalOffsetMs = (newOffsetSeconds * 1000).toLong())
                    updateTimeOffsetText()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // 字体颜色设置
        val fontColors = arrayOf("白色", "黄色", "红色", "绿色", "蓝色", "黑色")
        val fontColorAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, fontColors)
        fontColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fontColorSpinner.adapter = fontColorAdapter
        fontColorSpinner.setSelection(getColorIndex(tempConfig.textColor))
        fontColorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                tempConfig = tempConfig.copy(textColor = getColorFromIndex(position))
                updatePreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 背景颜色设置
        val backgroundColors = arrayOf("透明", "半透明黑色", "黑色", "半透明白色")
        val backgroundColorAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, backgroundColors)
        backgroundColorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        backgroundColorSpinner.adapter = backgroundColorAdapter
        backgroundColorSpinner.setSelection(getBackgroundColorIndex(tempConfig.backgroundColor))
        backgroundColorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                tempConfig = tempConfig.copy(backgroundColor = getBackgroundColorFromIndex(position))
                updatePreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 位置设置
        val positions = arrayOf("底部", "顶部", "中间")
        val positionAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, positions)
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        positionSpinner.adapter = positionAdapter
        positionSpinner.setSelection(getPositionIndex(tempConfig.position))
        positionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                tempConfig = tempConfig.copy(position = getPositionFromIndex(position))
                updatePreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        updateFontSizeText()
        updateTimeOffsetText()
    }

    private fun updateFontSizeText() {
        fontSizeText.text = "${tempConfig.textSize.toInt()}sp"
    }

    private fun updateTimeOffsetText() {
        val offsetSeconds = tempConfig.globalOffsetMs / 1000.0f
        timeOffsetText.text = String.format("%.1fs", offsetSeconds)
    }

    private fun updatePreview() {
        previewText.textSize = tempConfig.textSize
        previewText.setTextColor(tempConfig.textColor)
        previewText.setBackgroundColor(tempConfig.backgroundColor)
    }

    private fun resetToDefault() {
        tempConfig = SubtitleConfig.getDefault()
        fontSizeSeekBar.progress = (tempConfig.textSize.toInt() - 10).coerceIn(0, 40)
        val offsetSeconds = tempConfig.globalOffsetMs / 1000.0f
        timeOffsetSeekBar.progress = ((offsetSeconds + 30) * 10).toInt().coerceIn(0, 600)
        fontColorSpinner.setSelection(getColorIndex(tempConfig.textColor))
        backgroundColorSpinner.setSelection(getBackgroundColorIndex(tempConfig.backgroundColor))
        positionSpinner.setSelection(getPositionIndex(tempConfig.position))
        updateFontSizeText()
        updateTimeOffsetText()
        updatePreview()
    }

    private fun getColorIndex(color: Int): Int {
        return when (color) {
            Color.WHITE -> 0
            Color.YELLOW -> 1
            Color.RED -> 2
            Color.GREEN -> 3
            Color.BLUE -> 4
            Color.BLACK -> 5
            else -> 0
        }
    }

    private fun getColorFromIndex(index: Int): Int {
        return when (index) {
            0 -> Color.WHITE
            1 -> Color.YELLOW
            2 -> Color.RED
            3 -> Color.GREEN
            4 -> Color.BLUE
            5 -> Color.BLACK
            else -> Color.WHITE
        }
    }

    private fun getBackgroundColorIndex(color: Int): Int {
        return when (color) {
            Color.TRANSPARENT -> 0
            Color.parseColor("#80000000") -> 1
            Color.BLACK -> 2
            Color.parseColor("#80FFFFFF") -> 3
            else -> 0
        }
    }

    private fun getBackgroundColorFromIndex(index: Int): Int {
        return when (index) {
            0 -> Color.TRANSPARENT
            1 -> Color.parseColor("#80000000")
            2 -> Color.BLACK
            3 -> Color.parseColor("#80FFFFFF")
            else -> Color.TRANSPARENT
        }
    }

    private fun getPositionIndex(position: com.tvplayer.webdav.data.model.SubtitlePosition): Int {
        return when (position) {
            com.tvplayer.webdav.data.model.SubtitlePosition.BOTTOM -> 0
            com.tvplayer.webdav.data.model.SubtitlePosition.TOP -> 1
            com.tvplayer.webdav.data.model.SubtitlePosition.CENTER -> 2
        }
    }

    private fun getPositionFromIndex(index: Int): com.tvplayer.webdav.data.model.SubtitlePosition {
        return when (index) {
            0 -> com.tvplayer.webdav.data.model.SubtitlePosition.BOTTOM
            1 -> com.tvplayer.webdav.data.model.SubtitlePosition.TOP
            2 -> com.tvplayer.webdav.data.model.SubtitlePosition.CENTER
            else -> com.tvplayer.webdav.data.model.SubtitlePosition.BOTTOM
        }
    }
}
