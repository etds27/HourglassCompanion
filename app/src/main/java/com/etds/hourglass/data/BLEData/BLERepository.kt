package com.etds.hourglass.data.BLEData

import com.etds.hourglass.data.BLEData.local.BLELocalDatasource
import com.etds.hourglass.data.BLEData.remote.BLERemoteDatasource
import java.util.UUID

class BLERepository(
    private val localDatasource: BLELocalDatasource,
    private val remoteDatasource: BLERemoteDatasource
) {

    companion object {
        val serviceUUID = UUID.fromString("d7560343-51d4-4c24-a0fe-118fd9078144")
    }
}