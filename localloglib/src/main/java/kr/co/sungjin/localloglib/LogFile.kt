package kr.co.sungjin.localloglib

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * Log file info
 * @since 2021.03.29
 * @author lim.sung.jin
 */
class LogFile (
    val applicationContext : Context,
    val path        : String = "",
    var fileName    : String = "",
    val trunLogFileSize : Long = -1,
    val isTrunDate : Boolean = false
){

    private constructor(builder: Builder) : this(builder.applicationContext, builder.path, builder.fileName, builder.trunLogFileSize, builder.isTrunDate) {

        if (isTrunDate) {
            fileName = Util.createFileDateName(this.applicationContext, this.path, this.fileName)
        }

    }

    class Builder {

        val applicationContext : Context
        var path : String = ""
        var fileName : String = ""
        var trunLogFileSize : Long = -1;         // fileSize : truncation Log file
        var isTrunDate : Boolean = false;        // date : truncation Log file

        constructor(applicationContext : Context) {
            this.applicationContext = applicationContext
        }

        fun path(path : String) = apply { this.path = path }
        fun fileName(fileName : String) = apply { this.fileName = fileName }
        fun build() = LogFile(this)

        /**
         * if want truncation file size
         * @param Long   :   file size
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        //fun asTrunLogFileSize(byteSize : Long) = apply {this.trunLogFileSize = byteSize}

        /**
         * if want truncation date
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        fun asTrunDate() = apply {this.isTrunDate = true}
    }

}