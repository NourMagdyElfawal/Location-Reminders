package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.MainCoroutineRule
import org.hamcrest.MatcherAssert
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
//because we run at mobile component not on jvm
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt
    private lateinit var reminderDatabase: RemindersDatabase
    private lateinit var reminderLocalRepository: RemindersLocalRepository

    private val reminder1 = ReminderDTO("title1", "description1", "location1", (-430..150).random().toDouble()
        ,(-230..640).random().toDouble())
    private val reminder2 = ReminderDTO("title2", "description2", "location2", (-430..150).random().toDouble(),
        (-230..640).random().toDouble())
    @get:Rule
    var instantExecutorRule= InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

@Before
fun setup(){
        reminderDatabase=Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

    reminderLocalRepository= RemindersLocalRepository(reminderDatabase.reminderDao(),Dispatchers.Main)
}
@After
fun tearUp(){
    reminderDatabase.close()
}



    @Test
    fun saveReminder_returnSuccess()= mainCoroutineRule.runBlockingTest{
        reminderLocalRepository.saveReminder(reminder1)
        reminderLocalRepository.saveReminder(reminder2)

        val result=reminderLocalRepository.getReminder(reminder1.id)
        //check if its return right data
        result as Result.Success

        assertThat(result.data).isEqualTo(reminder1)

    }

@Test
fun deleteListRemiders_returnEmptyList()=mainCoroutineRule.runBlockingTest {
    reminderLocalRepository.saveReminder(reminder1)
    reminderLocalRepository.saveReminder(reminder2)

    reminderLocalRepository.deleteAllReminders()

    val result=reminderLocalRepository.getReminders()

    result as Result.Success

    assertThat(result.data).isEmpty()

}

}
