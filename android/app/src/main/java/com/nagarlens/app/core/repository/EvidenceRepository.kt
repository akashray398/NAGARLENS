package com.nagarlens.app.core.repository

import com.nagarlens.app.core.location.LocationData
import com.nagarlens.app.core.model.Evidence
import kotlinx.coroutines.flow.Flow
import java.io.File

interface EvidenceRepository {
    fun savePhotoEvidence(tempPhotoFile: File, locationData: LocationData?): Flow<Result<Evidence>>
    fun saveVideoEvidence(tempVideoFile: File, locationData: LocationData?): Flow<Result<Evidence>>
    fun getAllEvidence(): Flow<List<Evidence>>
    fun getEvidenceById(id: String): Flow<Evidence?>
}
