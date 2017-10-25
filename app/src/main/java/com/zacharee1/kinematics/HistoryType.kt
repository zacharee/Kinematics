package com.zacharee1.kinematics

class HistoryType constructor(vF: Double, vI: Double, dX: Double, a: Double, t: Double, time: Long) {
    public val vF = vF
    public val vI = vI
    public val dX = dX
    public val a = a
    public val t = t
    public val time = time

    override fun toString(): String {
        return "[ vF = $vF \n vI = $vI :\n dX = $dX \n a = $a \n t = $t \n time = $time ]"
    }
}