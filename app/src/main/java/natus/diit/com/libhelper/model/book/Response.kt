package natus.diit.com.libhelper.model.book

import com.google.gson.annotations.SerializedName

class Response(@field:SerializedName("current_page") var currentPage:Int) {

    @SerializedName("data")
    var books:List<Book>? = null
}