package com.zacharee1.kinematics

open class KinematicsItem constructor(var vF: Double?, var vI: Double?, var dX: Double?, var a: Double?, var t: Double?) {
    override fun toString(): String {
        return "[ vF = $vF \n vI = $vI :\n dX = $dX \n a = $a \n t = $t ]"
    }
}