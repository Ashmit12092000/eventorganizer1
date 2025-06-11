package com.example.eventorganizer.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventorganizer.R;
import com.example.eventorganizer.activities.LoginActivity;
import com.example.eventorganizer.adapters.RegisteredEventsAdapter;
import com.example.eventorganizer.models.Event;
import com.example.eventorganizer.util.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    // View Components
    private ImageView imageViewBackButton;
    private ShapeableImageView imageViewProfileAvatar;
    private TextView textViewUserName, textViewUserStatus, textViewEmail, textViewPhone;
    private RecyclerView recyclerViewHostedEvents;
    private Button buttonLogout;
    private TextView textViewHostedEventsTitle;
    private TextView textViewNoHostedEvents;

    // Data & Adapters
    // Initialize the list here to prevent it from ever being null
    private List<Event> hostedEventsList = new ArrayList<>();
    private RegisteredEventsAdapter hostedEventsAdapter;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        try {
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            initializeViews(view);
            setupRecyclerView();
            setupListeners();
            loadUserProfile();
            loadHostedEvents();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView: ", e);
            Toast.makeText(getContext(), "An error occurred setting up the profile.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void initializeViews(View view) {
        imageViewBackButton = view.findViewById(R.id.imageViewBackButton);
        imageViewProfileAvatar = view.findViewById(R.id.imageViewProfileAvatar);
        textViewUserName = view.findViewById(R.id.textViewUserName);
        textViewUserStatus = view.findViewById(R.id.textViewUserStatus);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        buttonLogout = view.findViewById(R.id.buttonLogout);
        recyclerViewHostedEvents = view.findViewById(R.id.recyclerViewHostedEvents);
        textViewHostedEventsTitle = view.findViewById(R.id.textViewHostedEventsTitle);
        textViewNoHostedEvents = view.findViewById(R.id.textViewNoHostedEvents);
    }

    private void setupRecyclerView() {
        // The list is already initialized, so we just create the adapter
        hostedEventsAdapter = new RegisteredEventsAdapter(hostedEventsList, requireContext());
        recyclerViewHostedEvents.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewHostedEvents.setAdapter(hostedEventsAdapter);
        recyclerViewHostedEvents.setNestedScrollingEnabled(false);
    }

    private void setupListeners() {
        imageViewBackButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        buttonLogout.setOnClickListener(v -> logoutUser());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && isAdded()) {
            textViewEmail.setText(currentUser.getEmail());
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (isAdded() && documentSnapshot.exists()) {
                            textViewUserName.setText(documentSnapshot.getString("fullName"));
                            String avatarUrl = documentSnapshot.getString("avatarUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(requireContext()).load(avatarUrl).into(imageViewProfileAvatar);
                            }
                        }
                    });
            textViewUserStatus.setText("Event Enthusiast • Joined 2025");

        }
    }

    private void loadHostedEvents() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        db.collection("events")
                .whereEqualTo("creatorId", currentUser.getUid())
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isAdded()) {
                        // This call is now safe because the list was initialized
                        hostedEventsList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Event event = document.toObject(Event.class);
                            event.setId(document.getId());
                            hostedEventsList.add(event);
                        }
                        handleEmptyState();
                        hostedEventsAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    if(isAdded()) {
                        Log.e(TAG, "Failed to load hosted events.", e);
                        Toast.makeText(getContext(), "Failed to load hosted events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleEmptyState() {
        if (hostedEventsList.isEmpty()) {
            textViewHostedEventsTitle.setVisibility(View.VISIBLE);
            recyclerViewHostedEvents.setVisibility(View.GONE);
            textViewNoHostedEvents.setVisibility(View.VISIBLE);
        } else {
            textViewHostedEventsTitle.setVisibility(View.VISIBLE);
            recyclerViewHostedEvents.setVisibility(View.VISIBLE);
            textViewNoHostedEvents.setVisibility(View.GONE);
        }
    }

    private void logoutUser() {
        if (getActivity() == null) return;
        SessionManager sessionManager = new SessionManager(requireContext());
        mAuth.signOut();
        sessionManager.clearSession();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }
}