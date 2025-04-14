// MainActivity.java
package com.example.googleauthapp;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private View loginLayout;
    private View homeLayout;
    private TextView welcomeText;
    private ImageView profileImage;
    private MaterialCardView profileCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        loginLayout = findViewById(R.id.login_layout);
        homeLayout = findViewById(R.id.home_layout);
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        Button logoutButton = findViewById(R.id.logout_button);
        welcomeText = findViewById(R.id.welcome_text);
        profileImage = findViewById(R.id.profile_image);
        profileCard = findViewById(R.id.profile_card);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
//                .requestProfile()
//                .requestIdToken("YOUR_WEB_CLIENT_ID_HERE")  // Add this line with your actual client ID
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up the sign-in button click listener
        signInButton.setOnClickListener(v -> signIn());

        // Set up the logout button click listener
        logoutButton.setOnClickListener(v -> signOut());

        // Check if the user is already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            updateUI(account);
        }

        // Apply elevation and animation to card
        ViewCompat.setElevation(profileCard, 16f);
    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignInResult(task);
            }
    );

    private void signIn() {
        Log.d(TAG, "Starting sign-in process");

        // Check if Google Play Services are available
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(TAG, "Google Play Services not available: " + resultCode);
            googleApiAvailability.makeGooglePlayServicesAvailable(this);
            return;
        }

        // Print the GSO (Google Sign-In Options) for debugging
        Log.d(TAG, "GSO: " + mGoogleSignInClient.getApiOptions().toString());

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Log.d(TAG, "Launching sign-in intent");
        signInLauncher.launch(signInIntent);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Sign-in successful, update UI
            updateUI(account);
        } catch (ApiException e) {
            // Log more detailed error information
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Log.e(TAG, "Error message: " + e.getMessage());
            Log.e(TAG, "Stack trace: ", e);

            String errorMessage;
            switch (e.getStatusCode()) {
                case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                    errorMessage = "Sign in was cancelled";
                    break;
                case GoogleSignInStatusCodes.NETWORK_ERROR:
                    errorMessage = "Network error occurred. Check your connection";
                    break;
                case GoogleSignInStatusCodes.INVALID_ACCOUNT:
                    errorMessage = "Invalid account selected";
                    break;
                case GoogleSignInStatusCodes.SIGN_IN_REQUIRED:
                    errorMessage = "Sign in required";
                    break;
                case GoogleSignInStatusCodes.SIGN_IN_FAILED:
                    errorMessage = "Sign in failed. Please check your configuration";
                    break;
                default:
                    errorMessage = "Sign in failed (code: " + e.getStatusCode() + "). Please try again later";
            }

            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            // User is signed in
            String displayName = account.getDisplayName();
            welcomeText.setText("Welcome, " + displayName + "!");

            // Animate transition from login to home screen
            loginLayout.setVisibility(View.GONE);
            homeLayout.setVisibility(View.VISIBLE);

            // Circular reveal animation for home layout
            homeLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    homeLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    int centerX = homeLayout.getWidth() / 2;
                    int centerY = homeLayout.getHeight() / 2;
                    float finalRadius = (float) Math.hypot(centerX, centerY);

                    Animator animator = ViewAnimationUtils.createCircularReveal(
                            homeLayout, centerX, centerY, 0, finalRadius);
                    animator.setDuration(800);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.start();

                    // Animate welcome card
                    profileCard.setScaleX(0.7f);
                    profileCard.setScaleY(0.7f);
                    profileCard.setAlpha(0f);
                    profileCard.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .alpha(1f)
                            .setDuration(500)
                            .setStartDelay(300)
                            .start();
                }
            });
        } else {
            // User is signed out
            loginLayout.setVisibility(View.VISIBLE);
            homeLayout.setVisibility(View.GONE);
        }
    }

    private void signOut() {
        // Animate logout
        int centerX = homeLayout.getWidth() / 2;
        int centerY = homeLayout.getHeight() / 2;
        float initialRadius = (float) Math.hypot(centerX, centerY);

        Animator animator = ViewAnimationUtils.createCircularReveal(
                homeLayout, centerX, centerY, initialRadius, 0);
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mGoogleSignInClient.signOut().addOnCompleteListener(MainActivity.this, task -> {
                    // Show login screen
                    loginLayout.setVisibility(View.VISIBLE);
                    homeLayout.setVisibility(View.GONE);

                    // Animate login screen appearance
                    loginLayout.setAlpha(0f);
                    loginLayout.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                });
            }
        });

        // Animate profile card first
        profileCard.animate()
                .scaleX(0.7f)
                .scaleY(0.7f)
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animator.start();
                    }
                })
                .start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in and update UI accordingly
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }
}