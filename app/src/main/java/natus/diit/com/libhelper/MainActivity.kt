package natus.diit.com.libhelper

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
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
import natus.diit.com.libhelper.model.user.LogOutResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private var etSearchByNumber: EditText? = null
    private var etSearchByAuthor: EditText? = null
    private var etSearchByKeywords: EditText? = null
    private var etSearchByYear: EditText? = null
    private var etSearchByBookName: EditText? = null

    private var receivedCookie: String? = null

    private lateinit var searchByNumber: String
    private lateinit var searchByAuthor: String
    private lateinit var searchByYear: String
    private lateinit var searchByKeywords: String
    private lateinit var searchByBookName: String

    private var isAuthorized: Boolean = false
    private var isRemembered: Boolean = false

    private lateinit var preferences: Preferences


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //suspendApp()

        preferences = Preferences(this)

        requestWriteDataPermission()
        checkLogin()

        setContentView(R.layout.activity_main)

        setToolbar()

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

            val intent = Intent(this@MainActivity, BooksListActivity::class.java)
            intent.putExtra(SEARCH_AUTHOR, searchByAuthor)
            intent.putExtra(SEARCH_NUMBER, searchByNumber)
            intent.putExtra(SEARCH_KEYWORDS, searchByNumber)
            intent.putExtra(SEARCH_YEAR, searchByYear)
            intent.putExtra(SEARCH_BOOK_NAME, searchByBookName)
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

        if (!isAuthorized) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

//        val call = apiService?.checkUser(receivedCookie)
//
//        call?.enqueue(object : Callback<CheckUserResponse> {
//            override fun onResponse(call: Call<CheckUserResponse>,
//                                    response: Response<CheckUserResponse>) {
//                val resp = response.body().isAuthorized
//                Log.i(LOG, "responce = $resp")
//                Log.i(LOG, "response.body = ${response.body()}")
//
//                if(resp == false){
//                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
//                    startActivity(intent)
//                    finish()
//                }else{
//                    preferences?.savedIsAuthorized = true
//                }
//            }
//
//            override fun onFailure(call: Call<CheckUserResponse>, t: Throwable) {
//                Toast.makeText(this@MainActivity, "Перевірте інтернет з'єднання",
//                        Toast.LENGTH_LONG)
//                        .show()
//                Log.i(LOG, "${t.message}")
//            }
//        })

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
            R.id.main_menu_exit -> logOut()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!isBackPressed) {
            isBackPressed = true
            showSnackBar("Натисніть ще раз для виходу",
                    findViewById(R.id.search_container))

            val handler = Handler()
            handler.postDelayed({ isBackPressed = false }, 2500)

        } else {
            if (!isRemembered) {
                isAuthorized = false
                preferences!!.savedIsAuthorized = false
            }
            super.onBackPressed()
            finish()
        }

    }

    companion object {
        internal val SEARCH_AUTHOR = "search_author"
        internal val SEARCH_NUMBER = "search_number"
        internal val SEARCH_KEYWORDS = "search_keywords"
        internal val SEARCH_YEAR = "search_year"
        internal val SEARCH_BOOK_NAME = "search_book_name"

        private val PERMISSION_REQUEST_CODE = 10
        private var isBackPressed = false
    }

    private fun setToolbar() {
        val myToolbar = findViewById(R.id.my_toolbar) as Toolbar?
        myToolbar?.title = getString(R.string.app_name)
        setSupportActionBar(myToolbar)
    }

    private fun logOut() {
        val call = apiService?.logOut(receivedCookie)

        call?.enqueue(object : Callback<LogOutResponse> {
            override fun onResponse(call: Call<LogOutResponse>, response: Response<LogOutResponse>) {
                val resp = response.body().response
                Log.i(LOG, "$resp")
                Log.i(LOG, "${response.body()}")
                when (resp) {
                    true -> {
                        preferences!!.savedIsAuthorized = false
                        preferences!!.savedLogin = ""
                        preferences!!.savedPassword = ""
                        preferences!!.savedIsRemembered = false

                        val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)

                        Toast.makeText(this@MainActivity, "Ви вийшли з акаунту",
                                Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    else -> {
                        Toast.makeText(this@MainActivity, "Не вдалося вийти з акаунту",
                                Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<LogOutResponse>, t: Throwable) {
                showSnackBar("Перевірте інтернет з'єднання",
                        findViewById(R.id.search_container))
            }
        })
    }


}