package com.example.runningtracker.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.runningtracker.R;
import com.example.runningtracker.databinding.ActivityRunRecordBinding;
import com.example.runningtracker.viewmodel.RunRecordViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

public class RunRecordActivity extends AppCompatActivity {
    private RunRecordViewModel runRecordViewModel;
    ActivityRunRecordBinding activityRunRecordBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get viewModel and bind layout views to architecutre component
        activityRunRecordBinding = ActivityRunRecordBinding.inflate(LayoutInflater.from(this));
        runRecordViewModel = new ViewModelProvider(this,
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.
                        getInstance(this.getApplication())).get(RunRecordViewModel.class);
        activityRunRecordBinding.setLifecycleOwner(this);
        setContentView(activityRunRecordBinding.getRoot());
        activityRunRecordBinding.setViewmodel(runRecordViewModel);


        // Get run ID from intent to display the information of the Run
        Intent intent = getIntent();
        runRecordViewModel.setRunID(intent.getStringExtra(RunActivity.KEY_RUNID));
        // Assign the currentRunData and display data via DataBinding
        runRecordViewModel.setCurrentRun(runRecordViewModel.getRun(runRecordViewModel.getRunID()));


        // Set onClickListener for top app bar
        setSupportActionBar(activityRunRecordBinding.topAppBar);
        activityRunRecordBinding.topAppBar.setNavigationOnClickListener(view -> {
            finish();
        });

        // Set placeholder image if image view is null
        if (activityRunRecordBinding.imageViewRun.getDrawable() == null) {
            activityRunRecordBinding.imageViewRun.setImageResource(R.drawable.image_placeholder);
        }

        // Set onClickListener for upload image button
        activityRunRecordBinding.buttonUploadImage.setOnClickListener(view -> {
            Intent uploadImage = new Intent();
            uploadImage.setType("image/*");
            uploadImage.setAction(Intent.ACTION_GET_CONTENT);
            uploadImageActivityResultLauncher.launch(Intent.createChooser(uploadImage, "Select Picture"));
        });

        // Button Save onClickListener
        activityRunRecordBinding.buttonSaveRunRecord.setOnClickListener(view -> {
            String runID = Objects.requireNonNull(runRecordViewModel.getCurrentRun().getValue()).getRunID();
            String runName = Objects.requireNonNull(activityRunRecordBinding.editTextRunName.getText()).toString();
            float runRating = activityRunRecordBinding.ratingBarRun.getRating();
            String runNote = Objects.requireNonNull(activityRunRecordBinding.editTextRunNote.getText()).toString();

            BitmapDrawable bitmapDrawable = ((BitmapDrawable) activityRunRecordBinding.imageViewRun.getDrawable());
            Bitmap bitmap = bitmapDrawable .getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] runPhoto = stream.toByteArray();

            runRecordViewModel.update(runID, runName, runRating, runNote, runPhoto);
        });
    }

    ActivityResultLauncher<Intent> uploadImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Log.d("comp3018", "Image selected!");

                    Intent imageIntent = result.getData();
                    //  image intent and its data is not empty
                    if ((imageIntent != null) && (imageIntent.getData() != null)) {
                        Uri selectedImageUri = result.getData().getData();
                        Bitmap selectedImageBitmap = null;
                        try {
                            selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    selectedImageUri);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        activityRunRecordBinding.imageViewRun.setImageBitmap(selectedImageBitmap);
                    }
                }
            });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.top_app_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.deleteRunActivity) {
            Toast.makeText(this, "Delete", Toast.LENGTH_SHORT).show();
            runRecordViewModel.delete(Objects.requireNonNull(runRecordViewModel.getCurrentRun().getValue()).getRunID());
        }
        finish();
        return super.onOptionsItemSelected(item);
    }
}