package com.mate.subsmate.ui.receipt_scan

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ReceiptScanUiState(
    val imageUri: Uri? = null,
    val recognizedText: String = "",
    val extractedName: String = "",
    val extractedPrice: String = "",
    val isProcessing: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false
)

class ReceiptScanViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptScanUiState())
    val uiState: StateFlow<ReceiptScanUiState> = _uiState.asStateFlow()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun onImageCaptured(uri: Uri, context: Context) {
        _uiState.update { it.copy(imageUri = uri, isProcessing = true, error = null) }

        viewModelScope.launch {
            try {
                val image = InputImage.fromFilePath(context, uri)
                val result = recognizer.process(image).await()
                val text = result.text

                _uiState.update { it.copy(recognizedText = text) }

                val parsed = parseReceiptText(text)
                _uiState.update {
                    it.copy(
                        extractedName = parsed.name,
                        extractedPrice = parsed.price,
                        isProcessing = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = "Failed to process image: ${e.message}"
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(extractedName = name) }
    }

    fun onPriceChange(price: String) {
        _uiState.update { it.copy(extractedPrice = price) }
    }

    fun confirmExtraction() {
        _uiState.update { it.copy(isComplete = true) }
    }

    fun reset() {
        _uiState.update { ReceiptScanUiState() }
    }

    private fun parseReceiptText(text: String): ParsedReceipt {
        val lines = text.lines().filter { it.isNotBlank() }

        // Try to find a price pattern (e.g., $12.99, ฿350, 12.99 USD)
        val priceRegex = Regex("""[\$฿€£¥]?\s*\d{1,3}(?:[,\.]\d{1,3})*(?:\.\d{2})?(?:\s*(?:USD|THB|EUR|GBP|JPY))?""", RegexOption.IGNORE_CASE)
        var price = ""
        for (line in lines.reversed()) {
            val match = priceRegex.find(line)
            if (match != null) {
                price = match.value.trim()
                    .replace(Regex("[\$฿€£¥]"), "")
                    .trim()
                break
            }
        }

        // Try to find subscription name (usually the first meaningful line or a known service)
        val knownServices = listOf(
            "Netflix", "Spotify", "YouTube Premium", "Disney+", "ChatGPT",
            "Claude", "Midjourney", "Adobe", "Microsoft 365", "Google One",
            "Viu Premium", "iCloud", "Amazon Prime", "Hulu", "HBO Max"
        )
        var name = ""
        for (line in lines) {
            val lower = line.lowercase()
            for (service in knownServices) {
                if (lower.contains(service.lowercase())) {
                    name = service
                    break
                }
            }
            if (name.isNotBlank()) break
        }

        // Fallback: use the first non-empty line as name
        if (name.isBlank() && lines.isNotEmpty()) {
            name = lines.first().take(50)
        }

        return ParsedReceipt(name = name, price = price)
    }

    private data class ParsedReceipt(val name: String, val price: String)
}
