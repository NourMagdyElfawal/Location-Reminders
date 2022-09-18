package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    //TODO: provide testing to the SaveReminderView and its live data objects

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp(){
        saveReminderViewModel =
            SaveReminderViewModel(ApplicationProvider.getApplicationContext(),FakeDataSource())
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Config(sdk = [28])
    @Test
    fun check_loading_Returns_True () {
        // Given a fresh SaveReminderViewModelTest
        //to stop coroutine we will use pauseDispatcher because we want to check loading only
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(
                    ReminderDataItem(
                        "title",
                        "description",
                        "location",
                        (-430..350).random().toDouble(),
                        (-230..640).random().toDouble()
                    )
                )

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))

    }



    @Config(sdk = [28])
    @Test
    fun empty_location_returns_false () {
        val result=saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                "title",
                "description",
                "",
                (-430..350).random().toDouble(),
                (-230..640).random().toDouble()
            )
        )
        assertThat(result, CoreMatchers.`is`(false))
    }
    @Config(sdk = [28])
    @Test
    fun  empty_title_returns_false () {
        val result=saveReminderViewModel.validateEnteredData(
            ReminderDataItem(
                "",
                "description",
                "location",(-430..350).random().toDouble(),(-230..640).random().toDouble()
            )
        )
        assertThat(result, CoreMatchers.`is`(false))
    }



}