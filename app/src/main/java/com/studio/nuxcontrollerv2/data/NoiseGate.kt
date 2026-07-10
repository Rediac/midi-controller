package com.studio.nuxcontrollerv2.data

import com.studio.nuxcontrollerv2.models.KnobInfo
import com.studio.nuxcontrollerv2.models.PedalBank
import com.studio.nuxcontrollerv2.models.PedalInfo

object NoiseGateData {
    val bank = PedalBank("NG", listOf(
        PedalInfo("Noise Gate", toggleCC = 5, pedalIndex = 1, knobs = listOf(
            KnobInfo("Thre", 44), KnobInfo("Decay", 45)
        ))
    ))
}
