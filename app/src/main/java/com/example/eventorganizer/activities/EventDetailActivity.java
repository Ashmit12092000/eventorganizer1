package com.example.eventorganizer.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventorganizer.R;
import com.example.eventorganizer.adapters.CommentAdapter;
import com.example.eventorganizer.models.Comment;
import com.example.eventorganizer.models.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private String currentUserFullName;
    private String currentUserAvatarUrl;
    private ImageView imageViewEventPoster;
    private TextView textViewDetailTitle, textViewDetailDate, textViewDetailDescription;
    private RecyclerView recyclerViewComments;
    private EditText editTextComment;
    private ImageButton buttonPostComment;

    // Adapters and Data
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    // State Variables
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        // --- Initialize Firebase and State ---
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        eventId = getIntent().getStringExtra("EVENT_ID");

        // Safety check: if no eventId is passed, close the activity
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Error: Event not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- Setup Toolbar with Back Arrow ---
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false); // We'll set the title later
        }

        // --- Initialize Views ---
        imageViewEventPoster = findViewById(R.id.imageViewEventPoster);
        textViewDetailTitle = findViewById(R.id.textViewDetailTitle);
        textViewDetailDate = findViewById(R.id.textViewDetailDate);
        textViewDetailDescription = findViewById(R.id.textViewDetailDescription);
        recyclerViewComments = findViewById(R.id.recyclerViewComments);
        editTextComment = findViewById(R.id.editTextComment);
        buttonPostComment = findViewById(R.id.buttonPostComment);

        // --- Setup RecyclerView ---
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList, this, eventId);
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewComments.setAdapter(commentAdapter);

        // --- Load Data from Firestore ---
        fetchCurrentUserDetails();
        loadEventDetails();
        loadComments();

        // --- Set Click Listeners ---
        buttonPostComment.setOnClickListener(v -> addComment());
    }

    // Handle clicks on the toolbar (specifically the back arrow)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // This is the standard behavior for the back arrow
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Fetches the logged-in user's full name to attach to new comments
    private void fetchCurrentUserDetails() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            DocumentReference userRef = db.collection("users").document(user.getUid());
            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    currentUserFullName = documentSnapshot.getString("fullName");
                    currentUserAvatarUrl = documentSnapshot.getString("avatarUrl");
                }
            });
        }
    }

    // Fetches the main event details from Firestore
    private void loadEventDetails() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            // Populate the UI with event data
                            toolbar.setTitle(event.getTitle()); // Set title on collapsing toolbar
                            textViewDetailTitle.setText(event.getTitle());
                            textViewDetailDate.setText(event.getDate());
                            textViewDetailDescription.setText(event.getDescription());

                            // Load the poster image using Glide library
                            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                                Glide.with(this)
                                        .load(event.getImageUrl())
                                        .placeholder(R.drawable.ic_launcher_background) // Add a placeholder image
                                        .error(R.drawable.ic_launcher_background)       // Image to show on error
                                        .into(imageViewEventPoster);
                            }
                        }
                    }
                });
    }

    // Fetches the list of comments for this event
    private void loadComments() {
        db.collection("events").document(eventId).collection("comments")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Show newest comments first
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        commentList.clear();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : task.getResult()) {
                            Comment comment = document.toObject(Comment.class);
                            comment.setId(document.getId());
                            commentList.add(comment);
                        }
                        commentAdapter.notifyDataSetChanged(); // Refresh the list
                    }
                });
    }

    // Saves a new comment to Firestore
    private void addComment() {
        String commentText = editTextComment.getText().toString().trim();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Comment cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null || currentUserFullName == null) {
            Toast.makeText(this, "You must be logged in to comment.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new comment object
        Comment comment = new Comment();
        comment.setText(commentText);
        comment.setCreatorId(currentUser.getUid());
        comment.setCreatorName(currentUserFullName);
        comment.setCreatorAvatarUrl(currentUserAvatarUrl);
        // The timestamp is set automatically by Firestore via the @ServerTimestamp annotation

        db.collection("events").document(eventId).collection("comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Comment posted.", Toast.LENGTH_SHORT).show();
                    editTextComment.setText(""); // Clear the input field
                    loadComments(); // Refresh the comments list to show the new one
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to post comment.", Toast.LENGTH_SHORT).show());
    }
}