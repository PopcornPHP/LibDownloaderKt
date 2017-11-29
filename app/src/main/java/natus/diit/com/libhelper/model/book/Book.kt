package natus.diit.com.libhelper.model.book

import com.google.gson.annotations.SerializedName

class Book(@field:SerializedName("id")
           var id: Int,
           @field:SerializedName("category_id")
           var categoryId: Int?,
           @field:SerializedName("mfn")
           var mfn: Int?,
           @field:SerializedName("name")
           var name: String?,
           @field:SerializedName("year")
           var year: String?,
           @field:SerializedName("udk")
           var udk: String?,
           @field:SerializedName("link")
           var link: String,
           @field:SerializedName("link_name")
           var linkName: String?,
           @field:SerializedName("file_size")
           var fileSize: Double?,
           @field:SerializedName("is_free_download")
           var isFreeDownload: Boolean?,
           @field:SerializedName("is_enable_order")
           var isEnableOrder: Boolean?,
           @field:SerializedName("copies")
           var copies: String?,
           @field:SerializedName("cipher")
           var cipher: String?,
           @field:SerializedName("author_sign")
           var authorSign: String?
) {
    @SerializedName("rel_branch")
    var relBranch: List<Branch>? = null
    @SerializedName("rel_author")
    var authors: List<BookAuthor>? = null

    var currentBranch: Int? = 0
        get() {

            if(relBranch == null)
                return 0

            for (branch in relBranch!!) {
                currentBranch = branch.id
                break
            }
            return field
        }

    companion object {
        fun getShortBookName(bookName: String): String {
            var bn = ""
            for (z in 0 until bookName.length) {
                if (bookName[z] != '/') {
                    bn += bookName[z]
                } else {
                    break
                }
            }
            return bn
        }
    }
}