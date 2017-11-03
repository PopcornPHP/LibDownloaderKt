package natus.diit.com.libhelper

import android.support.v7.app.AlertDialog
import android.text.Html

class LibBook private constructor(lB: LibBookBuilder) {

    val bookId: Int
    val bookName: String?
    val downloadLink: String?
    val category: String?
    val fileSize: Double
    val orderStatus: String?
    val delPoint: String?
    val linkName: String?
    val author: String?
    val year: String?
    val branch: Int

    init {
        this.bookId = lB.bookId
        this.downloadLink = lB.downloadLink
        this.bookName = lB.bookName
        this.category = lB.category
        this.fileSize = lB.fileSize
        this.orderStatus = lB.orderStatus
        this.delPoint = lB.delPoint
        this.linkName = lB.linkName
        this.author = lB.author
        this.year = lB.year
        this.branch = lB.branch
    }

    internal class LibBookBuilder {
        var bookName: String? = null
            private set
        var downloadLink: String? = null
            private set
        var category: String? = null
            private set
        var orderStatus: String? = null
            private set
        var delPoint: String? = null
            private set
        var linkName: String? = null
            private set
        var author: String? = null
            private set
        var year: String? = null
            private set

        var fileSize: Double = 0.toDouble()
            private set
        var bookId: Int = 0
            private set
        var branch: Int = 0
            private set

        fun bookName(bookName: String): LibBookBuilder {
            this.bookName = bookName
            return this
        }

        fun downloadLink(downloadLink: String): LibBookBuilder {
            this.downloadLink = downloadLink
            return this
        }

        fun category(category: String): LibBookBuilder {
            this.category = category
            return this
        }

        fun orderStatus(orderStatus: String): LibBookBuilder {
            this.orderStatus = orderStatus
            return this
        }

        fun delPoint(delPoint: String): LibBookBuilder {
            this.delPoint = delPoint
            return this
        }

        fun linkName(linkName: String): LibBookBuilder {
            this.linkName = linkName
            return this
        }

        fun author(author: String): LibBookBuilder {
            this.author = author
            return this
        }

        fun year(year: String): LibBookBuilder {
            this.year = year
            return this
        }

        fun fileSize(fileSize: Double): LibBookBuilder {
            this.fileSize = fileSize
            return this
        }

        fun bookId(bookId: Int): LibBookBuilder {
            this.bookId = bookId
            return this
        }

        fun branch(branch: Int): LibBookBuilder {
            this.branch = branch
            return this
        }

        fun build(): LibBook {
            return LibBook(this)
        }
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

        fun showOrderInfo(lb: LibBook?, builder: AlertDialog.Builder) {
            val status = lb?.orderStatus
            val bookName = lb?.bookName
            val delPoint = lb?.delPoint

            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.N) {
                builder.setTitle("Інформація про замовлення")
                        .setMessage(Html.fromHtml("<b>" + "Назва книги: " + "</b>" + bookName
                                + "<br>" + ("<b>" + "Автори: " + "</b>") + lb!!.author
                                + "<br>" + ("<b>" + "Рік: " + "</b>") + lb.year
                                + "<br>" + ("<b>" + "Статус замовлення: " + "</b>") + status
                                + "<br>" + ("<b>" + "Місце видачі: " + "</b>") + delPoint))
                        .show()
            } else {
                builder.setTitle("Інформація про замовлення")
                        .setMessage(Html.fromHtml("<b>" + "Назва книги: " + "</b>" + bookName
                                + "<br>" + ("<b>" + "Автори: " + "</b>") + lb!!.author
                                + "<br>" + ("<b>" + "Рік: " + "</b>") + lb.year
                                + "<br>" + ("<b>" + "Статус замовлення: " + "</b>") + status
                                + "<br>" + ("<b>" + "Місце видачі: " + "</b>") + delPoint, Html.FROM_HTML_MODE_LEGACY))
                        .show()
            }
        }
    }
}
