package natus.diit.com.libhelper

import android.os.Bundle
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import android.support.v7.view.ActionMode
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import natus.diit.com.libhelper.adapter.DividerItemDecoration
import natus.diit.com.libhelper.adapter.FilesAdapter
import java.io.File
import java.util.*

class FileListActivity : AppCompatActivity(),
        FilesAdapter.MessageAdapterListener {
    override fun onMessageRowClicked(position: Int) {
        Toast.makeText(this, "Hello", Toast.LENGTH_LONG).show()
    }

    override fun onRowLongClicked(position: Int) {
        Toast.makeText(this, "World", Toast.LENGTH_LONG).show()
        enableActionMode(position)
    }

    private val messages = ArrayList<File>()
    private lateinit var recyclerView: RecyclerView
    private var mAdapter: FilesAdapter? = null
    private var actionModeCallback: ActionModeCallback? = null
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_list)
        val toolbar = findViewById<Toolbar>(R.id.toolbar2)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recycler_view)

        mAdapter = FilesAdapter(this, messages, this)

        val mLayoutManager = LinearLayoutManager(applicationContext)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
        recyclerView.adapter = mAdapter

        actionModeCallback = ActionModeCallback()

        getInbox()

    }

    private fun getInbox() {
        val folder = File(Environment.getExternalStorageDirectory()
                .toString() + File.separator + "DNURTBooks")
        Log.i(LOG, "${folder.listFiles()}")
        for (file in folder.listFiles()) {
            messages.add(file)
        }

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
            when (item.itemId) {
                R.id.action_delete -> {
                    // delete all the selected messages
                    //deleteMessages()
                    mode.finish()
                    return true
                }

                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            mAdapter!!.clearSelections()
            //swipeRefreshLayout!!.isEnabled = true
            actionMode = null
//            recyclerView.post {
//               // mAdapter!!.resetAnimationIndex()
                mAdapter!!.notifyDataSetChanged();
//            }
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
}
