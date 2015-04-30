package com.tealium.kitchensink.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.widget.ArrayAdapter;

import com.tealium.kitchensink.R;

public final class Model extends SQLiteOpenHelper {

    public static final String DEFAULT_ACCOUNT = "tealiummobile";
    public static final String DEFAULT_PROFILE = "demo";
    public static final String DEFAULT_ENV = "dev";

    private static final String TBL_ACCOUNT = "account";
    private static final String TBL_PROFILE = "profile";
    private static final String TBL_ENV = "environment";
    private static final String TBL_FIRSTNAME = "firstname";

    private static final String COL_ACCT_NAME = "name";
    private static final String COL_ACCT_CREATED = "created_ms";
    private static final String COL_PROF_NAME = "name";
    private static final String COL_PROF_CREATED = "created_ms";
    private static final String COL_ENV_NAME = "name";
    private static final String COL_ENV_CREATED = "created_ms";
    private static final String COL_FIRSTNAME_NAME = "name";
    private static final String COL_FIRSTNAME_CREATED = "created_ms";

    private static final String KEY_ACCOUNT_NAME = "account_name";
    private static final String KEY_PROFILE_NAME = "profile_name";
    private static final String KEY_ENV_NAME = "environment_name";
    private static final String KEY_BACKGROUND_COLOR = "background_color";
    private static final String KEY_FIRSTNAME = "firstname";

    private final Context context;
    private final SharedPreferences sharedPreferences;

    public Model(Context context) {
        super(context, "kitchensink_model.db", null, 1);
        this.context = context.getApplicationContext();
        this.sharedPreferences = this.context.getSharedPreferences("kitchensink_model.sp", 0);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String query : this.context.getResources().getStringArray(R.array.query_create_db)) {
            db.execSQL(query);
        }

        final long created = System.currentTimeMillis();

        ContentValues contentValues = new ContentValues(2);
        contentValues.put(COL_ENV_NAME, "dev");
        contentValues.put(COL_ENV_CREATED, created);
        db.insertWithOnConflict(TBL_ENV, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);

        contentValues.put(COL_ENV_NAME, "qa");
        db.insertWithOnConflict(TBL_ENV, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);

        contentValues.put(COL_ENV_NAME, "prod");
        db.insertWithOnConflict(TBL_ENV, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);

        contentValues = new ContentValues(2);
        contentValues.put(COL_FIRSTNAME_CREATED, created);

        for (String name : this.context.getResources().getStringArray(R.array.firstname_names)) {
            contentValues.put(COL_FIRSTNAME_NAME, name);
            db.insertWithOnConflict(TBL_FIRSTNAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        }

        this.setConfig(db, DEFAULT_ACCOUNT, DEFAULT_PROFILE, DEFAULT_ENV);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void setConfig(String accountName, String profileName, String envName) {
        if (accountName == null || profileName == null || envName == null) {
            throw new IllegalArgumentException("accountName, profileName, and environmentName must not be null.");
        }

        SQLiteDatabase db = this.getWritableDatabase();
        this.setConfig(db, accountName, profileName, envName);
        db.close();
    }

    private void setConfig(SQLiteDatabase openedDB, String accountName, String profileName, String envName) {

        final long createdTS = System.currentTimeMillis();

        ContentValues cv = new ContentValues(2);
        cv.put(COL_ACCT_NAME, accountName);
        cv.put(COL_ACCT_CREATED, createdTS);
        openedDB.insertWithOnConflict(TBL_ACCOUNT, null, cv, SQLiteDatabase.CONFLICT_IGNORE);

        cv = new ContentValues(2);
        cv.put(COL_PROF_NAME, profileName);
        cv.put(COL_PROF_CREATED, createdTS);
        openedDB.insertWithOnConflict(TBL_PROFILE, null, cv, SQLiteDatabase.CONFLICT_IGNORE);

        cv = new ContentValues(2);
        cv.put(COL_ENV_NAME, envName);
        cv.put(COL_ENV_CREATED, createdTS);
        openedDB.insertWithOnConflict(TBL_ENV, null, cv, SQLiteDatabase.CONFLICT_IGNORE);

        this.sharedPreferences.edit()
                .putString(KEY_ACCOUNT_NAME, accountName)
                .putString(KEY_PROFILE_NAME, profileName)
                .putString(KEY_ENV_NAME, envName)
                .apply();
    }

    public boolean setDefaultFirstName(String name) {

        if (name == null || name.length() == 0) {
            return true;
        }

        ContentValues contentValues = new ContentValues(2);
        contentValues.put(COL_FIRSTNAME_NAME, name);
        contentValues.put(COL_FIRSTNAME_CREATED, System.currentTimeMillis());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insertWithOnConflict(TBL_FIRSTNAME, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();

        this.sharedPreferences.edit().putString(KEY_FIRSTNAME, name).commit();
        return true;
    }

    public String getAccountName() {
        return this.sharedPreferences.getString(KEY_ACCOUNT_NAME, DEFAULT_ACCOUNT);
    }

    public String getProfileName() {
        return this.sharedPreferences.getString(KEY_PROFILE_NAME, DEFAULT_PROFILE);
    }

    public String getEnvironmentName() {
        return this.sharedPreferences.getString(KEY_ENV_NAME, DEFAULT_ENV);
    }

    public String getCurrentFirstName() {
        return this.sharedPreferences.getString(
                KEY_FIRSTNAME,
                this.context.getResources().getString(R.string.firstname_default));
    }

    public int getBackgroundColor() {
        return this.sharedPreferences.getInt(KEY_BACKGROUND_COLOR, Color.WHITE);
    }

    public ArrayAdapter<String> getAccountAdapter() {
        return new ArrayAdapter<>(
                this.context,
                R.layout.cell_tealium_setting_autocomplete_value,
                this.extractNames(R.string.query_select_account));
    }

    public ArrayAdapter<String> getProfileAdapter() {
        return new ArrayAdapter<>(
                this.context,
                R.layout.cell_tealium_setting_autocomplete_value,
                this.extractNames(R.string.query_select_profile));
    }

    public ArrayAdapter<String> getEnvironmentAdapter() {
        return new ArrayAdapter<>(
                this.context,
                R.layout.cell_tealium_setting_autocomplete_value,
                this.extractNames(R.string.query_select_env));
    }

    public ArrayAdapter<String> getFirstNameAdapter() {
        return new ArrayAdapter<>(
                this.context,
                R.layout.cell_tealium_setting_autocomplete_value,
                this.extractNames(R.string.query_select_firstname));
    }


    public void setBackgroundColor(int color) {
        this.sharedPreferences.edit().putInt(KEY_BACKGROUND_COLOR, color).commit();
    }


    private String[] extractNames(int queryStringId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(this.context.getString(queryStringId), null);
        c.moveToFirst();

        int i = 0;
        String[] names = new String[c.getCount()];
        while (!c.isAfterLast()) {
            names[i++] = c.getString(0);
            c.moveToNext();
        }
        c.close();
        db.close();

        return names;
    }
}

