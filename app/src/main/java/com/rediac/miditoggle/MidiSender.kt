package com.studio.nuxcontrollerv2

import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint

class MidiSender {
    private var connection: UsbDeviceConnection? = null
    private var endpointOut: UsbEndpoint? = null

    fun setConnection(conn: UsbDeviceConnection?, ep: UsbEndpoint?) {
        connection = conn
        endpointOut = ep
    }

    fun isConnected(): Boolean = connection != null && endpointOut != null

    fun sendCC(cc: Int, value: Int) {
        val conn = connection ?: return
        val ep = endpointOut ?: return
        val msg = byteArrayOf(0x0B.toByte(), 0xB0.toByte(), cc.toByte(), value.toByte())
        conn.bulkTransfer(ep, msg, msg.size, 100)
    }

    fun sendPedalOn(toggleCC: Int, pedalIndex: Int) = sendCC(toggleCC, pedalIndex)
    fun sendPedalOff(toggleCC: Int, pedalIndex: Int) = sendCC(toggleCC, pedalIndex + 64)
}
