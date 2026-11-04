package com.nagarlens.app.core.integrity

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class EvidenceFileStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val evidenceDirectory: File by lazy {
        File(context.filesDir, "evidence").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    open fun storeOriginalEvidence(sourceFile: File, targetFileName: String): File {
        require(sourceFile.exists()) { "Source media file does not exist: ${sourceFile.absolutePath}" }
        require(sourceFile.length() > 0) { "Source media file is empty: ${sourceFile.absolutePath}" }

        val targetFile = File(evidenceDirectory, targetFileName)
        if (targetFile.exists()) {
            throw IllegalStateException("Evidence file already exists in private store and cannot be overwritten: ${targetFile.name}")
        }

        sourceFile.copyTo(targetFile, overwrite = false)

        if (!targetFile.exists() || targetFile.length() != sourceFile.length()) {
            targetFile.delete()
            throw IllegalStateException("Failed to preserve original evidence to app-private storage.")
        }

        return targetFile
    }

    open fun getEvidenceFile(fileName: String): File? {
        val file = File(evidenceDirectory, fileName)
        return if (file.exists() && file.length() > 0) file else null
    }
}
