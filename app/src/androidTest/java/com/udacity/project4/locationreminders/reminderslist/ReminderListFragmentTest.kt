package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {


//    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderListViewModel: RemindersListViewModel

    @Before
    fun setup() {
//        fakeDataSource = FakeDataSource()
//        reminderListViewModel =
//            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
//        stopKoin()
//
//        val myModule = module {
//            single {
//                reminderListViewModel
//            }
//        }
//        // new koin module
//        startKoin {
//            modules(listOf(myModule))
//        }
    }

//    TODO: test the navigation of the fragments.
@Test
fun testNavigationToSaveReminderFragment() {
    // Create a mock NavController object which has all navController function without any  implementation
    val mockNavController = mock(NavController::class.java)

     launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
    .onFragment {
         Navigation.setViewNavController(it.view!!, mockNavController)
    }
    // Verify that performing a click prompts the correct Navigation action
    onView(withId(R.id.addReminderFAB)).perform(click())
    verify(mockNavController).navigate(
        ReminderListFragmentDirections.toSaveReminder())
}



//    TODO: test the displayed data on the UI.



//    TODO: add testing for the error messages.
}