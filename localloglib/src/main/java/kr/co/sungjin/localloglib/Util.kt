package kr.co.sungjin.localloglib

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class Util {

    companion object {

        /**
         * check today file name
         * @since 2021.04.07
         * @author lim.sung.jin
         */
        fun getCheckTodayFile(fileName: String) : Boolean {

            var result : Boolean = false
            var strDate = getTodayStr()

            if (!fileName.contains(strDate)) {
                result = false
            } else {
                result = true
            }

            return result
        }

        /**
         * get today file name and check file name
         * @since 2021.04.07
         * @author lim.sung.jin
         */
        fun getTodayFile(fileName: String) : String {
            var result: String = ""
            var strDate = getTodayStr()
            result = strDate + "_" + fileName.split("_")[1]
            return result
        }

        /**
         * get regx string
         * @since 2021.04.07
         * @author lim.sung.jin
         */
        fun getRegxString(value : String, regx : String) : String{

            var p: Pattern = Pattern.compile(regx)
            var m: Matcher = p.matcher(value)
            var isFound : Boolean = m.find()

            if (isFound) {
                return m.group(1)
            } else {
                return ""
            }

        }

        /**
         * new file
         * @param   file  : file
         * @since 2021.04.09
         * @author lim.sung.jin
         */
        fun newFile(logFile : File){
            if (!logFile!!.exists()) {
                try {
                    logFile!!.parentFile.mkdirs()
                    logFile!!.createNewFile()
                } catch (e: IOException) {
                    throw e;
                }
            }
        }

        /**
         * next file numbring
         * @param   fileName  : file name
         * @return  numbering file name : ex) 20210409_filename (2)
         * @since 2021.04.09
         * @author lim.sung.jin
         */
        fun getNextFileNameNumbering(fileName: String) : String{
            var nextNumbering : String = getNextNumbering(fileName)
            var nextFileName : String = getOriginName(fileName) + " (" + nextNumbering + ")"
            return nextFileName
        }

        /**
         * get origin file name,
         * remove numbering
         * @since 2021.04.08
         * @author lim.sung.jin
         */
        fun getOriginName(fileName: String) : String {
            var cutName : String = removeMimeTxt(fileName)
            return cutName.replace("\\((.*?)\\)".toRegex(), "").replace(" ", "")
        }

        /**
         * remove file mime type text
         * remove numbering
         * @since 2021.04.08
         * @author lim.sung.jin
         */
        fun removeMimeTxt(fileName : String) : String {
            var cutNameIndex = fileName.indexOfFirst { it == '.' }
            if (cutNameIndex > -1) {
                return fileName.substring(0, cutNameIndex);
            } else {
                return fileName
            }
        }

        /**
         * remove numbering from file name,
         * remove numbering
         * @since 2021.04.08
         * @author lim.sung.jin
         */
        fun getRemoveNumberingFromFileName(fileName : String) : String {
            return fileName.replace("\\((.*?)\\)".toRegex(), "").replace(" ", "")
        }

        /**
         * next numbering
         * @since 2021.04.08
         * @author lim.sung.jin
         */
        fun getNextNumbering(fileName : String) : String{
            var strNumber : String = Util.getRegxString(fileName, "\\((.*?)\\)")
            var iNumber : Int = 0

            if (!strNumber.isNullOrEmpty()) {
                iNumber = strNumber.toInt()
            }

            iNumber+=1

            return iNumber.toString()
        }

        /**
         * find file url
         * @param contentResolver   :   ContentResolver {@code android.content.ContentResolver}
         * @param fileName          :   file name
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        fun findFileUri(contentResolver : ContentResolver, fileName : String) : Uri?{
            var findUrl : Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, null, null, null).use {
                    cursor ->
                    when(cursor!!.count) {
                        null, 0    -> return@use
                        else       -> {
                            while(cursor.moveToNext()) {
                                val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (cursor.getString(nameIndex).equals(fileName)) {
                                    findUrl = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                            cursor.getLong(cursor.getColumnIndex(MediaStore.DownloadColumns._ID)));
                                    return@use
                                }
                            }
                        }
                    }
                }
            }
            return findUrl
        }

        /**
         * create file date name
         * @since 2021.04.07
         * @author lim.sung.jin
         */
        fun createFileDateName(applicationContext: Context, path: String, fileName: String) : String{

            var contentResolver: ContentResolver = applicationContext.contentResolver;
            var list : LinkedList<FileInfo> = findSameFileNames(contentResolver, path, fileName)
            var sortList : List<FileInfo> = sortOrderDate(list)

            var strDate = getTodayStr()

            var result : String = ""
            var name : String = ""

            if (sortList.count() > 0) {
                name = sortList.get(0).name
                if (!name.contains(strDate)) {
                    result = strDate + "_" + fileName
                } else {
                    result = name
                }
            } else {
                result = strDate + "_" + fileName
            }

            return result
        }

        /**
         * get today string
         * @return yyyMMdd
         * @since 2021.04. 07
         * @author lim.sung.jin
         */
        fun getTodayStr() : String {

            var simpleFormatter: SimpleDateFormat = SimpleDateFormat("yyyyMMdd");
            var date: Date = Calendar.getInstance().getTime()
            var strDate : String = simpleFormatter.format(date).toString()

            return strDate

        }

        /**
         * find same file names
         * @param contentResolver       :   ContentResolver {@code android.content.ContentResolver}
         * @param path          :   file path
         * @param fileName      :   file name
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        fun findSameFileNames(contentResolver: ContentResolver, path: String, fileName: String) : LinkedList<FileInfo> {
            var findSameFileInfoList : LinkedList<FileInfo> = LinkedList<FileInfo>()
            var fileInfo : FileInfo = FileInfo()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, null, null, null).use { cursor ->
                    when (cursor!!.count) {
                        null, 0 -> return@use
                        else -> {
                            while (cursor.moveToNext()) {
                                var nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (cursor.getString(nameIndex).matches((".*" + getOriginName(fileName) + ".*.*").toRegex())) {
                                    fileInfo = FileInfo()
                                    fileInfo.name = removeMimeTxt(cursor.getString(nameIndex))
                                    fileInfo.size = cursor.getLong(cursor.getColumnIndex(MediaStore.DownloadColumns.SIZE))
                                    fileInfo.lastModify = cursor.getLong(cursor.getColumnIndex(MediaStore.DownloadColumns.DATE_MODIFIED))
                                    fileInfo.uri = ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                                            cursor.getLong(cursor.getColumnIndex(MediaStore.DownloadColumns._ID)));
                                    findSameFileInfoList.add(fileInfo)
                                }
                            }
                        }
                    }
                }
            } else {

                var logDirectoryFile : File = File(Environment.getExternalStorageDirectory().absolutePath
                        + "/"
                        + path)

                var fileList = logDirectoryFile.listFiles()

                if (fileList != null) {
                    for (file in fileList) {
                        if (file.isFile && file.name.matches((".*" + fileName + ".*.*").toRegex())) {
                            fileInfo = FileInfo()
                            fileInfo.name = file.name
                            fileInfo.size = file.length()
                            fileInfo.lastModify = file.lastModified()
                            findSameFileInfoList.add(fileInfo)
                        }
                    }
                }


            }

            return findSameFileInfoList
        }

        /**
         * get file name
         * @param contentResolver       :   ContentResolver {@code android.content.ContentResolver}
         * @param uri                   :   file path
         * @since 2021.04.08
         * @author lim.sung.jin
         */
        fun getFileName(contentResolver: ContentResolver, uri: Uri) : String {

            var fileName = ""

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                var projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
                var metaCursor: Cursor? = contentResolver.query(uri, projection, null, null, null)

                if (metaCursor != null) {

                    try {
                        if (metaCursor.moveToFirst()) {
                            fileName = metaCursor.getString(0)
                        }
                    } finally {
                        metaCursor.close()
                    }

                }

            } else {

                var file : File = File(uri.toString())
                fileName = file.name

            }

            return fileName

        }

        /**
         * sort order
         *
         * @param LinkedList<FileInfo>  :   file info list
         * @since 2021.04.07
         * @author lim.sung.jin
         */
        fun sortOrderDate(findSameFileInfoList: LinkedList<FileInfo>) : List<FileInfo> {

            var list : ArrayList<FileInfo> = ArrayList()

            for (temp in findSameFileInfoList) {
                if (temp.name.split("_")[0].matches("^\\d{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$".toRegex())) {
                    list.add(temp)
                }
            }

            //val sortedList = list.sortedWith(compareBy({ it.age }, { it.name }))
            var sortList : List<FileInfo> = list.sortedWith(compareByDescending<FileInfo> { it.lastModify }.thenByDescending { Util.getRegxString(it.name, "\\((.*?)\\)") })

            return sortList

        }

    }
}