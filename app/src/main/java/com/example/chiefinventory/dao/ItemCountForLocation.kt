package com.example.chiefinventory.dao

/**
 * A simple data class to hold the result of a query that counts items per location.
 *
 * @property locationId The ID of the location.
 * @property count The number of items in that location.
 */
data class ItemCountForLocation(val locationId: Long, val count: Int)
