package com.example.runningtracker.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.runningtracker.R;
import com.example.runningtracker.databinding.ActivityRunRecordBinding;
import com.example.runningtracker.viewmodel.RunRecordViewModel;

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
        runRecordViewModel = new ViewModelProvider(this).get(RunRecordViewModel.class);

        activityRunRecordBinding.setLifecycleOwner(this);
        setContentView(activityRunRecordBinding.getRoot());
        activityRunRecordBinding.setViewmodel(runRecordViewModel);


        // Get run ID from intent to display the information of the Run
        Intent intent = getIntent();
        runRecordViewModel.setRunID(intent.getLongExtra(RunActivity.KEY_RUNID, 0));
        // Assign the currentRunData and display data via DataBinding
        runRecordViewModel.setCurrentRun(runRecordViewModel.getRun(runRecordViewModel.getRunID()));


        // Set onClickListener for top app bar
        setSupportActionBar(activityRunRecordBinding.topAppBar);
        activityRunRecordBinding.topAppBar.setNavigationOnClickListener(view -> finish());

        // Set onClickListener for upload image button
        // Open the image storage and let the user to pick an image
        activityRunRecordBinding.buttonUploadImage.setOnClickListener(view -> {
            Intent uploadImage = new Intent();
            uploadImage.setType("image/*");
            uploadImage.setAction(Intent.ACTION_GET_CONTENT);
            uploadImageActivityResultLauncher.launch(Intent.createChooser(uploadImage, "Select Picture"));
        });

        // Set observable for map snapshot and image view
        runRecordViewModel.getRun(runRecordViewModel.getRunID()).observe(this, run -> {
            try {
                if (run.getPhoto() != null)
                    activityRunRecordBinding.imageViewRun.setImageBitmap(runRecordViewModel.getImage(run.getPhoto()));
                if (run.getMapSnapshot() != null)
                    activityRunRecordBinding.imageViewMapSnapshot.setImageBitmap(runRecordViewModel.getImage(run.getMapSnapshot()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Button Save onClickListener: Update and save the information using the UPDATE database query
        activityRunRecordBinding.buttonSaveRunRecord.setOnClickListener(view -> {
            Toast.makeText(this, "Run activity information saved", Toast.LENGTH_SHORT).show();

            long runID = Objects.requireNonNull(runRecordViewModel.getCurrentRun().getValue()).getRun_ID();
            String runName = Objects.requireNonNull(activityRunRecordBinding.editTextRunName.getText()).toString();
            float runRating = activityRunRecordBinding.ratingBarRun.getRating();
            String runNote = Objects.requireNonNull(activityRunRecordBinding.editTextRunNote.getText()).toString();
            byte[] runPhoto = runRecordViewModel.getImageBytes(activityRunRecordBinding.imageViewRun.getDrawable());

            runRecordViewModel.update(runID, runName, runRating, runNote, runPhoto);
        });
    }

    /*
    * Get the selected image URI and convert it into bitmap for image view display.
    * */
    ActivityResultLauncher<Intent> uploadImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {

                    Intent imageIntent = result.getData();
                    //  image intent and its data is not empty
                    if ((imageIntent != null) && (imageIntent.getData() != null)) {
                        Uri selectedImageUri = result.getData().getData();
                        Bitmap selectedImageBitmap = null;
                        try {
                            selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
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

    /*
     * Set action for delete/discard icon in the end of top app bar.
     * When onClick, delete the current run record and finish the activity.
     * */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.deleteRunActivity) {
            String currentActivityName = Objects.requireNonNull(runRecordViewModel.getCurrentRun().getValue()).getName();
            Toast.makeText(this, currentActivityName + " successfully deleted!", Toast.LENGTH_SHORT).show();
            runRecordViewModel.delete(runRecordViewModel.getRunID());
        }
        finish();
        return super.onOptionsItemSelected(item);
    }
}