package com.example.runningtracker.contentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;

import com.example.runningtracker.model.RunRoomDatabase;
import com.example.runningtracker.model.daos.RunDao;
import com.example.runningtracker.model.entity.Run;

public class RunProvider extends ContentProvider {

    // Defines runDao to perform the database operations
    private RunDao runDao;

    // Defines the database name
    private static final String DBNAME = "run_database";

    // Creates a UriMatcher object.
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // Querying for the entire table
        uriMatcher.addURI(RunProviderContract.AUTHORITY, "run", 1);
        // Querying for a single record
        uriMatcher.addURI(RunProviderContract.AUTHORITY, "run/#", 2);
        uriMatcher.addURI(RunProviderContract.AUTHORITY, "*", 3);
    }

    @Override
    public boolean onCreate() {
        // Creates a new database object.
        RunRoomDatabase database =
                Room.databaseBuilder(this.getContext(), RunRoomDatabase.class, DBNAME).build();
        // Gets a Data Access Object to perform the database operations
        runDao = database.runDao();

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        final Cursor cursor;
        switch(uriMatcher.match(uri)) {
            // If the incoming URI was for all of run
            case 1:
                cursor =  runDao.getCursorRuns();
                break;
            // If the incoming URI was for a single row
            case 2:
                cursor = runDao.getCursorSelectedRun(ContentUris.parseId(uri));
                break;
            default:
                return null;
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        String contentType;

        if (uri.getLastPathSegment() == null) {
            contentType = RunProviderContract.CONTENT_TYPE_MULTIPLE;
        } else {
            contentType = RunProviderContract.CONTENT_TYPE_SINGLE;
        }

        return contentType;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        long id;
        Uri nu;

        // Only available to 1 run insert
        if (uriMatcher.match(uri) == 1) {
            id = runDao.insertCPRun(Run.fromContentValues(contentValues));
            nu = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(nu, null);
            return nu;
        }

        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        int deletedCount = 0;

        switch(uriMatcher.match(uri)) {
            // Delete all run records
            case 1:
                deletedCount = runDao.deleteCPAllRuns();
                getContext().getContentResolver().notifyChange(uri, null);
                break;
                // Delete the 1 run record with the specified id
            case 2:
                deletedCount = runDao.deleteCPRun(Integer.parseInt(uri.getLastPathSegment()));
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                break;
        }
        return deletedCount;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        int updateCount = 0;

        switch(uriMatcher.match(uri)) {
            case 1:
                throw new IllegalArgumentException("Invalid URI, cannot update without a specified ID");
            // Update the specific run
            case 2:
                updateCount = runDao.updateCPRun(Run.fromContentValues(contentValues));
                getContext().getContentResolver().notifyChange(uri, null);
                break;
            default:
                break;
        }

        return updateCount;
    }
}
