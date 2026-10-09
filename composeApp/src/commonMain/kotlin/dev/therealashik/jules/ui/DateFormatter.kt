package dev.therealashik.jules.ui

fun formatSessionTimestamp(isoTimestamp: String): String {
    if (isoTimestamp.isBlank()) return ""
    return try {
        val datePart = isoTimestamp.substringBefore("T")
        val timePart = isoTimestamp.substringAfter("T").substringBefore("Z").substringBefore(".")

        val dateTokens = datePart.split("-")
        if (dateTokens.size < 3) return isoTimestamp

        val monthInt = dateTokens[1].toIntOrNull() ?: 1
        val day = dateTokens[2].toIntOrNull() ?: 1

        val monthNames = listOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val monthName = monthNames.getOrElse(monthInt - 1) { "Jan" }

        val timeTokens = timePart.split(":")
        val timeFormatted = if (timeTokens.size >= 2) {
            val hour24 = timeTokens[0].toIntOrNull() ?: 0
            val min = timeTokens[1]
            val amPm = if (hour24 >= 12) "PM" else "AM"
            val hour12 = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }
            "$hour12:$min $amPm"
        } else ""

        if (timeFormatted.isNotBlank()) {
            "$monthName $day · $timeFormatted"
        } else {
            "$monthName $day"
        }
    } catch (e: Exception) {
        isoTimestamp
    }
}
