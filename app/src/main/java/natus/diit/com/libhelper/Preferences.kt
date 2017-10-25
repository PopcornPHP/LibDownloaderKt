package natus.diit.com.libhelper

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.support.v7.app.AlertDialog
import android.text.Html
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import java.text.DecimalFormat
import javax.net.ssl.*

//Auxiliary class which works with SharesPreferences
//and contains some global variables
class Preferences : Application {
    val domain = "https://library.diit.edu.ua"
    private lateinit var prefs: SharedPreferences
    private val sharedPrefsFile = "MyPreferences"

    var savedLogin: String? = null
    set(value) {
        field = value
        val ed = prefs.edit()
        ed.putString("login", value)
        ed.apply()
    }
    var savedPassword: String? = null
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

    var savedReceivedCookie: String? = null
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

    var savedSearchByYear: String? = null
    set(value) {
        field = value

        val ed = prefs.edit()
        ed.putString("searchByYear", value)
        ed.apply()
    }
    var savedSearchByNumber: String? = null
    set(value) {
        field = value

        val ed = prefs.edit()
        ed.putString("searchByNumber", value)
        ed.apply()
    }
    var savedSearchByBookName: String? = null
    set(value) {
        field = value

        val ed = prefs.edit()
        ed.putString("searchByBookName", value)
        ed.apply()
    }
    var savedSearchByKeywords: String? = null
    set(value) {
        field = value

        val ed = prefs.edit()
        ed.putString("searchByKeywords", value)
        ed.apply()
    }
    var savedSearchByAuthor: String? = null
    set(value) {
        field = value

        val ed = prefs.edit()
        ed.putString("searchByAuthor", value)
        ed.apply()
    }

    override fun onCreate() {
        super.onCreate()

        //Insecure method, must be deleted as soon as possible.
        //trustAllCertificates();
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

    private fun trustAllCertificates() {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate>? {
                return null
            }

            override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) {}

            override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) {}

        })

        try {
            val sc = SSLContext.getInstance("SSL")
            sc.init(null, trustAllCerts, java.security.SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)
        } catch (e: Exception) {

        }

        // Create all-trusting host name verifier
        val allHostsValid = HostnameVerifier { hostname, session -> true }
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
    }

    fun getJSONFromServer(url: URL, cookie: String?): String {
        val urlConnection: HttpsURLConnection
        val reader: BufferedReader

        urlConnection = url.openConnection() as HttpsURLConnection
        urlConnection.doOutput = true
        urlConnection.requestMethod = "POST"
        urlConnection.setRequestProperty("Cookie", cookie)
        urlConnection.connect()

        val inputStream = urlConnection.inputStream
        val buffer = StringBuffer()

        reader = BufferedReader(InputStreamReader(inputStream))

        var line: String?
        while ( true ) {
            line = reader.readLine()
            if(line == null)
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
        while ( true ) {
            line = reader.readLine()
            if(line == null)
                break
            buffer.append(line)
        }
        return buffer.toString()
    }

    fun showBookInfo(lb: LibBook, builder: AlertDialog.Builder) {

        val formattedDouble = DecimalFormat("#0.00")
                .format(lb.fileSize / (1024 * 1024))

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
            builder.setTitle("Інформація про книгу")
                    .setMessage(Html.fromHtml("<b>" + "Назва книги: " + "</b>" + lb.bookName
                            + "<br>" + ("<b>" + "Скорочена назва: " + "</b>") + lb.linkName
                            + "<br>" + ("<b>" + "Автори: " + "</b>") + lb.author
                            + "<br>" + ("<b>" + "Рік: " + "</b>") + lb.year
                            + "<br>" + ("<b>" + "Розмір: " + "</b>") + formattedDouble + " мб"))
                    .show()
        } else {
            builder.setTitle("Інформація про книгу")
                    .setMessage(Html.fromHtml("<b>" + "Назва книги: " + "</b>" + lb.bookName
                            + "<br>" + ("<b>" + "Скорочена назва: " + "</b>") + lb.linkName
                            + "<br>" + ("<b>" + "Автори: " + "</b>") + lb.author
                            + "<br>" + ("<b>" + "Рік: " + "</b>") + lb.year
                            + "<br>" + ("<b>" + "Розмір: " + "</b>") + formattedDouble + " мб", Html.FROM_HTML_MODE_LEGACY))
                    .show()
        }
    }

    fun showOrderInfo(lb: LibBook?, builder: AlertDialog.Builder) {
        val status = lb?.orderStatus
        val bookName = lb?.bookName
        val delPoint = lb?.delPoint

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
            builder.setTitle("Інформація про замовлення")
                    .setMessage(Html.fromHtml("<b>" + "Назва книги: " + "</b>" + bookName
                            + "<br>" + ("<b>" + "Автори: " + "</b>") + lb!!.author
                            + "<br>" + ("<b>" + "Рік: " + "</b>") + lb.year
                            + "<br>" + ("<b>" + "Статус замовлення: " + "</b>") + status
                            + "<br>" + ("<b>" + "Місце видачі: " + "</b>") + delPoint))
                    .show()
        } else {
            builder.setTitle("Інформація про замовлення")
                    .setMessage(Html.fromHtml("<b>" + "Назва книги: " + "</b>" + bookName
                            + "<br>" + ("<b>" + "Автори: " + "</b>") + lb!!.author
                            + "<br>" + ("<b>" + "Рік: " + "</b>") + lb.year
                            + "<br>" + ("<b>" + "Статус замовлення: " + "</b>") + status
                            + "<br>" + ("<b>" + "Місце видачі: " + "</b>") + delPoint, Html.FROM_HTML_MODE_LEGACY))
                    .show()
        }
    }
}
