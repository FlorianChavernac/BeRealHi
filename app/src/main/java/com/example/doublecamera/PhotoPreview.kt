import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap


@Composable
fun PhotoPreview(frontImage: ByteArray, backImage: ByteArray, onSave: () -> Unit) {

    //val (frontBitmap, setFrontBitmap) = remember { mutableStateOf(frontImage.asImageBitmap()) }
    //val (backBitmap, setBackBitmap) = remember { mutableStateOf(backImage.asImageBitmap()) }
    //val (frontBitmap, setFrontBitmap) = remember { mutableStateOf(BitmapFactory.decodeByteArray(frontImage, 0, frontImage.size)) }
    //val (backBitmap, setBackBitmap) = remember { mutableStateOf(BitmapFactory.decodeByteArray(backImage, 0, backImage.size)) }
    // Convertir les ByteArray en Bitmap puis en ImageBitmap
    val frontBitmap = BitmapFactory.decodeByteArray(frontImage, 0, frontImage.size).asImageBitmap()
    val backBitmap = BitmapFactory.decodeByteArray(backImage, 0, backImage.size).asImageBitmap()
    val (imagesSwapped, setImagesSwapped) = remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Image(
                    bitmap = if (imagesSwapped) backBitmap else frontBitmap,
                    contentDescription = "Front image",
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Image(
                    bitmap = if (imagesSwapped) frontBitmap else backBitmap,
                    contentDescription = "Back image",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Button(
            onClick = {
                setImagesSwapped(!imagesSwapped)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Swap Images")
        }

        Button(
            onClick = {
                onSave()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Save")
        }
    }
}
