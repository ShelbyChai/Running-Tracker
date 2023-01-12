package com.example.runningtracker.model;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.runningtracker.model.daos.RunDao;
import com.example.runningtracker.model.entity.Run;

import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Database scheme version number, incrementing this number will enable us to trigger destruction
// or migration of the database.
@Database(entities = {Run.class}, version = 1, exportSchema = false)
public abstract class RunRoomDatabase extends RoomDatabase {
    /*
     * Implement the database as singleton to prevent multiple instances of the database from
     * being opened at the same time. Unlike a service, where the singleton is managed for us
     * , we will have to implement the singleton pattern ourselves in the database class.
     * */
    private static volatile RunRoomDatabase instance;

    /*
     * Queries are not permitted on the main thread as these could be "long running" queries.
     * Solution:
     * 1. Uncomment allowMainThreadQueries() in the database building call.
     * 2. Specify an Executor based thread pool in the database class.*/
    public static RunRoomDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (RunRoomDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), RunRoomDatabase.class, "run_database").fallbackToDestructiveMigration()
//                            .addCallback(createCallback)
                            // This is to migrate the database when the version is incremented
                            // else an error would occured
                            .fallbackToDestructiveMigration()
                            //allowMainThreadQueries()
                            .build();
                }
            }
        }
        return instance;
    }

    // Specify an Executor based thread pool
    private static final int threadCount = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(threadCount);

    // If using a thread pool then all database queries need to be made using this pool
    // CatRoomDatabase.databaseWriteExecutor.execute(() -> {});

    /*
     * Pre-populate the database by adding some entities when the database in created using callback.
     * This function is only called when the database is first created!
     * */
//    private static final RoomDatabase.Callback createCallback = new RoomDatabase.Callback() {
//        @Override
//        public void onCreate(@NonNull SupportSQLiteDatabase db) {
//            super.onCreate(db);
//
//            Log.d("comp3018", "RoomDatabase onCreate");
//            databaseWriteExecutor.execute(() -> {
//                RunDao runDao = instance.runDao();
//
//                String uniqueRunID = String.valueOf(Calendar.getInstance().getTime());
//
//                Run run = new Run(uniqueRunID, "Activity 1", uniqueRunID, 100, 1000, 10, 60);
//                runDao.insert(run);
//            });
//        }
//    };

    // Abstract method to retrieve each of the Dao objects
    public abstract RunDao runDao();
}
