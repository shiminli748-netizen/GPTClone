package com.gptclone.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gptclone.app.R;
import com.gptclone.app.database.Conversation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder> {

    private List<Conversation> conversations = new ArrayList<>();
    private OnConversationActionListener listener;
    private long selectedConversationId = -1;

    public interface OnConversationActionListener {
        void onConversationClick(Conversation conversation);
        void onConversationDelete(Conversation conversation);
        void onConversationRename(Conversation conversation);
    }

    public ConversationAdapter(OnConversationActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation conversation = conversations.get(position);
        holder.textTitle.setText(conversation.title);
        holder.textDate.setText(formatDate(conversation.updatedAt));

        holder.itemView.setSelected(conversation.id == selectedConversationId);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationClick(conversation);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationDelete(conversation);
            }
        });

        holder.btnRename.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConversationRename(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversations.size();
    }

    public void setConversations(List<Conversation> conversations) {
        this.conversations = conversations != null ? conversations : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setSelectedConversationId(long id) {
        this.selectedConversationId = id;
        notifyDataSetChanged();
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView textDate;
        ImageButton btnDelete;
        ImageButton btnRename;

        ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.text_conversation_title);
            textDate = itemView.findViewById(R.id.text_conversation_date);
            btnDelete = itemView.findViewById(R.id.btn_delete_conversation);
            btnRename = itemView.findViewById(R.id.btn_rename_conversation);
        }
    }
}
