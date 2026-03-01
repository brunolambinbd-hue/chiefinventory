package com.example.chiefinventory.data

import androidx.room.TypeConverter
import com.example.chiefinventory.model.LocationType

/**
 * Type converters to allow Room to reference complex data types.
 */
class TypeConverters {
    @TypeConverter
    fun fromLocationType(value: LocationType): String {
        return value.name
    }

    @TypeConverter
    fun toLocationType(value: String): LocationType {
        return try {
            LocationType.valueOf(value)
        } catch (e: Exception) {
            LocationType.COLLECTION
        }
    }
}
