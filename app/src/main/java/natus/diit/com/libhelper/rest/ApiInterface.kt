package info.androidhive.retrofit.rest

import natus.diit.com.libhelper.model.book.JsonResponse
import natus.diit.com.libhelper.model.order.OrderResponce
import retrofit2.Call
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiInterface {
    @POST("catalog/getAll")
    fun getAllBooks(@Header("Cookie") cookie: String?,
                    @Query("name") name: String?,
                    @Query("author") author: String?,
                    @Query("keywords") keywords: String?,
                    @Query("year") year: String?,
                    @Query("link_name") linkName: String?,
                    @Query("take") take: Int? = 1000,
                    @Query("offset") offset: Int? = 0
    ): Call<JsonResponse>

    @POST("order/createOrder")
    fun createOrder(@Header("Cookie") cookie: String?,
                    @Query("book_id") bookId: Int?,
                    @Query("branch_id") branchId: Int?

    ): Call<OrderResponce>
}
