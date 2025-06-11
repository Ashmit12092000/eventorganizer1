package com.example.eventorganizer.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventorganizer.R;
import com.example.eventorganizer.activities.AddEditEventActivity;
import com.example.eventorganizer.activities.EventDetailActivity;
import com.example.eventorganizer.models.Event;
import com.example.eventorganizer.util.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.TestViewHolder> {

    private final List<Event> eventList;
    private final Context context;
    private final SessionManager sessionManager;

    public EventAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
        this.sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        Event event = eventList.get(position);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Safety check for logged-in user
        if (currentUser == null) return;
        String currentUserId = currentUser.getUid();

        // --- Populate Views ---
        holder.textViewTitle.setText(event.getTitle());
        holder.textViewDescription.setText(event.getDescription());
        holder.textViewDate.setText(event.getDate());

        Glide.with(context)
                .load(event.getImageUrl())
                .placeholder(R.drawable.applogo)
                .error(R.drawable.applogo)
                .into(holder.imageViewEvent);

        // --- Visibility Logic ---
        boolean isAdmin = sessionManager.isAdmin();

        // Show "Delete" button ONLY for admins
        if (isAdmin) {
            holder.layoutDelete.setVisibility(View.VISIBLE);
        } else {
            holder.layoutDelete.setVisibility(View.GONE);
        }

        // Show the Edit menu button if the user is an admin OR the event creator
        if (isAdmin || event.getCreatorId().equals(currentUserId)) {
            holder.buttonMenu.setVisibility(View.VISIBLE);
        } else {
            holder.buttonMenu.setVisibility(View.GONE);
        }

        // --- Click Listeners ---
        View.OnClickListener detailClickListener = v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            context.startActivity(intent);
        };

        holder.layoutView.setOnClickListener(detailClickListener);
        holder.layoutComment.setOnClickListener(detailClickListener);

        holder.layoutDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance().collection("events").document(event.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                        eventList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, eventList.size());
                    });
        });

        // Set listener for the edit menu button
        holder.buttonMenu.setOnClickListener(v -> showPopupMenu(v, event));
    }

    private void showPopupMenu(View view, Event event) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.event_item_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_event) {
                // User clicked "Edit Event"
                Intent intent = new Intent(context, AddEditEventActivity.class);
                intent.putExtra("EVENT_ID", event.getId()); // Pass the event ID to edit
                context.startActivity(intent);
                return true;
            }
            return false;
        });

        popup.show();
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    // ViewHolder now renamed to TestViewHolder
    static class TestViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle, textViewDescription, textViewDate;
        LinearLayout layoutView, layoutComment, layoutDelete;
        ShapeableImageView imageViewEvent;
        ImageView buttonMenu;

        public TestViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewEventTitle);
            textViewDescription = itemView.findViewById(R.id.textViewEventDescription);
            textViewDate = itemView.findViewById(R.id.textViewEventDate);
            imageViewEvent = itemView.findViewById(R.id.imageViewEvent);
            layoutView = itemView.findViewById(R.id.layoutView);
            layoutComment = itemView.findViewById(R.id.layoutComment);
            layoutDelete = itemView.findViewById(R.id.layoutDelete);
            buttonMenu = itemView.findViewById(R.id.buttonMenu);
        }
    }
}
