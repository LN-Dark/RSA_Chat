package com.luanegra.rsachat.RSA

import java.io.File

fun String.isEndSeparator(): Boolean {
    return this.reversed().startsWith(File.separator)
}