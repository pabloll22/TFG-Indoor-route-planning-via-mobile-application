import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object CarnetValidator {
    fun validarCarnet(
        context: Context,
        uri: Uri,
        onResult: (isValid: Boolean, niu: String?, rol: String?) -> Unit
    ) {
        val image = InputImage.fromFilePath(context, uri)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val texto = visionText.text.lowercase()

                // 1. Verificamos que sea de la UMA
                val esUMA = texto.contains("universidad de málaga") || texto.contains("uma")

                // 2. Buscamos el NIU con una Expresión Regular
                // Buscamos un número de exactamente 7 u 8 dígitos rodeado de espacios o saltos de línea
                val regexNIU = Regex("\\b\\d{7,8}\\b")
                val matchNIU = regexNIU.find(visionText.text)
                val niuEncontrado = matchNIU?.value

                // 3. Detectamos Rol
                val rol = if (texto.contains("pdi") || texto.contains("profesor")) "PROFESOR" else "ALUMNO"

                if (esUMA && niuEncontrado != null) {
                    onResult(true, niuEncontrado, rol)
                } else {
                    onResult(false, null, null)
                }
            }
            .addOnFailureListener { onResult(false, null, null) }
    }
}