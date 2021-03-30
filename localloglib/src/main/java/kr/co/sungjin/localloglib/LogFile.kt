package kr.co.sungjin.localloglib

/**
 * Log file info
 * @since 2021.03.29
 * @author lim.sung.jin
 */
class LogFile (
    val path        : String = "",
    val fileName    : String = ""
){

    private constructor(builder: Builder) : this(builder.path, builder.fileName)

    class Builder {
        var path : String = ""
        var fileName : String = ""

        fun path(path : String) = apply { this.path = path }
        fun fileName(fileName : String) = apply { this.fileName = fileName }
        fun build() = LogFile(this)
    }

}