package natus.diit.com.libhelper.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import natus.diit.com.libhelper.R
import java.io.File
import java.util.*

class FilesAdapter(private val mContext: Context,
                   private val files: MutableList<File>,
                   private val listener: MessageAdapterListener)
    : RecyclerView.Adapter<FilesAdapter.MyViewHolder>() {
    internal val selectedItems: SparseBooleanArray = SparseBooleanArray()

    // array used to perform multiple animation at once
    private val animationItemsIndex: SparseBooleanArray = SparseBooleanArray()
    private var reverseAllAnimations = false

    val selectedItemCount: Int
        get() = selectedItems.size()

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnLongClickListener {
        var from: TextView
        var imgProfile: ImageView
        var messageContainer: LinearLayout
        var iconFront: RelativeLayout

        init {
            from = view.findViewById<TextView>(R.id.from) as TextView
            iconFront = view.findViewById<RelativeLayout>(R.id.icon_front) as RelativeLayout
            imgProfile = view.findViewById<ImageView>(R.id.icon_profile) as ImageView
            messageContainer = view.findViewById<LinearLayout>(R.id.message_container) as LinearLayout
            view.setOnLongClickListener(this)
        }

        override fun onLongClick(view: View): Boolean {
            listener.onRowLongClicked(adapterPosition)
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            return true
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.files_list_row, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = files[position]

        // displaying text view data
        holder.from.text = "Методичка ${message.name}"

        // apply click events
        applyClickEvents(holder, position)
    }

    private fun applyClickEvents(holder: MyViewHolder, position: Int) {

        holder.messageContainer.setOnClickListener { listener.onMessageRowClicked(position) }

        holder.messageContainer.setOnLongClickListener { view ->
            listener.onRowLongClicked(position)
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            true
        }
    }

//
//    override fun getItemId(position: Int): Long {
//        return files[position].id.toLong()
//    }



    override fun getItemCount(): Int {
        return files.size
    }

    fun toggleSelection(pos: Int) {
        currentSelectedIndex = pos
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos)
            animationItemsIndex.delete(pos)
        } else {
            selectedItems.put(pos, true)
            animationItemsIndex.put(pos, true)
        }
        notifyItemChanged(pos)
    }

    fun clearSelections() {
        reverseAllAnimations = true
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<Int> {
        val items = ArrayList<Int>(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            items.add(selectedItems.keyAt(i))
        }
        return items
    }

    fun removeData(position: Int) {
        files.removeAt(position)
        resetCurrentIndex()
    }

    private fun resetCurrentIndex() {
        currentSelectedIndex = -1
    }

    interface MessageAdapterListener {
        fun onMessageRowClicked(position: Int)

        fun onRowLongClicked(position: Int)
    }

    companion object {

        // index is used to animate only the selected row
        // dirty fix, find a better solution
        private var currentSelectedIndex = -1
    }
}
