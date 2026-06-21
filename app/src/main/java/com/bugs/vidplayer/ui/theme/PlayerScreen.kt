package com.bugs.vidplayer.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import com.bugs.vidplayer.player.VideoPlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(viewModel: VideoPlayerViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isPlaying by viewModel.isPlaying.collectAsState()
    val localFiles by viewModel.localFiles.collectAsState()

    var currentPosition by remember { mutableLongStateOf(0L) }
    var totalDuration by remember { mutableLongStateOf(0L) }

    // Estado para controlar si el reproductor está expandido a pantalla completa
    var isFullscreen by remember { mutableStateOf(false) }

    // Sincroniza el progreso del Slider cada segundo
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPosition = viewModel.player.currentPosition
            totalDuration = viewModel.player.duration.coerceAtLeast(0L)
            delay(1000)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        // 1. Contenedor de Video (Ajusta su tamaño dinámicamente según el estado)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(if (isFullscreen) 1f else 0.4f)
                .background(Color.Black)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.player
                        useController = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Botón flotante de Pantalla Completa superpuesto en la esquina inferior derecha del video
            IconButton(
                onClick = {
                    isFullscreen = !isFullscreen
                    activity?.requestedOrientation = if (isFullscreen) {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .background(Color(0x88000000))
            ) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
                    contentDescription = "Cambiar Pantalla Completa",
                    tint = Color.White
                )
            }
        }

        // Si está en pantalla completa, ocultamos todos los controles inferiores y la lista de reproducción
        if (!isFullscreen) {
            // 2. Panel de Control Central (Slider y Botonera)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = currentPosition.toFloat(),
                    valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                    onValueChange = { newValue ->
                        currentPosition = newValue.toLong()
                        viewModel.player.seekTo(currentPosition)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(currentPosition), color = Color.Gray, fontSize = 12.sp)
                    Text(text = formatTime(totalDuration), color = Color.Gray, fontSize = 12.sp)
                }

                // Botonera Multimedia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.player.seekToPrevious() }) {
                        Icon(Icons.Filled.SkipPrevious, "Anterior", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { if (isPlaying) viewModel.player.pause() else viewModel.player.play() }) {
                        val icon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                        Icon(painter = painterResource(id = icon), "Play/Pausa", tint = Color.White, modifier = Modifier.size(38.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(onClick = { viewModel.player.seekToNext() }) {
                        Icon(Icons.Filled.SkipNext, "Siguiente", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }

            // 3. Título de la sección de lista
            Text(
                text = "Lista de Reproducción (${localFiles.size})",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // 4. Lista de Archivos (LazyColumn)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {
                itemsIndexed(localFiles) { index, file ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.playSpecificFile(index) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (file.isVideo) Icons.Filled.Movie else Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = if (file.isVideo) Color(0xFF00E676) else Color(0xFF29B6F6),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = file.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (file.isVideo) "Video" else "Audio",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Text(
                            text = formatTime(file.duration),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                    Divider(color = Color(0xFF2C2C2C), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
