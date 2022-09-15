package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Database
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    //tell the class we will execute functions one after  other at the same thread
    @get:Rule
    var instantTaskExecutorRule=InstantTaskExecutorRule()


    private lateinit var database:RemindersDatabase
    private lateinit var dao:RemindersDao
//    TODO: Add testing implementation to the RemindersDao.kt
@Before
fun setup(){
    //save database in ram memory not in the device (inMemoryDatabaseBuilder)
    //usually we use background thread to save  data at database because its maybe blocking thread
    //but her we will use same thread to make everything execute after each other independent(allowMainThreadQueries)
    database=Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        RemindersDatabase::class.java).
        allowMainThreadQueries().build()
    dao=database.reminderDao()
}
@After
fun tearDown(){
    database.close()
}
@Test
fun insertReminder()= runBlockingTest {
    val reminderDTO=ReminderDTO(
        "title",
        "description",
        "location",
        (-430..350).random().toDouble(),
        (-230..640).random().toDouble()
    )
    dao.saveReminder(reminderDTO)

    val allReminder=dao.getReminders()
    assertThat(allReminder).contains(reminderDTO)

}

    @Test
    fun deleteAllReminders()= runBlockingTest {
        val reminderDTO1=ReminderDTO(
            "title",
            "description",
            "location",
            (-430..350).random().toDouble(),
            (-230..640).random().toDouble()
        )
        val reminderDTO2=ReminderDTO(
            "title",
            "description",
            "location",
            (-430..350).random().toDouble(),
            (-230..640).random().toDouble()
        )
        val reminderDTO3=ReminderDTO(
            "title",
            "description",
            "location",
            (-430..350).random().toDouble(),
            (-230..640).random().toDouble()
        )

        dao.saveReminder(reminderDTO1)
        dao.saveReminder(reminderDTO2)
        dao.saveReminder(reminderDTO3)

        dao.deleteAllReminders()
        val allReminder=dao.getReminders()
        assertThat(allReminder).isEmpty()

    }
    @Test
    fun getReminderById()= runBlockingTest {
        val reminderDTO = ReminderDTO(
            "title",
            "description",
            "location",
            (-430..350).random().toDouble(),
            (-230..640).random().toDouble(),
            id = "1"
        )
        dao.saveReminder(reminderDTO)
        val allReminder = dao.getReminders()
        val reminder = dao.getReminderById("1")
        assertThat(allReminder).contains(reminder)
    }
    @Test
    fun getReminderByIdNotExist_returnNull()= runBlockingTest {
        val reminderDTO = ReminderDTO(
            "title",
            "description",
            "location",
            (-430..350).random().toDouble(),
            (-230..640).random().toDouble(),
            id = "1"
        )
        dao.saveReminder(reminderDTO)
        val allReminder = dao.getReminders()
        val reminder = dao.getReminderById("2")
        val boolean=allReminder.contains(reminder)
        assertThat(boolean).isFalse()
    }

}
