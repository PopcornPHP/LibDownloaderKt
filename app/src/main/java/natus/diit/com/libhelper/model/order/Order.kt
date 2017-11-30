package natus.diit.com.libhelper.model.order

import android.support.v7.app.AlertDialog
import android.text.Html
import com.google.gson.annotations.SerializedName
import natus.diit.com.libhelper.model.book.Book

class Order(@field:SerializedName("id")
            var id: Int,
            @field:SerializedName("name")
            var name: String?,
            @field:SerializedName("status")
            var status: String?,
            @field:SerializedName("branch_id")
            var branchId: Int?
){
    @SerializedName("rel_book")
    var relBook:Book? = null

    companion object {
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