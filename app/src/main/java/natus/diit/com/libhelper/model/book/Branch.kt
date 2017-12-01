package natus.diit.com.libhelper.model.book

import com.google.gson.annotations.SerializedName

class Branch(
        @field:SerializedName("id")
        var id: Int? = null,
        @field:SerializedName("name")
        var name: String? = null
){
        override fun toString(): String {
                return name ?: ""
        }
}