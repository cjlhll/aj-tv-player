package com.tvplayer.webdav.ui.webdav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.tvplayer.webdav.R
import com.tvplayer.webdav.data.model.WebDAVServer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * WebDAV连接配置Fragment
 * 允许用户输入服务器信息并测试连接
 */
@AndroidEntryPoint
class WebDAVConnectionFragment : Fragment() {

    private val viewModel: WebDAVConnectionViewModel by viewModels()

    private lateinit var etServerUrl: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etServerName: EditText
    private lateinit var btnConnect: Button
    private lateinit var btnTest: Button
    private lateinit var tvStatus: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_webdav_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupListeners()
        observeViewModel()

        // 如果已有已保存的服务器，预填充表单；否则填充示例数据
        viewModel.currentServer.value?.let { applyServerToForm(it) } ?: fillExampleData()
    }

    private fun initViews(view: View) {
        etServerUrl = view.findViewById(R.id.et_server_url)
        etUsername = view.findViewById(R.id.et_username)
        etPassword = view.findViewById(R.id.et_password)
        etServerName = view.findViewById(R.id.et_server_name)
        btnConnect = view.findViewById(R.id.btn_connect)
        btnTest = view.findViewById(R.id.btn_test)
        tvStatus = view.findViewById(R.id.tv_status)
    }

    private fun setupListeners() {
        btnTest.setOnClickListener {
            testConnection()
        }

        btnConnect.setOnClickListener {
            saveAndConnect()
        }
    }

    private fun observeViewModel() {
        viewModel.connectionStatus.observe(viewLifecycleOwner) { status ->
            tvStatus.text = status
        }

        viewModel.currentServer.observe(viewLifecycleOwner) { server ->
            server?.let { applyServerToForm(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            btnTest.isEnabled = !isLoading
            btnConnect.isEnabled = !isLoading

            if (isLoading) {
                tvStatus.text = "连接中..."
            }
        }

        viewModel.connectionResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    Toast.makeText(context, "连接成功！", Toast.LENGTH_SHORT).show()
                    // TODO: 导航到文件浏览界面
                } else {
                    val error = it.exceptionOrNull()?.message ?: "连接失败"
                    Toast.makeText(context, "连接失败: $error", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun testConnection() {
        val server = createServerFromInput()
        if (server != null) {
            lifecycleScope.launch {
                viewModel.testConnection(server)
            }
        }
    }

    private fun saveAndConnect() {
        val server = createServerFromInput()
        if (server != null) {
            lifecycleScope.launch {
                viewModel.saveAndConnect(server)
            }
        }
    }

    private fun createServerFromInput(): WebDAVServer? {
        val url = etServerUrl.text.toString().trim()
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val name = etServerName.text.toString().trim().ifEmpty { "默认服务器" }

        if (url.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "请填写所有必填字段", Toast.LENGTH_SHORT).show()
            return null
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            Toast.makeText(context, "URL必须以http://或https://开头", Toast.LENGTH_SHORT).show()
            return null
        }

        return WebDAVServer(
            name = name,
            url = url,
            username = username,
            password = password,
            isDefault = true
        )
    }

    private fun fillExampleData() {
        etServerName.setText("示例服务器")
        etServerUrl.setText("https://example.com/webdav")
        etUsername.setText("username")
        etPassword.setText("password")
        tvStatus.text = "请输入WebDAV服务器信息"
    }

    private fun applyServerToForm(server: WebDAVServer) {
        etServerName.setText(server.name)
        etServerUrl.setText(server.url)
        etUsername.setText(server.username)
        etPassword.setText(server.password)
        tvStatus.text = "已加载已保存的配置"
    }

    companion object {
        fun newInstance() = WebDAVConnectionFragment()
    }
}
