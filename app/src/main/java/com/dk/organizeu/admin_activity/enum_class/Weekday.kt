package com.dk.organizeu.admin_activity.enum_class

enum class Weekday(val displayName: String) {
    MONDAY("Monday"),
    TUESDAY("Tuesday"),
    WEDNESDAY("Wednesday"),
    THURSDAY("Thursday"),
    FRIDAY("Friday"),
    SATURDAY("Saturday"),
    SUNDAY("Sunday");

    companion object {
        fun getWeekdayNameByNumber(dayNumber: Int): String {
            return when (dayNumber) {
                1 -> MONDAY.displayName
                2 -> TUESDAY.displayName
                3 -> WEDNESDAY.displayName
                4 -> THURSDAY.displayName
                5 -> FRIDAY.displayName
                6 -> SATURDAY.displayName
                7 -> SUNDAY.displayName
                else -> throw IllegalArgumentException("Invalid day number: $dayNumber. Day number should be between 1 and 7.")
            }
        }

        fun getWeekdayNumberByName(dayName: String): Int {
            return when (dayName) {
                MONDAY.displayName -> 1
                TUESDAY.displayName -> 2
                WEDNESDAY.displayName -> 3
                THURSDAY.displayName -> 4
                FRIDAY.displayName -> 5
                SATURDAY.displayName -> 6
                SUNDAY.displayName -> 7
                else -> throw IllegalArgumentException("Invalid day name: $dayName.")
            }
        }
    }
}
