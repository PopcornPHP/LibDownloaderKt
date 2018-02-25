package natus.diit.com.libhelper

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import natus.diit.com.libhelper.model.book.Book
import natus.diit.com.libhelper.model.order.Order
import natus.diit.com.libhelper.model.order.OrderJsonResponse
import natus.diit.com.libhelper.model.order.OrderResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Class which manipulates with orders
 */
class OrderListActivity : AppCompatActivity() {

    private var orders: List<Order?>? = null
    private var receivedCookie: String? = null
    private var orderList: ListView? = null

    private var preferences: Preferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_list)

        setToolbar(this, R.string.title_activity_order_list)

        preferences = Preferences(this.applicationContext)

        receivedCookie = preferences!!.savedReceivedCookie

        orderList = findViewById(R.id.orders_list) as ListView
        orderList!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val order = orders!![position]

            val bookID = order!!.relBook?.id

            val builder: AlertDialog.Builder =
                    AlertDialog.Builder(this@OrderListActivity)
            builder.setPositiveButton("Скасувати замовлення") { dialog, which ->
                cancelOrder(bookID)
            }

            Order.showOrderInfo(order, builder)
        }

        fetchOrdersList()
    }

    /**
     * Cancels order of given book
     * @param bookID Id of book, the order of which we want to cancel
     */
    private fun cancelOrder(bookID: Int?) {
        val call = libBookApi?.cancelOrder(receivedCookie, bookID)

        call?.enqueue(object : Callback<OrderResponse> {
            override fun onResponse(call: Call<OrderResponse>, response: Response<OrderResponse>) {
                val resp = response.body().response
                if (resp == null) {
                    showSnackBar(findViewById(R.id.order_list_container),
                            "Замовлення скасувати неможливо").show()
                } else {
                    showSnackBar(findViewById(R.id.order_list_container),
                            "Замовлення було скасовано").show()
                }
            }

            override fun onFailure(call: Call<OrderResponse>, t: Throwable) {
                showSnackBar(findViewById(R.id.order_list_container)).show()
            }
        })
    }

    /**
     * Gets order list from server
     */
    private fun fetchOrdersList() {
        val call = libBookApi?.getAllOrders(receivedCookie)
        call?.enqueue(object : Callback<OrderJsonResponse> {
            override fun onResponse(call: Call<OrderJsonResponse>,
                                    response: Response<OrderJsonResponse>) {
                orders = response.body().response?.orders
                Log.i(LOG, "$orders")
                registerForContextMenu(orderList)

                val adapter = OrderListAdapter(orders)

                orderList!!.adapter = adapter

                //Hide progress bar
                hideProgressBar()

                //Maybe server is off or list is empty
                checkServerStatus(orders?.size!!)

            }

            override fun onFailure(call: Call<OrderJsonResponse>, t: Throwable) {
                Log.e(LOG, "OrderList Error + " + t.message)
                showSnackBar(findViewById(R.id.order_list_container)).show()
            }
        })
    }

    private fun checkServerStatus(len: Int) {
        if (len == 0) {
            val builder = AlertDialog.Builder(this@OrderListActivity)
            builder.setPositiveButton("ОК") { dialog, which -> finish() }
            builder.setTitle("Список замовлень порожній.").show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                if (NavUtils.getParentActivityName(this) != null) {
                    NavUtils.navigateUpFromSameTask(this)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun hideProgressBar() {
        val progressBar = findViewById(R.id.book_list_progress)
        progressBar?.visibility = View.INVISIBLE
    }

    /**
     * Adapter for ListView which contains order list
     */
    private inner class OrderListAdapter(orders: List<Order?>?)
        : ArrayAdapter<Order>(this@OrderListActivity, android.R.layout.simple_list_item_1, orders) {

        lateinit var titleBookTv: TextView
        lateinit var orderStatusTv: TextView

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var retView = convertView
            if (retView == null) {
                retView = this@OrderListActivity
                        .layoutInflater.inflate(R.layout.orderlist_item, null)
            }
            titleBookTv = retView?.findViewById(R.id.orderList_item_tv_bookTitle) as TextView

            val currentOrder = getItem(position) as Order
            val currentBook = currentOrder.relBook

            val category = currentBook?.categoryId
            val linkName = currentBook?.linkName
            val title = Book.getShortBookName(currentBook?.name!!)

            if (category == 1 || category == 3) {
                titleBookTv.text = linkName
            } else {
                titleBookTv.text = title
            }

            //Status for showing up
            val readableStatus: String
            readableStatus = when (currentOrder.status) {
                "new" -> "нове замовлення"
                "process" -> "на обробці"
                "wait" -> "очікує отримання"
                "success" -> "успішний"
                "absent" -> "нема в наявності"
                "cancel" -> "скасовано"
                "cancelByUser" -> "скасовано користувачем"
                else -> "невідомий"
            }

            orderStatusTv = retView.findViewById(R.id.orderList_item_tv_orderStatus) as TextView
            orderStatusTv.text = readableStatus

            setOrderStatusColor(readableStatus)

            return retView
        }

        /**
         * Sets relevant color for order`s status
         * @param _orderStatus Current order`s status
         */
        private fun setOrderStatusColor(_orderStatus: String?) {
            when (_orderStatus) {
                "очікує отримання" ->
                    orderStatusTv.setTextColor(Color.BLUE)
                "скасовано користувачем", "скасовано" ->
                    orderStatusTv.setTextColor(Color.RED)
                "успішний" ->
                    orderStatusTv.setTextColor(Color.GREEN)
                "нема в наявності" ->
                    orderStatusTv.setTextColor(Color.YELLOW)
                "на обробці", "нове замовлення" ->
                    orderStatusTv.setTextColor(Color.CYAN)
            }
        }

    }

}