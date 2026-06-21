package com.bugs.vidplayer

import android.app.Application
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.launch
import kotlinx.flow.MutableStateFlow
import kotlinx.flow.StateFlow

@OptIn(UnstableApi::class)
class VideoPlayerViewModel(application: Application) : AndroidViewModel(application), Player.Listener {

    private val mediaScanner = MediaScanner(application)

    val player: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        addListener(this@VideoPlayerViewModel)
    }

    // Vinculación explícita a la biblioteca kotlinx.flow
    private val _localFiles = MutableStateFlow<List<MediaFile>>(emptyList())
    val localFiles: StateFlow<List<MediaFile>> = _localFiles

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    fun loadLocalMedia() {
        viewModelScope.launch {
            val files = mediaScanner.scanLocalFiles()
            _localFiles.value = files

            if (files.isNotEmpty()) {
                val mediaItems = files.map { MediaItem.fromUri(it.uri) }
                player.setMediaItems(mediaItems)
                player.prepare()
            }
        }
    }

    fun playSpecificFile(index: Int) {
        if (index in 0 until _localFiles.value.size) {
            player.seekTo(index, 0L)
            player.play()
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
