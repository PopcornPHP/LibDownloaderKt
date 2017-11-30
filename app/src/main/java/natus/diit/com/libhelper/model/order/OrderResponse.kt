package natus.diit.com.libhelper.model.order

import com.google.gson.annotations.SerializedName

class OrderResponse {
    @SerializedName("response")
    var response:Order? = null

    @SerializedName("data")
    var orders:List<Order>? = null
}