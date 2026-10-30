package com.chefinventory.app.data

import androidx.room.TypeConverter
import com.chefinventory.app.model.LocationType

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
        } catch (_: Exception) {
            LocationType.INGREDIENT
        }
    }
}
