package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {


    val reminderDTOList = listOf<ReminderDTO>(
        ReminderDTO("title", "description", "location", (-430..150).random().toDouble(),
            (-230..640).random().toDouble()),
        ReminderDTO("title", "description", "location", (-430..150).random().toDouble(),
            (-230..640).random().toDouble())
    )
    private lateinit var reminderListViewModel: RemindersListViewModel
    //TODO: provide testing to the RemindersListViewModel and its live data objects
    //to make sure we will run everything in the same thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp(){
//        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource())
    }

    @After
    fun tearDown() {
        stopKoin()
    }
    @Config(sdk = [28])
    @Test
    fun check_loading() {
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(mutableListOf()))
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue()).isTrue()

    }


    @Config(sdk = [28])
    @Test
    fun get_reminders_isEmpty_shouldReturnError() {
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
            FakeDataSource(emptyList<ReminderDTO>().toMutableList()))
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.showSnackBar.getOrAwaitValue()).isEqualTo("Error")

    }



    @Config(sdk = [28])
    @Test
     fun get_reminder_List_shouldReturnList() {
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
            FakeDataSource(reminderDTOList as MutableList<ReminderDTO>))
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.remindersList.getOrAwaitValue()).isNotEmpty()

    }

}