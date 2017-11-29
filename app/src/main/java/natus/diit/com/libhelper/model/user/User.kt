package natus.diit.com.libhelper.model.user

import com.google.gson.annotations.SerializedName

class User(
        @field:SerializedName("id")
        var id:Int? = null,
        @field:SerializedName("name")
        var name:String? = null,
        @field:SerializedName("surname")
        var surname:String? = null,
        @field:SerializedName("email")
        var email:String? = null,
        @field:SerializedName("is_activated")
        var isActivated:Boolean? = null
)