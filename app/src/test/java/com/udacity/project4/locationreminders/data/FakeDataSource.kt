package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource( var reminderDTOList: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {

    private var shouldReturnError=false

    fun setShouldReturnError(value:Boolean){
        shouldReturnError=value
    }
//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Error")
        }
        reminderDTOList?.let { return Result.Success(ArrayList(it)) }

        return Result.Error("Error")

    }
    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTOList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        return if (shouldReturnError) {
            Result.Error("Error")
        } else {

            val reminderDTO = reminderDTOList.firstOrNull {
                it.id == id
            }
            if (reminderDTO != null) {
                return Result.Success(reminderDTO)
            } else {
                return Result.Error("Error")
            }
        }
    }
    override suspend fun deleteAllReminders() {
        reminderDTOList= mutableListOf()
    }


}