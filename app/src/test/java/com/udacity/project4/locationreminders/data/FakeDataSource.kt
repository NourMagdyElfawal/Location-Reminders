package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private var reminderDTOList= mutableListOf<ReminderDTO>()

    private var shouldReturnError=false

    fun setShouldReturnError(value:Boolean){
        shouldReturnError=value
    }
//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
//        reminderDTOList?.let {
//            return Result.Success(it)
//        }
//        return Result.Error("Error")
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
//        reminderDTOList.firstOrNull {
//            it.id == id
//        }
//            ?.let {
//                return Result.Success(it)
//            }
//                return Result.Error("Error")
//
    }

    override suspend fun deleteAllReminders() {
        reminderDTOList= mutableListOf()
    }


}