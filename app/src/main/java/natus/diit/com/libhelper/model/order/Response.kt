package natus.diit.com.libhelper.model.order

import com.google.gson.annotations.SerializedName

class Response(
        @field:SerializedName("user_id")
        var userId:Int? = null,
        @field:SerializedName("book_id")
        var bookId:Int? = null,
        @field:SerializedName("branch_id")
        var branchId:Int? = null,
        @field:SerializedName("status")
        var status:String? = null,
        @field:SerializedName("updated_at")
        var updatedAt:String? = null,
        @field:SerializedName("created_at")
        var createdAt:String? = null,
        @field:SerializedName("id")
        var id:Int? = null
)