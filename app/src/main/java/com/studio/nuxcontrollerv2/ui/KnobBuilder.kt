package com.studio.nuxcontrollerv2.ui

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.studio.nuxcontrollerv2.KnobView
import com.studio.nuxcontrollerv2.MidiSender
import com.studio.nuxcontrollerv2.models.KnobInfo
import com.studio.nuxcontrollerv2.models.PedalInfo
import com.studio.nuxcontrollerv2.models.SpecialControl

class KnobBuilder(
    private val context: Context,
    private val midiSender: MidiSender,
    private val container: LinearLayout
) {
    fun build(pedal: PedalInfo) {
        container.removeAllViews()
        val allControls = pedal.knobs.size + pedal.specials.size
        if (allControls == 0) return

        val rowsNeeded = if (allControls > 3) 2 else 1
        val perRow = (allControls + rowsNeeded - 1) / rowsNeeded
        var count = 0
        var currentRow: LinearLayout? = null

        for (knob in pedal.knobs) {
            if (count % perRow == 0) {
                currentRow = newRow()
                container.addView(currentRow)
            }
            currentRow?.addView(buildKnobWrapper(knob))
            count++
        }

        for (special in pedal.specials) {
            if (count % perRow == 0) {
                currentRow = newRow()
                container.addView(currentRow)
            }
            currentRow?.addView(buildSpecialWrapper(special))
            count++
        }
    }

    private fun newRow() = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER
    }

    private fun buildKnobWrapper(knob: KnobInfo): LinearLayout {
        val knobView = KnobView(context, knob.name) { value ->
            midiSender.sendCC(knob.cc, value)
        }
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(12, 0, 12, 0)
            addView(knobView)
            addView(makeLabel(knob.name))
        }
    }

    private fun buildSpecialWrapper(special: SpecialControl): LinearLayout {
        return when (special) {
            is SpecialControl.Toggle -> buildToggle(special)
            is SpecialControl.Cyclic -> buildCyclic(special)
        }
    }

    private fun buildToggle(special: SpecialControl.Toggle): LinearLayout {
        val btn = Button(context).apply {
            text = "${special.name} OFF"
            textSize = 12f
            setBackgroundColor(Color.parseColor("#333333"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                if (text == "${special.name} OFF") {
                    midiSender.sendCC(special.cc, special.onValue)
                    text = "${special.name} ON"
                    setBackgroundColor(Color.parseColor("#FF6B35"))
                } else {
                    midiSender.sendCC(special.cc, special.offValue)
                    text = "${special.name} OFF"
                    setBackgroundColor(Color.parseColor("#333333"))
                }
            }
        }
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(12, 20, 12, 0)
            addView(btn)
            addView(makeLabel(special.name))
        }
    }

    private fun buildCyclic(special: SpecialControl.Cyclic): LinearLayout {
        var index = 0
        val btn = Button(context).apply {
            text = special.options[index]
            textSize = 12f
            setBackgroundColor(Color.parseColor("#333333"))
            setTextColor(Color.WHITE)
            setOnClickListener {
                index = (index + 1) % special.options.size
                text = special.options[index]
                midiSender.sendCC(special.cc, index)
            }
        }
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(12, 20, 12, 0)
            addView(btn)
            addView(makeLabel(special.name))
        }
    }

    private fun makeLabel(text: String) = TextView(context).apply {
        this.text = text
        textSize = 10f
        setTextColor(Color.parseColor("#B0B0B0"))
        gravity = Gravity.CENTER
    }
}
