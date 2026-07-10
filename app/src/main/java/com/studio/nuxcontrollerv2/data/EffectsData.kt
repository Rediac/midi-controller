package com.studio.nuxcontrollerv2.data

import com.studio.nuxcontrollerv2.models.KnobInfo
import com.studio.nuxcontrollerv2.models.PedalBank
import com.studio.nuxcontrollerv2.models.PedalInfo
import com.studio.nuxcontrollerv2.models.SpecialControl

object EffectsData {
    val bank = PedalBank("EFX", listOf(
        PedalInfo("DIST+", toggleCC = 2, pedalIndex = 1, knobs = listOf(
            KnobInfo("Output", 18), KnobInfo("Drive", 19)
        )),
        PedalInfo("RC BST", toggleCC = 2, pedalIndex = 2, knobs = listOf(
            KnobInfo("Gain", 18), KnobInfo("Volume", 19),
            KnobInfo("Bass", 20), KnobInfo("Treble", 21)
        )),
        PedalInfo("AC BST", toggleCC = 2, pedalIndex = 3, knobs = listOf(
            KnobInfo("Gain", 18), KnobInfo("Vol", 19),
            KnobInfo("Bass", 20), KnobInfo("Treble", 21)
        )),
        PedalInfo("DIST ONE", toggleCC = 2, pedalIndex = 4, knobs = listOf(
            KnobInfo("Level", 18), KnobInfo("Tone", 19), KnobInfo("Drive", 20)
        )),
        PedalInfo("T SCREAMER", toggleCC = 2, pedalIndex = 5, knobs = listOf(
            KnobInfo("Drive", 18), KnobInfo("Tone", 19), KnobInfo("Level", 20)
        )),
        PedalInfo("BLUES DRIVE", toggleCC = 2, pedalIndex = 6, knobs = listOf(
            KnobInfo("Level", 18), KnobInfo("Tone", 19), KnobInfo("Gain", 20)
        )),
        PedalInfo("MORNING DRIVE", toggleCC = 2, pedalIndex = 7, knobs = listOf(
            KnobInfo("Vol", 18), KnobInfo("Drive", 19), KnobInfo("Tone", 20)
        )),
        PedalInfo("MODERN DIST", toggleCC = 2, pedalIndex = 8, knobs = listOf(
            KnobInfo("Dist", 18), KnobInfo("Filt", 19), KnobInfo("Vol", 20)
        )),
        PedalInfo("RED DIST", toggleCC = 2, pedalIndex = 9, knobs = listOf(
            KnobInfo("Drive", 18), KnobInfo("Tone", 19), KnobInfo("Level", 20)
        )),
        PedalInfo("CRUNCH", toggleCC = 2, pedalIndex = 10, knobs = listOf(
            KnobInfo("Vol", 18), KnobInfo("Tone", 19), KnobInfo("Gain", 20)
        )),
        PedalInfo("MUF DIST", toggleCC = 2, pedalIndex = 11, knobs = listOf(
            KnobInfo("Vol", 18), KnobInfo("Tone", 19), KnobInfo("Sust", 20)
        )),
        PedalInfo("KATANA", toggleCC = 2, pedalIndex = 12, knobs = listOf(
            KnobInfo("Volume", 19)
        ), specials = listOf(
            SpecialControl.Toggle("Boost", 18, 0, 1)
        )),
        PedalInfo("RED FUZZ", toggleCC = 2, pedalIndex = 13, knobs = listOf(
            KnobInfo("Gain", 18), KnobInfo("Tone", 19), KnobInfo("Out", 20)
        )),
        PedalInfo("TOUCH WAH", toggleCC = 2, pedalIndex = 14, knobs = listOf(
            KnobInfo("Decay", 19), KnobInfo("Sens", 20), KnobInfo("Level", 22)
        ), specials = listOf(
            SpecialControl.Cyclic("Mode", 18, listOf("Cry", "VX", "Full", "Talk")),
            SpecialControl.Toggle("Up/Down", 21, 0, 1)
        ))
    ))
}
