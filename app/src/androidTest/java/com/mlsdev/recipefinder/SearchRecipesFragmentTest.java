package com.mlsdev.recipefinder;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;

import com.mlsdev.recipefinder.data.entity.recipe.Recipe;
import com.mlsdev.recipefinder.data.source.remote.RemoteDataSource;
import com.mlsdev.recipefinder.idlingutils.RxIdlingResource;
import com.mlsdev.recipefinder.view.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static android.support.test.espresso.Espresso.registerIdlingResources;

public class SearchRecipesFragmentTest {
    private String searchText = "chicken";
    private String firstRecipeTitle = "Grilled Deviled Chickens Under a Brick";
    private String lastRecipeTitle = "Herb-Roasted Chickens";
    private String firstLoadMoreTitle = "Sour-Orange Yucatán Chickens";
    private int offset = 10;
    public MockWebServer mockWebServer;
    private RxIdlingResource rxIdlingResource;

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws IOException {
        rxIdlingResource = new RxIdlingResource();
        registerIdlingResources(rxIdlingResource);
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        RemoteDataSource.setBaseUrl(mockWebServer.url("/").url().toString());
        Context context = InstrumentationRegistry.getContext();
        mockWebServer.enqueue(new MockResponse().setBody(AssetUtils.getSearchResultJsonData(context)));
        mockWebServer.enqueue(new MockResponse().setBody(AssetUtils.getMoreRecipesJsonData(context)));
    }

    @After
    public void tearDown() throws IOException {
        Espresso.unregisterIdlingResources(rxIdlingResource);
        mockWebServer.shutdown();
    }

    @Test
    public void testStartSearching() {
        Espresso.onView(ViewMatchers.withId(R.id.et_search))
                .perform(ViewActions.typeText(searchText))
                .perform(ViewActions.pressImeActionButton())
                .perform(ViewActions.closeSoftKeyboard());

        Espresso.onView(ViewMatchers.withText(firstRecipeTitle))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.rv_recipe_list))
                .perform(RecyclerViewActions.scrollToPosition(9));

        Espresso.onView(ViewMatchers.withText(lastRecipeTitle))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        // Scroll to top and click upon the first item
        Recipe recipe = AssetUtils.getRecipeEntity(InstrumentationRegistry.getContext());
        Espresso.onView(ViewMatchers.withId(R.id.rv_recipe_list))
                .perform(RecyclerViewActions.scrollToPosition(0));

        Espresso.onView(ViewMatchers.withText(recipe.getLabel()))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.tv_health_labels))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.iv_recipe_image))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.toolbar))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}
