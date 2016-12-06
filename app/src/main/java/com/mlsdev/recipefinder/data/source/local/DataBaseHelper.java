package com.mlsdev.recipefinder.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.mlsdev.recipefinder.data.entity.Ingredient;
import com.mlsdev.recipefinder.data.entity.Recipe;
import com.mlsdev.recipefinder.data.entity.stringwrapper.Label;

import java.sql.SQLException;

public class DataBaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "local_data.db";
    private static final int DATABASE_VERSION = 1;
    private Dao<Recipe, String> recipeDao;
    private Dao<Label, Integer> labelDao;
    private Dao<Ingredient, Integer> ingredientDao;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Recipe.class);
            TableUtils.createTable(connectionSource, Label.class);
            TableUtils.createTable(connectionSource, Ingredient.class);
        } catch (SQLException e) {
            Log.e(DataBaseHelper.class.getName(), "Unable to create database", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Recipe.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            Log.e(DataBaseHelper.class.getName(), "Unable to upgrade database from version " + oldVersion + " to new "
                    + newVersion, e);
        }
    }

    public Dao<Recipe, String> getRecipeDao() throws SQLException {
        if (recipeDao == null)
            recipeDao = getDao(Recipe.class);

        return recipeDao;
    }

    public Dao<Label, Integer> getLabelDao() throws SQLException {
        if (labelDao == null)
            labelDao = getDao(Label.class);

        return labelDao;
    }

    public Dao<Ingredient, Integer> getIngredientDao() throws SQLException {
        if (ingredientDao == null)
            ingredientDao = getDao(Ingredient.class);

        return ingredientDao;
    }

    @Override
    public void close() {
        super.close();
        recipeDao = null;
    }
}