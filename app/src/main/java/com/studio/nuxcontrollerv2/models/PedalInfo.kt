package com.studio.nuxcontrollerv2.models

data class PedalInfo(
    val name: String,
    val toggleCC: Int,
    val pedalIndex: Int,
    val knobs: List<KnobInfo> = emptyList(),
    val specials: List<SpecialControl> = emptyList()
)
