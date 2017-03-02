package com.mlsdev.recipefinder.data.source.repository;

import android.content.Context;
import android.support.annotation.NonNull;

import com.mlsdev.recipefinder.data.entity.nutrition.NutritionAnalysisResult;
import com.mlsdev.recipefinder.data.entity.nutrition.RecipeAnalysisParams;
import com.mlsdev.recipefinder.data.entity.recipe.Hit;
import com.mlsdev.recipefinder.data.entity.recipe.Recipe;
import com.mlsdev.recipefinder.data.entity.recipe.SearchResult;
import com.mlsdev.recipefinder.data.source.DataSource;
import com.mlsdev.recipefinder.data.source.local.LocalDataSource;
import com.mlsdev.recipefinder.data.source.remote.ParameterKeys;
import com.mlsdev.recipefinder.data.source.remote.RemoteDataSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class DataRepository {
    private final int offset = 10;
    private int from = 0;
    private int to = offset;
    private boolean more = true;

    private static DataRepository instance;
    private DataSource localDataSource;

    private List<Recipe> cachedRecipes;
    private boolean cacheIsDirty = false;

    private DataRepository(Context context) {
        localDataSource = new LocalDataSource(context);
        cachedRecipes = new ArrayList<>();
    }

    public static DataRepository getInstance(Context context) {
        if (instance == null)
            instance = new DataRepository(context);

        return instance;
    }

    public void setCacheIsDirty() {
        cacheIsDirty = true;
    }

    public Observable<List<Recipe>> searchRecipes(Map<String, String> params) {
        if (!cacheIsDirty) {
            return Observable.from(cachedRecipes).toList();
        }

        more = true;
        cachedRecipes.clear();

        params.put(ParameterKeys.FROM, String.valueOf(0));
        params.put(ParameterKeys.TO, String.valueOf(offset));

        return getRecipes(params);
    }

    public Observable<List<Recipe>> loadMore(Map<String, String> params) {

        if (!more) {
            return Observable.from(new ArrayList<Recipe>()).toList();
        }

        params.put(ParameterKeys.FROM, String.valueOf(from));
        params.put(ParameterKeys.TO, String.valueOf(to));

        return getRecipes(params);
    }

    @NonNull
    private Observable<List<Recipe>> getRecipes(Map<String, String> params) {
        return RemoteDataSource.getInstance().searchRecipes(params)
                .map(new Func1<SearchResult, List<Recipe>>() {
                    @Override
                    public List<Recipe> call(SearchResult searchResult) {
                        from = searchResult.getTo();
                        to = from + offset;
                        more = searchResult.isMore();

                        List<Recipe> recipes = new ArrayList<>();
                        for (Hit hit : searchResult.getHits())
                            recipes.add(hit.getRecipe());

                        cachedRecipes.addAll(recipes);

                        return recipes;
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        cacheIsDirty = false;
                    }
                });
    }

    public Observable<List<Recipe>> getFavoriteRecipes() {
        return localDataSource.getFavorites();
    }

    public Observable<Boolean> addToFavorites(Recipe favoriteRecipe) {
        return localDataSource.addToFavorites(favoriteRecipe);
    }

    public Observable<Boolean> removeFromFavorites(Recipe removedRecipe) {
        return localDataSource.removeFromFavorites(removedRecipe);
    }

    public Observable<Boolean> isInFavorites(Recipe recipe) {
        return localDataSource.isInFavorites(recipe);
    }

    public Observable<NutritionAnalysisResult> getIngredientData(final Map<String, String> params) {
        return localDataSource.getIngredientData(params)
                .flatMap(new Func1<NutritionAnalysisResult, Observable<NutritionAnalysisResult>>() {
                    @Override
                    public Observable<NutritionAnalysisResult> call(NutritionAnalysisResult analysisResult) {
                        if (analysisResult == null)
                            return RemoteDataSource.getInstance().getIngredientData(params)
                                    .doOnNext(new Action1<NutritionAnalysisResult>() {
                                        @Override
                                        public void call(NutritionAnalysisResult analysisResult) {
                                            localDataSource.addAnalyzingResult(analysisResult, params);
                                        }
                                    });
                        else
                            return Observable.from(new NutritionAnalysisResult[]{analysisResult});

                    }
                });
    }

    public Observable<NutritionAnalysisResult> getRecipeAnalysisData(final RecipeAnalysisParams params){
        return RemoteDataSource.getInstance().getRecipeAnalysingResult(params);
    }

}
