package com.zacharee1.kinematics

import android.content.*
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

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

        setUpActionBar()
        setElements()
    }

    fun setUpActionBar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
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
            Toast.makeText(this, "History", Toast.LENGTH_SHORT).show()
            true
        }
        calc.setOnLongClickListener {
            Toast.makeText(this, "Calculate", Toast.LENGTH_SHORT).show()
            true
        }
        reset.setOnLongClickListener {
            Toast.makeText(this, "Reset", Toast.LENGTH_SHORT).show()
            true
        }

        toolbar.addView(history)
        toolbar.addView(calc)
        toolbar.addView(reset)
    }

    fun setElements() {
        vFInput = findViewById(R.id.vfinal_text)
        vIInput = findViewById(R.id.vinitial_text)
        dXInput = findViewById(R.id.deltax_text)
        aInput = findViewById(R.id.acc_text)
        tInput = findViewById(R.id.time_text)
    }

    fun onReset(v: View?) {
        vFInput.text = null
        vIInput.text = null
        dXInput.text = null
        aInput.text = null
        tInput.text = null

        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(window.currentFocus.windowToken, 0)

        vFInput.clearFocus()
        vIInput.clearFocus()
        dXInput.clearFocus()
        aInput.clearFocus()
        tInput.clearFocus()
    }

    fun onCalc(v: View?) {
        setVF()
        setVI()
        setDX()
        setA()
        setT()

        checkNull()
    }

    fun checkNull() {
        val nulls: ArrayList<String> = ArrayList()

        if (isNull(vF)) nulls.add(VF)
        if (isNull(vI)) nulls.add(VI)
        if (isNull(dX)) nulls.add(DX)
        if (isNull(a)) nulls.add(A)
        if (isNull(t)) nulls.add(T)

        if (nulls.size > 2) Toast.makeText(this, "Need at least three knowns", Toast.LENGTH_LONG).show()
        else doCalc(nulls)
    }

    fun doCalc(nulls: ArrayList<String>) {
        if (nulls.contains(VI)) {
            solveForVI()
            nulls.remove(VI)
        }

        if (nulls.contains(VF)) {
            solveForVF()
            nulls.remove(VF)
        }

        if (nulls.contains(DX)) {
            solveForDX()
            nulls.remove(DX)
        }

        if (nulls.contains(A)) {
            solveForA()
            nulls.remove(A)
        }

        if (nulls.contains(T)) {
            solveForT()
            nulls.remove(T)
        }

        saveHistory()
        printHistory(null)
        onReset(null)
    }

    fun saveHistory() {
        val gBuilder = GsonBuilder()
        var json = sharedPreferences.getString("history_json", null)
        val type = object: TypeToken<ArrayList<HistoryType>>(){}.type

        var historyList: ArrayList<HistoryType>? = gBuilder.serializeSpecialFloatingPointValues().create().fromJson(json, type)

        if (historyList == null) historyList = ArrayList()

        val historyItem = HistoryType(vF, vI, dX, a, t, System.currentTimeMillis())

        historyList.add(0, historyItem)

        json = gBuilder.serializeSpecialFloatingPointValues().create().toJson(historyList)

        sharedPreferences.edit().putString("history_json", json).apply()
    }

    fun printHistory(v: View?) {
        val gson = GsonBuilder()
        val json = sharedPreferences.getString("history_json", null)
        val type = object: TypeToken<ArrayList<HistoryType>>(){}.type

        var historyList: ArrayList<HistoryType>? = gson.serializeSpecialFloatingPointValues().create().fromJson(json, type)

        if (historyList == null) historyList = ArrayList()

        findViewById<LinearLayout>(R.id.history_layout).visibility = View.VISIBLE

        if (historyList.size > 0) {
            val history = historyList[0]

            val date: TextView = findViewById(R.id.date_text)
            val time: TextView = findViewById(R.id.time_history)
            val acc: TextView = findViewById(R.id.acc_history)
            val vi: TextView = findViewById(R.id.vinitial_history)
            val vf: TextView = findViewById(R.id.vfinal_history)
            val dx: TextView = findViewById(R.id.dx_history)

            date.text = SimpleDateFormat("E MM/dd/yy hh:mm:ss a", Locale.getDefault()).format(history.time)
            time.text = String.format("T (s) = %.4f", history.t)
            acc.text = String.format("A (m/s²) = %.4f", history.a)
            vi.text = String.format("VI (m/s) = %.4f", history.vI)
            vf.text = String.format("VF (m/s) = %.4f", history.vF)
            dx.text = String.format("Δx (m) = %.4f", history.dX)

            val manager: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            findViewById<LinearLayout>(R.id.history_layout).setOnClickListener {
                val alertDialog = AlertDialog.Builder(this)
                        .setTitle((findViewById<TextView>(R.id.date_text)).text.toString())
                        .setView(R.layout.layout_full_history)
                        .setPositiveButton("OK", null)
                        .show()

                val time: TextView? = alertDialog.findViewById(R.id.time_measure)
                val acc: TextView? = alertDialog.findViewById(R.id.acc_measure)
                val vi: TextView? = alertDialog.findViewById(R.id.vi_measure)
                val vf: TextView? = alertDialog.findViewById(R.id.vf_measure)
                val dx: TextView? = alertDialog.findViewById(R.id.dx_measure)

                time?.text = history.t.toString()
                acc?.text = history.a.toString()
                vi?.text = history.vI.toString()
                vf?.text = history.vF.toString()
                dx?.text = history.dX.toString()

                val clickListen: View.OnClickListener = View.OnClickListener {view: View ->
                    var name = "unknown"

                    when (view) {
                        time -> name = "time"
                        acc -> name = "acceleration"
                        vi -> name = "vinitial"
                        vf -> name = "vfinal"
                        dx -> name = "delta"
                    }

                    val valueToSave = (view as TextView).text.toString()
                    val clip: ClipData = ClipData.newPlainText(name, valueToSave)
                    manager.primaryClip = clip

                    Toast.makeText(this, "$name copied to clipboard", Toast.LENGTH_SHORT).show()
                }

                time?.setOnClickListener(clickListen)
                acc?.setOnClickListener(clickListen)
                vi?.setOnClickListener(clickListen)
                vf?.setOnClickListener(clickListen)
                dx?.setOnClickListener(clickListen)
            }
        }
    }

    fun isNull(num: Double): Boolean {
        return num == Double.NEGATIVE_INFINITY
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
            value = a * t + vI
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

        logAll("VF")
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
                     && !isNull(dX)) {
            Log.e("VI", "4")
            value = Math.sqrt(vF * vF - 2 * a * dX)
        }

        logAll("VI")
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
                     && !isNull(a) && a != 0.0) {
            Log.e("DX", "3")
            value = (vF * vF - vI * vI) / 2 * a
        }

        logAll("DX")
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

        logAll("A")
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
                && !isNull(a) && a != 0.0) {
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

        logAll("T")
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

    fun logAll(source: String) {
        Log.e("Source", source)
        Log.e("VFVal", vF.toString())
        Log.e("VIVal", vI.toString())
        Log.e("DXVal", dX.toString())
        Log.e("AVal", a.toString())
        Log.e("TVal", t.toString())
    }
}
