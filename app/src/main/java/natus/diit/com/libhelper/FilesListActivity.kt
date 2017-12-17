package natus.diit.com.libhelper

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import natus.diit.com.libhelper.adapter.DividerItemDecoration
import natus.diit.com.libhelper.adapter.FilesAdapter
import java.io.File
import java.io.FileInputStream
import java.net.URLConnection
import java.util.*


class FilesListActivity : AppCompatActivity(),
        FilesAdapter.FilesAdapterListener {

    private val files = ArrayList<File>()
    private lateinit var recyclerView: RecyclerView
    private var mAdapter: FilesAdapter? = null
    private var actionModeCallback: ActionModeCallback? = null
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)
        setToolbar(this, R.string.title_activity_files_list, toolbarResId = R.id.files_toolbar)

        recyclerView = findViewById(R.id.recycler_view) as RecyclerView

        mAdapter = FilesAdapter(this, files, this)

        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.adapter = mAdapter

        actionModeCallback = ActionModeCallback()

        fetchDownloadedBooks()

    }

    private fun fetchDownloadedBooks() {
        val folder = booksFolder
        Log.i(LOG, "${folder.listFiles()}")
        files += folder.listFiles()

        mAdapter!!.notifyDataSetChanged()
    }

    private inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_files_action_mode, menu)

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_delete -> {
                    // delete all the selected files
                    deleteFiles()
                    mode.finish()
                    true
                }

                else -> false
            }
        }

        private fun deleteFiles() {
            val selectedItemPositions = mAdapter!!.getSelectedItems()
            for (i in selectedItemPositions.size - 1 downTo 0) {
                val file: File = mAdapter!!.getFile(selectedItemPositions[i])
                mAdapter!!.removeData(selectedItemPositions[i])
                val deleted = file.delete()
                Log.i(LOG, "$deleted")
            }
            mAdapter!!.notifyDataSetChanged()
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mAdapter!!.clearSelections()
            actionMode = null
            mAdapter!!.notifyDataSetChanged();
        }
    }

    private fun enableActionMode(position: Int) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback!!)
        }
        toggleSelection(position)
    }

    private fun toggleSelection(position: Int) {
        mAdapter!!.toggleSelection(position)
        val count = mAdapter!!.selectedItemCount

        if (count == 0) {
            actionMode!!.finish()
        } else {
            actionMode!!.title = count.toString()
            actionMode!!.invalidate()
        }
    }

    override fun onMessageRowClicked(position: Int) {
        if (mAdapter!!.selectedItemCount > 0) {
            enableActionMode(position)
        } else {
            val file = mAdapter!!.getFile(position)
            val myIntent = Intent(Intent.ACTION_VIEW)
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                var mime = URLConnection.guessContentTypeFromStream(FileInputStream(file))
                if (mime == null) mime = URLConnection.guessContentTypeFromName(file.getName())
                myIntent.setDataAndType(Uri.fromFile(file), mime)
                startActivity(myIntent)
            }else{
                myIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                val uri = FileProvider.getUriForFile(this,
                        applicationContext.
                                packageName + PROVIDERS_PATH, file)
                myIntent.data = uri
                startActivity(myIntent)
            }
        }
    }

    override fun onRowLongClicked(position: Int) {
        enableActionMode(position)
    }
}
