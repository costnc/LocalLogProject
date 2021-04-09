package kr.co.sungjin.localloglib

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


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

        val MIME_NAME = ".txt"

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

        // file uri
        private var _uri: Uri? = null

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
         * append file log
         * @param msg   :   message
         * @param level :   debug level
         * @since 2021.03.30
         * @author lim.sung.jin
         */
        fun appendFileLog(date : String, msg: String, level: Int) {
            try {
                if (_logFile != null) {

                    if (isDebug() || _enableReleaseSaveFileLog) {

                        try {

                            var printMsg = date + " " + msg

                            // error level
                            when (level) {
                                DEBUG -> printMsg = "[DEBUG]" + printMsg
                                INFO -> printMsg = "[INFO]" + printMsg
                                WARN -> printMsg = "[WARN]" + printMsg
                                ERROR -> printMsg = "[ERROR]" + printMsg
                            }

                            // check trun date file
                            if (_logFile!!.isTrunDate) {
                                if (!Util.getCheckTodayFile(Util.getOriginName(this._logFile!!.fileName))) {
                                    this._logFile!!.fileName = Util.getTodayFile(Util.getRemoveNumberingFromFileName(Util.getOriginName(this._logFile!!.fileName)))
                                    this._uri = null
                                }
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                                var contentResolver: ContentResolver = _applicationContext!!.contentResolver;

                                var values: ContentValues = ContentValues()
                                values.put(MediaStore.Downloads.MIME_TYPE, "text/plain")

                                // LOCK
                                values.put(MediaStore.Downloads.IS_PENDING, 1)

                                if (this._uri == null) {
                                    this._uri = Util.findFileUri(contentResolver, Util.removeMimeTxt(_logFile!!.fileName)+MIME_NAME)
                                }

                                var logFile: ParcelFileDescriptor

                                if (this._uri == null) {
                                    values.put(MediaStore.Downloads.DISPLAY_NAME, Util.getRemoveNumberingFromFileName(_logFile!!.fileName)+MIME_NAME)
                                    this._uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
                                    logFile = contentResolver.openFileDescriptor(this._uri!!, "w", null)!!
                                } else {
                                    values.put(MediaStore.Downloads.DISPLAY_NAME, this._logFile!!.fileName+MIME_NAME)
                                    logFile = contentResolver.openFileDescriptor(this._uri!!, "wa", null)!!

                                    // file size check
                                    if (this._logFile!!.trunLogFileSize > 0) {
                                        if (logFile.statSize > this._logFile!!.trunLogFileSize) {

                                            // close file
                                            logFile.close()

                                            // new numbering file naming
                                            while(true) {
                                                this._logFile!!.fileName = Util.getNextFileNameNumbering(_logFile!!.fileName)
                                                this._uri = Util.findFileUri(contentResolver, _logFile!!.fileName+MIME_NAME)
                                                if (this._uri == null) break
                                            }

                                            values.put(MediaStore.Downloads.DISPLAY_NAME, this._logFile!!.fileName+MIME_NAME)

                                            // new file
                                            this._uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!

                                            // open new file
                                            logFile = contentResolver.openFileDescriptor(this._uri!!, "w", null)!!
                                        }
                                    }

                                }

                                var bufferedWriter: BufferedWriter = BufferedWriter(FileWriter(logFile.fileDescriptor))
                                bufferedWriter.append(printMsg)
                                bufferedWriter.newLine()
                                bufferedWriter.close()

                                logFile.close()

                                // UnLock
                                values.put(MediaStore.Downloads.IS_PENDING, 0);

                                contentResolver.update(this._uri!!, values, null, null)

                            } else {

                                var logFile: File = File(Environment.getExternalStorageDirectory().absolutePath
                                        + "/"
                                        + this._logFile!!.path, this._logFile!!.fileName)

                                // new file
                                Util.newFile(logFile)

                                // file size check
                                if (this._logFile!!.trunLogFileSize > 0) {
                                    if (logFile.length() > this._logFile!!.trunLogFileSize) {

                                        // new numbering file naming
                                        while(true) {
                                            logFile =  File(Environment.getExternalStorageDirectory().absolutePath
                                                    + "/"
                                                    + this._logFile!!.path, Util.getNextFileNameNumbering(logFile.name))

                                            if (!logFile.exists()) break
                                        }

                                        // new file
                                        Util.newFile(logFile)

                                        this._logFile!!.fileName = logFile.name
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
            }catch(e: Exception) {
                e.printStackTrace()
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