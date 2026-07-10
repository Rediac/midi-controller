package com.studio.nuxcontrollerv2.data

import com.studio.nuxcontrollerv2.models.KnobInfo
import com.studio.nuxcontrollerv2.models.PedalBank
import com.studio.nuxcontrollerv2.models.PedalInfo

object CompressorData {
    val bank = PedalBank("CMP", listOf(
        PedalInfo("Red Comp", toggleCC = 1, pedalIndex = 1, knobs = listOf(
            KnobInfo("Out", 14), KnobInfo("Sens", 15)
        )),
        PedalInfo("Rose Comp", toggleCC = 1, pedalIndex = 2, knobs = listOf(
            KnobInfo("Sust", 14), KnobInfo("Level", 15)
        ))
    ))
}
