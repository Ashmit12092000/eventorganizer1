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

public class UpcomingEventsAdapter extends RecyclerView.Adapter<UpcomingEventsAdapter.ViewHolder> {

    private final List<Event> eventList;
    private final Context context;

    public UpcomingEventsAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_upcoming_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.textViewTitle.setText(event.getTitle());
        holder.textViewDate.setText(event.getDate());

        Glide.with(context)
                .load(event.getImageUrl())
                .placeholder(R.drawable.circle_background)
                .into(holder.imageViewPoster);

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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPoster;
        TextView textViewTitle, textViewDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPoster = itemView.findViewById(R.id.imageViewEventPoster);
            textViewTitle = itemView.findViewById(R.id.textViewEventTitle);
            textViewDate = itemView.findViewById(R.id.textViewEventDate);
        }
    }
}