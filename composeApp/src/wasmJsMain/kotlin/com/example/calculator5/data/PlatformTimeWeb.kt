package com.example.calculator5.data

actual fun currentTimeMillis(): Long =
    js("Date.now()")