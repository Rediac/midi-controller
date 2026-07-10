package com.studio.nuxcontrollerv2.models

sealed class SpecialControl {
    data class Toggle(
        val name: String,
        val cc: Int,
        val offValue: Int,
        val onValue: Int
    ) : SpecialControl()

    data class Cyclic(
        val name: String,
        val cc: Int,
        val options: List<String>
    ) : SpecialControl()
}
