package com.naveenai.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.naveenai.app.ai.AIResponse
import com.naveenai.app.ai.AssistantCoordinator
import com.naveenai.app.databinding.ActivityMainBinding
import com.naveenai.app.voice.SpeechRecognitionManager
import com.naveenai.app.voice.TextToSpeechManager
import com.naveenai.app.voice.VoiceState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), SpeechRecognitionManager.Listener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var requestAudioPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var speechRecognitionManager: SpeechRecognitionManager
    private lateinit var textToSpeechManager: TextToSpeechManager
    private val assistantCoordinator = AssistantCoordinator()
    private var processingJob: Job? = null
    private var voiceResponseEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPermissionLauncher()
        speechRecognitionManager = SpeechRecognitionManager(this, this)
        textToSpeechManager = TextToSpeechManager(this, object : TextToSpeechManager.Listener {
            override fun onReady() = Unit

            override fun onUnavailable(message: String) {
                onError(message)
            }
        })
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
            if (processingJob != null) return@setOnClickListener
            if (hasPermission(Manifest.permission.RECORD_AUDIO)) {
                startListening()
            } else {
                onStateChanged(VoiceState.REQUESTING_PERMISSION)
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
        binding.sendButton.setOnClickListener {
            val text = binding.commandInput.text?.toString().orEmpty().trim()
            if (text.isNotEmpty() && processingJob == null) {
                processCommand(text)
                binding.commandInput.text?.clear()
            }
        }
        binding.voiceToggle.setOnClickListener {
            voiceResponseEnabled = !voiceResponseEnabled
            binding.voiceToggle.text = if (voiceResponseEnabled) "Voice ON" else "Voice OFF"
            if (!voiceResponseEnabled) textToSpeechManager.stop()
        }
        binding.stopSpeakingButton.setOnClickListener { textToSpeechManager.stop() }
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
            VoiceState.AI_THINKING -> "Thinking..." to "Asking the local AI model"
            VoiceState.RESPONDING -> "Responding..." to "Preparing voice output"
            VoiceState.SUCCESS -> "Command received" to "Ready for another command"
            VoiceState.ERROR -> "Something went wrong" to "Try again or use text input"
        }
        binding.statusText.text = status
        binding.listeningIndicator.text = indicator
    }

    override fun onTextRecognized(text: String) {
        binding.recognizedText.text = text
        if (processingJob == null) processCommand(text)
    }

    override fun onError(message: String) {
        binding.errorText.text = message
        binding.errorText.visibility = View.VISIBLE
    }

    private fun processCommand(text: String) {
        if (processingJob != null) return
        onStateChanged(VoiceState.PROCESSING)
        binding.errorText.visibility = View.GONE
        binding.sendButton.isEnabled = false
        binding.micButton.isEnabled = false
        processingJob = lifecycleScope.launch {
            onStateChanged(VoiceState.AI_THINKING)
            try {
                displayResult(assistantCoordinator.process(text))
            } catch (_: Exception) {
                displayResult(AIResponse(false, "", "The assistant could not process that request."))
            } finally {
                processingJob = null
                binding.sendButton.isEnabled = true
                binding.micButton.isEnabled = true
            }
        }
    }

    private fun displayResult(result: AIResponse) {
        if (result.success) {
            onStateChanged(VoiceState.RESPONDING)
            binding.responseText.text = result.text
            if (voiceResponseEnabled) textToSpeechManager.speak(result.text)
            onStateChanged(VoiceState.SUCCESS)
        } else {
            val message = result.errorMessage ?: "The assistant could not generate a response."
            binding.responseText.text = message
            binding.errorText.text = message
            binding.errorText.visibility = View.VISIBLE
            onStateChanged(VoiceState.ERROR)
        }
    }

    override fun onDestroy() {
        processingJob?.cancel()
        speechRecognitionManager.release()
        textToSpeechManager.release()
        super.onDestroy()
    }
}
