package com.zacharee1.kinematics

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import jp.wasabeef.recyclerview.animators.FadeInAnimator
import kotlinx.android.synthetic.main.activity_history.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryActivity : AppCompatActivity() {
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val adapter by lazy {
        val gson = GsonBuilder()
        val json = sharedPreferences.getString("history_json", null)
        val type = object: TypeToken<ArrayList<HistoryItem>>(){}.type

        CustomAdapter(gson
                .serializeSpecialFloatingPointValues()
                .create().fromJson(json, type) ?: ArrayList(), history_list)
    }

    private val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            adapter.removeItem(viewHolder.adapterPosition)
        }

        override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            val itemView = viewHolder.itemView
            val height = itemView.bottom - itemView.top
            val icon = ContextCompat.getDrawable(this@HistoryActivity, R.drawable.ic_delete_black_24dp)!!
                    .apply { setColorFilter(ContextCompat.getColor(this@HistoryActivity, R.color.delete_icon), PorterDuff.Mode.SRC_IN) }

            val canceled = dX == 0f && !isCurrentlyActive

            if (canceled) {
                c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(),
                        Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) })
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                return
            }

            val background = ColorDrawable()
            background.color = ContextCompat.getColor(this@HistoryActivity, R.color.delete_background)
            background.bounds = Rect(itemView.left, itemView.top, itemView.right, itemView.bottom)
            background.draw(c)

            val iconMargin = (height - icon.intrinsicHeight) / 4

            val iconLeft = if (dX < 0) itemView.right - iconMargin - icon.intrinsicWidth else itemView.left + iconMargin
            val iconTop = itemView.top + (height - icon.intrinsicHeight) / 2
            val iconRight = if (dX < 0) itemView.right - iconMargin else itemView.left + iconMargin + icon.intrinsicWidth
            val iconBottom = iconTop + icon.intrinsicHeight

            icon.bounds = Rect(iconLeft, iconTop, iconRight, iconBottom)
            icon.draw(c)

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        history_list.setHasFixedSize(true)

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL

        history_list.layoutManager = llm
        history_list.adapter = adapter

        touchHelper.attachToRecyclerView(history_list)

        val decorator = DividerItemDecoration(this, llm.orientation)

        history_list.addItemDecoration(decorator)
        history_list.itemAnimator = FadeInAnimator(OvershootInterpolator())
    }

    class CustomAdapter constructor(
            private val historyList: ArrayList<HistoryItem>,
            private val snackView: View) : RecyclerView.Adapter<CustomAdapter.CustomHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_layout, parent, false)
            return CustomHolder(view)
        }

        override fun onBindViewHolder(holder: CustomHolder, position: Int) {
            val history = historyList[position]

            holder.date.text = SimpleDateFormat("E MM/dd/yy hh:mm:ss a", Locale.getDefault()).format(history.time)
            holder.time.text = "${history.t}"
            holder.acc.text = "${history.a}"
            holder.vi.text = "${history.vI}"
            holder.vf.text = "${history.vF}"
            holder.dx.text = "${history.dX}"
        }

        override fun getItemCount(): Int {
            return historyList.size
        }

        fun removeItem(position: Int) {
            val item = historyList.removeAt(position)
            notifyItemRemoved(position)

            Snackbar.make(snackView, R.string.removed, Snackbar.LENGTH_LONG)
                    .apply {
                        setAction(R.string.undo) {
                            historyList.add(position, item)
                            notifyItemInserted(position)
                        }
                        show()
                    }
        }

        class CustomHolder constructor(v: View) : RecyclerView.ViewHolder(v) {
            val date: TextView = v.findViewById(R.id.date_text)
            val time: TextView = v.findViewById(R.id.time_history)
            val acc: TextView = v.findViewById(R.id.acc_history)
            val vi: TextView = v.findViewById(R.id.vinitial_history)
            val vf: TextView = v.findViewById(R.id.vfinal_history)
            val dx: TextView = v.findViewById(R.id.dx_history)
        }
    }
}
