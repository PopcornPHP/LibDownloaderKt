package natus.diit.com.libhelper.model.user

import com.google.gson.annotations.SerializedName

class CheckUserResponse {

    @SerializedName("response")
    var isAuthorized:Boolean? = null
}