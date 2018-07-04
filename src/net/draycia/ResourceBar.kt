package net.draycia

import kotlin.io.Serializable

class ResourceBar(var cap : Int, var current : Double, var regen : Double): Serializable {
    companion object {
        @JvmStatic private val serialVersionUID: Long = -3065998223739472606L;
    }
}