package com.example.runningtracker.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.databinding.Bindable;
import androidx.lifecycle.LiveData;

import com.example.runningtracker.model.entity.Run;
import com.example.runningtracker.model.repository.MyRepository;

import java.io.ByteArrayOutputStream;

public class RunRecordViewModel extends ObservableViewModel {
    /* Instantiate required variables */
    private String runID;
    private LiveData<Run> currentRun;
    private final MyRepository myRepository;

    public RunRecordViewModel(@NonNull Application application) {
        super(application);

        myRepository = new MyRepository(application);
    }

    // Convert byte[] image format (database format) to Bitmap
    public Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    // Convert drawable into byte[] image format for storing in the database
    public byte[] getImageBytes(Drawable drawable) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    /* Getter & Setter */

    public String getRunID() {
        return runID;
    }

    public void setRunID(String runID) {
        this.runID = runID;
    }

    @Bindable
    public LiveData<Run> getCurrentRun() {
        return currentRun;
    }

    public void setCurrentRun(LiveData<Run> currentRun) {
        this.currentRun = currentRun;
    }

    /* Getter & Setter (Repository) */

    public void update(String runID, String runName, float runRating, String runNote, byte[] runPhoto) {
        myRepository.update(runID, runName, runRating, runNote, runPhoto);
    }

    public void delete(String runID) {
        myRepository.delete(runID);
    }

    public LiveData<Run> getRun(String runID) {
        return myRepository.getRun(runID);
    }
}
