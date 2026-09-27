package com.example.notifyrelay

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notifyrelay.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var appListAdapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.editApiUrl.setText(Prefs.getApiUrl(this))
        binding.editParamTitle.setText(Prefs.getParamTitle(this))
        binding.editParamBody.setText(Prefs.getParamBody(this))

        val logLimitValues = resources.getIntArray(R.array.log_limit_values)
        val currentLimit = Prefs.getLogLimit(this)
        val currentLimitIndex = logLimitValues.indexOf(currentLimit).let { if (it < 0) 2 else it } // default: 100件
        binding.spinnerLogLimit.setSelection(currentLimitIndex)

        binding.switchSkipOngoing.isChecked = Prefs.getSkipOngoing(this)
        binding.editExcludeKeywords.setText(Prefs.getExcludeKeywordsRaw(this))

        val selected = Prefs.getSelectedPackages(this)
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { it.packageName != packageName }
            .map {
                AppEntry(
                    packageName = it.packageName,
                    label = it.loadLabel(pm).toString(),
                    icon = it.loadIcon(pm),
                    checked = selected.contains(it.packageName)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toMutableList()

        appListAdapter = AppListAdapter(apps)
        binding.recyclerApps.layoutManager = LinearLayoutManager(this)
        binding.recyclerApps.adapter = appListAdapter

        binding.buttonSave.setOnClickListener {
            Prefs.setApiUrl(this, binding.editApiUrl.text.toString().trim())
            Prefs.setParamTitle(this, binding.editParamTitle.text.toString().trim().ifEmpty { "title" })
            Prefs.setParamBody(this, binding.editParamBody.text.toString().trim().ifEmpty { "body" })
            Prefs.setSelectedPackages(this, appListAdapter.getSelectedPackages())

            val selectedLimit = logLimitValues[binding.spinnerLogLimit.selectedItemPosition]
            Prefs.setLogLimit(this, selectedLimit)
            RelayLog.setMaxEntries(selectedLimit)

            Prefs.setSkipOngoing(this, binding.switchSkipOngoing.isChecked)
            Prefs.setExcludeKeywordsRaw(this, binding.editExcludeKeywords.text.toString())

            Toast.makeText(this, "保存しました", Toast.LENGTH_SHORT).show()
        }

        binding.buttonOpenNotificationSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.buttonOpenBatterySettings.setOnClickListener {
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        }

        updateStatusLabels()
    }

    override fun onResume() {
        super.onResume()
        updateStatusLabels()
    }

    private fun updateStatusLabels() {
        val notifGranted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)
        binding.textNotificationStatus.text =
            if (notifGranted) "通知アクセス: 許可済み" else "通知アクセス: 未許可（下のボタンから許可してください）"

        val pmSys = getSystemService(PowerManager::class.java)
        val batteryIgnored = pmSys?.isIgnoringBatteryOptimizations(packageName) ?: false
        binding.textBatteryStatus.text =
            if (batteryIgnored) "バッテリー最適化: 除外済み" else "バッテリー最適化: 対象（除外を推奨）"
    }
}
