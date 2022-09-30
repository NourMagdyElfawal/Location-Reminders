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
import kotlinx.coroutines.test.runBlockingTest
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

    private val reminderDTOList = mutableListOf<ReminderDTO>()
    private val reminder1 = ReminderDTO("title", "description", "location", (-430..150).random().toDouble()
            ,(-230..640).random().toDouble())
    private val reminder2 = ReminderDTO("title", "description", "location", (-430..150).random().toDouble(),
            (-230..640).random().toDouble())

    private lateinit var reminderListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource
    //TODO: provide testing to the RemindersListViewModel and its live data objects
    //to make sure we will run everything in the same thread
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp(){
        fakeDataSource = FakeDataSource(reminderDTOList)
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource())
    }

    @After
    fun tearDown() {
        stopKoin()
    }
    @Config(sdk = [28])
    @Test
    fun check_loading()  = mainCoroutineRule.runBlockingTest{
         reminderDTOList.add(reminder1)
         reminderDTOList.add(reminder2)
        mainCoroutineRule.pauseDispatcher()
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue()).isTrue()
        mainCoroutineRule.resumeDispatcher()
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue()).isFalse()

    }


    @Config(sdk = [28])
    @Test
    fun get_reminders_isEmpty_shouldReturnError() {
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
            FakeDataSource(emptyList<ReminderDTO>().toMutableList()))
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue()).isTrue()

    }


    @Test
    fun get_reminders_returnErrorMessage(){
        fakeDataSource.setShouldReturnError(true)
        reminderListViewModel.loadReminders()
        assertThat(reminderListViewModel.showSnackBar.value).isEqualTo("Error")
    }

}