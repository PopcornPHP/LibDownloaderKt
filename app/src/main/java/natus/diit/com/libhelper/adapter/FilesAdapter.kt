package natus.diit.com.libhelper.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import natus.diit.com.libhelper.R
import java.io.File
import java.util.*

/**
 * Adapter class for downloaded manuals
 */

class FilesAdapter(private val mContext: Context,
                   private val files: MutableList<File>,
                   private val listener: FilesAdapterListener)
    : RecyclerView.Adapter<FilesAdapter.MyViewHolder>() {
    private val selectedItems: SparseBooleanArray = SparseBooleanArray()

    val selectedItemCount: Int
        get() = selectedItems.size()

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnLongClickListener {
        var tvBookName: TextView = view.findViewById(R.id.files_tv_bookname) as TextView
        var filesContainer = view.findViewById(R.id.files_container) as LinearLayout

        init {
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
        val file = files[position]
        // displaying text view data
        holder.tvBookName.text = String
                .format(mContext.resources.getString(R.string.files_manual_number),
                        file.nameWithoutExtension)

        holder.itemView.isActivated = selectedItems.get(position, false)
        applyClickEvents(holder, position)
    }

    private fun applyClickEvents(holder: MyViewHolder, position: Int) {

        holder.filesContainer.setOnClickListener { listener.onMessageRowClicked(position) }

        holder.filesContainer.setOnLongClickListener { view ->
            listener.onRowLongClicked(position)
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            true
        }
    }

    override fun getItemCount(): Int {
        return files.size
    }

    /**
     * Enable or disable item`s selection
     * @param pos Clicked item`s position
     */
    fun toggleSelection(pos: Int) {
        currentSelectedIndex = pos
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos)
        } else {
            selectedItems.put(pos, true)
        }
        notifyItemChanged(pos)
    }

    fun clearSelections() {
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

    interface FilesAdapterListener {
        fun onMessageRowClicked(position: Int)

        fun onRowLongClicked(position: Int)
    }

    fun getFile(position: Int): File {
        return files[position]
    }

    companion object {

        // index is used to animate only the selected row
        // dirty fix, find a better solution
        private var currentSelectedIndex = -1
    }
}
