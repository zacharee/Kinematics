package com.zacharee1.kinematics

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MenuItem
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
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zacharee1.kinematics.utils.sharedPreferences
import jp.wasabeef.recyclerview.animators.ScaleInTopAnimator
import kotlinx.android.synthetic.main.activity_history.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HistoryActivity : AppCompatActivity() {
    private val sharedPreferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private val adapter by lazy {
        CustomAdapter(history_list) {
            val lm = history_list.layoutManager as LinearLayoutManager

            if (it == 0
                    || it < lm.findFirstVisibleItemPosition()
                    || it > lm.findLastVisibleItemPosition())
                history_list.scrollToPosition(it)
        }
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

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        history_list.setHasFixedSize(true)

        val llm = LinearLayoutManager(this@HistoryActivity)
        llm.orientation = LinearLayoutManager.VERTICAL

        history_list.layoutManager = llm
        history_list.adapter = adapter

        touchHelper.attachToRecyclerView(history_list)

        val decorator = DividerItemDecoration(this@HistoryActivity, llm.orientation)

        history_list.addItemDecoration(decorator)

        history_list.itemAnimator = ScaleInTopAnimator().apply {
            setInterpolator(OvershootInterpolator())
            addDuration = resources.getInteger(R.integer.recview_anim_duration_ms).toLong()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onEnterAnimationComplete() {
        generate()
    }

    private fun generate() {
        GlobalScope.async {
            val json = sharedPreferences.getString("history_json", null)
            val type = object : TypeToken<ArrayList<HistoryItem>>() {}.type

            val items = GsonBuilder()
                    .serializeSpecialFloatingPointValues()
                    .create().fromJson<ArrayList<HistoryItem>>(json, type) ?: ArrayList()

            runOnUiThread {
                adapter.addItems(items)
            }
        }
    }

    class CustomAdapter constructor(private val snackView: View, private val undoListener: ((Int) -> Unit))
        : RecyclerView.Adapter<CustomAdapter.CustomHolder>() {
        private val historyList: ArrayList<HistoryItem> = ArrayList()
        private val dateFormat = SimpleDateFormat("E MM/dd/yy hh:mm:ss a", Locale.getDefault())

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.history_layout, parent, false)
            return CustomHolder(view)
        }

        override fun onBindViewHolder(holder: CustomHolder, position: Int) {
            val history = historyList[position]

            holder.date.text = dateFormat.format(history.time)
            holder.time.text = "${history.t}"
            holder.acc.text = "${history.a}"
            holder.vi.text = "${history.vI}"
            holder.vf.text = "${history.vF}"
            holder.dx.text = "${history.dX}"
        }

        override fun getItemCount(): Int {
            return historyList.size
        }

        fun addItem(item: HistoryItem) {
            historyList.add(item)
            notifyItemInserted(historyList.lastIndex)
        }

        fun addItems(items: List<HistoryItem>) {
            val prevLast = historyList.lastIndex

            historyList.addAll(items)

            notifyItemRangeInserted(prevLast + 1, historyList.lastIndex)
        }

        fun removeItem(position: Int) {
            val item = historyList.removeAt(position)
            notifyItemRemoved(position)

            Snackbar.make(snackView, R.string.removed, Snackbar.LENGTH_LONG)
                    .apply {
                        setAction(R.string.undo) {
                            historyList.add(position, item)

                            val toPos = when {
                                position > historyList.lastIndex -> historyList.lastIndex
                                else -> position
                            }

                            notifyItemInserted(position)
                            undoListener.invoke(toPos)
                        }
                        addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                saveHistory()
                            }
                        })

                        show()
                    }
        }

        private fun saveHistory() {
            val gBuilder = GsonBuilder()
            val json = gBuilder.serializeSpecialFloatingPointValues().create().toJson(historyList)

            snackView.context.sharedPreferences.edit().putString("history_json", json).apply()
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
