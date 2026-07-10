package com.rediac.miditoggle

import android.content.Context
import android.media.midi.MidiDevice
import android.media.midi.MidiManager
import android.media.midi.MidiOutputPort
import android.media.midi.MidiReceiver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private lateinit var midiManager: MidiManager
    private var midiDevice: MidiDevice? = null
    private var outputPort: MidiOutputPort? = null
    private val handler = Handler(Looper.getMainLooper())

    private val midiChannel = 0
    private val ccNumber = 2
    private val onValue = 127
    private val offValue = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        midiManager = getSystemService(Context.MIDI_SERVICE) as MidiManager

        setContent {
            var isOn by remember { mutableStateOf(false) }
            var status by remember { mutableStateOf("Buscando dispositivo MIDI…") }

            LaunchedEffect(Unit) {
                connectToFirstDevice { connected ->
                    status = if (connected) "Conectado" else "Sin dispositivo MIDI"
                }
            }

            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A0A0A)
                ) {
                    ToggleScreen(
                        isOn = isOn,
                        status = status,
                        ccNumber = ccNumber,
                        onValue = onValue,
                        offValue = offValue,
                        onToggle = {
                            isOn = !isOn
                            sendCc(
                                channel = midiChannel,
                                cc = ccNumber,
                                value = if (isOn) onValue else offValue
                            )
                        }
                    )
                }
            }
        }
    }

    private fun connectToFirstDevice(onResult: (Boolean) -> Unit) {
        val devices = midiManager.devices
        if (devices.isEmpty()) {
            onResult(false)
            return
        }
        midiManager.openDevice(devices[0], { device ->
            if (device == null) {
                onResult(false)
                return@openDevice
            }
            midiDevice = device
            outputPort = device.openOutputPort(0)
            onResult(outputPort != null)
        }, handler)
    }

    private fun sendCc(channel: Int, cc: Int, value: Int) {
        val port = outputPort ?: return
        val statusByte = (0xB0 or (channel and 0x0F)).toByte()
        val message = byteArrayOf(statusByte, cc.toByte(), value.toByte())
        try {
            val receiver = port.javaClass
                .getMethod("getReceiver")
                .invoke(port) as? MidiReceiver
            receiver?.send(message, 0, message.size)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        outputPort?.close()
        midiDevice?.close()
    }
}

@Composable
private fun ToggleScreen(
    isOn: Boolean,
    status: String,
    ccNumber: Int,
    onValue: Int,
    offValue: Int,
    onToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MIDI Toggle",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFB0B0B0)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = status,
            fontSize = 13.sp,
            color = Color(0xFF6E6E6E)
        )
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onToggle,
            modifier = Modifier.size(160.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isOn) Color(0xFFB71C1C) else Color(0xFF2A2A2A)
            )
        ) {
            Text(
                text = if (isOn) "ON" else "OFF",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "CH1  ·  CC $ccNumber  ·  Val ${if (isOn) onValue else offValue}",
            fontSize = 13.sp,
            color = Color(0xFF6E6E6E)
        )
    }
}
