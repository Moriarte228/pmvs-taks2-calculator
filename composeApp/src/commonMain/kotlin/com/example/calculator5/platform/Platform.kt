package com.example.calculator5.platform

/**
 * Поддерживаемые платформы. Определяется в actual-реализациях каждого таргета.
 */
enum class Platform { Android, Ios, Desktop, Web }

expect val currentPlatform: Platform
