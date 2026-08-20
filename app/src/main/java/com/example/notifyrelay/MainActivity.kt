package com.example.notifyrelay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notifyrelay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var logAdapter: LogAdapter
    private val logListener: () -> Unit = { refreshLog() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        logAdapter = LogAdapter()
        binding.recyclerLog.layoutManager = LinearLayoutManager(this)
        binding.recyclerLog.adapter = logAdapter

        binding.switchEnabled.isChecked = Prefs.isEnabled(this)
        binding.switchEnabled.setOnCheckedChangeListener { _, checked ->
            Prefs.setEnabled(this, checked)
        }

        binding.buttonTestSend.setOnClickListener {
            ApiSender.send(this, packageName, "テスト通知", "これはテスト送信です")
        }

        binding.buttonSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        refreshLog()
    }

    override fun onStart() {
        super.onStart()
        RelayLog.addListener(logListener)
        refreshLog()
    }

    override fun onStop() {
        super.onStop()
        RelayLog.removeListener(logListener)
    }

    private fun refreshLog() {
        runOnUiThread {
            logAdapter.submitList(RelayLog.getAll())
        }
    }
}
