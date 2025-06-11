package com.example.eventorganizer.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventorganizer.R;
import com.example.eventorganizer.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Initialize Firebase and Session Manager ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(getApplicationContext());

        // --- AUTOMATIC LOGIN LOGIC ---
        // Check if a user is already signed in from a previous session
        if (mAuth.getCurrentUser() != null) {
            // If yes, show a temporary loading screen
            setContentView(R.layout.activity_splash_loading);
            // Fetch their role and proceed directly to the main app
            fetchUserRoleAndProceed(mAuth.getCurrentUser().getUid());
            // Stop executing the rest of onCreate for the login form
            return;
        }

        // --- MANUAL LOGIN UI SETUP ---
        // If no user is signed in, show the normal login screen
        setContentView(R.layout.activity_login);

        // Initialize Views for the login form
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        progressBar = findViewById(R.id.progressBar);

        // Set Click Listeners
        buttonLogin.setOnClickListener(v -> loginUser());
        textViewRegister.setOnClickListener(v -> {
            // Start the registration activity (Test.java)
            startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
        });
        // You can add a listener for forgot password if needed

    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            fetchUserRoleAndProceed(user.getUid());
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication failed. Check your credentials.", Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }

    private void fetchUserRoleAndProceed(String uid) {
        DocumentReference docRef = db.collection("users").document(uid);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                sessionManager.setUserRole(role); // Store the user's role

                // Navigate to the main activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Close LoginActivity so the user can't navigate back to it
            } else {
                // This is a rare case where a user exists in Auth but not in Firestore
                Toast.makeText(this, "User data not found. Please log in again.", Toast.LENGTH_LONG).show();
                mAuth.signOut(); // Log out the inconsistent user
                setLoading(false);
                // Reload the login screen if it was bypassed
                setContentView(R.layout.activity_login);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch user role. Please try again.", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            setLoading(false);
            setContentView(R.layout.activity_login);
        });
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (buttonLogin != null) {
            buttonLogin.setEnabled(!isLoading);
        }
    }
}
