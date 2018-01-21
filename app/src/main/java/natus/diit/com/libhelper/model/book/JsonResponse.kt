package natus.diit.com.libhelper.model.book

import com.google.gson.annotations.SerializedName
/**
 * Encapsulates json response
 * Works with Retrofit
 */
class JsonResponse {
    @SerializedName("response")
    var response: Response? = null
}