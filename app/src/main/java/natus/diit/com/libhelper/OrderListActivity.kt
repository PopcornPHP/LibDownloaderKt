package natus.diit.com.libhelper

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import java.net.URL

class OrderListActivity : AppCompatActivity() {

    private var libBooks: Array<LibBook?>? = null
    private var bookNames: Array<String?>? = null
    private var receivedCookie: String? = null
    internal var LOG = "MyLog"
    private var booksList: ListView? = null

    private var preferences: Preferences? = null
    private var domain: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)

        preferences = Preferences(this.applicationContext)

        receivedCookie = preferences!!.savedReceivedCookie

        domain = preferences!!.domain

        booksList = findViewById(R.id.order_books) as ListView
        booksList!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val lb = libBooks!![position]

            val bookID = lb!!.bookId

            val builder: AlertDialog.Builder
            builder = AlertDialog.Builder(this@OrderListActivity)
            builder.setPositiveButton("Скасувати замовлення") { dialog, which -> CancelOrderTask().execute(bookID) }

            preferences!!.showOrderInfo(lb, builder)
        }

        OrderListTask().execute()
    }

    private inner class OrderListTask : AsyncTask<Void, Void, String>() {
        internal var resultJson = ""

        override fun doInBackground(vararg params: Void): String {
            try {
                val url = URL(domain!! + "/api/order/getAll")
                resultJson = preferences!!.getJSONFromServer(url, receivedCookie)
                Log.i(LOG, "JSON " + resultJson)

            } catch (e: Exception) {
                finish()
            }

            return resultJson
        }

        override fun onPostExecute(strJson: String) {
            super.onPostExecute(strJson)
            val dataJsonObj: JSONObject
            try {

                dataJsonObj = JSONObject(strJson)
                val tmpObj = dataJsonObj.getJSONObject("response")
                val booksArray = tmpObj.getJSONArray("data")

                libBooks = arrayOfNulls(booksArray.length())
                bookNames = arrayOfNulls(booksArray.length())
                var deliveryPoint: String
                for (i in 0 until booksArray.length()) {
                    val tempObj = booksArray.getJSONObject(i)
                    val relBookObj = tempObj.getJSONObject("rel_book")

                    try {
                        val relBranchObj = tempObj.getJSONObject("rel_branch")
                        deliveryPoint = relBranchObj.getString("name")
                    } catch (jE: JSONException) {
                        deliveryPoint = ""
                    }

                    val bookId = relBookObj.getInt("id")
                    val category = relBookObj.getString("category_id")
                    val bookName = relBookObj.getString("name")
                    val link = relBookObj.getString("link")
                    val linkName = relBookObj.getString("link_name")
                    val fileSize = relBookObj.getInt("file_size")
                    val year = relBookObj.getString("year")
                    val orderStatus = tempObj.getString("status")

                    val relAuthorArray = relBookObj.getJSONArray("rel_author")
                    var author = ""
                    for (k in 0 until relAuthorArray.length()) {
                        val authorObject = relAuthorArray.getJSONObject(k)
                        if (k == relAuthorArray.length() - 1) {
                            author += authorObject.getString("full_name")
                        } else {
                            author += authorObject.getString("full_name") + ", "
                        }
                    }

                    var readableStatus: String
                    when (orderStatus) {
                        "new" -> readableStatus = "нове замовлення"
                        "process" -> readableStatus = "на обробці"
                        "wait" -> readableStatus = "очікує отримання"
                        "success" -> readableStatus = "успішний"
                        "absent" -> readableStatus = "нема в наявності"
                        "cancel" -> readableStatus = "скасований"
                        else -> readableStatus = "невідомий"
                    }

                    val shortBookName = LibBook.getShortBookName(bookName)

                    //Create book from JSON
                    libBooks!![i] = LibBook.LibBookBuilder()
                            .bookId(bookId)
                            .bookName(shortBookName)
                            .downloadLink(link)
                            .category(category)
                            .fileSize(fileSize.toDouble())
                            .linkName(linkName)
                            .year(year)
                            .author(author)
                            .delPoint(deliveryPoint)
                            .build()

                    if (category == "1" || category == "3") {
                        bookNames!![i] = linkName + "\nСтатус: " + readableStatus
                    } else {
                        bookNames!![i] = bookName + "\nСтатус: " + readableStatus
                    }
                }

                registerForContextMenu(booksList)
                val adapter = ArrayAdapter(this@OrderListActivity,
                        android.R.layout.simple_list_item_1,
                        bookNames!!)
                booksList?.adapter = adapter
                //Maybe server is off or list is empty
                checkServerStatus(booksArray.length())

            } catch (e: Exception) {
                Log.i(LOG, "JSON Error " + e.message)
//                preferences!!.savedIsAuthorized = false
//                Toast.makeText(this@OrderListActivity, "fdfdf", Toast.LENGTH_LONG)
//                        .show()
//                val intent = Intent(this@OrderListActivity, MainActivity::class.java)
//                startActivity(intent)
                //finish()
            }

        }

        private fun getRealBookName(bookName: String): String {
            var bn = ""
            for (z in 0 until bookName.length) {
                if (bookName[z] != '/') {
                    bn += bookName[z]
                } else {
                    break
                }
            }
            return bn
        }
    }

    private fun checkServerStatus(len: Int) {
        if (len == 0) {
            val builder: AlertDialog.Builder
            builder = AlertDialog.Builder(this@OrderListActivity)
            builder.setPositiveButton("ОК") { dialog, which -> finish() }
            builder.setTitle("Список замовлень порожній.").show()
        }
    }

    private inner class CancelOrderTask : AsyncTask<Int, Void, String>() {
        internal var resultJson = ""

        override fun doInBackground(vararg params: Int?): String {
            try {
                val bookID = params[0]
                val url = URL(domain + "/api/order/deleteOrder?book_id="
                        + bookID)
                resultJson = preferences!!.getJSONFromServer(url, receivedCookie)
                Log.i(LOG, "JSON " + resultJson)

            } catch (e: Exception) {
                finish()
            }

            return resultJson
        }

        override fun onPostExecute(strJson: String) {
            super.onPostExecute(strJson)
            val dataJsonObj: JSONObject
            try {
                Log.i(LOG, "cancelGot " + strJson)
                dataJsonObj = JSONObject(strJson)
                val tmpObj = dataJsonObj.getJSONObject("response")
                Toast.makeText(this@OrderListActivity, "Замовлення було скасовано", Toast.LENGTH_LONG)
                        .show()
            } catch (e: JSONException) {
                Toast.makeText(this@OrderListActivity, "Замовлення скасувати неможливо", Toast.LENGTH_LONG)
                        .show()
            }

        }


    }
}
