package com.muttaqi.android.feature.share

import android.Manifest
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.OutputStream

// Sharing and saving the card's image: a PNG in the cache shared through the FileProvider, or saved to the gallery

/** The card's image as a bitmap to write, or null before the card has been drawn */
internal suspend fun GraphicsLayer.toShareBitmap(): Bitmap? {
    if (size.width == 0 || size.height == 0) return null
    val bitmap = toImageBitmap().asAndroidBitmap()
    // The layer can hand back a hardware bitmap, which lives on the GPU; PNG encoding needs its pixels
    return if (bitmap.config == Bitmap.Config.HARDWARE) bitmap.copy(Bitmap.Config.ARGB_8888, false) else bitmap
}

/** Opens the share sheet with the image, titled with where the passage is from, as iOS's share sheet is */
internal suspend fun Context.shareImage(bitmap: Bitmap, title: String, fileName: String) {
    val file = withContext(Dispatchers.IO) {
        File(File(cacheDir, SHARE_CACHE_DIR).apply { mkdirs() }, "$fileName.png").also { file ->
            file.outputStream().use { bitmap.writePng(it) }
        }
    }
    val uri = FileProvider.getUriForFile(this, "$packageName$SHARE_AUTHORITY_SUFFIX", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TITLE, title)
        // Lets the share sheet show the card as its preview
        clipData = ClipData.newUri(contentResolver, title, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    startActivity(Intent.createChooser(send, null))
}

/** Only Android 8 and 9 need a permission to add a photo to the gallery */
internal fun Context.needsStoragePermissionToSave(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

/** Saves the image to the gallery, in Pictures/Muttaqi; false if it couldn't be written */
internal suspend fun Context.saveImageToGallery(bitmap: Bitmap, fileName: String): Boolean = withContext(Dispatchers.IO) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) saveWithMediaStore(bitmap, fileName) else saveToPictures(bitmap, fileName)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: IOException) {
        false
    } catch (_: SecurityException) {
        false
    } catch (_: IllegalStateException) {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun Context.saveWithMediaStore(bitmap: Bitmap, fileName: String): Boolean {
    val details = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM")
        // Hidden from other apps until it's written
        put(MediaStore.Images.Media.IS_PENDING, 1)
    }
    val uri = contentResolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), details) ?: return false
    val written = contentResolver.openOutputStream(uri)?.use { bitmap.writePng(it) } ?: false
    if (!written) {
        contentResolver.delete(uri, null, null)
        return false
    }
    contentResolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
    return true
}

/** Android 8 and 9: into Pictures/Muttaqi, beside any card saved before, then into the gallery's index */
private fun Context.saveToPictures(bitmap: Bitmap, fileName: String): Boolean {
    @Suppress("DEPRECATION") // The public folders are only deprecated from Android 10, which saves through MediaStore
    val album = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ALBUM).apply { mkdirs() }
    val file = generateSequence(1) { it + 1 }
        .map { copy -> File(album, if (copy == 1) "$fileName.png" else "$fileName ($copy).png") }
        .first { !it.exists() }
    if (!file.outputStream().use { bitmap.writePng(it) }) return false
    MediaScannerConnection.scanFile(this, arrayOf(file.path), arrayOf("image/png"), null)
    return true
}

private fun Bitmap.writePng(out: OutputStream): Boolean = compress(Bitmap.CompressFormat.PNG, 100, out)

/** Where shared images are written, as `res/xml/share_paths.xml` exposes */
private const val SHARE_CACHE_DIR = "share"
/** The FileProvider's authority after the package name, as in the manifest */
internal const val SHARE_AUTHORITY_SUFFIX = ".share"
private const val ALBUM = "Muttaqi"
