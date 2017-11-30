package natus.diit.com.libhelper.model.book

import android.support.v7.app.AlertDialog
import android.text.Html
import com.google.gson.annotations.SerializedName
import natus.diit.com.libhelper.model.order.Order

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
           var authorSign: String?,
           @field:SerializedName("branch_id")
           var branchId: String?
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
        fun getShortBookName(bookName: String?): String? {
            var bn: String? = bookName ?: return ""

            (0 until bookName.length)
                    .takeWhile { bookName[it] != '/' }
                    .forEach { bn += bookName[it] }
            return bn
        }

        fun showOrderInfo(order: Order?, builder: AlertDialog.Builder) {
            val bookName = order?.relBook?.name
            val delPoint = order?.relBook?.relBranch?.get(0)

            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
                builder.setTitle("Інформація про замовлення")
                        .setMessage(Html.fromHtml("<b>" + "Назва книги: " + "</b>" + bookName
                                + "<br>" + ("<b>" + "Автори: " + "</b>") + order?.relBook?.authors
                                + "<br>" + ("<b>" + "Рік: " + "</b>") + order?.relBook?.year
                                + "<br>" + ("<b>" + "Місце видачі: " + "</b>") + delPoint))
                        .show()
            } else {
                builder.setTitle("Інформація про замовлення")
                        .setMessage(Html.fromHtml("<b>" + "Назва книги: " + "</b>" + bookName
                                + "<br>" + ("<b>" + "Автори: " + "</b>") + order?.relBook?.authors
                                + "<br>" + ("<b>" + "Рік: " + "</b>") + order?.relBook?.year
                                + "<br>" + ("<b>" + "Місце видачі: " + "</b>") + delPoint,
                                Html.FROM_HTML_MODE_LEGACY))
                        .show()
            }
        }
    }
}