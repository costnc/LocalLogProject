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

class Util {

    companion object {

        /**
         * get today file name and check file name
         * @since 2021.04.07
         * @author lim.sung.jin
         */
        fun getCheckTodayFile(fileName : String) : String {

            var result : String = ""
            var strDate = getTodayStr()

            if (!fileName.contains(strDate)) {
                result = strDate + "_" + fileName.split("_")[1]
            } else {
                result = fileName
            }

            return result
        }

        /**
         * create file date name
         * @since 2021.04.07
         * @author lim.sung.jin
         */
        fun createFileDateName(applicationContext : Context, path : String , fileName : String) : String{

            var contentResolver: ContentResolver = applicationContext.contentResolver;
            var list : LinkedList<FileInfo> = findSameFileNames(contentResolver, path, fileName)
            var sortList : ArrayList<String> = sortOrderDate(list)

            var strDate = getTodayStr()

            var result : String = ""

            if (sortList.count() > 0) {
                if (!sortList.get(0).contains(strDate)) {
                    result = strDate + "_" + fileName
                } else {
                    result = strDate + "_" + fileName
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
        fun findSameFileNames(contentResolver : ContentResolver, path : String, fileName : String) : LinkedList<FileInfo> {
            var findSameFileInfoList : LinkedList<FileInfo> = LinkedList<FileInfo>()
            var fileInfo : FileInfo = FileInfo()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI, null, null, null, null).use { cursor ->
                    when (cursor!!.count) {
                        null, 0 -> return@use
                        else -> {
                            while (cursor.moveToNext()) {
                                val nameIndex = cursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                if (cursor.getString(nameIndex).matches((".*" + fileName + ".*.*").toRegex())) {
                                    fileInfo = FileInfo()
                                    fileInfo.name = cursor.getString(nameIndex)
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

            return findSameFileInfoList
        }

        /**
         * sort order
         *
         * @param LinkedList<FileInfo>  :   file info list
         * @since 2021.04.07
         * @author lim.sung.jin
         */
        fun sortOrderDate(findSameFileInfoList : LinkedList<FileInfo>) : ArrayList<String>{

            var list : ArrayList<String> = ArrayList()

            for (temp in findSameFileInfoList) {
                if (temp.name.split("_")[0].matches("^\\d{4}(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$".toRegex())) {
                    list.add(temp.name)
                }
            }

            list.sortDescending()

            return list

        }

    }
}