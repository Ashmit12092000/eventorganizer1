package com.example.eventorganizer.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eventorganizer.R;
import com.example.eventorganizer.activities.AddEditEventActivity;
import com.example.eventorganizer.adapters.EventAdapter;
import com.example.eventorganizer.models.Event;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList;
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // This line is crucial for the fragment to handle its own menu items
        setHasOptionsMenu(true);
    }

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);

        db = FirebaseFirestore.getInstance();
        eventList = new ArrayList<>();

        // Setup Toolbar
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(eventList, getContext());
        recyclerView.setAdapter(eventAdapter);

        // Setup Floating Action Button
        FloatingActionButton fabAddEvent = view.findViewById(R.id.fabAddEvent);
        fabAddEvent.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), AddEditEventActivity.class)));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load events every time the fragment is shown
        loadAllEvents();
    }

    private void loadAllEvents() {
        db.collection("events").orderBy("date", Query.Direction.DESCENDING).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isAdded()) {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Event event = document.toObject(Event.class);
                            event.setId(document.getId());
                            eventList.add(event);
                        }
                        eventAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error loading events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // This method is called to inflate the fragment's menu items into the toolbar
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.event_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // This method handles clicks on the fragment's menu items
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_event) {
            startActivity(new Intent(getActivity(), AddEditEventActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}