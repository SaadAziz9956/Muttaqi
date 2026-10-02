package com.muttaqi.android.feature.share

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.ComponentActivity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [35])
class ShareImageFilesTest {
    @Test
    fun sharingOpensTheShareSheetWithThePngAndItsTitle() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val card = Bitmap.createBitmap(30, 20, Bitmap.Config.ARGB_8888)

        runBlocking { activity.shareImage(card, "Quran (26:83)", "Quran (26-83)") }

        val chooser = shadowOf(activity).nextStartedActivity
        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        val send = chooser.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)!!
        assertEquals(Intent.ACTION_SEND, send.action)
        assertEquals("image/png", send.type)
        assertEquals("Quran (26:83)", send.getStringExtra(Intent.EXTRA_TITLE))
        assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)

        val image = send.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)!!
        assertEquals("${activity.packageName}$SHARE_AUTHORITY_SUFFIX", image.authority)
        assertEquals("Quran (26-83).png", image.lastPathSegment)
        assertEquals(image, send.clipData?.getItemAt(0)?.uri)
        val written = activity.contentResolver.openInputStream(image)!!.use { BitmapFactory.decodeStream(it) }
        assertEquals(30 to 20, written.width to written.height)
    }
}
