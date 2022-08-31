package com.example.notekeeper;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NoteCreationTest {
    static DataManager dataManager;

    @BeforeClass
    public static void classSetUp() {
        dataManager = DataManager.getInstance();
    }


    @Rule
    public ActivityScenarioRule<NoteListActivity> noteListActivityActivityTestRule =
            new ActivityScenarioRule<>(NoteListActivity.class);

    @Test
    public void createNewNote() {
        final CourseInfo course = dataManager.getCourse("java_lang");
        final String noteTitle = "Test note title";
        final String noteText = "This is the body of our test note";

        onView(withId(R.id.fabAdd)).perform(click());

        onView(withId(R.id.spinnerCourses)).perform(click());
        onData(allOf(instanceOf(CourseInfo.class), equalTo(course)))
                .perform(click());

        onView(withId(R.id.textNoteTitle))
                .perform(typeText(noteTitle));
        onView(withId(R.id.textNoteText))
                .perform(typeText(noteText), closeSoftKeyboard());

        pressBack();

        int noteIndex = dataManager.getNotes().size() - 1;
        NoteInfo note = dataManager.getNotes().get(noteIndex);
        assertEquals(course, note.getCourse());
        assertEquals(noteTitle, note.getTitle());
        assertEquals(noteText, note.getText());
    }
}