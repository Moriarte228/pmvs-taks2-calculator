package com.example.calculator5.data

import kotlin.js.Date

actual fun currentTimeMillis(): Long = Date.now().toLong()
