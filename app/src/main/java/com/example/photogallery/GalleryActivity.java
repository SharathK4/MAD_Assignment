package com.example.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GalleryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyGallery;
    private String folderPath;
    private List<File> imageFiles = new ArrayList<>();
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gallery");
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyGallery = findViewById(R.id.tvEmptyGallery);

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns

        folderPath = getIntent().getStringExtra("FOLDER_PATH");

        if (folderPath != null) {
            File folder = new File(folderPath);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(folder.getName());
            }

            // Load the images in background
            loadImages(folder);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (folderPath != null) {
            loadImages(new File(folderPath));
        }
    }

    private void loadImages(File folder) {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyGallery.setVisibility(View.GONE);

        executorService.execute(() -> {
            imageFiles.clear();
            try {
                // Fetch only image files
                File[] files = folder.listFiles((dir, name) -> {
                    String lowercase = name.toLowerCase();
                    return lowercase.endsWith(".jpg") || lowercase.endsWith(".jpeg") ||
                            lowercase.endsWith(".png") || lowercase.endsWith(".gif");
                });

                if (files != null && files.length > 0) {
                    Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
                    imageFiles.addAll(Arrays.asList(files));
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(GalleryActivity.this,
                            "Error accessing files: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (imageFiles.isEmpty()) {
                    tvEmptyGallery.setVisibility(View.VISIBLE);
                } else {
                    adapter = new GalleryAdapter(imageFiles);
                    recyclerView.setAdapter(adapter);
                }
            });
        });
    }

    class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

        private List<File> images;

        GalleryAdapter(List<File> images) {
            this.images = images;
        }

        @NonNull
        @Override
        public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_image, parent, false);
            return new GalleryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
            File imageFile = images.get(position);

            executorService.execute(() -> {
                Bitmap thumbnail = getThumbnail(imageFile.getAbsolutePath());
                runOnUiThread(() -> {
                    if (thumbnail != null) {
                        holder.imageView.setImageBitmap(thumbnail);
                    } else {
                        holder.imageView.setImageResource(R.drawable.ic_image); // fallback image
                    }
                });
            });

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(GalleryActivity.this, ImageDetailsActivity.class);
                intent.putExtra("IMAGE_PATH", imageFile.getAbsolutePath());
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        class GalleryViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            GalleryViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.imageView);
            }
        }

        private Bitmap getThumbnail(String imagePath) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, options);

                options.inSampleSize = calculateInSampleSize(options, 200, 200);
                options.inJustDecodeBounds = false;
                return BitmapFactory.decodeFile(imagePath, options);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            int height = options.outHeight;
            int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                int halfHeight = height / 2;
                int halfWidth = width / 2;

                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
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
