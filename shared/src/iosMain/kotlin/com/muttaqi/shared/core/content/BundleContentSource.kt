package com.muttaqi.shared.core.content

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/** Reads the bundled texts from the app bundle, where an Xcode build phase copies them from content/data */
class BundleContentSource(private val bundle: NSBundle = NSBundle.mainBundle) : BundledContentSource {
    @OptIn(ExperimentalForeignApi::class)
    override fun read(fileName: String): String {
        val name = fileName.substringBeforeLast('.')
        val type = fileName.substringAfterLast('.', "")
        val path = bundle.pathForResource(name, type) ?: error("$fileName isn't in the app bundle")
        return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) ?: error("Couldn't read $fileName")
    }
}
