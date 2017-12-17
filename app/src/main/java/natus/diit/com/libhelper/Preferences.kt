package natus.diit.com.libhelper

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Environment
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import natus.diit.com.libhelper.rest.ApiInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


//Auxiliary class which works with SharesPreferences
//and contains some global variables
const val LOG = "MyLog"
const val PROVIDERS_PATH = ".natus.diit.com.libhelper.provider"
val booksFolder = File(Environment.getExternalStorageDirectory()
.toString() + File.separator + "DNURTBooks")

internal var libBookApi: ApiInterface? = null
fun showSnackBar(view: View, text: String = "Перевірте інтернет з'єднання"): Snackbar {
    return Snackbar.make(view,
            text, Snackbar.LENGTH_LONG)
}

fun setToolbar(activity: AppCompatActivity,
               toolbarTitleRes: Int,
               arrowColorRes: Int = R.color.colorWhite,
               toolbarResId: Int = R.id.my_toolbar
) {

    val myToolbar = activity.findViewById(toolbarResId) as Toolbar?
    myToolbar?.title = activity.getString(toolbarTitleRes)
    activity.setSupportActionBar(myToolbar)

    val upArrow: Drawable = ContextCompat.getDrawable(activity, R.drawable.ic_arrow_back_white_24dp)
    upArrow.setColorFilter(activity.resources.getColor(arrowColorRes), PorterDuff.Mode.SRC_ATOP)
    activity.supportActionBar?.setHomeAsUpIndicator(upArrow)

    if (NavUtils.getParentActivityName(activity) != null) {
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }
}

class Preferences : Application {
    val domain = "https://library.diit.edu.ua"
    private lateinit var prefs: SharedPreferences
    private val sharedPrefsFile = "MyPreferences"
    private val BASE_URL = "https://library.diit.edu.ua/api/"
    private lateinit var retrofit: Retrofit


    override fun onCreate() {
        super.onCreate()

        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        libBookApi = retrofit.create(ApiInterface::class.java)
    }

    var savedLogin: String = ""
        set(value) {
            field = value
            val ed = prefs.edit()
            ed.putString("login", value)
            ed.apply()
        }
    var savedPassword: String = ""
        set(value) {
            field = value
            val ed = prefs.edit()
            ed.putString("password", value)
            ed.apply()
        }

    var savedTranslateDirection: String? = null
        set(value) {
            field = value

            val ed = prefs.edit()
            ed.putString("translateDirection", value)
            ed.apply()
        }
    var savedDictionarySearch: String? = null
        set(value) {
            field = value

            val ed = prefs.edit()
            ed.putString("search", value)
            ed.apply()
        }

    var savedReceivedCookie: String = ""
        set(value) {
            field = value

            val ed = prefs.edit()
            ed.putString("cookie", value)
            ed.apply()
        }
    var savedIsAuthorized: Boolean = false
        set(value) {
            field = value
            val ed = prefs.edit()
            ed.putBoolean("isAuthorized", value)
            ed.apply()
        }
    var savedIsRemembered: Boolean = false
        set(value) {
            field = value
            val ed = prefs.edit()
            ed.putBoolean("remember", value)
            ed.apply()
        }

    var savedSearchByYear: String = ""
        set(value) {
            field = value

            val ed = prefs.edit()
            ed.putString("searchByYear", value)
            ed.apply()
        }
    var savedSearchByNumber: String = ""
        set(value) {
            field = value

            val ed = prefs.edit()
            ed.putString("searchByNumber", value)
            ed.apply()
        }
    var savedSearchByBookName: String = ""
        set(value) {
            field = value

            val ed = prefs.edit()
            ed.putString("searchByBookName", value)
            ed.apply()
        }
    var savedSearchByKeywords: String = ""
        set(value) {
            field = value

            val ed = prefs.edit()
            ed.putString("searchByKeywords", value)
            ed.apply()
        }
    var savedSearchByAuthor: String = ""
        set(value) {
            field = value

            val ed = prefs.edit()
            ed.putString("searchByAuthor", value)
            ed.apply()
        }

    //default constructor
    constructor() {}

    constructor(context: Context) {
        prefs = context.getSharedPreferences(sharedPrefsFile, Context.MODE_PRIVATE)

        savedReceivedCookie = prefs.getString("cookie", "")
        savedIsAuthorized = prefs.getBoolean("isAuthorized", false)
        savedIsRemembered = prefs.getBoolean("remember", false)

        savedSearchByYear = prefs.getString("searchByYear", "")
        savedSearchByNumber = prefs.getString("searchByNumber", "")
        savedSearchByBookName = prefs.getString("searchByBookName", "")
        savedSearchByKeywords = prefs.getString("searchByKeywords", "")
        savedSearchByAuthor = prefs.getString("searchByAuthor", "")

        savedDictionarySearch = prefs.getString("search", "")
        savedTranslateDirection = prefs.getString("translateDirection", "")

        savedLogin = prefs.getString("login", "")
        savedPassword = prefs.getString("password", "")
    }

    fun getJSONFromServer(url: URL, cookie: String?): String {
        val urlConnection: HttpsURLConnection
        val reader: BufferedReader

        urlConnection = url.openConnection() as HttpsURLConnection
        urlConnection.doOutput = true
        urlConnection.requestMethod = "POST"
        urlConnection.setRequestProperty("Cookie", cookie)
        Log.i(LOG, "$cookie")
        urlConnection.connect()

        val inputStream = urlConnection.inputStream
        val buffer = StringBuffer()

        reader = BufferedReader(InputStreamReader(inputStream))

        var line: String?
        while (true) {
            line = reader.readLine()
            if (line == null)
                break
            buffer.append(line)
        }

        return buffer.toString()
    }


    fun getJSONFromServer(url: URL): String {

        val urlConnection: HttpURLConnection
        val reader: BufferedReader
        val cookie: String

        urlConnection = url.openConnection() as HttpsURLConnection
        urlConnection.setRequestMethod("POST")
        urlConnection.connect()
        cookie = urlConnection.getHeaderField("Set-Cookie")
        savedReceivedCookie = cookie

        val inputStream = urlConnection.getInputStream()
        val buffer = StringBuilder()
        reader = BufferedReader(InputStreamReader(inputStream))

        var line: String?
        while (true) {
            line = reader.readLine()
            if (line == null)
                break
            buffer.append(line)
        }
        return buffer.toString()
    }


    companion object {

    }
}
