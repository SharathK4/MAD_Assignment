package com.example.photogallery;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageDetailsActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView tvImageName, tvImagePath, tvImageSize, tvImageDate;
    private Button btnDeleteImage;
    private ProgressBar progressBar;
    private String imagePath;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Image Details");
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        imageView = findViewById(R.id.imageView);
        tvImageName = findViewById(R.id.tvImageName);
        tvImagePath = findViewById(R.id.tvImagePath);
        tvImageSize = findViewById(R.id.tvImageSize);
        tvImageDate = findViewById(R.id.tvImageDate);
        btnDeleteImage = findViewById(R.id.btnDeleteImage);
        progressBar = findViewById(R.id.progressBar);

        // Create a single thread executor
        executorService = Executors.newSingleThreadExecutor();

        // Get the image path from the intent
        imagePath = getIntent().getStringExtra("IMAGE_PATH");
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                displayImageDetails(imageFile);
                loadImageInBackground(imageFile);
            }
        }

        btnDeleteImage.setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });
    }

    private void displayImageDetails(File imageFile) {
        // Display image name
        tvImageName.setText("Name: " + imageFile.getName());

        // Display image path
        tvImagePath.setText("Path: " + imageFile.getAbsolutePath());

        // Display image size
        long fileSizeInBytes = imageFile.length();
        String size;
        if (fileSizeInBytes > 1024 * 1024) {
            // Continuing from where we left off...

// ImageDetailsActivity.java (continued)
            size = String.format(Locale.getDefault(), "%.2f MB", fileSizeInBytes / (1024.0 * 1024.0));
        } else if (fileSizeInBytes > 1024) {
            size = String.format(Locale.getDefault(), "%.2f KB", fileSizeInBytes / 1024.0);
        } else {
            size = fileSizeInBytes + " B";
        }
        tvImageSize.setText("Size: " + size);

        // Display image date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String dateCreated = sdf.format(new Date(imageFile.lastModified()));
        tvImageDate.setText("Date: " + dateCreated);
    }

    private void loadImageInBackground(File imageFile) {
        progressBar.setVisibility(View.VISIBLE);

        executorService.execute(() -> {
            try {
                // Get the dimensions of the View
                int targetW = imageView.getWidth();
                int targetH = imageView.getHeight();

                // If width is not available yet, use screen width as fallback
                if (targetW == 0) {
                    targetW = getResources().getDisplayMetrics().widthPixels;
                    targetH = getResources().getDisplayMetrics().heightPixels;
                }

                // Get the dimensions of the bitmap
                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);

                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;

                // Determine how much to scale down the image
                int scaleFactor = Math.max(1, Math.min(photoW / targetW, photoH / targetH));

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inSampleSize = scaleFactor;

                final Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);

                runOnUiThread(() -> {
                    imageView.setImageBitmap(bitmap);
                    progressBar.setVisibility(View.GONE);

                    // Add fade-in animation
                    imageView.setAlpha(0f);
                    imageView.animate().alpha(1f).setDuration(500).start();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ImageDetailsActivity.this,
                            "Error loading image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteImage();
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteImage() {
        File imageFile = new File(imagePath);
        if (imageFile.exists()) {
            if (imageFile.delete()) {
                Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();

                // Go back to gallery
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                Toast.makeText(this, "Failed to delete image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
