package natus.diit.com.libhelper

import android.app.ProgressDialog
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder


class BooksListActivity : AppCompatActivity() {

    internal var LOG = "MyLog"
    private var booksList: ListView? = null
    private var bookNames: Array<String?>? = null
    private var libBooks: Array<LibBook?>? = null

    private var searchByNumber: String? = null
    private var searchByAuthor: String? = null
    private var searchByYear: String? = null
    private var searchByKeywords: String? = null
    private var searchByBookName: String? = null

    private var receivedCookie: String? = null
    private var preferences: Preferences? = null
    private var domain: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        val appCompatActivity = this as AppCompatActivity

        if (NavUtils.getParentActivityName(this) != null) {
            appCompatActivity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        preferences = Preferences(this)

        searchByYear = preferences!!.savedSearchByYear
        searchByNumber = preferences!!.savedSearchByNumber
        searchByBookName = preferences!!.savedSearchByBookName
        searchByKeywords = preferences!!.savedSearchByKeywords
        searchByAuthor = preferences!!.savedSearchByAuthor
        receivedCookie = preferences!!.savedReceivedCookie

        domain = preferences!!.domain

        booksList = findViewById(R.id.list_books) as ListView
        booksList!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val lb = libBooks!![position]
            val builder: AlertDialog.Builder
            builder = AlertDialog.Builder(this@BooksListActivity)
            val fileSize = lb!!.fileSize / 1024
            if (fileSize > 0) {
                builder.setPositiveButton("Завантажити книгу") { dialog, which ->
                    val downloadLink = lb.downloadLink

                    Log.i(LOG, "dL = " + downloadLink!!)
                    Log.i(LOG, "category = " + lb.category!!)
                    val folder = File(Environment.getExternalStorageDirectory().toString() +
                            File.separator + "DNURTBooks")
                    Log.i(LOG, "File path = " + folder.absolutePath)
                    if (!folder.exists()) {
                        folder.mkdir()
                    }

                    val uri = Uri.parse(downloadLink)

                    val file = File(folder, uri.lastPathSegment)

                    downloadFile(downloadLink, file)
                }
            }

            builder.setNegativeButton("Замовити книгу") { dialog, which ->
                OrderCreator().execute(lb.bookId, lb.branch)
            }

            preferences!!.showBookInfo(lb, builder)
        }

        DownloaderTask().execute()
    }

    private inner class BookListAdapter(books: Array<LibBook?>?)
        : ArrayAdapter<LibBook>(this@BooksListActivity, android.R.layout.simple_list_item_1, books) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var retView = convertView
            if (retView == null) {
                retView = this@BooksListActivity
                        .layoutInflater.inflate(R.layout.booklist_item, null)
            }

            val lB = getItem(position) as LibBook
            val titleBookTv = retView?.findViewById(R.id.booklist_item_tv_bookTitle) as TextView
            titleBookTv.text = bookNames!![position]

            val authorsBookTv = retView.findViewById(R.id.booklist_item_tv_bookAuthors) as TextView
            authorsBookTv.text = if(lB.author!!.isBlank()) "Без авторів" else lB.author

            return retView
        }

    }

    private inner class DownloaderTask : AsyncTask<Void, Void, String>() {
        internal var resultJson = ""

        override fun doInBackground(vararg params: Void): String {
            try {
                val author = URLEncoder.encode(searchByAuthor, "UTF-8")
                val year = URLEncoder.encode(searchByYear, "UTF-8")
                val keywords = URLEncoder.encode(searchByKeywords, "UTF-8")
                val bookNumber = URLEncoder.encode(searchByNumber, "UTF-8")
                val bookName = URLEncoder.encode(searchByBookName, "UTF-8")

                val url = URL(domain + "/api/catalog/getAll?take=1000&offset=0"
                        + "&name=" + bookName + "&author=" + author + "&keyword="
                        + keywords + "&year=" + year + "&link_name=" + bookNumber)

                resultJson = preferences!!.getJSONFromServer(url, receivedCookie)
                Log.i(LOG, "JSON " + resultJson)

            } catch (e: Exception) {
                Log.i(LOG, "BookList json error " + e.message)
                showSnackBar("Перевірте інтернет з'єднання",
                        findViewById(R.id.book_list_container))
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

                for (i in 0 until booksArray.length()) {
                    val tempObj = booksArray.getJSONObject(i)
                    val bookId = tempObj.getInt("id")
                    val category = tempObj.getString("category_id")
                    val bookName = tempObj.getString("name")
                    val year = tempObj.getString("year")
                    val link = tempObj.getString("link")
                    val linkName = tempObj.getString("link_name")
                    val fileSize = tempObj.getInt("file_size")

                    val relAuthorArray = tempObj.getJSONArray("rel_author")
                    var author = ""
                    for (authorsN in 0 until relAuthorArray.length()) {
                        val authorObject = relAuthorArray.getJSONObject(authorsN)
                        if (authorsN == relAuthorArray.length() - 1) {
                            author += authorObject.getString("name")
                        } else {
                            author += authorObject.getString("name") + ", "
                        }
                    }

                    val relBranch = tempObj.getJSONArray("rel_branch")
                    var branchID = 0
                    if (relBranch.length() != 0) {
                        val branchObj = relBranch.getJSONObject(0)
                        branchID = branchObj.getInt("id")
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
                            .branch(branchID)
                            .build()

                    if (category == "1" || category == "3") {
                        bookNames!![i] = linkName
                    } else {
                        bookNames!![i] = bookName
                    }

                }

                registerForContextMenu(booksList)

                val adapter = BookListAdapter(libBooks)
                booksList!!.adapter = adapter

                //Maybe server is off or list is empty
                checkServerStatus(booksArray.length())


            } catch (e: Exception) {
                Log.i(LOG, "BookList Error + " + e.message)
//                preferences!!.savedIsAuthorized = false
                showSnackBar("Перевірте інтернет з'єднання",
                        findViewById(R.id.book_list_container))
            }

        }

    }

    private fun checkServerStatus(len: Int) {
        if (len == 0) {
            val builder: AlertDialog.Builder
            builder = AlertDialog.Builder(this@BooksListActivity)
            builder.setPositiveButton("ОК") { dialog, which -> finish() }
            builder.setTitle("За вашим запитом нічого не знайдено.").show()
        }
    }

    private inner class OrderCreator : AsyncTask<Int, Void, String>() {
        internal var resultJson = ""

        override fun doInBackground(vararg params: Int?): String {
            try {
                val bookID = params[0]
                val branch = params[1]
                Log.i(LOG, "bookID = " + bookID)
                Log.i(LOG, "branch = " + branch)

                val url = URL(domain + "/api/order/createOrder?book_id="
                        + bookID + "&branch_id=" + branch)

                resultJson = preferences!!.getJSONFromServer(url, receivedCookie)
                Log.i(LOG, "JSON " + resultJson)

            } catch (e: Exception) {
                Toast.makeText(this@BooksListActivity, "Перевірте інтернет з'єднання", Toast.LENGTH_LONG)
                        .show()
            }

            return resultJson
        }

        override fun onPostExecute(strJson: String) {
            super.onPostExecute(strJson)
            val dataJsonObj: JSONObject
            try {
                dataJsonObj = JSONObject(strJson)
                val tmpObj = dataJsonObj.getJSONObject("response")
                Toast.makeText(this@BooksListActivity, "Книгу було замовлено", Toast.LENGTH_LONG)
                        .show()
            } catch (e: JSONException) {
                Toast.makeText(this@BooksListActivity, "Книгу замовити неможливо", Toast.LENGTH_LONG)
                        .show()
            }

        }
    }

    private inner class FileDownloader internal constructor(internal var file: File)
        : AsyncTask<String, Int, File>() {
        private var m_error: Exception? = null
        internal val progressDialog = ProgressDialog(this@BooksListActivity)

        override fun onPreExecute() {
            progressDialog.setMessage("Downloading ...")
            progressDialog.setCancelable(false)
            progressDialog.max = 100
            progressDialog
                    .setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)

            progressDialog.show()
            progressDialog.setCanceledOnTouchOutside(true)
        }

        override fun doInBackground(vararg params: String): File? {
            val url: URL
            val urlConnection: HttpURLConnection
            val inputStream: InputStream
            val totalSize: Int
            var downloadedSize: Int
            val buffer: ByteArray
            var bufferLength: Int

            val fos: FileOutputStream

            try {
                url = URL(params[0])
                urlConnection = url.openConnection() as HttpURLConnection

                urlConnection.requestMethod = "GET"
                urlConnection.doOutput = true
                urlConnection.connect()

                fos = FileOutputStream(file)
                inputStream = urlConnection.inputStream

                totalSize = urlConnection.contentLength
                downloadedSize = 0

                buffer = ByteArray(1024)
                bufferLength = 0

                // читаем со входа и пишем в выход,
                // с каждой итерацией публикуем прогресс
                while (true) {
                    bufferLength = inputStream.read(buffer)
                    if (bufferLength <= 0)
                        break
                    fos.write(buffer, 0, bufferLength)
                    downloadedSize += bufferLength
                    publishProgress(downloadedSize, totalSize)
                }

                fos.close()
                inputStream.close()

                return file
            } catch (e: IOException) {
                e.printStackTrace()
                m_error = e
            }

            return null
        }

        //обновляем progressDialog
        override fun onProgressUpdate(vararg values: Int?) {
            if (isCancelled) progressDialog.hide()
            val val1: Float = values[0]?.toFloat() ?: 0F
            val val2: Float = values[1]?.toFloat() ?: 0F
            Log.i(LOG, "val1 = $val1, val2 = $val2")
            progressDialog.progress = ((val1 / val2) * 100).toInt()
            Log.i(LOG, "rez = ${((val1 / val2) * 100).toInt()}")
        }


        override fun onPostExecute(file: File) {
            // отображаем сообщение, если возникла ошибка
            if (m_error != null) {
                m_error!!.printStackTrace()
                return
            }

            // закрываем прогресс и удаляем временный файл
            progressDialog.hide()
            Toast.makeText(this@BooksListActivity, "Книгу було завантажено", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this)
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun downloadFile(url: String?, file: File) {
        val fl = FileDownloader(file)
        fl.execute(url)
    }

}
