import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraPreview() {
    val context = LocalContext.current
    val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { view ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val lifecycleOwner = context as LifecycleOwner

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(view.surfaceProvider)
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                cameraProvider.unbindAll()

                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview
                )

            } catch (exception: Exception) {
                // Handle camera initialization error
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
