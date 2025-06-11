package com.example.eventorganizer.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventorganizer.R;
import com.example.eventorganizer.activities.AddEditEventActivity;
import com.example.eventorganizer.activities.AdminManageActivity;
import com.example.eventorganizer.adapters.UpcomingEventsAdapter;
import com.example.eventorganizer.models.Event;
import com.example.eventorganizer.util.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewUpcomingEvents;
    private UpcomingEventsAdapter upcomingEventsAdapter;
    private TextView textViewUpcomingEventsTitle;
    private TextView textViewNoUpcomingEvents;
    private List<Event> upcomingEventList;
    private LinearLayout layoutManageEvents;
    private SessionManager sessionManager;
    private FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // --- Initialize ---
        db = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(requireContext());
        upcomingEventList = new ArrayList<>();

        // --- Setup Toolbar ---
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> Toast.makeText(getActivity(), "Navigation Drawer not implemented", Toast.LENGTH_SHORT).show());
        textViewUpcomingEventsTitle = view.findViewById(R.id.textViewUpcomingEventsTitle);
        textViewNoUpcomingEvents = view.findViewById(R.id.textViewNoUpcomingEvents);
        // --- Admin-only Section Visibility ---
        layoutManageEvents = view.findViewById(R.id.layoutManageEvents);
        if (sessionManager.isAdmin()) {
            layoutManageEvents.setVisibility(View.VISIBLE);
        } else {
            layoutManageEvents.setVisibility(View.GONE);
        }

        // --- Setup Upcoming Events RecyclerView ---
        recyclerViewUpcomingEvents = view.findViewById(R.id.recyclerViewUpcomingEvents);
        recyclerViewUpcomingEvents.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        upcomingEventsAdapter = new UpcomingEventsAdapter(upcomingEventList, getContext());
        recyclerViewUpcomingEvents.setAdapter(upcomingEventsAdapter);

        // --- Setup Manage Event Button Clicks ---
        LinearLayout buttonCreateEvent = view.findViewById(R.id.buttonCreateEvent);
        LinearLayout buttonEditEvent = view.findViewById(R.id.buttonEditEvent);
        LinearLayout buttonDeleteEvent = view.findViewById(R.id.buttonDeleteEvent);

        buttonCreateEvent.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddEditEventActivity.class)));

        // For Edit and Delete, we send the user to the list of all events
        // In a more complex app, this might go to a dedicated management screen.
        View.OnClickListener manageListener = v -> {
            // Check if the hosting activity is MainActivity
            Intent intent = new Intent(getActivity(), AdminManageActivity.class);
            startActivity(intent);
        };
        buttonEditEvent.setOnClickListener(manageListener);
        buttonDeleteEvent.setOnClickListener(manageListener);

        // --- Load Data ---
        loadUpcomingEvents();

        return view;
    }

    private void loadUpcomingEvents() {
        db.collection("events")
                .orderBy("date", Query.Direction.ASCENDING)
                .limit(10) // Get the next 10 upcoming events
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isAdded()) { // Check if fragment is still attached
                        upcomingEventList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Event event = document.toObject(Event.class);
                            event.setId(document.getId());
                            upcomingEventList.add(event);
                        }
                        upcomingEventsAdapter.notifyDataSetChanged();
                        if (upcomingEventList.isEmpty()) {
                            // If list is empty, show the message
                            textViewUpcomingEventsTitle.setVisibility(View.GONE);
                            recyclerViewUpcomingEvents.setVisibility(View.GONE);
                            textViewNoUpcomingEvents.setVisibility(View.VISIBLE);
                        } else {
                            // If list has items, show the list
                            textViewUpcomingEventsTitle.setVisibility(View.VISIBLE);
                            recyclerViewUpcomingEvents.setVisibility(View.VISIBLE);
                            textViewNoUpcomingEvents.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error loading events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}