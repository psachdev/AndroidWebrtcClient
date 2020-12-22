package com.psachdev.androidwebrtcclient

import android.util.Log
import com.psachdev.androidwebrtcclient.Logger.logMessage
import java.io.PrintWriter
import java.io.StringWriter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object Logger {
    var tag: String = "WebrtcClientLogger"

    var packageName = ""
    var isLogsShown = true
    var isThreadNameVisible = false
    var isTimeVisible = true
    var isPackageNameVisible = false
    var isClassNameVisible = true
    var isMethodNameVisible = true
    var isSpacingEnabled = true
    var isLengthShouldWrap = true

    var classNameLength = 15
    var packageAndClassNameLength = 35
    var methodNameLength = 15
    var threadNameLength = 6
    var timeFormat = "HH:mm:ss.SSS"

    internal fun logMessage(level: Int, msg: String, t: Throwable?, customTag: String?) {
        if (!isLogsShown) return

        val stackTrace = Thread.currentThread().stackTrace
        val elementIndex: Int = getElementIndex(stackTrace)
        if (elementIndex == 0) return

        val element = stackTrace[elementIndex]
        val result = StringBuilder()

        if (isTimeVisible) result.append(getTime()).append(" - ")
        if (isThreadNameVisible) result.append("T:").append(getThreadId()).append(" | ")
        if (isClassNameVisible) addClassName(element, result)
        if (isMethodNameVisible) addMethodName(element, result)

        addMessage(msg, result)
        addExceptionIfNotNull(t, result)

        val tag = if (customTag == null) tag else "$tag>$customTag"

        Log.println(level, tag, result.toString())
    }

    private fun getElementIndex(stackTrace: Array<StackTraceElement>?): Int {
        if (stackTrace == null) return 0
        for (i in 2..stackTrace.size) {
            val className = stackTrace[i].className ?: ""
            if (className.contains(this.javaClass.simpleName)) continue
            return i
        }
        return 0
    }

    private fun getThreadId(): StringBuilder? {
        val name = Thread.currentThread().name
        val threadId = StringBuilder(name)
        addSpaces(threadId, threadNameLength - name.length)
        return threadId
    }

    private fun addClassName(element: StackTraceElement, result: StringBuilder) {
        val fullClassName = element.className
        val maxLength = if (isPackageNameVisible) packageAndClassNameLength else classNameLength

        var classNameFormatted = if (isPackageNameVisible) {
            fullClassName.replace(packageName, "")
        } else {
            fullClassName.substring(fullClassName.lastIndexOf('.') + 1)
        }

        if (isLengthShouldWrap) classNameFormatted = wrapString(classNameFormatted, maxLength)
        result.append(classNameFormatted)

        addSpaces(result, maxLength - classNameFormatted.length)
        result.append(" # ")
    }

    private fun addMethodName(element: StackTraceElement, result: StringBuilder) {
        var methodName = element.methodName
        if (isLengthShouldWrap) methodName = wrapString(methodName, methodNameLength)
        result.append("$methodName()")
        addSpaces(result, methodNameLength - methodName.length)
    }

    private fun addMessage(msg: String, result: StringBuilder) {
        if (msg.isNotEmpty()) {
            result.append("=> ")
            result.append(msg)
        }
    }

    private fun addExceptionIfNotNull(t: Throwable?, result: StringBuilder) {
        if (t != null) {
            val sw = StringWriter()
            val pw = PrintWriter(sw)
            t.printStackTrace(pw)
            pw.flush()
            result.append("\n Throwable: ")
            result.append(sw.toString())
        }
    }

    private fun wrapString(string: String, maxLength: Int): String {
        return string.substring(0, string.length.coerceAtMost(maxLength))
    }

    private fun addSpaces(result: StringBuilder, spaces: Int) {
        if (isSpacingEnabled && spaces > 0) result.append(" ".repeat(spaces))
    }

    private fun getTime(): String? {
        val df: DateFormat = SimpleDateFormat(timeFormat, Locale.getDefault())
        return df.format(Calendar.getInstance().time)
    }
}

fun verbose(message: String = "", tag: String? = null, throwable: Throwable? = null) {
    logMessage(Log.VERBOSE, message, throwable, tag)
}

fun debug(message: String = "", tag: String? = null, throwable: Throwable? = null) {
    logMessage(Log.DEBUG, message, throwable, tag)
}

fun info(message: String = "", tag: String? = null, throwable: Throwable? = null) {
    logMessage(Log.INFO, message, throwable, tag)
}

fun warn(message: String = "", tag: String? = null, throwable: Throwable? = null) {
    logMessage(Log.WARN, message, throwable, tag)
}

fun error(message: String = "", tag: String? = null, throwable: Throwable? = null) {
    logMessage(Log.ERROR, message, throwable, tag)
}