package com.bugs.vidplayer

class MediaFile {package com.bugs.vidplayer.data

    import android.net.Uri

    data class MediaFile(
        val id: Long,
        val title: String,
        val duration: Long,
        val uri: Uri,
        val isVideo: Boolean
    )

}