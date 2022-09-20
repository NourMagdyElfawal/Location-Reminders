package com.udacity.project4.locationreminders.reminderslist

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Database
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.FakeDataSource
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.TypeSafeMatcher
import org.junit.After
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

    private lateinit var fakeRepository: FakeDataSource



    @Before
    fun initRepository() {

        fakeRepository = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(getApplicationContext(), fakeRepository)
        stopKoin()

        val myModule = module {
            single {
                reminderListViewModel
            }
        }
        // new koin module
        startKoin {
            modules(listOf(myModule))
        }

    }

    @After
    fun tearUp(){
//        database.close()
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

    @Test
    fun activeTaskDetails_DisplayedInUi() = runBlockingTest {
        // GIVEN - Add active (incomplete) task to the DB
        val activeTask0 = ReminderDTO("title", "description", "location", (-430..150).random().toDouble(),
            (-230..640).random().toDouble())
        val activeTask1 = ReminderDTO("title1", "description1", "location1", (-430..150).random().toDouble(),
            (-230..640).random().toDouble())


        fakeRepository.saveReminder(activeTask0)
        fakeRepository.saveReminder(activeTask1)

        val reminders = (fakeRepository.getReminders() as? Result.Success)?.data
        val reminder0 = reminders!![0]
        val reminder1 = reminders!![1]

        // WHEN - ReminderList fragment launched to display task
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

// for first item
        onView(
            Matchers.allOf(
                withText(reminder0.title),
                childAtPosition(
                    childAtPosition(withId(R.id.reminderCardView), 0), 0), isDisplayed()))
            .check(matches(withText(reminder0.title)))
        onView(
            Matchers.allOf(
                withText(reminder0.description),
                childAtPosition(
                    childAtPosition(withId(R.id.reminderCardView), 0), 1), isDisplayed()))
            .check(matches(withText(reminder0.description)))
        onView(
            Matchers.allOf(
                withText(reminder0.location),
                childAtPosition(
                    childAtPosition(withId(R.id.reminderCardView), 0), 2), isDisplayed()))
            .check(matches(withText(reminder0.location)))


//for second item
        onView(
            Matchers.allOf(
                withText(reminder1.title),
                childAtPosition(
                    childAtPosition(withId(R.id.reminderCardView), 0), 0), isDisplayed()))
            .check(matches(withText(reminder1.title)))
        onView(
            Matchers.allOf(
                withText(reminder1.description),
                childAtPosition(
                    childAtPosition(withId(R.id.reminderCardView), 0), 1), isDisplayed()))
            .check(matches(withText(reminder1.description)))
        onView(
            Matchers.allOf(
                withText(reminder1.location),
                childAtPosition(
                    childAtPosition(withId(R.id.reminderCardView), 0), 2), isDisplayed()))
            .check(matches(withText(reminder1.location)))

    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }

//    TODO: add testing for the error messages (snackbar)



}