package com.udacity.project4

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDao
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private var reminderDTOList: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var shouldReturnError=false
//    private var reminderDTOList= mutableListOf<ReminderDTO>()

    fun setShouldReturnError(value:Boolean){
        shouldReturnError=value
    }
//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (reminderDTOList.isEmpty()){
                Result.Error("Error")
        }else{
            return Result.Success(reminderDTOList)

        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminderDTOList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
       val reminderDTO=reminderDTOList.firstOrNull{
            it.id==id
        }
        if (reminderDTO!=null){
            return Result.Success(reminderDTO)
        }else{
            return Result.Error("Error")
        }
    }

    override suspend fun deleteAllReminders() {
        reminderDTOList= mutableListOf()
    }


}