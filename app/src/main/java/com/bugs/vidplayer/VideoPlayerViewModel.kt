package com.bugs.vidplayer.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bugs.vidplayer.data.MediaFile
import com.bugs.vidplayer.data.MediaScanner
import kotlinx.flow.MutableStateFlow
import kotlinx.flow.StateFlow
import kotlinx.coroutines.launch

class VideoPlayerViewModel(application: Application) : AndroidViewModel(application), Player.Listener {

    // Instanciamos el escáner multimedia pasándole el contexto de la app
    private val mediaScanner = MediaScanner(application)

    // Motor de reproducción ExoPlayer
    val player: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        addListener(this@VideoPlayerViewModel)
    }

    // Estado que guarda la lista de canciones y videos encontrados en el teléfono
    private val _localFiles = MutableStateFlow<List<MediaFile>>(emptyList())
    val localFiles: StateFlow<List<MediaFile>> = _localFiles

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    // Función principal para buscar los archivos locales en segundo plano
    fun loadLocalMedia() {
        viewModelScope.launch {
            val files = mediaScanner.scanLocalFiles()
            _localFiles.value = files

            // Si encontró archivos, preparamos la lista inicial en ExoPlayer
            if (files.isNotEmpty()) {
                val mediaItems = files.map { MediaItem.fromUri(it.uri) }
                player.setMediaItems(mediaItems)
                player.prepare()
            }
        }
    }

    // Función para reproducir un archivo específico cuando el usuario haga clic en la lista
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
        player.release() // Liberación de memoria obligatoria
    }
}
