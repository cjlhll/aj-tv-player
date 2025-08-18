package com.tvplayer.webdav.ui.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.tvplayer.webdav.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * 媒体扫描Fragment
 * 允许用户扫描WebDAV目录并刮削媒体信息
 */
@AndroidEntryPoint
class ScannerFragment : Fragment() {

    private val viewModel: ScannerViewModel by viewModels()

    private lateinit var btnStartScan: Button
    private lateinit var tvMoviesDir: TextView
    private lateinit var tvTvDir: TextView
    private lateinit var btnPickMoviesDir: Button
    private lateinit var btnPickTvDir: Button
    private lateinit var tvScanStatus: TextView
    private lateinit var tvScanProgress: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        btnStartScan = view.findViewById(R.id.btn_start_scan)
        tvScanStatus = view.findViewById(R.id.tv_scan_status)
        tvScanProgress = view.findViewById(R.id.tv_scan_progress)
        progressBar = view.findViewById(R.id.progress_bar)

        tvMoviesDir = view.findViewById(R.id.tv_movies_dir)
        tvTvDir = view.findViewById(R.id.tv_tv_dir)
        btnPickMoviesDir = view.findViewById(R.id.btn_pick_movies_dir)
        btnPickTvDir = view.findViewById(R.id.btn_pick_tv_dir)

        // 读取已保存的分类目录
        tvMoviesDir.text = viewModel.getMoviesDir() ?: "未设置"
        tvTvDir.text = viewModel.getTvDir() ?: "未设置"
    }

    private fun setupListeners() {
        btnPickMoviesDir.setOnClickListener {
            showPathSelectionDialog { chosen ->
                viewModel.setMoviesDir(chosen)
                tvMoviesDir.text = chosen
            }
        }

        btnPickTvDir.setOnClickListener {
            showPathSelectionDialog { chosen ->
                viewModel.setTvDir(chosen)
                tvTvDir.text = chosen
            }
        }

        btnStartScan.setOnClickListener {
            startScan()
        }
    }

    private fun observeViewModel() {
        // 启动按钮在设置了任一分类目录后可用
        fun canStart(): Boolean = !viewModel.getMoviesDir().isNullOrEmpty() || !viewModel.getTvDir().isNullOrEmpty()
        btnStartScan.isEnabled = canStart()
        // 当分类目录变化时也可以刷新按钮状态（如有需要可监听 LiveData）

        viewModel.isScanning.observe(viewLifecycleOwner) { isScanning ->
            btnStartScan.isEnabled = !isScanning && viewModel.canStartScan()
            progressBar.visibility = if (isScanning) View.VISIBLE else View.GONE
        }

        viewModel.scanProgress.observe(viewLifecycleOwner) { progress ->
            tvScanProgress.text = progress
        }

        viewModel.scanStatus.observe(viewLifecycleOwner) { status ->
            tvScanStatus.text = status
        }

        viewModel.scanResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    val count = it.getOrNull()?.size ?: 0
                    Toast.makeText(context, "扫描完成！找到 $count 个媒体文件", Toast.LENGTH_LONG).show()
                    // TODO: 导航回主界面或显示结果
                } else {
                    val error = it.exceptionOrNull()?.message ?: "扫描失败"
                    Toast.makeText(context, "扫描失败: $error", Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.browseError.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(context, "目录读取失败: $it", Toast.LENGTH_LONG).show() }
        }
    }

    private fun showPathSelectionDialog(onChosen: (String) -> Unit) {
        // 简易目录浏览器：加载当前路径并选择
        viewModel.loadCurrentDirectory()

        val activity = requireActivity()
        val builder = android.app.AlertDialog.Builder(activity)
        builder.setTitle("选择WebDAV路径")

        val files = mutableListOf<com.tvplayer.webdav.data.model.WebDAVFile>()
        val selectedPaths = mutableSetOf<String>()

        val adapter = object : android.widget.BaseAdapter() {
            override fun getCount(): Int = files.size + if ((viewModel.currentPath.value ?: "/") != "/") 1 else 0
            override fun getItem(position: Int): Any? = null
            override fun getItemId(position: Int): Long = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup?): View {
                val ctx = parent?.context
                val view = convertView ?: LayoutInflater.from(ctx).inflate(R.layout.item_webdav_entry, parent, false)
                val cb = view.findViewById<android.widget.CheckBox>(R.id.cb_select)
                val tv = view.findViewById<android.widget.TextView>(R.id.tv_name)

                if ((viewModel.currentPath.value ?: "/") != "/" && position == 0) {
                    tv.text = "[..]"
                    cb.visibility = View.INVISIBLE
                    view.setOnClickListener { viewModel.goUp() }
                    return view
                } else {
                    view.setOnClickListener(null)
                }

                val idx = if ((viewModel.currentPath.value ?: "/") != "/") position - 1 else position
                val file = files[idx]
                tv.text = if (file.isDirectory) "[${file.name}]" else file.name
                cb.visibility = if (file.isDirectory) View.GONE else View.VISIBLE
                cb.isChecked = selectedPaths.contains(file.path)
                // 避免触发两次：先移除旧监听
                cb.setOnCheckedChangeListener(null)
                cb.isChecked = selectedPaths.contains(file.path)
                cb.setOnClickListener { v ->
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    if (cb.isChecked) selectedPaths.add(file.path) else selectedPaths.remove(file.path)
                }
                // 点击整行：目录进入；文件不改变勾选，交给checkbox自身
                view.setOnClickListener {
                    if (file.isDirectory) {
                        viewModel.enterDirectory(file)
                    }
                }
                return view
            }
        }

        val dialog = builder.setAdapter(adapter, null)
        .setNegativeButton("取消", null)
        .setPositiveButton("确认选择") { _, _ ->
            val chosen = selectedPaths.firstOrNull() ?: (viewModel.currentPath.value ?: "/")
            onChosen.invoke(chosen)
            Toast.makeText(context, "已选择: $chosen", Toast.LENGTH_SHORT).show()
        }
        .create()

        // 观察目录变化，更新列表
        viewModel.directoryItems.observe(viewLifecycleOwner) { list ->
            files.clear()
            files.addAll(list)
            adapter.notifyDataSetChanged()
        }

        dialog.show()

        // 列表点击：目录进入、文件切换勾选（需在 show() 之后才能拿到 listView）
        dialog.listView?.setOnItemClickListener { parent, _, position, _ ->
            val atRoot = (viewModel.currentPath.value ?: "/") == "/"
            if (!atRoot && position == 0) {
                viewModel.goUp()
                return@setOnItemClickListener
            }
            val idx = if (atRoot) position else position - 1
            if (idx < 0 || idx >= files.size) return@setOnItemClickListener
            val file = files[idx]
            // 仅对目录处理点击，文件点击不改勾选，避免冲突
            if (file.isDirectory) {
                viewModel.enterDirectory(file)
            }
        }
    }

    private fun startScan() {
        val moviesDir = viewModel.getMoviesDir()
        val tvDir = viewModel.getTvDir()
        if (moviesDir.isNullOrEmpty() && tvDir.isNullOrEmpty()) {
            Toast.makeText(context, "请先设置电影或电视剧目录", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            viewModel.startScanMoviesAndTv(moviesDir, tvDir)
        }
    }

    companion object {
        fun newInstance() = ScannerFragment()
    }
}
