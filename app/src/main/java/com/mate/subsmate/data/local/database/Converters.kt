package com.mate.subsmate.data.local.database

import androidx.room.TypeConverter
import com.mate.subsmate.domain.model.BillingCycle

class Converters {
    @TypeConverter
    fun fromBillingCycle(value: BillingCycle): String {
        return value.name
    }

    @TypeConverter
    fun toBillingCycle(value: String): BillingCycle {
        return BillingCycle.valueOf(value)
    }
}
