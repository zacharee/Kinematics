package com.zacharee1.kinematics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zacharee1.kinematics.utils.sharedPreferences
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.history_layout.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * vFinal = a * t + vInitial
 * vFinal = sqrt(2 * a * deltaX + vInitial^2)
 * deltaX = 0.5 * a * t^2 + vInitial * t
 * deltaX = 0.5 * (vInitial + vFinal) * t
 */

class MainActivity : AppCompatActivity() {
    companion object {
        private const val VF_TEXT = "VF"
        private const val VI_TEXT = "VI"
        private const val DX_TEXT = "DX"
        private const val A_TEXT = "A"
        private const val T_TEXT = "T"
    }

    private val item = KinematicsItem(0.0, 0.0, 0.0, 0.0, 0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpActionBar()
    }

    fun onCalc(v: View?) {
        setVF()
        setVI()
        setDX()
        setA()
        setT()

        checkNull()
    }

    fun onReset(v: View?) {
        vfinal_text.text = null
        vinitial_text.text = null
        deltax_text.text = null
        acc_text.text = null
        time_text.text = null

        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(window?.currentFocus?.windowToken, 0)

        vfinal_text.clearFocus()
        vinitial_text.clearFocus()
        deltax_text.clearFocus()
        acc_text.clearFocus()
        time_text.clearFocus()
    }

    fun printHistory(v: View?) {
        val gson = GsonBuilder()
        val json = sharedPreferences.getString("history_json", null)
        val type = object: TypeToken<ArrayList<HistoryItem>>(){}.type

        var historyList: ArrayList<HistoryItem>? = gson.serializeSpecialFloatingPointValues().create().fromJson(json, type)

        if (historyList == null) historyList = ArrayList()

        history_layout.visibility = View.VISIBLE

        if (historyList.size > 0) {
            val history = historyList[0]

            date_text.text = SimpleDateFormat("E MM/dd/yy hh:mm:ss a", Locale.getDefault()).format(history.time)
            time_history.setText("${history.t}")
            acc_history.setText("${history.a}")
            vinitial_history.setText("${history.vI}")
            vfinal_history.setText("${history.vF}")
            dx_history.setText("${history.dX}")
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(toolbar)

        val history = LayoutInflater.from(this).inflate(R.layout.history_button, toolbar, false) as ImageView
        val calc = LayoutInflater.from(this).inflate(R.layout.calculate_button, toolbar, false) as ImageView
        val reset = LayoutInflater.from(this).inflate(R.layout.reset_button, toolbar, false) as ImageView

        history.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        calc.setOnClickListener {
            onCalc(null)
        }
        reset.setOnClickListener {
            onReset(null)
        }

        history.setOnLongClickListener {
            Toast.makeText(this, R.string.history, Toast.LENGTH_SHORT).show()
            true
        }
        calc.setOnLongClickListener {
            Toast.makeText(this, R.string.calculate, Toast.LENGTH_SHORT).show()
            true
        }
        reset.setOnLongClickListener {
            Toast.makeText(this, R.string.reset, Toast.LENGTH_SHORT).show()
            true
        }

        toolbar.addView(history)
        toolbar.addView(calc)
        toolbar.addView(reset)
    }

    private fun checkNull() {
        val nulls: ArrayList<String> = ArrayList()

        if (item.vF == null) nulls.add(VF_TEXT)
        if (item.vI == null) nulls.add(VI_TEXT)
        if (item.dX == null) nulls.add(DX_TEXT)
        if (item.a == null) nulls.add(A_TEXT)
        if (item.t == null) nulls.add(T_TEXT)

        if (nulls.size > 2) Toast.makeText(this, R.string.need_three_knowns, Toast.LENGTH_LONG).show()
        else doCalc(nulls)
    }

    private fun doCalc(nulls: ArrayList<String>) {
        if (nulls.contains(VI_TEXT)) {
            solveForVI()
            nulls.remove(VI_TEXT)
        }

        if (nulls.contains(VF_TEXT)) {
            solveForVF()
            nulls.remove(VF_TEXT)
        }

        if (nulls.contains(DX_TEXT)) {
            solveForDX()
            nulls.remove(DX_TEXT)
        }

        if (nulls.contains(A_TEXT)) {
            solveForA()
            nulls.remove(A_TEXT)
        }

        if (nulls.contains(T_TEXT)) {
            solveForT()
            nulls.remove(T_TEXT)
        }

        saveHistory()
        printHistory(null)
        onReset(null)
    }

    private fun saveHistory() {
        val gBuilder = GsonBuilder()
        var json = sharedPreferences.getString("history_json", null)
        val type = object: TypeToken<ArrayList<HistoryItem>>(){}.type

        var historyList: ArrayList<HistoryItem>? = gBuilder.serializeSpecialFloatingPointValues().create().fromJson(json, type)

        if (historyList == null) historyList = ArrayList()

        val historyItem = HistoryItem(item.vF, item.vI, item.dX, item.a, item.t, System.currentTimeMillis())

        historyList.add(0, historyItem)

        json = gBuilder.serializeSpecialFloatingPointValues().create().toJson(historyList)

        sharedPreferences.edit().putString("history_json", json).apply()
    }

    private fun solveForVI() {
        //vI = vF - a * t
        //vI = 2 * dX / t - vF
        //vI = (dX - 0.5 * a * t^2) / t
        //vI = sqrt(vF^2 - 2 * a * dX)

        var value: Double? = null

        if (item.vF != null && item.a != null && item.t != null) {
            Log.e(VI_TEXT, "1")
            value = item.vF!! - item.a!! * item.t!!
        }
        else if (item.t != null && item.vF != null && item.dX != null) {
            Log.e(VI_TEXT, "2")
            value = 2 * item.dX!! / item.t!! - item.vF!!
        }
        else if (item.a != null && item.t != null && item.dX != null) {
            Log.e(VI_TEXT, "3")
            value = (item.dX!! - 0.5 * item.a!! * item.t!! * item.t!!) / item.t!!
        }
        else if (item.vF != null && item.a != null && item.dX != null) {
            Log.e(VI_TEXT, "4")
            value = Math.sqrt(item.vF!! * item.vF!! - 2 * item.a!! * item.dX!!)
        }

        logAll(VI_TEXT)
        item.vI = value
        vinitial_text.setText(value.toString())
    }

    private fun solveForVF() {
        //vF = a * t + vI
        //vF = 2 * dX / t - vI
        //vF = sqrt(2 * a * deltaX + vInitial^2)

        var value: Double? = null

        if (item.a != null && item.t != null) {
            Log.e(VF_TEXT, "1")
            value = item.a!! * item.t!! + item.vI!!
        } else if (item.dX != null && item.t != null) {
            Log.e(VF_TEXT, "2")
            value = 2 * item.dX!! / item.t!! - item.vI!!
        } else if (item.a != null && item.dX != null) {
            Log.e(VF_TEXT, "3")
            value = Math.sqrt(2 * item.a!! * item.dX!! + item.vI!! * item.vI!!)

            if (item.dX!! < 0) value = -value
        }

        logAll(VF_TEXT)
        item.vF = value
        vfinal_text.setText(value.toString())
    }

    private fun solveForDX() {
        //dX = 0.5 * a * t^2 + vI * t
        //dX = 0.5 * (vI + vF) * t
        //dX = (vF^2 - vI^2) / (2 * a)

        var value: Double? = null

        if (item.a != null && item.t != null) {
            Log.e(DX_TEXT, "1")
            value = 0.5 * item.a!! * item.t!! * item.t!! + item.vI!! * item.t!!
        }
        else if (item.vF != null && item.t != null) {
            Log.e(DX_TEXT, "2")
            value = 0.5 * (item.vI!! + item.vF!!) * item.t!!
        }
        else if (item.vF != null && item.a != null && item.a != 0.0) {
            Log.e(DX_TEXT, "3")
            value = (item.vF!! * item.vF!! - item.vI!! * item.vI!!) / (2 * item.a!!)
        }

        logAll(DX_TEXT)
        item.dX = value
        deltax_text.setText(value.toString())
    }

    private fun solveForA() {
        //a = (vF - vI) / t
        //a = 2 * (dX - vI * t) / t^2
        //a = (vF^2 - vI^2) / 2 * dX

        var value: Double? = null

        if (item.vF != null && item.t != null) {
            Log.e(A_TEXT, "1")
            value = (item.vF!! - item.vI!!) / item.t!!
        }
        else if (item.dX != null && item.t != null) {
            Log.e(A_TEXT, "2")
            value = 2 * (item.dX!! - item.vI!! * item.t!!) / item.t!! * item.t!!
        }
        else if (item.vF != null && item.dX != null) {
            Log.e(A_TEXT, "3")
            value = (item.vF!! * item.vF!! - item.vI!! * item.vI!!) / 2 * item.dX!!
        }

        logAll(A_TEXT)
        item.a = value
        acc_text.setText(value.toString())
    }

    private fun solveForT() {
        //t = (vF - vI) / a
        /**
         * 0 = (a / 2) * t^2 + (vI) * t - dX
         * t = (-vI + sqrt(vI^2 - 2 * a * -dX)) / a
         * t = (-vI - sqrt(vI^2 - 2 * a * -dX)) / a
         */
        //t = 2 * dX / (vI + vF)

        var value: Double? = null

        if (item.vF != null
                && item.a != null && item.a != 0.0) {
            Log.e(T_TEXT, "1")
            value = (item.vF!! - item.vI!!) / item.a!!
        } else if (item.dX != null && item.vF != null) {
            Log.e(T_TEXT, "2")
            value = 2 * item.dX!! / (item.vI!! + item.vF!!)
        } else if (item.a != null && item.t != null && item.dX != null) {
            Log.e(T_TEXT, "3")
            value = (-item.vI!! + Math.sqrt(item.vI!! * item.vI!! - 2 * item.a!! * -item.dX!!)) / item.a!!

            if (value < 0) {
                value = (-item.vI!! - Math.sqrt(item.vI!! * item.vI!! - 2 * item.a!! * -item.dX!!)) / item.a!!
            }
        }

        logAll(T_TEXT)
        item.t = value
        time_text.setText(value.toString())
    }

    private fun setVF() {
        val input = vfinal_text.text.toString()

        item.vF = try {
            input.toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun setVI() {
        val input = vinitial_text.text.toString()

        item.vI = try {
            input.toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun setDX() {
        val input = deltax_text.text.toString()

        item.dX = try {
            input.toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun setA() {
        val input = acc_text.text.toString()

        item.a = try {
            input.toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun setT() {
        val input = time_text.text.toString()

        item.t = try {
            input.toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun logAll(source: String) {
        if (BuildConfig.DEBUG) {
            Log.e("Source", source)
            Log.e("VFVal", item.vF.toString())
            Log.e("VIVal", item.vI.toString())
            Log.e("DXVal", item.dX.toString())
            Log.e("AVal", item.a.toString())
            Log.e("TVal", item.t.toString())
        }
    }
}
