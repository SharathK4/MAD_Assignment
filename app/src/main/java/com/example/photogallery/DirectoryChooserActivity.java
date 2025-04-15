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

public class DirectoryChooserActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DirectoryAdapter adapter;
    private File currentDirectory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_chooser);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Choose Folder");
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Start with the external storage directory
        currentDirectory = Environment.getExternalStorageDirectory();

        // Show directories
        listDirectories(currentDirectory);
    }

    private void listDirectories(File directory) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(directory.getName().isEmpty() ? "Storage" : directory.getName());
        }

        File[] dirs = directory.listFiles(File::isDirectory);
        List<File> directories = new ArrayList<>();

        // Add parent directory option if not at root
        if (directory.getParentFile() != null && !directory.equals(Environment.getExternalStorageDirectory())) {
            directories.add(directory.getParentFile());
        }

        // Add all subdirectories
        if (dirs != null) {
            Arrays.sort(dirs, Comparator.comparing(File::getName));
            directories.addAll(Arrays.asList(dirs));
        }

        adapter = new DirectoryAdapter(directories, directory.getParentFile() != null);
        recyclerView.setAdapter(adapter);
    }

    class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.DirectoryViewHolder> {

        private List<File> directories;
        private boolean hasParent;

        DirectoryAdapter(List<File> directories, boolean hasParent) {
            this.directories = directories;
            this.hasParent = hasParent;
        }

        @NonNull
        @Override
        public DirectoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_directory, parent, false);
            return new DirectoryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DirectoryViewHolder holder, int position) {
            File directory = directories.get(position);

            if (position == 0 && hasParent) {
                holder.directoryName.setText("../ (Parent Directory)");
            } else {
                holder.directoryName.setText(directory.getName());
            }

            holder.itemView.setOnClickListener(v -> {
                if (directory.isDirectory()) {
                    if (directory.canRead()) {
                        currentDirectory = directory;
                        listDirectories(directory);
                    }
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                // Select this directory
                Intent resultIntent = new Intent();
                resultIntent.putExtra("SELECTED_DIRECTORY", directory.getAbsolutePath());
                setResult(RESULT_OK, resultIntent);
                finish();
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return directories.size();
        }

        class DirectoryViewHolder extends RecyclerView.ViewHolder {
            TextView directoryName;

            DirectoryViewHolder(@NonNull View itemView) {
                super(itemView);
                directoryName = itemView.findViewById(R.id.directory_name);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (currentDirectory.getParentFile() != null &&
                !currentDirectory.equals(Environment.getExternalStorageDirectory())) {
            currentDirectory = currentDirectory.getParentFile();
            listDirectories(currentDirectory);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    }
}