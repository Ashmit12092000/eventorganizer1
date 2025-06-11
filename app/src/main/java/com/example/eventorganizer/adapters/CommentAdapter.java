package com.example.eventorganizer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eventorganizer.R;
import com.example.eventorganizer.models.Comment;
import com.example.eventorganizer.util.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> commentList;
    private final Context context;
    private final String eventId;
    private final SessionManager sessionManager;

    public CommentAdapter(List<Comment> commentList, Context context, String eventId) {
        this.commentList = commentList;
        this.context = context;
        this.eventId = eventId;
        this.sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // Populate the views with the comment data
        holder.textViewCommentUser.setText(comment.getCreatorName());
        holder.textViewCommentText.setText(comment.getText());

        // A real app would load a user's avatar URL. We use a placeholder.
        String avatarUrl = comment.getCreatorAvatarUrl();

        // Check if the user has an avatar URL
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_profile) // Default icon while loading
                    .error(R.drawable.ic_profile)       // Default icon if loading fails
                    .into(holder.imageViewUserAvatar);
        } else {
            // If they don't have an avatar, show the default profile icon
            holder.imageViewUserAvatar.setImageResource(R.drawable.ic_profile);
        }

        // Format and display the timestamp
        if (comment.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault());
            holder.textViewCommentTime.setText(sdf.format(comment.getTimestamp()));
        }

        // --- Visibility logic for the delete button ---
        boolean isAdmin = sessionManager.isAdmin();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Show the delete button if the logged-in user is an admin OR the original author
        if (isAdmin || comment.getCreatorId().equals(currentUserId)) {
            holder.buttonDeleteComment.setVisibility(View.VISIBLE);
        } else {
            holder.buttonDeleteComment.setVisibility(View.GONE);
        }

        // --- Set the click listener for the delete button ---
        holder.buttonDeleteComment.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("events").document(eventId)
                    .collection("comments").document(comment.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Comment deleted", Toast.LENGTH_SHORT).show();
                        // Instantly remove the comment from the list to update the UI
                        commentList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, commentList.size());
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error deleting comment", Toast.LENGTH_SHORT).show());
        });
    }

    /**
     * Returns the total number of items in the list.
     * This is crucial for the RecyclerView to know how many items to display.
     */
    @Override
    public int getItemCount() {
        return commentList.size();
    }

    /**
     * The ViewHolder class that holds references to the views in item_comment.xml.
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCommentUser, textViewCommentText, textViewCommentTime;
        ShapeableImageView imageViewUserAvatar;
        ImageView buttonDeleteComment;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCommentUser = itemView.findViewById(R.id.textViewCommentUser);
            textViewCommentText = itemView.findViewById(R.id.textViewCommentText);
            textViewCommentTime = itemView.findViewById(R.id.textViewCommentTime);
            imageViewUserAvatar = itemView.findViewById(R.id.imageViewUserAvatar);
            buttonDeleteComment = itemView.findViewById(R.id.buttonDeleteComment);
        }
    }
}