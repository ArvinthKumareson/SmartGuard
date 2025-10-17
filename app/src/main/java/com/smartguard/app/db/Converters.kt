package com.smartguard.app.db

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromList(list: List<String>): String = list.joinToString("||")

    @TypeConverter
    fun toList(data: String): List<String> = data.split("||").filter { it.isNotBlank() }
}
