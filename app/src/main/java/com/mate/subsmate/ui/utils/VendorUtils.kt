package com.mate.subsmate.ui.utils

import com.mate.subsmate.R
import java.util.concurrent.TimeUnit

object VendorUtils {
    fun getLogo(name: String): Int? {
        return when (name.lowercase()) {
            "netflix" -> R.drawable.ic_vendor_netflix
            "spotify" -> R.drawable.ic_vendor_spotify
            "youtube premium" -> R.drawable.ic_vendor_youtube_premium
            "disney+" -> R.drawable.ic_vendor_disney_plus
            "chatgpt plus" -> R.drawable.ic_vendor_chatgpt_plus
            "claude pro" -> R.drawable.ic_vendor_claude_pro
            "google one" -> R.drawable.ic_vendor_google_one
            "icloud" -> R.drawable.ic_vendor_icloud
            "midjourney" -> R.drawable.ic_vendor_midjourney
            "adobe creative cloud" -> R.drawable.ic_vendor_adobe_creative_cloud
            "microsoft 365" -> R.drawable.ic_vendor_microsoft_365
            "viu premium" -> R.drawable.ic_vendor_viu_premium
            else -> null
        }
    }

    fun getDaysRemaining(nextBillingDate: Long): Long {
        val diff = nextBillingDate - System.currentTimeMillis()
        return if (diff > 0) TimeUnit.MILLISECONDS.toDays(diff) else 0
    }
}
