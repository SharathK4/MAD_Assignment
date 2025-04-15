package com.example.photogallery;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class FolderPickerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FolderAdapter adapter;
    private File currentDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_picker);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Browse Folders");
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Start with external storage directory
        currentDirectory = Environment.getExternalStorageDirectory();

        // Show directories
        listFolders(currentDirectory);
    }

    private void listFolders(File directory) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(directory.getName().isEmpty() ? "Storage" : directory.getName());
        }

        File[] files = directory.listFiles();
        List<File> directories = new ArrayList<>();

        // Add parent directory option if not at root
        if (directory.getParentFile() != null && !directory.equals(Environment.getExternalStorageDirectory())) {
            directories.add(directory.getParentFile());
        }

        // Add all subdirectories
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                if (file.isDirectory()) {
                    directories.add(file);
                }
            }
        }

        adapter = new FolderAdapter(directories, directory.getParentFile() != null);
        recyclerView.setAdapter(adapter);
    }

    class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

        private List<File> folders;
        private boolean hasParent;

        FolderAdapter(List<File> folders, boolean hasParent) {
            this.folders = folders;
            this.hasParent = hasParent;
        }

        @NonNull
        @Override
        public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_folder, parent, false);
            return new FolderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
            File folder = folders.get(position);

            if (position == 0 && hasParent) {
                holder.folderName.setText("../ (Parent Directory)");
            } else {
                holder.folderName.setText(folder.getName());
            }

            holder.itemView.setOnClickListener(v -> {
                if (folder.isDirectory()) {
                    if (folder.canRead()) {
                        // Check if this folder contains images
                        File[] files = folder.listFiles((dir, name) -> {
                            String lowercase = name.toLowerCase();
                            return lowercase.endsWith(".jpg") || lowercase.endsWith(".jpeg") ||
                                    lowercase.endsWith(".png") || lowercase.endsWith(".gif");
                        });

                        if (files != null && files.length > 0) {
                            // Open gallery for this folder
                            Intent intent = new Intent(FolderPickerActivity.this, GalleryActivity.class);
                            intent.putExtra("FOLDER_PATH", folder.getAbsolutePath());
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        } else {
                            // Navigate to this folder
                            currentDirectory = folder;
                            listFolders(folder);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return folders.size();
        }

        class FolderViewHolder extends RecyclerView.ViewHolder {
            TextView folderName;

            FolderViewHolder(@NonNull View itemView) {
                super(itemView);
                folderName = itemView.findViewById(R.id.folder_name);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (currentDirectory.getParentFile() != null &&
                !currentDirectory.equals(Environment.getExternalStorageDirectory())) {
            currentDirectory = currentDirectory.getParentFile();
            listFolders(currentDirectory);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}
