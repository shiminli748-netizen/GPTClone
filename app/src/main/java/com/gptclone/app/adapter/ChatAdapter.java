package com.gptclone.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gptclone.app.R;
import com.gptclone.app.database.Message;
import io.noties.markwon.Markwon;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.syntax.Prism4jThemeDefault;
import io.noties.markwon.syntax.SyntaxHighlightPlugin;
import io.noties.prism4j.Prism4j;
import io.noties.prism4j.annotations.PrismBundle;
import java.util.ArrayList;
import java.util.List;

@PrismBundle(include = {"clike", "java", "python", "javascript", "json", "css", "markup", "sql", "kotlin", "c", "cpp", "go", "swift"})
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> messages = new ArrayList<>();
    private final Markwon markwon;
    private final OnMessageActionListener listener;
    private boolean isStreaming = false;

    public interface OnMessageActionListener {
        void onCopyMessage(String content);
        void onRegenerateMessage(long messageId);
    }

    public ChatAdapter(android.content.Context context, OnMessageActionListener listener) {
        this.listener = listener;
        Prism4j prism4j = new Prism4j(new GrammarLocatorDef());
        this.markwon = Markwon.builder(context)
            .usePlugin(SyntaxHighlightPlugin.create(prism4j, Prism4jThemeDefault.create()))
            .usePlugin(TablePlugin.create(context))
            .usePlugin(TaskListPlugin.create(context))
            .usePlugin(HtmlPlugin.create())
            .build();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
        }
        return new ChatViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder.textView != null) {
            if ("user".equals(message.role)) {
                holder.textView.setText(message.content);
            } else {
                markwon.setMarkdown(holder.textView, message.content);
            }
        }

        if (holder.btnCopy != null) {
            holder.btnCopy.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCopyMessage(message.content);
                }
            });
        }

        if (holder.btnRegenerate != null) {
            holder.btnRegenerate.setVisibility(isStreaming ? View.GONE : View.VISIBLE);
            holder.btnRegenerate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRegenerateMessage(message.id);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return "user".equals(messages.get(position).role) ? 0 : 1;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages != null ? messages : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void updateLastMessage(String content) {
        if (!messages.isEmpty()) {
            Message last = messages.get(messages.size() - 1);
            last.content = content;
            notifyItemChanged(messages.size() - 1);
        }
    }

    public void setStreaming(boolean streaming) {
        this.isStreaming = streaming;
    }

    public Message getLastMessage() {
        if (messages.isEmpty()) return null;
        return messages.get(messages.size() - 1);
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ImageButton btnCopy;
        ImageButton btnRegenerate;

        ChatViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            if (viewType == 0) {
                textView = itemView.findViewById(R.id.text_user_message);
            } else {
                textView = itemView.findViewById(R.id.text_ai_message);
                btnCopy = itemView.findViewById(R.id.btn_copy);
                btnRegenerate = itemView.findViewById(R.id.btn_regenerate);
            }
        }
    }
}
