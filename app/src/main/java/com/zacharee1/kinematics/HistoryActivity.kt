package com.zacharee1.kinematics

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val recView: RecyclerView = findViewById(R.id.history_list)
        recView.setHasFixedSize(true)

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL

        recView.layoutManager = llm

        val gson = Gson()
        val json = sharedPreferences.getString("history_json", null)
        val type = object: TypeToken<ArrayList<HistoryType>>(){}.type

        var historyList: ArrayList<HistoryType>? = gson.fromJson(json, type)

        if (historyList == null) historyList = ArrayList()

        val adapter = CustomAdapter(historyList)

        recView.adapter = adapter

        val decorator = DividerItemDecoration(this, llm.orientation)

        recView.addItemDecoration(decorator)
    }


    class CustomAdapter constructor(historyList: ArrayList<HistoryType>) : RecyclerView.Adapter<CustomAdapter.CustomHolder>() {
        private val historyList = historyList

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CustomHolder {
            val view: View = LayoutInflater.from(parent?.context).inflate(R.layout.history_layout, parent, false)

            return CustomHolder(view)
        }

        override fun onBindViewHolder(holder: CustomHolder, position: Int) {
            val history = historyList[position]

            holder.date.text = SimpleDateFormat("E MM/dd/yy hh:mm a", Locale.getDefault()).format(history.time)
            holder.time.text = "T (s) = " +  history.t.toString()
            holder.acc.text = "A (m/s²) = " + history.a.toString()
            holder.vi.text = "VI (m/s) = " + history.vI.toString()
            holder.vf.text = "VF (m/s) = " + history.vF.toString()
            holder.dx.text = "Δx (m) = " + history.dX.toString()
        }

        override fun getItemCount(): Int {
            return historyList.size
        }

        class CustomHolder constructor(v: View) : RecyclerView.ViewHolder(v) {
            var date: TextView
            var time: TextView
            var acc: TextView
            var vi: TextView
            var vf: TextView
            var dx: TextView

            init {
                date = v.findViewById(R.id.date_text)
                time = v.findViewById(R.id.time)
                acc = v.findViewById(R.id.acc)
                vi = v.findViewById(R.id.vinitial)
                vf = v.findViewById(R.id.vfinal)
                dx = v.findViewById(R.id.dx)
            }
        }
    }
}
