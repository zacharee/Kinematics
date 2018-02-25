package com.zacharee1.kinematics

class HistoryItem constructor(vF: Double?, vI: Double?, dX: Double?, a: Double?, t: Double?, var time: Long) :
        KinematicsItem(vF, vI, dX, a, t) {
    override fun toString(): String {
        return "[ vF = $vF \n vI = $vI :\n dX = $dX \n a = $a \n t = $t \n time = $time ]"
    }
}