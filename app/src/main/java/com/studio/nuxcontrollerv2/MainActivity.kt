package com.studio.nuxcontrollerv2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    // UI
    private lateinit var btnOpenFile: Button
    private lateinit var tvFileName: TextView
    private lateinit var tvResult: TextView

    // Slider 1: Pesos
    private lateinit var btnW_Down: Button
    private lateinit var btnW_Up: Button
    private lateinit var btnW_Reset: Button
    private lateinit var tvW_Value: TextView
    private var weightScale = 1.0f

    // Slider 2: Head Scale
    private lateinit var btnH_Down: Button
    private lateinit var btnH_Up: Button
    private lateinit var btnH_Reset: Button
    private lateinit var tvH_Value: TextView
    private var headScale = 1.0f

    // Slider 3: Gain Metadata
    private lateinit var btnG_Down: Button
    private lateinit var btnG_Up: Button
    private lateinit var btnG_Reset: Button
    private lateinit var tvG_Value: TextView
    private var gainScale = 1.0f

    // Slider 4: Max Value
    private lateinit var btnM_Down: Button
    private lateinit var btnM_Up: Button
    private lateinit var btnM_Reset: Button
    private lateinit var tvM_Value: TextView
    private var maxScale = 1.0f

    private var originalContent: String = ""
    private var fileName: String = ""
    private var fileUri: Uri? = null
    private var pendingNamData: String? = null

    companion object {
        private const val PICK_NAM_FILE = 100
        private const val SAVE_NAM_FILE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Vincular vistas
        btnOpenFile = findViewById(R.id.btnOpenFile)
        tvFileName = findViewById(R.id.tvFileName)
        tvResult = findViewById(R.id.tvResult)

        btnW_Down = findViewById(R.id.btnW_Down)
        btnW_Up = findViewById(R.id.btnW_Up)
        btnW_Reset = findViewById(R.id.btnW_Reset)
        tvW_Value = findViewById(R.id.tvW_Value)

        btnH_Down = findViewById(R.id.btnH_Down)
        btnH_Up = findViewById(R.id.btnH_Up)
        btnH_Reset = findViewById(R.id.btnH_Reset)
        tvH_Value = findViewById(R.id.tvH_Value)

        btnG_Down = findViewById(R.id.btnG_Down)
        btnG_Up = findViewById(R.id.btnG_Up)
        btnG_Reset = findViewById(R.id.btnG_Reset)
        tvG_Value = findViewById(R.id.tvG_Value)

        btnM_Down = findViewById(R.id.btnM_Down)
        btnM_Up = findViewById(R.id.btnM_Up)
        btnM_Reset = findViewById(R.id.btnM_Reset)
        tvM_Value = findViewById(R.id.tvM_Value)

        val btnResetAll = findViewById<Button>(R.id.btnResetAll)
        val btnReload = findViewById<Button>(R.id.btnReloadOriginal)
        val btnApplySave = findViewById<Button>(R.id.btnApplySave)

        // Listeners
        btnOpenFile.setOnClickListener { openFilePicker() }

        btnW_Down.setOnClickListener { adjustSlider(1, -0.1f) }
        btnW_Up.setOnClickListener { adjustSlider(1, 0.1f) }
        btnW_Reset.setOnClickListener { resetSlider(1) }
        btnW_Down.setOnLongClickListener { adjustSlider(1, -0.5f); true }
        btnW_Up.setOnLongClickListener { adjustSlider(1, 0.5f); true }

        btnH_Down.setOnClickListener { adjustSlider(2, -0.1f) }
        btnH_Up.setOnClickListener { adjustSlider(2, 0.1f) }
        btnH_Reset.setOnClickListener { resetSlider(2) }
        btnH_Down.setOnLongClickListener { adjustSlider(2, -0.5f); true }
        btnH_Up.setOnLongClickListener { adjustSlider(2, 0.5f); true }

        btnG_Down.setOnClickListener { adjustSlider(3, -0.1f) }
        btnG_Up.setOnClickListener { adjustSlider(3, 0.1f) }
        btnG_Reset.setOnClickListener { resetSlider(3) }
        btnG_Down.setOnLongClickListener { adjustSlider(3, -0.5f); true }
        btnG_Up.setOnLongClickListener { adjustSlider(3, 0.5f); true }

        btnM_Down.setOnClickListener { adjustSlider(4, -0.1f) }
        btnM_Up.setOnClickListener { adjustSlider(4, 0.1f) }
        btnM_Reset.setOnClickListener { resetSlider(4) }
        btnM_Down.setOnLongClickListener { adjustSlider(4, -0.5f); true }
        btnM_Up.setOnLongClickListener { adjustSlider(4, 0.5f); true }

        btnResetAll.setOnClickListener {
            resetAllSliders()
        }

        btnReload.setOnClickListener {
            if (originalContent.isNotEmpty()) {
                resetAllSliders()
                tvResult.text = "🔄 Original recargado"
            }
        }

        btnApplySave.setOnClickListener { applyAndSave() }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, PICK_NAM_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) return

        when (requestCode) {
            PICK_NAM_FILE -> {
                data.data?.let { uri ->
                    fileUri = uri
                    fileName = uri.lastPathSegment ?: "desconocido"
                    tvFileName.text = "📁 $fileName"
                    loadOriginalFile(uri)
                }
            }
            SAVE_NAM_FILE -> {
                data.data?.let { uri ->
                    try {
                        contentResolver.openOutputStream(uri)?.use { stream ->
                            pendingNamData?.let { stream.write(it.toByteArray(Charsets.UTF_8)) }
                        }
                        Toast.makeText(this, "✅ Guardado", Toast.LENGTH_SHORT).show()
                        tvResult.text = "💾 ${uri.lastPathSegment}"
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loadOriginalFile(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                originalContent = BufferedReader(InputStreamReader(stream)).readText()
                resetAllSliders()
                Toast.makeText(this, "✅ Archivo cargado", Toast.LENGTH_SHORT).show()
                tvResult.text = "Listo para modificar"
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun adjustSlider(slider: Int, delta: Float) {
        if (originalContent.isEmpty()) {
            Toast.makeText(this, "Abre un archivo primero", Toast.LENGTH_SHORT).show()
            return
        }

        when (slider) {
            1 -> { weightScale = ((weightScale + delta) * 10).toInt() / 10f; weightScale = weightScale.coerceIn(0.1f, 5.0f); tvW_Value.text = String.format("%.1f", weightScale) }
            2 -> { headScale = ((headScale + delta) * 10).toInt() / 10f; headScale = headScale.coerceIn(0.1f, 5.0f); tvH_Value.text = String.format("%.1f", headScale) }
            3 -> { gainScale = ((gainScale + delta) * 10).toInt() / 10f; gainScale = gainScale.coerceIn(0.1f, 5.0f); tvG_Value.text = String.format("%.1f", gainScale) }
            4 -> { maxScale = ((maxScale + delta) * 10).toInt() / 10f; maxScale = maxScale.coerceIn(0.1f, 5.0f); tvM_Value.text = String.format("%.1f", maxScale) }
        }
    }

    private fun resetSlider(slider: Int) {
        when (slider) {
            1 -> { weightScale = 1.0f; tvW_Value.text = "1.0" }
            2 -> { headScale = 1.0f; tvH_Value.text = "1.0" }
            3 -> { gainScale = 1.0f; tvG_Value.text = "1.0" }
            4 -> { maxScale = 1.0f; tvM_Value.text = "1.0" }
        }
    }

    private fun resetAllSliders() {
        resetSlider(1)
        resetSlider(2)
        resetSlider(3)
        resetSlider(4)
        tvResult.text = "↺ Todo en 1.0"
    }

    private fun applyAndSave() {
        if (originalContent.isEmpty()) {
            Toast.makeText(this, "Abre un archivo primero", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val data = JSONObject(originalContent)
            val architecture = data.optString("architecture", "")
            var totalWeights = 0

            when (architecture) {
                "SlimmableContainer" -> {
                    val submodels = data.optJSONObject("config")?.optJSONArray("submodels")
                    if (submodels != null) {
                        for (i in 0 until submodels.length()) {
                            val submodel = submodels.getJSONObject(i)
                            val model = submodel.optJSONObject("model")

                            if (model != null) {
                                // 1. ESCALAR PESOS
                                if (weightScale != 1.0f) {
                                    val weights = model.optJSONArray("weights")
                                    if (weights != null) {
                                        for (j in 0 until weights.length()) {
                                            weights.put(j, weights.getDouble(j) * weightScale)
                                            totalWeights++
                                        }
                                    }
                                }

                                // 2. ESCALAR HEAD SCALE
                                if (headScale != 1.0f) {
                                    val hs = model.optDouble("head_scale", 0.02)
                                    model.put("head_scale", hs * headScale)
                                }

                                // 3. ESCALAR GAIN METADATA (⚠️ experimental)
                                if (gainScale != 1.0f) {
                                    val meta = model.optJSONObject("metadata")
                                    if (meta != null) {
                                        val g = meta.optDouble("gain", 1.0)
                                        meta.put("gain", g * gainScale)
                                    }
                                }
                            }

                            // 4. ESCALAR MAX VALUE (⚠️ experimental)
                            if (maxScale != 1.0f) {
                                val mv = submodel.optDouble("max_value", 1.0)
                                submodel.put("max_value", mv * maxScale)
                            }
                        }
                    }
                }
                else -> {
                    // WaveNet simple
                    if (weightScale != 1.0f) {
                        val weights = data.optJSONArray("weights")
                        if (weights != null) {
                            for (i in 0 until weights.length()) {
                                weights.put(i, weights.getDouble(i) * weightScale)
                                totalWeights++
                            }
                        }
                    }
                }
            }

            // Guardar
            val baseName = fileName.replace(Regex("\\.nam$", RegexOption.IGNORE_CASE), "")
            val outputName = "${baseName}_W${weightScale}_H${headScale}_G${gainScale}_M${maxScale}.nam"

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_TITLE, outputName)
            }
            pendingNamData = data.toString()
            startActivityForResult(intent, SAVE_NAM_FILE)

            val parts = mutableListOf<String>()
            if (weightScale != 1.0f) parts.add("W×${weightScale}")
            if (headScale != 1.0f) parts.add("H×${headScale}")
            if (gainScale != 1.0f) parts.add("G×${gainScale}")
            if (maxScale != 1.0f) parts.add("M×${maxScale}")

            tvResult.text = if (parts.isEmpty()) "Sin cambios" else "✅ ${parts.joinToString(", ")} | $totalWeights pesos"

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
