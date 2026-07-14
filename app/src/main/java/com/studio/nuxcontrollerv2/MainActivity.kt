package com.studio.nuxcontrollerv2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var btnOpenFile: Button
    private lateinit var btnScaleDown: Button
    private lateinit var btnScaleUp: Button
    private lateinit var btnApplyScale: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvFileName: TextView
    private lateinit var tvGainInfo: TextView
    private lateinit var tvScaleFactor: TextView
    private lateinit var tvScaleDescription: TextView
    private lateinit var tvResult: TextView

    private var namData: JSONObject? = null
    private var currentScale: Float = 1.0f
    private var originalGain: Float = 0f
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

        tvStatus = findViewById(R.id.tvStatus)
        tvFileName = findViewById(R.id.tvFileName)
        tvGainInfo = findViewById(R.id.tvGainInfo)
        tvScaleFactor = findViewById(R.id.tvScaleFactor)
        tvScaleDescription = findViewById(R.id.tvScaleDescription)
        tvResult = findViewById(R.id.tvResult)
        btnOpenFile = findViewById(R.id.btnOpenFile)
        btnScaleDown = findViewById(R.id.btnScaleDown)
        btnScaleUp = findViewById(R.id.btnScaleUp)
        btnApplyScale = findViewById(R.id.btnApplyScale)

        btnOpenFile.setOnClickListener { openFilePicker() }
        btnScaleDown.setOnClickListener { adjustScale(-0.1f) }
        btnScaleUp.setOnClickListener { adjustScale(0.1f) }
        btnApplyScale.setOnClickListener { applyAndSave() }
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
                    tvFileName.text = "Archivo: $fileName"
                    loadNamFile(uri)
                }
            }
            SAVE_NAM_FILE -> {
                data.data?.let { uri ->
                    try {
                        contentResolver.openOutputStream(uri)?.use { stream ->
                            pendingNamData?.let { dataStr ->
                                stream.write(dataStr.toByteArray(Charsets.UTF_8))
                            }
                        }
                        Toast.makeText(this, "Archivo guardado", Toast.LENGTH_SHORT).show()
                        tvResult.text = "Guardado: ${uri.lastPathSegment}"
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun loadNamFile(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val reader = BufferedReader(InputStreamReader(stream))
                val content = reader.readText()
                namData = JSONObject(content)

                val submodels = namData?.optJSONArray("submodels")
                    ?: namData?.getJSONObject("config")?.optJSONArray("submodels")
                
                if (submodels != null && submodels.length() > 0) {
                    val firstModel = submodels.getJSONObject(0)
                    val model = firstModel.optJSONObject("model")
                    val metadata = model?.optJSONObject("metadata")
                    originalGain = metadata?.optDouble("gain")?.toFloat() ?: 0f
                    tvGainInfo.text = "Ganancia original: ${"%.4f".format(originalGain)}"
                }

                Toast.makeText(this, "Archivo cargado", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun adjustScale(delta: Float) {
        currentScale = ((currentScale + delta) * 10).toInt() / 10f
        currentScale = currentScale.coerceIn(0.1f, 5.0f)

        tvScaleFactor.text = String.format("%.1f", currentScale)

        val newGain = originalGain * currentScale
        val percent = (currentScale * 100).toInt()
        tvScaleDescription.text = "${percent}% = ganancia ${"%.4f".format(newGain)}"
    }

    private fun applyAndSave() {
        val data = namData
        if (data == null) {
            Toast.makeText(this, "Abre un archivo primero", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val submodels = data.optJSONArray("submodels")
                ?: data.getJSONObject("config")?.optJSONArray("submodels")

            var totalWeights = 0
            if (submodels != null) {
                for (i in 0 until submodels.length()) {
                    val submodel = submodels.getJSONObject(i)
                    val model = submodel.optJSONObject("model")
                    val weights = model?.optJSONArray("weights")
                    if (weights != null) {
                        for (j in 0 until weights.length()) {
                            val original = weights.getDouble(j)
                            weights.put(j, original * currentScale)
                            totalWeights++
                        }
                    }

                    val metadata = model?.optJSONObject("metadata")
                    if (metadata != null) {
                        val currentGain = metadata.optDouble("gain", 1.0)
                        metadata.put("gain", currentGain * currentScale)
                    }
                }
            }

            val outputName = fileName.replace(".nam", "_SCALE_${currentScale}.nam")
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_TITLE, outputName)
            }
            pendingNamData = data.toString()
            startActivityForResult(intent, SAVE_NAM_FILE)

            tvResult.text = "Escalados $totalWeights pesos por ${currentScale}x"
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
