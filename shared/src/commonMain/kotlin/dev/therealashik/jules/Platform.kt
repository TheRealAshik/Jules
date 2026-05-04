package dev.therealashik.jules

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform