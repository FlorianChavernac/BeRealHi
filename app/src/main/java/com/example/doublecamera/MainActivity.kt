package com.example.doublecamera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.Box
import android.util.Log
import android.widget.Toast
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    //horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Montage(
                        photoUri = photoUri,
                        photoUri2 = photoUri2,
                        shouldShowButton = shouldShowButton,
                        shouldShowPhoto = shouldShowPhoto,
                        shouldDisposition = shouldDisposition
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        if (shouldShowButton.value) {
                            Button(
                                onClick = {
                                    shouldShowPhoto.value = false
                                    shouldShowCamera.value = true
                                    first.value = true
                                }) {
                                Text(text = "Reprendre une photo")
                            }

                            Button(onClick = {
                                val transition: Uri = photoUri
                                photoUri = photoUri2
                                photoUri2 = transition
                                shouldShowPhoto.value = false
                                shouldShowPhoto.value = true

                            }) {
                                Text(text = "Inverser les photos")
                            }
                            //TODO: Ajouter un bouton pour enregistrer les photos

                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = { saveToBitmap(photoUri, photoUri2) }) {
                            Text(text = "Enregistrer")

                        }

                    }
                }
            }
        }
        requestCameraPermission()
        requestStoragePermission()
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

    private fun requestStoragePermission(){
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("kilo", "Permission previously granted")
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) -> Log.i("kilo", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
        if (first.value) {
            Log.i("kilo", "Camera Reverse")
            shouldShowCamera.value = true
            shouldShowPhoto.value = false
            shouldCameraFront.value = !shouldCameraFront.value
            Log.i("kilo", shouldCameraFront.value.toString())
        } else {
            Toast.makeText(this, "Vous ne pouvez pas changer de caméra", Toast.LENGTH_SHORT)
                .show()
        }
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
fun Montage(
    photoUri: Uri,
    photoUri2: Uri,
    shouldShowButton: MutableState<Boolean>,
    shouldShowPhoto: MutableState<Boolean>,
    shouldDisposition: MutableState<Int>
) {
    Box(
        contentAlignment = Alignment.TopStart,
        modifier = Modifier.fillMaxWidth()
    ) {
        //Image 1
        Spacer(modifier = Modifier.height(0.dp))
        Image(
            painter = rememberImagePainter(photoUri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(50.dp)
                )
        )
        //Image 2
        Image(
            painter = rememberImagePainter(photoUri2),
            contentDescription = null,
            modifier = Modifier
                .size(200.dp)
                .clip(
                    RoundedCornerShape(50.dp)
                )
                .align(Alignment.TopEnd)
        )
    }
}

fun saveToBitmap(photoUri: Uri, photoUri2: Uri) {
    val bitmap1 = BitmapFactory.decodeFile(photoUri.path)
    val bitmap2 = BitmapFactory.decodeFile(photoUri2.path)
    val bitmap3 = Bitmap.createBitmap(
        bitmap1.width,
        bitmap1.height,
        bitmap1.config
    )
    val canvas = Canvas(bitmap3)
    canvas.drawBitmap(bitmap1, 0f, 0f, null)
    canvas.drawBitmap(bitmap2, 10f, 20f, null)
    val file = File(Environment.getExternalStorageDirectory().toString() + "/test.png")
    try {
        val out = FileOutputStream(file)
        bitmap3.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
    } catch (e: Exception) {
        e.printStackTrace()
        Log.i("Kilo", "photo 2" + e.toString())
    }
    Log.i(
        "Kilo", "photo 3" + file.absolutePath
    )

}

//Ancienne méthode render
/*if (shouldDisposition.value == 1) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                shouldShowButton.value = !shouldShowButton.value
                shouldShowPhoto.value = false
                shouldShowPhoto.value = true
            }, verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Image(
            painter = rememberImagePainter(photoUri),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .weight(5f)
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
*/

