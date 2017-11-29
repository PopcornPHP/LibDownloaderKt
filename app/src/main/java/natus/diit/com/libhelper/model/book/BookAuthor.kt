package natus.diit.com.libhelper.model.book

import com.google.gson.annotations.SerializedName

class BookAuthor(
        @field:SerializedName("id")
        var id: Int? = null,
        @field:SerializedName("name")
        var name: String? = null
) {
        var pivot: Pivot? = null

        override fun toString(): String = this.name!!
}