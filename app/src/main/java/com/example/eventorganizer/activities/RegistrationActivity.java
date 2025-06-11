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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class RegistrationActivity 

 extends AppCompatActivity {

    private TextInputEditText editTextFullName, editTextEmail, editTextPassword;
    private Button buttonRegister;
    private TextView textViewLoginLink;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This class will use the modern Material Design layout.
       
        setContentView(R.layout.activity_registration);

        // --- Toolbar Setup ---
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable and handle back arrow navigation
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // --- Initialize Firebase ---
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- Initialize Views ---
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLoginLink = findViewById(R.id.textViewLoginLink);
        progressBar = findViewById(R.id.progressBar);

        // --- Set Click Listeners ---
        buttonRegister.setOnClickListener(v -> registerUser());
        textViewLoginLink.setOnClickListener(v -> {
            // Finish this activity and go back to the previous one (LoginActivity)
            Intent intent = new Intent(RegistrationActivity

                    .this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    // This method handles the click on the toolbar's back arrow
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void registerUser() {
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        setLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            createUserDocument(firebaseUser, fullName);
                        }
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        setLoading(false);
                    }
                });
    }
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonRegister.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonRegister.setEnabled(true);
        }
    }


    private void createUserDocument(FirebaseUser firebaseUser, String fullName) {
        String email = firebaseUser.getEmail();
        String uid = firebaseUser.getUid();
        String[] avatarUrls = getResources().getStringArray(R.array.default_avatar_urls);
        int randomIndex = new Random().nextInt(avatarUrls.length);
        String randomAvatarUrl = avatarUrls[randomIndex];

        Map<String, Object> user = new HashMap<>();
        user.put("fullName", fullName);
        user.put("email", email);
        user.put("role", "user");
        user.put("avatarUrl", randomAvatarUrl);

        db.collection("users").document(uid)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegistrationActivity 

.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    mAuth.signOut();
                    Intent intent = new Intent(RegistrationActivity 

.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(RegistrationActivity 

.this, "Error saving user data.", Toast.LENGTH_SHORT).show();
                });
    }
}
