package com.example.eventorganizer.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventorganizer.R;
import com.example.eventorganizer.activities.EventDetailActivity;
import com.example.eventorganizer.models.Event;
import java.util.List;

public class RegisteredEventsAdapter extends RecyclerView.Adapter<RegisteredEventsAdapter.ViewHolder> {

    private final List<Event> eventList;
    private final Context context;

    public RegisteredEventsAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the correct layout file for the item row
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registered_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Get the data for the current event
        Event event = eventList.get(position);

        // Set the event title and subtitle (using description as a stand-in)
        holder.textViewEventTitle.setText(event.getTitle());
        holder.textViewEventType.setText(event.getDescription());

        // Load the event image using the Glide library
        Glide.with(context)
                .load(event.getImageUrl())
                .placeholder(R.drawable.applogo)
                .error(R.drawable.ic_contact_host)
                .into(holder.imageViewEventImage);

        // Make the entire item clickable to navigate to the EventDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("EVENT_ID", event.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    /**
     * The ViewHolder class holds references to the views in item_registered_event.xml.
     * It's crucial that the IDs here match the IDs in the XML file.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewEventImage;
        TextView textViewEventTitle;
        TextView textViewEventType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewEventImage = itemView.findViewById(R.id.imageViewEventImage);
            textViewEventTitle = itemView.findViewById(R.id.textViewEventTitle);
            textViewEventType = itemView.findViewById(R.id.textViewEventType);
        }
    }
}