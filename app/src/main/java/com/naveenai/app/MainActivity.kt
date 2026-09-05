package com.naveenai.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.naveenai.app.command.CommandResult
import com.naveenai.app.command.CommandRouter
import com.naveenai.app.databinding.ActivityMainBinding
import com.naveenai.app.voice.SpeechRecognitionManager
import com.naveenai.app.voice.VoiceState

class MainActivity : AppCompatActivity(), SpeechRecognitionManager.Listener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestAudioPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private val commandRouter = CommandRouter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermissionLauncher()
        speechRecognitionManager = SpeechRecognitionManager(this, this)
        setupUi()
    }

    private fun setupPermissionLauncher() {
        requestAudioPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                startListening()
            } else {
                onStateChanged(VoiceState.ERROR)
                val message = if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    "Microphone permission is required for voice commands."
                } else {
                    "Microphone permission is denied. Enable it in Android settings to use voice commands."
                }
                onError(message)
            }
        }
    }

    private fun setupUi() {
        binding.titleText.text = "NAVEEN AI"
        onStateChanged(VoiceState.IDLE)
        binding.settingsButton.setOnClickListener {
            Toast.makeText(this, "Settings screen will be implemented in a future step.", Toast.LENGTH_SHORT).show()
        }
        binding.micButton.setOnClickListener {
            if (hasPermission(Manifest.permission.RECORD_AUDIO)) {
                startListening()
            } else {
                onStateChanged(VoiceState.REQUESTING_PERMISSION)
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
        binding.sendButton.setOnClickListener {
            val text = binding.commandInput.text?.toString().orEmpty().trim()
            if (text.isNotEmpty()) {
                processCommand(text)
                binding.commandInput.text?.clear()
            }
        }
    }

    private fun startListening() {
        if (!speechRecognitionManager.isAvailable()) {
            onStateChanged(VoiceState.ERROR)
            onError("Speech recognition is not available on this device.")
        } else {
            speechRecognitionManager.startListening()
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onStateChanged(state: VoiceState) {
        val (status, indicator) = when (state) {
            VoiceState.IDLE -> "Ready" to "Tap the microphone or type a command"
            VoiceState.REQUESTING_PERMISSION -> "Microphone permission needed" to "Waiting for permission"
            VoiceState.LISTENING -> "Listening..." to "Speak your command"
            VoiceState.PROCESSING -> "Thinking..." to "Processing command"
            VoiceState.SUCCESS -> "Command received" to "Ready for another command"
            VoiceState.ERROR -> "Something went wrong" to "Try again or use text input"
        }
        binding.statusText.text = status
        binding.listeningIndicator.text = indicator
    }

    override fun onTextRecognized(text: String) {
        binding.recognizedText.text = text
        processCommand(text)
    }

    override fun onError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = android.view.View.VISIBLE
    }

    private fun processCommand(text: String) {
        onStateChanged(VoiceState.PROCESSING)
        binding.errorText.visibility = android.view.View.GONE
        val result = commandRouter.route(text)
        displayResult(result)
    }

    private fun displayResult(result: CommandResult) {
        binding.responseText.text = result.response
        onStateChanged(if (result.success) VoiceState.SUCCESS else VoiceState.ERROR)
        if (!result.success) {
            binding.errorText.text = result.response
            binding.errorText.visibility = android.view.View.VISIBLE
        }
    }

    override fun onDestroy() {
        speechRecognitionManager.release()
        super.onDestroy()
    }
}
