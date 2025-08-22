package com.tvplayer.webdav.ui.player

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.SubtitleConfig
import com.tvplayer.webdav.data.model.SubtitlePosition
import com.tvplayer.webdav.data.model.SubtitleAlignment

/**
 * 字幕配置对话框
 * 允许用户调整字幕样式、位置和行为设置
 */
class SubtitleConfigDialog(
    context: Context,
    private val currentConfig: SubtitleConfig,
    private val onConfigChanged: (SubtitleConfig) -> Unit
) : Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
    
    private lateinit var enabledSwitch: Switch
    private lateinit var textSizeSeekBar: SeekBar
    private lateinit var textSizeValue: TextView
    private lateinit var positionSpinner: Spinner
    private lateinit var alignmentSpinner: Spinner
    private lateinit var languageSpinner: Spinner
    private lateinit var offsetSeekBar: SeekBar
    private lateinit var offsetValue: TextView
    private lateinit var autoDownloadSwitch: Switch
    private lateinit var previewText: TextView
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var resetButton: Button
    
    private var workingConfig = currentConfig.copy()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupDialog()
    }
    
    private fun setupDialog() {
        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_subtitle_config, null)
        setContentView(contentView)
        
        initViews(contentView)
        setupSpinners()
        setupSeekBars()
        setupSwitches()
        setupButtons()
        updatePreview()
    }
    
    private fun initViews(view: View) {
        enabledSwitch = view.findViewById(R.id.switch_enabled)
        textSizeSeekBar = view.findViewById(R.id.seekbar_text_size)
        textSizeValue = view.findViewById(R.id.text_size_value)
        positionSpinner = view.findViewById(R.id.spinner_position)
        alignmentSpinner = view.findViewById(R.id.spinner_alignment)
        languageSpinner = view.findViewById(R.id.spinner_language)
        offsetSeekBar = view.findViewById(R.id.seekbar_offset)
        offsetValue = view.findViewById(R.id.text_offset_value)
        autoDownloadSwitch = view.findViewById(R.id.switch_auto_download)
        previewText = view.findViewById(R.id.text_preview)
        saveButton = view.findViewById(R.id.btn_save)
        cancelButton = view.findViewById(R.id.btn_cancel)
        resetButton = view.findViewById(R.id.btn_reset)
    }
    
    private fun setupSpinners() {
        // 位置选择器
        val positions = arrayOf("顶部", "中央", "底部")
        val positionAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, positions)
        positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        positionSpinner.adapter = positionAdapter
        positionSpinner.setSelection(when (workingConfig.position) {
            SubtitlePosition.TOP -> 0
            SubtitlePosition.CENTER -> 1
            SubtitlePosition.BOTTOM -> 2
        })
        positionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                workingConfig = workingConfig.copy(position = when (position) {
                    0 -> SubtitlePosition.TOP
                    1 -> SubtitlePosition.CENTER
                    else -> SubtitlePosition.BOTTOM
                })
                updatePreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 对齐方式选择器
        val alignments = arrayOf("左对齐", "居中", "右对齐")
        val alignmentAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, alignments)
        alignmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        alignmentSpinner.adapter = alignmentAdapter
        alignmentSpinner.setSelection(when (workingConfig.alignment) {
            SubtitleAlignment.LEFT -> 0
            SubtitleAlignment.CENTER -> 1
            SubtitleAlignment.RIGHT -> 2
        })
        alignmentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                workingConfig = workingConfig.copy(alignment = when (position) {
                    0 -> SubtitleAlignment.LEFT
                    1 -> SubtitleAlignment.CENTER
                    else -> SubtitleAlignment.RIGHT
                })
                updatePreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 语言选择器
        val languages = arrayOf("简体中文", "繁体中文", "English", "日本語", "한국어")
        val languageAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, languages)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter
        languageSpinner.setSelection(when (workingConfig.primaryLanguage) {
            "zh-cn" -> 0
            "zh-tw" -> 1
            "en" -> 2
            "ja" -> 3
            "ko" -> 4
            else -> 0
        })
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val (primary, fallback) = when (position) {
                    0 -> Pair("zh-cn", "zh")
                    1 -> Pair("zh-tw", "zh")
                    2 -> Pair("en", "en-us")
                    3 -> Pair("ja", "en")
                    4 -> Pair("ko", "en")
                    else -> Pair("zh-cn", "zh")
                }
                workingConfig = workingConfig.copy(
                    primaryLanguage = primary,
                    fallbackLanguage = fallback
                )
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupSeekBars() {
        // 字体大小
        textSizeSeekBar.max = 30 // 10-40sp范围
        textSizeSeekBar.progress = (workingConfig.textSize - 10).toInt()
        textSizeValue.text = "${workingConfig.textSize.toInt()}sp"
        textSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = progress + 10
                workingConfig = workingConfig.copy(textSize = size.toFloat())
                textSizeValue.text = "${size}sp"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // 时间偏移
        offsetSeekBar.max = 100 // -5000ms to +5000ms
        val currentOffsetProgress = ((workingConfig.globalOffsetMs + 5000) / 100).toInt()
        offsetSeekBar.progress = currentOffsetProgress
        offsetValue.text = "${workingConfig.globalOffsetMs}ms"
        offsetSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val offset = (progress * 100) - 5000L
                workingConfig = workingConfig.copy(globalOffsetMs = offset)
                offsetValue.text = "${offset}ms"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupSwitches() {
        // 启用字幕
        enabledSwitch.isChecked = workingConfig.isEnabled
        enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            workingConfig = workingConfig.copy(isEnabled = isChecked)
            updatePreview()
        }
        
        // 自动下载
        autoDownloadSwitch.isChecked = workingConfig.autoDownload
        autoDownloadSwitch.setOnCheckedChangeListener { _, isChecked ->
            workingConfig = workingConfig.copy(autoDownload = isChecked)
        }
    }
    
    private fun setupButtons() {
        saveButton.setOnClickListener {
            onConfigChanged(workingConfig)
            dismiss()
        }
        
        cancelButton.setOnClickListener {
            dismiss()
        }
        
        resetButton.setOnClickListener {
            workingConfig = SubtitleConfig.getDefault()
            updateAllControls()
            updatePreview()
        }
    }
    
    private fun updateAllControls() {
        enabledSwitch.isChecked = workingConfig.isEnabled
        textSizeSeekBar.progress = (workingConfig.textSize - 10).toInt()
        textSizeValue.text = "${workingConfig.textSize.toInt()}sp"
        
        positionSpinner.setSelection(when (workingConfig.position) {
            SubtitlePosition.TOP -> 0
            SubtitlePosition.CENTER -> 1
            SubtitlePosition.BOTTOM -> 2
        })
        
        alignmentSpinner.setSelection(when (workingConfig.alignment) {
            SubtitleAlignment.LEFT -> 0
            SubtitleAlignment.CENTER -> 1
            SubtitleAlignment.RIGHT -> 2
        })
        
        languageSpinner.setSelection(when (workingConfig.primaryLanguage) {
            "zh-cn" -> 0
            "zh-tw" -> 1
            "en" -> 2
            "ja" -> 3
            "ko" -> 4
            else -> 0
        })
        
        val offsetProgress = ((workingConfig.globalOffsetMs + 5000) / 100).toInt()
        offsetSeekBar.progress = offsetProgress
        offsetValue.text = "${workingConfig.globalOffsetMs}ms"
        
        autoDownloadSwitch.isChecked = workingConfig.autoDownload
    }
    
    private fun updatePreview() {
        previewText.apply {
            text = "字幕预览效果\nSubtitle Preview Effect"
            textSize = workingConfig.textSize
            setTextColor(workingConfig.textColor)
            
            // 应用阴影效果
            if (workingConfig.shadowRadius > 0) {
                setShadowLayer(
                    workingConfig.shadowRadius,
                    workingConfig.shadowOffsetX,
                    workingConfig.shadowOffsetY,
                    workingConfig.shadowColor
                )
            }
            
            // 应用对齐方式
            gravity = when (workingConfig.alignment) {
                SubtitleAlignment.LEFT -> android.view.Gravity.START
                SubtitleAlignment.CENTER -> android.view.Gravity.CENTER
                SubtitleAlignment.RIGHT -> android.view.Gravity.END
            }
            
            // 应用启用状态
            alpha = if (workingConfig.isEnabled) 1.0f else 0.5f
        }
    }
}