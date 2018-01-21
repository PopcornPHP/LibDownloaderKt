package natus.diit.com.libhelper.model.book

import android.support.v7.app.AlertDialog
import android.text.Html
import com.google.gson.annotations.SerializedName
import java.text.DecimalFormat

/**
 * Encapsulates book
 */
class Book(@field:SerializedName("id")
           var id: Int,
           @field:SerializedName("category_id")
           var categoryId: Int?,
           @field:SerializedName("name")
           var name: String?,
           @field:SerializedName("year")
           var year: String?,
           @field:SerializedName("link")
           var link: String,
           @field:SerializedName("link_name")
           var linkName: String?,
           @field:SerializedName("file_size")
           var fileSize: Double?,
           @field:SerializedName("is_enable_order")
           var isEnableOrder: Boolean?,
           @field:SerializedName("branch_id")
           var branchId: String?
) {
    @SerializedName("rel_branch")
    var relBranch: List<Branch>? = null
    @SerializedName("rel_author")
    var authors: List<BookAuthor>? = null

    fun getAuthorsNames(): StringBuilder {

        val sBuilder = StringBuilder()
        for (author in authors!!) {
            sBuilder.append(author)
        }
        return sBuilder

    }

    var currentBranch: Int? = 0
        get() {

            if (relBranch == null)
                return 0

            for (branch in relBranch!!) {
                currentBranch = branch.id
                break
            }
            return field
        }

    companion object {
        /**
         * Returns shorten book name
         * @param bookName Original book name
         * @return shorten book name
         */
        fun getShortBookName(bookName: String): String? {
            var bn: String? = ""

            (0 until bookName.length)
                    .takeWhile { bookName[it] != '/' }
                    .forEach { bn += bookName[it] }
            return bn
        }

        /**
         * Shows information dialog
         * @param lb book
         * @param builder builder of AlertDialog
         */
        fun showBookInfo(lb: Book?, builder: AlertDialog.Builder) {

            val formattedDouble = DecimalFormat("#0.00")
                    .format(lb?.fileSize!! / (1024 * 1024))

            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
                builder.setTitle("Інформація про книгу")
                        .setMessage(Html.fromHtml("<b>" + "Назва книги: " + "</b>" + lb.name
                                + "<br>" + ("<b>" + "Скорочена назва: " + "</b>") + lb.linkName
                                + "<br>" + ("<b>" + "Автори: " + "</b>") + lb.authors
                                + "<br>" + ("<b>" + "Рік: " + "</b>") + lb.year
                                + "<br>" + ("<b>" + "Розмір: " + "</b>") + formattedDouble + " мб"))
                        .show()
            } else {
                builder.setTitle("Інформація про книгу")
                        .setMessage(Html.fromHtml("<b>" + "Назва книги: " + "</b>" + lb.name
                                + "<br>" + ("<b>" + "Скорочена назва: " + "</b>") + lb.linkName
                                + "<br>" + ("<b>" + "Автори: " + "</b>") + lb.authors
                                + "<br>" + ("<b>" + "Рік: " + "</b>") + lb.year
                                + "<br>" + ("<b>" + "Розмір: " + "</b>") + formattedDouble + " мб",
                                Html.FROM_HTML_MODE_LEGACY))
                        .show()
            }
        }
    }
}