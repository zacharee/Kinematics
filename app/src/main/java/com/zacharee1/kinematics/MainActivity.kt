package com.zacharee1.kinematics

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TextInputEditText
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.reflect.Type
import kotlin.coroutines.experimental.buildIterator

/**
 * vFinal = a * t + vInitial
 * vFinal = sqrt(2 * a * deltaX + vInitial^2)
 * deltaX = 0.5 * a * t^2 + vInitial * t
 * deltaX = 0.5 * (vInitial + vFinal) * t
 */

class MainActivity : AppCompatActivity() {
    private var vF: Double = Double.NEGATIVE_INFINITY
    private var vI: Double = Double.NEGATIVE_INFINITY
    private var dX: Double = Double.NEGATIVE_INFINITY
    private var a: Double = Double.NEGATIVE_INFINITY
    private var t: Double = Double.NEGATIVE_INFINITY

    private lateinit var vFInput: TextInputEditText
    private lateinit var vIInput: TextInputEditText
    private lateinit var dXInput: TextInputEditText
    private lateinit var aInput: TextInputEditText
    private lateinit var tInput: TextInputEditText

    private lateinit var calc: Button

    private lateinit var sharedPreferences: SharedPreferences

    private val VF = "VF"
    private val VI = "VI"
    private val DX = "DX"
    private val A = "A"
    private val T = "T"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        setElements()
    }

    fun setElements() {
        vFInput = findViewById(R.id.vfinal_text)
        vIInput = findViewById(R.id.vinitial_text)
        dXInput = findViewById(R.id.deltax_text)
        aInput = findViewById(R.id.acc_text)
        tInput = findViewById(R.id.time_text)

        calc = findViewById(R.id.calculate)
    }

    fun onReset(v: View) {
        vFInput.text = null
        vIInput.text = null
        dXInput.text = null
        aInput.text = null
        tInput.text = null
    }

    fun onCalc(v: View) {
        setVF()
        setVI()
        setDX()
        setA()
        setT()

        checkNull()
    }

    fun checkNull() {
        val nulls: ArrayList<String> = ArrayList()

        if (vF == Double.NEGATIVE_INFINITY) nulls.add(VF)
        if (vI == Double.NEGATIVE_INFINITY) nulls.add(VI)
        if (dX == Double.NEGATIVE_INFINITY) nulls.add(DX)
        if (a == Double.NEGATIVE_INFINITY) nulls.add(A)
        if (t == Double.NEGATIVE_INFINITY) nulls.add(T)

        if (nulls.size > 2) Toast.makeText(this, "Need at least three knowns", Toast.LENGTH_LONG).show()
        else doCalc(nulls)
    }

    fun doCalc(nulls: ArrayList<String>) {
        if (nulls.contains(VI)) solveForVI()

        for (s in nulls) {
            if (s == VI) solveForVI()
            if (s == VF) solveForVF()
            if (s == DX) solveForDX()
            if (s == A) solveForA()
            if (s == T) solveForT()
        }

        saveHistory()
        printHistory(null)
    }

    fun saveHistory() {
        val gson = Gson()
        var json = sharedPreferences.getString("history_json", null)
        val type = object: TypeToken<ArrayList<HistoryType>>(){}.type

        var historyList: ArrayList<HistoryType>? = gson.fromJson(json, type)

        if (historyList == null) historyList = ArrayList()

        val historyItem = HistoryType(vF, vI, dX, a, t, System.currentTimeMillis())

        historyList.add(historyItem)

        json = gson.toJson(historyList)

        sharedPreferences.edit().putString("history_json", json).apply()
    }

    fun printHistory(v: View?) {
        val gson = Gson()
        val json = sharedPreferences.getString("history_json", null)
        val type = object: TypeToken<ArrayList<HistoryType>>(){}.type

        var historyList: ArrayList<HistoryType>? = gson.fromJson(json, type)

        if (historyList == null) historyList = ArrayList()

        for (history in historyList) {
            Log.e("History", history.toString())
        }
    }

    fun isNull(num: Double): Boolean {
        return num == Double.NEGATIVE_INFINITY || num == Double.NaN
    }

    fun solveForVF() {
        //vF = a * t + vI
        //vF = 2 * dX / t - vI
        //vF = sqrt(2 * a * deltaX + vInitial^2)

        //check vI not null

        var value: Double = Double.NEGATIVE_INFINITY

        if (!isNull(a)
                && !isNull(t)) {
            Log.e("VF", "1")
            value = a * t * vI
        }
        else if (!isNull(dX)
                     && !isNull(t)) {
            Log.e("VF", "2")
            value = 2 * dX / t - vI
        }
        else if (!isNull(a)
                     && !isNull(dX)) {
            Log.e("VF", "3")
            value = Math.sqrt(2 * a * dX + vI * vI)

            if (dX < 0) value = -value
        }

        logAll()
        vF = value
        vFInput.setText(value.toString())
    }

    fun solveForVI() {
        //vI = vF - a * t
        //vI = 2 * dX / t - vF
        //vI = (dX - 0.5 * a * t^2) / t
        //vI = sqrt(vF^2 - 2 * a * dX)

        var value: Double = Double.NEGATIVE_INFINITY

        if (!isNull(vF)
                && !isNull(a)
                && !isNull(t)) {
            Log.e("VI", "1")
            value = vF - a * t
        }
        else if (!isNull(t)
                && !isNull(vF)
                     && !isNull(dX)) {
            Log.e("VI", "2")
            value = 2 * dX / t - vF
        }
        else if (!isNull(a)
                     && !isNull(t)
                     && !isNull(dX)) {
            Log.e("VI", "3")
            value = (dX - 0.5 * a * t * t) / t
        }
        else if (!isNull(vF)
                     && !isNull(a)
                     &&!isNull(dX)) {
            Log.e("VI", "4")
            value = Math.sqrt(vF * vF - 2 * a * dX)
        }

        logAll()
        vI = value
        vIInput.setText(value.toString())
    }

    fun solveForDX() {
        //dX = 0.5 * a * t^2 + vI * t
        //dX = 0.5 * (vI + vF) * t
        //dX = (vF^2 - vI^2) / 2 * a

        //check vI not null

        var value: Double = Double.NEGATIVE_INFINITY

        if (!isNull(a)
                && !isNull(t)) {
            Log.e("DX", "1")
            value = 0.5 * a * t * t + vI * t
        }
        else if (!isNull(vF)
                     && !isNull(t)) {
            Log.e("DX", "2")
            value = 0.5 * (vI + vF) * t
        }
        else if (!isNull(vF)
                     && !isNull(a)) {
            Log.e("DX", "3")
            value = (vF * vF - vI * vI) / 2 * a
        }

        logAll()
        dX = value
        dXInput.setText(value.toString())
    }

    fun solveForA() {
        //a = (vF - vI) / t
        //a = 2 * (dX - vI * t) / t^2
        //a = (vF^2 - vI^2) / 2 * dX

        //check vI not null

        var value: Double = Double.NEGATIVE_INFINITY

        if (!isNull(vF)
                && !isNull(t)) {
            Log.e("A", "1")
            value = (vF - vI) / t
        }
        else if (!isNull(dX)
                     && !isNull(t)) {
            Log.e("A", "2")
            value = 2 * (dX - vI * t) / t * t
        }
        else if (!isNull(vF)
                     && !isNull(dX)) {
            Log.e("A", "3")
            value = (vF * vF - vI * vI) / 2 * dX
        }

        logAll()
        a = value
        aInput.setText(value.toString())
    }

    fun solveForT() {
        //t = (vF - vI) / a
        /**
         * 0 = (a / 2) * t^2 + (vI) * t - dX
         * t = (-vI + sqrt(vI^2 - 2 * a * -dX)) / a
         * t = (-vI - sqrt(vI^2 - 2 * a * -dX)) / a
         */
        //t = 2 * dX / (vI + vF)

        //check vI not null

        var value: Double = Double.NEGATIVE_INFINITY

        if (!isNull(vF)
                && !isNull(a)) {
            Log.e("T", "1")
            value = (vF - vI) / a
        }
        else if (!isNull(dX)
                && !isNull(vF)) {
            Log.e("T", "2")
            value = 2 * dX / (vI + vF)
        }
        else if (!isNull(a)
                     && !isNull(t)
                     && !isNull(dX)) {
            Log.e("T", "3")
            value = (-vI + Math.sqrt(vI * vI - 2 * a * -dX)) / a

            if (value < 0) {
                value = (-vI - Math.sqrt(vI * vI - 2 * a * -dX)) / a
            }
        }

        logAll()
        t = value
        tInput.setText(value.toString())
    }

    fun setVF() {
        val input = vFInput.text.toString()

        try {
            vF = input.toDouble()
        } catch (e: NumberFormatException) {
            vF = Double.NEGATIVE_INFINITY
        }
    }

    fun setVI() {
        val input = vIInput.text.toString()

        try {
            vI = input.toDouble()
        } catch (e: NumberFormatException) {
            vI = Double.NEGATIVE_INFINITY
        }
    }

    fun setDX() {
        val input = dXInput.text.toString()

        try {
            dX = input.toDouble()
        } catch (e: NumberFormatException) {
            dX = Double.NEGATIVE_INFINITY
        }
    }

    fun setA() {
        val input = aInput.text.toString()

        try {
            a = input.toDouble()
        } catch (e: NumberFormatException) {
            a = Double.NEGATIVE_INFINITY
        }
    }

    fun setT() {
        val input = tInput.text.toString()

        try {
            t = input.toDouble()
        } catch (e: NumberFormatException) {
            t = Double.NEGATIVE_INFINITY
        }
    }

    fun logAll() {
        Log.e("VFVal", vF.toString())
        Log.e("VIVal", vI.toString())
        Log.e("DXVal", dX.toString())
        Log.e("AVal", a.toString())
        Log.e("TVal", t.toString())
    }
}
