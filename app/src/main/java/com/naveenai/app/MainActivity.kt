package com.naveenai.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.naveenai.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestAudioPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermissionLauncher()
        setupUi()
        requestRequiredRuntimePermissionsIfNeeded()
    }

    private fun setupPermissionLauncher() {
        requestAudioPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                updateStatus("Microphone ready")
            } else {
                updateStatus("Microphone permission denied")
                Toast.makeText(this, "Microphone access is required for voice features.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupUi() {
        binding.titleText.text = "NAVEEN AI"
        binding.statusText.text = "Ready"
        binding.listeningIndicator.text = "Listening standby"
        binding.settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings screen will be implemented in a future step.", Toast.LENGTH_SHORT).show()
        }
        binding.micButton.setOnClickListener {
            if (hasPermission(Manifest.permission.RECORD_AUDIO)) {
                updateStatus("Listening...")
                binding.listeningIndicator.text = "Wake word check"
            } else {
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun requestRequiredRuntimePermissionsIfNeeded() {
        if (!hasPermission(Manifest.permission.RECORD_AUDIO)) {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun updateStatus(status: String) {
        binding.statusText.text = status
    }
}
