package natus.diit.com.libhelper.model.book

import com.google.gson.annotations.SerializedName

class Pivot(
        @field:SerializedName("book_id")
        var bookId: Int? = null,
        @field:SerializedName("author_id")
        var authorId: Int? = null,
        @field:SerializedName("author_type_id")
        var authorTypeId: Int? = null
)