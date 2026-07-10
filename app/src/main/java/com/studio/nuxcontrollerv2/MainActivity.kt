package com.studio.nuxcontrollerv2

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.studio.nuxcontrollerv2.data.CompressorData
import com.studio.nuxcontrollerv2.data.EffectsData
import com.studio.nuxcontrollerv2.data.NoiseGateData
import com.studio.nuxcontrollerv2.ui.KnobBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var usbManager: UsbManager
    private var connection: android.hardware.usb.UsbDeviceConnection? = null
    private var endpointOut: android.hardware.usb.UsbEndpoint? = null
    private val midiSender = MidiSender()
    private var currentBankIndex = 0
    private var currentPedalIndex = 0
    private var toggleOn = false

    private lateinit var btnToggle: Button
    private lateinit var btnUp: Button
    private lateinit var btnDown: Button
    private lateinit var btnBankNG: Button
    private lateinit var btnBankCMP: Button
    private lateinit var btnBankEFX: Button
    private lateinit var tvPedalName: TextView
    private lateinit var tvStatus: TextView
    private lateinit var knobsContainer: LinearLayout
    private lateinit var knobBuilder: KnobBuilder

    private val banks = listOf(
        NoiseGateData.bank,
        CompressorData.bank,
        EffectsData.bank
    )

    private val currentBank get() = banks[currentBankIndex]
    private val currentPedal get() = currentBank.pedals[currentPedalIndex]

    private val ACTION_USB_PERMISSION = "com.studio.nuxcontrollerv2.USB_PERMISSION"

    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ACTION_USB_PERMISSION == intent.action) {
                val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    device?.let { connectToDevice(it) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvPedalName = findViewById(R.id.tvPedalName)
        btnToggle = findViewById(R.id.btnToggle)
        btnUp = findViewById(R.id.btnUp)
        btnDown = findViewById(R.id.btnDown)
        btnBankNG = findViewById(R.id.btnBankNG)
        btnBankCMP = findViewById(R.id.btnBankCMP)
        btnBankEFX = findViewById(R.id.btnBankEFX)
        knobsContainer = findViewById(R.id.knobsContainer)

        knobBuilder = KnobBuilder(this, midiSender, knobsContainer)
        usbManager = getSystemService(Context.USB_SERVICE) as UsbManager

        registerReceiver(permissionReceiver, IntentFilter(ACTION_USB_PERMISSION))

        setupButtons()
        findDevice()
    }

    private fun findDevice() {
        val devices = usbManager.deviceList
        if (devices.isEmpty()) {
            tvStatus.text = "No hay dispositivos USB"
            return
        }
        val device = devices.values.first()
        tvStatus.text = "Encontrado: ${device.productName}"
        if (usbManager.hasPermission(device)) connectToDevice(device)
        else requestPermission(device)
    }

    private fun requestPermission(device: UsbDevice) {
        val pi = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE)
        usbManager.requestPermission(device, pi)
    }

    private fun connectToDevice(device: UsbDevice) {
        connection = usbManager.openDevice(device) ?: return
        for (i in 0 until device.interfaceCount) {
            val intf = device.getInterface(i)
            connection?.claimInterface(intf, true)
            for (j in 0 until intf.endpointCount) {
                val ep = intf.getEndpoint(j)
                if (ep.direction == android.hardware.usb.UsbConstants.USB_DIR_OUT &&
                    ep.type == android.hardware.usb.UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    endpointOut = ep
                    midiSender.setConnection(connection, endpointOut)
                    tvStatus.text = "Conectado: ${device.productName}"
                    enableAll(true)
                    highlightBank()
                    updateDisplay()
                    return
                }
            }
        }
    }

    private fun enableAll(enabled: Boolean) {
        btnToggle.isEnabled = enabled
        btnUp.isEnabled = enabled
        btnDown.isEnabled = enabled
        btnBankNG.isEnabled = enabled
        btnBankCMP.isEnabled = enabled
        btnBankEFX.isEnabled = enabled
    }

    private fun highlightBank() {
        val buttons = listOf(btnBankNG, btnBankCMP, btnBankEFX)
        for (i in buttons.indices) {
            if (i == currentBankIndex) {
                buttons[i].setBackgroundColor(Color.parseColor("#FF6B35"))
                buttons[i].setTextColor(Color.WHITE)
            } else {
                buttons[i].setBackgroundColor(Color.parseColor("#333333"))
                buttons[i].setTextColor(Color.parseColor("#B0B0B0"))
            }
        }
    }

    private fun updateDisplay() {
        tvPedalName.text = currentPedal.name
        btnToggle.text = "OFF"
        toggleOn = false
        highlightBank()
        knobBuilder.build(currentPedal)
    }

    private fun setupButtons() {
        btnToggle.setOnClickListener {
            if (toggleOn) {
                midiSender.sendPedalOff(currentPedal.toggleCC, currentPedal.pedalIndex)
                toggleOn = false
                btnToggle.text = "OFF"
            } else {
                midiSender.sendPedalOn(currentPedal.toggleCC, currentPedal.pedalIndex)
                toggleOn = true
                btnToggle.text = "ON"
            }
        }

        btnUp.setOnClickListener {
            if (currentPedalIndex < currentBank.pedals.size - 1) {
                currentPedalIndex++
                updateDisplay()
                midiSender.sendPedalOn(currentPedal.toggleCC, currentPedal.pedalIndex)
                toggleOn = true
                btnToggle.text = "ON"
            }
        }

        btnDown.setOnClickListener {
            if (currentPedalIndex > 0) {
                currentPedalIndex--
                updateDisplay()
                midiSender.sendPedalOn(currentPedal.toggleCC, currentPedal.pedalIndex)
                toggleOn = true
                btnToggle.text = "ON"
            }
        }

        btnBankNG.setOnClickListener {
            currentBankIndex = 0
            currentPedalIndex = 0
            updateDisplay()
        }

        btnBankCMP.setOnClickListener {
            currentBankIndex = 1
            currentPedalIndex = 0
            updateDisplay()
        }

        btnBankEFX.setOnClickListener {
            currentBankIndex = 2
            currentPedalIndex = 0
            updateDisplay()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(permissionReceiver)
        connection?.close()
    }
}
