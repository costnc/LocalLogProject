package kr.co.sungjin.localloglib

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log
import java.lang.Exception

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

        // init error
        val INIT_ERROR : Exception = Exception("**** Settup Init > LocalLog.init(applicationContext) ****");

        // application context
        private var _applicationContext : Context? = null;

        // if wnat release mode log
        var enableReleaseLog : Boolean = false;

        /**
         * init
         * @param context   :   ApplicationContext
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        fun init(applicationContext: Context) {
            this._applicationContext = applicationContext;
        }

        /**
         * debug
         * @param tag   :   tag name
         * @param msg   :   message
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        fun d(tag : String, msg : String) {
            if (!isDebug() || enableReleaseLog) {
                Log.d(tag, msg);
            }
        }

        /**
         * info
         * @param tag   :   tag name
         * @param msg   :   message
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        fun i(tag : String, msg : String) {
            if (!isDebug() || enableReleaseLog) {
                Log.i(tag, msg);
            }
        }

        /**
         * error
         * @param tag   :   tag name
         * @param msg   :   message
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        fun e(tag : String, msg : String) {
            if (!isDebug() || enableReleaseLog) {
                Log.e(tag, msg);
            }
        }

        /**
         * warn
         * @param tag   :   tag name
         * @param msg   :   message
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        fun w(tag : String, msg : String) {
            if (!isDebug() || enableReleaseLog) {
                Log.w(tag, msg);
            }
        }

        /**
         * check debug mode
         * @return Boolean  : isDebug
         * @since 2021.03.29
         * @author lim.sung.jin
         */
        private fun isDebug() : Boolean {
            if (this._applicationContext == null)
                throw INIT_ERROR;
            return (this._applicationContext!!.applicationInfo.flags == ApplicationInfo.FLAG_DEBUGGABLE);
        }
    }

}