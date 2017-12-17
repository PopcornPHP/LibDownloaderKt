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
import natus.diit.com.libhelper.model.book.Book
import natus.diit.com.libhelper.model.book.JsonResponse
import natus.diit.com.libhelper.model.order.OrderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class BooksListActivity : AppCompatActivity() {

    private var booksList: ListView? = null
    private var libBooks: List<Book?>? = null

    private lateinit var searchByNumber: String
    private lateinit var searchByAuthor: String
    private lateinit var searchByYear: String
    private lateinit var searchByKeywords: String
    private lateinit var searchByBookName: String

    private var receivedCookie: String? = null
    private lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        setToolbar(this, R.string.title_activity_books_list)

        preferences = Preferences(this)

        setSearchFields()

        booksList = findViewById(R.id.list_books) as ListView
        booksList!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val lb = libBooks!![position]
            val builder: AlertDialog.Builder
            builder = AlertDialog.Builder(this@BooksListActivity)
            val fileSize = lb?.fileSize!! / 1024
            if (fileSize > 0) {
                builder.setPositiveButton("Завантажити книгу") { dialog, which ->
                    val downloadLink = lb.link
                    val folder = File(Environment.getExternalStorageDirectory()
                            .toString() + File.separator + "DNURTBooks")
                    Log.i(LOG, "File path = " + folder.absolutePath)
                    if (!folder.exists()) {
                        folder.mkdir()
                    }
                    val uri = Uri.parse(downloadLink)
                    val file = File(folder, uri.lastPathSegment)

                    downloadFile(downloadLink, file)
                }
            }

            if (lb.isEnableOrder!!) {
                builder.setNegativeButton("Замовити книгу") { dialog, which ->
                    createOrder(lb.id, lb.currentBranch)
                }
            }
            Book.showBookInfo(lb, builder)
        }


        fetchBooksList()
    }

    private fun setSearchFields() {
        searchByYear = intent.getStringExtra(MainActivity.SEARCH_YEAR)
        searchByNumber = intent.getStringExtra(MainActivity.SEARCH_NUMBER)
        searchByBookName = intent.getStringExtra(MainActivity.SEARCH_BOOK_NAME)
        searchByKeywords = intent.getStringExtra(MainActivity.SEARCH_KEYWORDS)
        searchByAuthor = intent.getStringExtra(MainActivity.SEARCH_AUTHOR)
        receivedCookie = preferences.savedReceivedCookie
    }

    private fun createOrder(bookId: Int, currentBranch: Int?) {
        val call = libBookApi?.createOrder(
                receivedCookie,
                bookId,
                currentBranch)

        call?.enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                val resp = response.body().response
                if (resp == null) {
                    showSnackBar(findViewById(R.id.book_list_container),
                            "Книгу замовити неможливо").show()
                } else {
                    showSnackBar(findViewById(R.id.book_list_container),
                            "Книгу було замовлено").setAction("Відмінити") {
                        val cancelCall = libBookApi?.cancelOrder(receivedCookie, bookId)

                        cancelCall?.enqueue(object : Callback<OrderResponse> {
                            override fun onResponse(call: Call<OrderResponse>,
                                                    response: Response<OrderResponse>) {
                                val cancelResp = response.body().response
                                if (cancelResp == null) {
                                    showSnackBar(findViewById(R.id.book_list_container),
                                            "Замовлення скасувати неможливо").show()
                                } else {
                                    showSnackBar(findViewById(R.id.book_list_container),
                                            "Замовлення було скасовано").show()
                                }
                            }

                            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                                showSnackBar(findViewById(R.id.book_list_container)).show()
                            }
                        })
                    }.show()
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                showSnackBar(findViewById(R.id.book_list_container)).show()
            }
        })
    }

    private fun fetchBooksList() {

        val call = libBookApi?.getAllBooks(receivedCookie,
                searchByBookName, searchByAuthor,
                searchByKeywords,
                searchByYear,
                searchByNumber)
        call?.enqueue(object : Callback<JsonResponse> {
            override fun onResponse(call: Call<JsonResponse>, response: Response<JsonResponse>) {
                libBooks = response.body().response?.books

                registerForContextMenu(booksList)

                val adapter = BookListAdapter(libBooks)
                booksList?.adapter = adapter

                hideProgressBar()

                //Maybe server is off or list is empty
                checkServerStatus(libBooks?.size!!)
            }

            override fun onFailure(call: Call<JsonResponse>, t: Throwable) {
                Log.e(LOG, "BookList Error + " + t.message)
                showSnackBar(findViewById(R.id.book_list_container)).show()
            }
        })
    }

    private inner class BookListAdapter(var books: List<Book?>?)
        : ArrayAdapter<Book>(this@BooksListActivity, android.R.layout.simple_list_item_1, books) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

            var retView = convertView
            if (retView == null) {
                retView = this@BooksListActivity
                        .layoutInflater.inflate(R.layout.booklist_item, null)
            }

            val lB = getItem(position) as Book
            val titleBookTv = retView?.findViewById(R.id.booklist_item_tv_bookTitle) as TextView

            val category = books!![position]?.categoryId
            val linkName = books!![position]?.linkName
            val title = Book.getShortBookName(books!![position]?.name!!)

            if (category == 1 || category == 3) {
                titleBookTv.text = linkName
            } else {
                titleBookTv.text = title
            }

            val authorsBookTv = retView.findViewById(R.id.booklist_item_tv_bookAuthors) as TextView

            if(lB.getAuthorsNames().isBlank()){
                authorsBookTv.text = "Без Авторів"
            }else {
                authorsBookTv.text = lB.getAuthorsNames()
            }

            return retView
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

    private fun hideProgressBar() {
        val progressBar = findViewById(R.id.book_list_progress)
        progressBar?.visibility = View.INVISIBLE
    }

    private fun checkServerStatus(len: Int) {
        if (len == 0) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@BooksListActivity)
            builder.setPositiveButton("ОК") { dialog, which -> finish() }
            builder.setTitle("За вашим запитом нічого не знайдено.").show()
        }
    }

}