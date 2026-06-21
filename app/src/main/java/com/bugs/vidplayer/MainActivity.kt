package com.bugs.vidplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.bugs.vidplayer.player.VideoPlayerViewModel
import com.bugs.vidplayer.ui.PlayerScreen

class MainActivity : ComponentActivity() {

    // Instanciamos el motor de reproducción de manera segura
    private val videoViewModel: VideoPlayerViewModel by viewModels()

    override fun bundleLayout() {} // Método auxiliar vacío si el compilador lo pide

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current

            // Determinar qué permisos se necesitan según la versión de Android corriendo en el dispositivo
            val permissionsNeeded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            // Estado que controla si el acceso a los archivos fue concedido
            var hasPermission by remember {
                mutableStateOf(
                    permissionsNeeded.all {
                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                    }
                )
            }

            // Lanzador interactivo de la ventana nativa de permisos de Android
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                hasPermission = results.values.all { it }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF121212)),
                contentAlignment = Alignment.Center
            ) {
                if (hasPermission) {
                    // Si tenemos permiso, iniciamos una lista de prueba vacía y cargamos el reproductor
                    LaunchedEffect(Unit) {
                        // Reemplaza la línea anterior por esta en tu MainActivity:
                        LaunchedEffect(Unit) {
                            videoViewModel.loadLocalMedia()
                        }

                    }
                    PlayerScreen(viewModel = videoViewModel)
                } else {
                    // Si no hay permiso, mostramos la interfaz informativa intermedia
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Se requieren permisos de lectura para buscar tus archivos de video y audio (.mp3, .flac, .mp4).",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { launcher.launch(permissionsNeeded) }) {
                            Text(text = "Conceder Acceso")
                        }
                    }
                }
            }
        }
    }
}
