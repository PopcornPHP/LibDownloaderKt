package natus.diit.com.libhelper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import java.net.URL


class MainActivity : AppCompatActivity() {

    private var etSearchByNumber: EditText? = null
    private var etSearchByAuthor: EditText? = null
    private var etSearchByKeywords: EditText? = null
    private var etSearchByYear: EditText? = null
    private var etSearchByBookName: EditText? = null

    private var receivedCookie: String? = null

    private var searchByNumber: String? = null
    private var searchByAuthor: String? = null
    private var searchByYear: String? = null
    private var searchByKeywords: String? = null
    private var searchByBookName: String? = null

    private var isAuthorized: Boolean = false
    private var isRemembered: Boolean = false

    private var preferences: Preferences? = null
    private var domain: String? = null




    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //suspendApp()

        preferences = Preferences(this)

        requestWriteDataPermission()
        checkLogin()

        setContentView(R.layout.activity_main)

        setToolbar()

        domain = preferences!!.domain

        etSearchByNumber = findViewById(R.id.search_number_field) as EditText
        etSearchByAuthor = findViewById(R.id.searchAuthor) as EditText
        etSearchByKeywords = findViewById(R.id.searchByKeywords) as EditText
        etSearchByYear = findViewById(R.id.searchByYear) as EditText
        etSearchByBookName = findViewById(R.id.search_name_field) as EditText

        val btnSearch = findViewById(R.id.search_button_main) as Button
        btnSearch.setOnClickListener {
            searchByNumber = etSearchByNumber!!.text.toString()
            searchByAuthor = etSearchByAuthor!!.text.toString()
            searchByKeywords = etSearchByKeywords!!.text.toString()
            searchByYear = etSearchByYear!!.text.toString()
            searchByBookName = etSearchByBookName!!.text.toString()

            preferences!!.savedSearchByNumber = searchByNumber
            preferences!!.savedSearchByAuthor = searchByAuthor
            preferences!!.savedSearchByKeywords = searchByKeywords
            preferences!!.savedSearchByYear = searchByYear
            preferences!!.savedSearchByBookName = searchByBookName

            val intent = Intent(this@MainActivity, BooksListActivity::class.java)
            startActivity(intent)
        }
        btnSearch.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val animation = AnimationUtils.loadAnimation(this@MainActivity, R.anim.my_anim)
                    btnSearch.startAnimation(animation)
                }
            }
            false
        }


    }

    fun suspendApp() {
        val builder: AlertDialog.Builder
        builder = AlertDialog.Builder(this@MainActivity)
        builder.setNegativeButton("Ок") { dialog, which -> finish() }
        builder.setOnCancelListener { finish() }

        builder.setOnDismissListener { finish() }


        builder.setTitle("Інформація")
                .setMessage("Додаток тимчасово призупинено.")
                .show()

    }

    private fun requestWriteDataPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE)
        }
    }


    private fun checkLogin() {
        receivedCookie = preferences!!.savedReceivedCookie
        isAuthorized = preferences!!.savedIsAuthorized
        isRemembered = preferences!!.savedIsRemembered
        Log.i(LOG, "$isAuthorized, $isRemembered")
        if (!isAuthorized) {
            Log.i(LOG, "lol here")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
        //Dictionary
            R.id.main_menu_dictionary -> {
                val intent = Intent(this, DictionaryActivity::class.java)
                startActivity(intent)
            }
        //Orders
            R.id.main_menu_orders -> {
                val intent = Intent(this, OrderListActivity::class.java)
                startActivity(intent)
            }
        //Exit
            R.id.main_menu_exit -> LogOutTask().execute()
        }

        return super.onOptionsItemSelected(item)
    }

    private inner class LogOutTask : AsyncTask<Void, Void, String>() {
        internal var resultJson = ""

        override fun doInBackground(vararg params: Void): String {
            try {
                val url = URL(domain!! + "/api/user/signout")
                resultJson = preferences!!.getJSONFromServer(url, receivedCookie)
                Log.i(LOG, "JSON " + resultJson)

            } catch (e: Exception) {
                Log.i(LOG, "LogOut error" + e.message)
            }

            return resultJson
        }

        override fun onPostExecute(s: String) {
            super.onPostExecute(s)
            try {
                if (s != "") {
                    preferences!!.savedIsAuthorized = false
                    preferences!!.savedLogin = ""
                    preferences!!.savedPassword = ""
                    preferences!!.savedIsRemembered = false

                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)

//                    Toast.makeText(this@MainActivity, "Ви вийшли з акаунту",
//                            Toast.LENGTH_SHORT).show()
                    showSnackBar("Ви вийшли з акаунту", findViewById(R.id.passw_login_form))
                    finish()
                } else {
                    showSnackBar("Перевірте інтернет з'єднання",
                            findViewById(R.id.search_container))
                }
            } catch (e: Exception) {
                Log.i(LOG, "LogOut error 2" + e.message)
            }

        }
    }

    override fun onBackPressed() {
        Log.i(LOG, "isBackPressed = $isBackPressed")
        if(!isBackPressed){
            isBackPressed = true
            showSnackBar("Натисніть ще раз для виходу",
                    findViewById(R.id.search_container))

            val handler = Handler()
            handler.postDelayed({ isBackPressed = false }, 2500 )

        }else{
            if (!isRemembered) {
                isAuthorized = false
                preferences!!.savedIsAuthorized = false
            }
            super.onBackPressed()
            finish()
        }

    }

    companion object {

        private val PERMISSION_REQUEST_CODE = 10
        private var isBackPressed = false
    }

    private fun setToolbar() {
        val myToolbar = findViewById(R.id.my_toolbar) as Toolbar?
        Log.i(LOG, "$myToolbar")
        myToolbar?.title = getString(R.string.app_name)
        setSupportActionBar(myToolbar)
    }
}