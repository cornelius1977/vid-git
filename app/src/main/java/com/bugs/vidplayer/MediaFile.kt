package com.bugs.vidplayer

import android.net.Uri

data class MediaFile(
    val id: Long,
    val title: String,
    val duration: Long,
    val uri: Uri,
    val isVideo: Boolean
)
