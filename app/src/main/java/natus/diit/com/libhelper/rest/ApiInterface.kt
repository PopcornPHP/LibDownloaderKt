package natus.diit.com.libhelper.rest

import natus.diit.com.libhelper.model.book.JsonResponse
import natus.diit.com.libhelper.model.order.OrderJsonResponse
import natus.diit.com.libhelper.model.order.OrderResponse
import natus.diit.com.libhelper.model.user.CheckUserLogIn
import natus.diit.com.libhelper.model.user.CheckUserResponse
import natus.diit.com.libhelper.model.user.LogOutResponse
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

    @POST("order/getAll")
    fun getAllOrders(@Header("Cookie") cookie: String?
    ): Call<OrderJsonResponse>

    @POST("order/createOrder")
    fun createOrder(@Header("Cookie") cookie: String?,
                    @Query("book_id") bookId: Int?,
                    @Query("branch_id") branchId: Int?

    ): Call<OrderResponse>

    @POST("order/deleteOrder")
    fun cancelOrder(@Header("Cookie") cookie: String?,
                    @Query("book_id") bookId: Int?
    ): Call<OrderResponse>

    @POST("user/signout")
    fun logOut(@Header("Cookie") cookie: String?) : Call<LogOutResponse>

    @POST("user/check")
    fun checkUser(@Header("Cookie") cookie: String?) : Call<CheckUserResponse>

    @POST("user/signin")
    fun signIn(@Query("login") login:String?,
               @Query("password") password:String?,
               @Query("remember") remember:Boolean?
    ) : Call<CheckUserLogIn>
}
