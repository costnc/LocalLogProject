package kr.co.sungjin.localloglib

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log


/**
 * LocalLog Library
 * @since 2021.03.29
 * @author lim.sung.jin
 */
class LocalLog {

    /**
     * static object
     */
    companion object{

        // log level
        private const val DEBUG = 3
        private const val INFO = 4
        private const val WARN = 5
        private const val ERROR = 6

        private val compositeDisposable : CompositeDisposable = CompositeDisposable()

        // errors
        val ERROR_INIT : Exception = Exception("**** Settup Init > LocalLog.init(applicationContext) ****");

        // application context
        private var _applicationContext : Context? = null

        // if want release mode log
        private var _enableReleaseLog : Boolean = false;

        // if want release mode save file log
        private var _enableReleaseSaveFileLog : Boolean = false;

        // save log file
        private var _logFile : LogFile? = null;

        // date log format
        private var _logDateFormat : String = "yyyy-MM-dd HH:mm:ss";

        /**
         * init
         * @param context   :   ApplicationContext
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        fun initialize(applicationContext: Context) = apply {this._applicationContext = applicationContext}

        /**
         * save log file
         * @param LogFile   :   log file info
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        fun saveLogFile(logFile : LogFile) = apply { this._logFile = logFile }

        /**
         * if use save log file > time formet
         * @param format    :   "yyyy-MM-dd HH:mm:ss"
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        fun logFileDateFormat(format: String) {
            _logDateFormat = format;
        }

        /**
         * if want release mode log
         * @param Boolean   :   true is release mode log
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        fun enableReleaseLog(isEnable: Boolean) = apply { this._enableReleaseLog = isEnable }

        /**
         * if want release mode save file log
         * @param Boolean   :   true is release mode log
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        fun enableReleaseSaveFileLog(isEnable: Boolean) = apply { this._enableReleaseSaveFileLog = isEnable }

        /**
         * debug
         * @param tag   :   tag name
         * @param msg   :   message
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        fun d(tag: String, msg: String) {
            if (isDebug() || this._enableReleaseLog) {
                Log.d(tag, msg);
            }
            if (this._logFile != null) {
                val nowDate = getNowDateString()
                compositeDisposable.add(
                        Observable
                                .fromCallable { appendFileLog(nowDate, msg, DEBUG) }
                                .subscribeOn(Schedulers.single())
                                .subscribe())
            }
        }

        /**
         * info
         * @param tag   :   tag name
         * @param msg   :   message
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        fun i(tag: String, msg: String) {
            if (isDebug() || this._enableReleaseLog) {
                Log.i(tag, msg);
            }
            if (this._logFile != null) {
                val nowDate = getNowDateString()
                compositeDisposable.add(
                        Observable
                                .fromCallable { appendFileLog(nowDate, msg, INFO) }
                                .subscribeOn(Schedulers.single())
                                .subscribe())
            }
        }

        /**
         * error
         * @param tag   :   tag name
         * @param msg   :   message
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        fun e(tag: String, msg: String) {
            if (isDebug() || this._enableReleaseLog) {
                Log.e(tag, msg);
            }
            if (this._logFile != null) {
                val nowDate = getNowDateString()
                compositeDisposable.add(
                        Observable
                                .fromCallable { appendFileLog(nowDate, msg, ERROR) }
                                .subscribeOn(Schedulers.single())
                                .subscribe())
            }
        }

        /**
         * warn
         * @param tag   :   tag name
         * @param msg   :   message
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        fun w(tag: String, msg: String) {
            if (isDebug() || this._enableReleaseLog) {
                Log.w(tag, msg);
            }
            if (this._logFile != null) {
                val nowDate = getNowDateString()
                compositeDisposable.add(
                        Observable
                                .fromCallable { appendFileLog(nowDate, msg, WARN) }
                                .subscribeOn(Schedulers.single())
                                .subscribe())
            }
        }

        /**
         * get current date string
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        private fun getNowDateString() : String {
            var simpleFormatter: SimpleDateFormat = SimpleDateFormat(_logDateFormat);
            val date: Date = Calendar.getInstance().getTime()
            return simpleFormatter.format(date).toString();
        }

        /**
         * check debug mode
         * @return Boolean  : isDebug
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        private fun isDebug() : Boolean {
            if (this._applicationContext == null)
                throw ERROR_INIT;
            var isDebug : Boolean = (this._applicationContext!!.applicationInfo.flags.and(ApplicationInfo.FLAG_DEBUGGABLE)) != 0
            return isDebug
        }

        /**
         * find file url
         * @param contentResolver   :   ContentResolver {@code android.content.ContentResolver}
         * @param fileName          :   file name
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        private fun findFileUri(contentResolver : ContentResolver, fileName : String) : Uri?{
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
         * append file log
         * @param msg   :   message
         * @param level :   debug level
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        fun appendFileLog(date : String, msg: String, level: Int) {
            if (_logFile != null) {

                if (isDebug() || _enableReleaseSaveFileLog) {

                    try {

                        var printMsg = date + " " + msg

                        // error level
                        when(level) {
                            DEBUG -> printMsg = "[DEBUG]" + printMsg
                            INFO -> printMsg = "[INFO]" + printMsg
                            WARN -> printMsg = "[WARN]" + printMsg
                            ERROR -> printMsg = "[ERROR]" + printMsg
                        }

                        // check trun date file
                        if (_logFile!!.isTrunDate) {
                            this._logFile!!.fileName = Util.getCheckTodayFile(this._logFile!!.fileName)
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                            var contentResolver : ContentResolver = _applicationContext!!.contentResolver;

                            var values : ContentValues = ContentValues()
                            values.put(MediaStore.Downloads.DISPLAY_NAME, _logFile!!.fileName)
                            values.put(MediaStore.Downloads.MIME_TYPE, "text/plain")

                            // LOCK
                            values.put(MediaStore.Downloads.IS_PENDING, 1)

                            var uri: Uri? = findFileUri(contentResolver, _logFile!!.fileName)
                            var logFile : ParcelFileDescriptor

                            if (uri == null) {
                                uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
                                logFile = contentResolver.openFileDescriptor(uri, "w", null)!!
                            } else {
                                logFile = contentResolver.openFileDescriptor(uri, "wa", null)!!
                            }

                            var bufferedWriter: BufferedWriter = BufferedWriter(FileWriter(logFile.fileDescriptor))
                            bufferedWriter.append(printMsg)
                            bufferedWriter.newLine()
                            bufferedWriter.close()

                            logFile.close()

                            // UnLock
                            values.put(MediaStore.Downloads.IS_PENDING, 0);

                            contentResolver.update(uri, values, null, null)

                        } else {

                            val logFile : File = File(Environment.getExternalStorageDirectory().absolutePath
                                        + "/"
                                        + this._logFile!!.path
                                    , this._logFile!!.fileName)

                            if (!logFile!!.exists()) {
                                try {
                                    logFile!!.parentFile.mkdirs()
                                    logFile!!.createNewFile()
                                } catch (e: IOException) {
                                    throw e;
                                }
                            }

                            var bufferedWriter: BufferedWriter = BufferedWriter(FileWriter(logFile, true))
                            bufferedWriter.append(printMsg)
                            bufferedWriter.newLine()
                            bufferedWriter.close()

                        }

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        /**
         * get date file name
         * @param fileName : fileName
         * @return yyyyMMdd_fileName
         */
        fun getFileDateName(fileName : String) : String {
            var simpleFormatter: SimpleDateFormat = SimpleDateFormat("yyyyMMdd");
            var date: Date = Calendar.getInstance().getTime()
            return simpleFormatter.format(date).toString() + "_" + fileName
        }
    }

}