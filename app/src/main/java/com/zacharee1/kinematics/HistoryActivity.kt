package com.zacharee1.kinematics

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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

        val gson = GsonBuilder()
        val json = sharedPreferences.getString("history_json", null)
        val type = object: TypeToken<ArrayList<HistoryType>>(){}.type

        var historyList: ArrayList<HistoryType>? = gson.serializeSpecialFloatingPointValues().create().fromJson(json, type)

        if (historyList == null) historyList = ArrayList()

        val adapter = CustomAdapter(historyList, this)

        recView.adapter = adapter

        val decorator = DividerItemDecoration(this, llm.orientation)

        recView.addItemDecoration(decorator)
        recView.itemAnimator = FadeInAnimator(OvershootInterpolator())
    }

    class CustomAdapter constructor(historyList: ArrayList<HistoryType>, context: Context) : RecyclerView.Adapter<CustomAdapter.CustomHolder>() {
        private val historyList = historyList
        private val context = context

        override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CustomHolder {
            val view: View = LayoutInflater.from(parent?.context).inflate(R.layout.history_layout, parent, false)

            return CustomHolder(view)
        }

        override fun onBindViewHolder(holder: CustomHolder, position: Int) {
            val history = historyList[position]

            holder.date.text = SimpleDateFormat("E MM/dd/yy hh:mm:ss a", Locale.getDefault()).format(history.time)
            holder.time.text = String.format("T (s) = %.4f", history.t)
            holder.acc.text = String.format("A (m/s²) = %.4f", history.a)
            holder.vi.text = String.format("VI (m/s) = %.4f", history.vI)
            holder.vf.text = String.format("VF (m/s) = %.4f", history.vF)
            holder.dx.text = String.format("Δx (m) = %.4f", history.dX)

            holder.layout.setOnLongClickListener {
                AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Delete?")
                        .setMessage("Remove From History?")
                        .setPositiveButton("Yes", { dialog, which ->
                            historyList.removeAt(position)
                            saveNewHistory(historyList)
                            notifyItemRemoved(position)
                        })
                        .setNegativeButton("No", null)
                        .show()
                true
            }
        }

        override fun getItemCount(): Int {
            return historyList.size
        }

        fun saveNewHistory(list: ArrayList<HistoryType>) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

            val gson = GsonBuilder()
            var json = gson.serializeSpecialFloatingPointValues().create().toJson(list)

            sharedPreferences.edit().putString("history_json", json).apply()
        }

        class CustomHolder constructor(v: View) : RecyclerView.ViewHolder(v) {
            var date: TextView
            var time: TextView
            var acc: TextView
            var vi: TextView
            var vf: TextView
            var dx: TextView

            var layout: LinearLayout

            init {
                date = v.findViewById(R.id.date_text)
                time = v.findViewById(R.id.time_history)
                acc = v.findViewById(R.id.acc_history)
                vi = v.findViewById(R.id.vinitial_history)
                vf = v.findViewById(R.id.vfinal_history)
                dx = v.findViewById(R.id.dx_history)

                layout = v.findViewById(R.id.history_layout)
            }
        }
    }
}
