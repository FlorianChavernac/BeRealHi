package com.example.doublecamera

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.compose.material.Text
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter

class MainActivity : ComponentActivity() {
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)
    private var shouldCameraFront: MutableState<Boolean> = mutableStateOf(false)

    private lateinit var photoUri: Uri
    private lateinit var photoUri2: Uri

    private var first: MutableState<Boolean> = mutableStateOf(true)

    private var shouldShowPhoto: MutableState<Boolean> = mutableStateOf(false)
    private var shouldShowButton: MutableState<Boolean> = mutableStateOf(true)

    private var shouldDisposition: MutableState<Int> = mutableStateOf(1)


    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("kilo", "Permission granted")
            shouldShowCamera.value = true
        } else {
            Log.i("kilo", "Permission denied")
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {/*
            DoubleCameraTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
            */
            if (shouldShowCamera.value) {
                //var lensFacing = CameraSelector.LENS_FACING_BACK
                if (shouldCameraFront.value) {
                    CameraView(
                        outputDirectory = outputDirectory,
                        executor = cameraExecutor,
                        onImageCaptured = ::handleImageCapture1,
                        onError = { Log.e("kilo", "View error:", it) },
                        onCameraReversed = ::handleReverseCamera,
                        lensFacing = CameraSelector.LENS_FACING_FRONT
                    )
                } else {
                    CameraView(
                        outputDirectory = outputDirectory,
                        executor = cameraExecutor,
                        onImageCaptured = ::handleImageCapture1,
                        onError = { Log.e("kilo", "View error:", it) },
                        onCameraReversed = ::handleReverseCamera,
                        lensFacing = CameraSelector.LENS_FACING_BACK
                    )
                }
            }
            if (shouldShowPhoto.value) {
                render(
                    photoUri = photoUri,
                    photoUri2 = photoUri2,
                    shouldShowButton = shouldShowButton,
                    shouldShowPhoto = shouldShowPhoto,
                    shouldDisposition = shouldDisposition
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (shouldShowButton.value) {
                        Button(onClick = {
                            shouldShowPhoto.value = false
                            shouldShowCamera.value = true
                            first.value = true
                        }) {
                            Text(text = "Reprendre une photo")
                        }
                        Spacer(modifier = Modifier.height(500.dp))

                        Button(onClick = {
                            val transition: Uri = photoUri
                            photoUri = photoUri2
                            photoUri2 = transition
                            shouldShowPhoto.value = false
                            shouldShowPhoto.value = true

                        }) {
                            Text(text = "Inverser les photos")
                        }
                        Button(onClick = {
                            shouldDisposition.value= (shouldDisposition.value+1)%2
                            shouldShowPhoto.value = false
                            shouldShowPhoto.value = true

                        }) {
                            Text(text = "Changer la disposition")
                        }
                    }
                }
            }
        }
        requestCameraPermission()
        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("kilo", "Permission previously granted")
                shouldShowCamera.value = true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> Log.i("kilo", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    private fun handleImageCapture1(uri: Uri) {
        Log.i("kilo", "Image captured: $uri")
        if (first.value) {
            photoUri = uri
            shouldCameraFront.value = !shouldCameraFront.value
            first.value = !first.value
        } else {
            photoUri2 = uri
            shouldShowCamera.value = false
            shouldShowPhoto.value = true
        }


    }

    private fun handleReverseCamera() {
        Log.i("kilo", "Camera Reverse")
        shouldShowCamera.value = true
        shouldShowPhoto.value = false
        shouldCameraFront.value = !shouldCameraFront.value
        Log.i("kilo", shouldCameraFront.value.toString())

    }


    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
fun render(
    photoUri: Uri,
    photoUri2: Uri,
    shouldShowButton: MutableState<Boolean>,
    shouldShowPhoto: MutableState<Boolean>,
    shouldDisposition: MutableState<Int>
) {


//Créer plusieurs disposition de photo

    if (shouldDisposition.value == 1) {
        Column(modifier = Modifier
            .fillMaxSize()
            .clickable {
                shouldShowButton.value = !shouldShowButton.value
                shouldShowPhoto.value = false
                shouldShowPhoto.value = true
            }, verticalArrangement = Arrangement.SpaceEvenly) {
            Image(
                painter = rememberImagePainter(photoUri),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Log.i("Kilo", "photo 1" + photoUri)
            Image(
                painter = rememberImagePainter(photoUri2),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                /*modifier = Modifier
                    .absoluteOffset(60.dp, 20.dp)
                    .size(200.dp)
                    .clip(CircleShape)
                 */
            )
            Log.i("Kilo", "photo 2" + photoUri2)
        }
    } else {
        Image(
            painter = rememberImagePainter(photoUri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
        )
        Log.i("Kilo", "photo 1" + photoUri)
        Image(
            painter = rememberImagePainter(photoUri2),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .size(200.dp)
                .clip(CircleShape)
        )
        Log.i("Kilo", "photo 2" + photoUri2)
    }

}

