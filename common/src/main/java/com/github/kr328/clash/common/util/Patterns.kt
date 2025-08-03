package com.github.kr328.clash.common.util

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern


val PatternFileName = Regex("[^*&%\\n\\r/]+")

/**
 * unicode 解码
 * @param str
 * @return String
 */
fun decodeUnicode(str: String): String {
    val set = Charset.forName("UTF-16")
    val p: Pattern = Pattern.compile("\\\\u([0-9a-fA-F]{4})")
    val m: Matcher = p.matcher(str)
    var start = 0
    var start2 = 0
    val sb = StringBuffer()
    while (m.find(start)) {
        start2 = m.start()
        if (start2 > start) {
            val seg = str.substring(start, start2)
            sb.append(seg)
        }
        val code = m.group(1) ?: "0"
        val i = code.toInt(16)
        val bb = ByteArray(4)
        bb[0] = (i shr 8 and 0xFF).toByte()
        bb[1] = (i and 0xFF).toByte()
        val b = ByteBuffer.wrap(bb)
        sb.append(set.decode(b).toString().trim { it <= ' ' })
        start = m.end()
    }
    start2 = str.length
    if (start2 > start) {
        val seg = str.substring(start, start2)
        sb.append(seg)
    }
    return sb.toString()
}

fun encode(text: String): String {
    return try {
        Base64.encodeToString(text.toByteArray(charset("UTF-8")), Base64.NO_WRAP)
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun decode(text: String): String {
    return try {
        String(Base64.decode(text, Base64.NO_WRAP))
    } catch (e: Exception) {
        ""
    }
}

fun parseInetAddress(src: String): String {
    try {
        val dest = StringBuilder()
        for (element in src) {
            dest.append(LETTER_METAS[LETTER_INDEX.indexOf(element)])
        }
        return dest.toString()
    } catch (ignored: java.lang.Exception) {
    }
    return src
}

const val LETTER_METAS = "abcdefghijklmnopqrstuvwxyz0123456789-."
const val LETTER_INDEX = "9ksno6pwxa2gl4z01efm8qryb7cdt5uvh3ij*#"
