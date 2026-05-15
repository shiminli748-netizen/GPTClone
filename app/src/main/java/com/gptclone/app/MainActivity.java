package com.gptclone.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.gptclone.app.adapter.ChatAdapter;
import com.gptclone.app.adapter.ConversationAdapter;
import com.gptclone.app.database.AppDatabase;
import com.gptclone.app.database.Conversation;
import com.gptclone.app.database.Message;
import com.gptclone.app.model.ApiConfig;
import com.gptclone.app.model.ChatRequest;
import com.gptclone.app.network.StreamProcessor;
import com.gptclone.app.util.SharedPreferencesUtil;
import com.gptclone.app.util.SystemPrompt;
import com.gptclone.app.util.ThemeHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements
        ChatAdapter.OnMessageActionListener,
        ConversationAdapter.OnConversationActionListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private RecyclerView recyclerViewChat;
    private RecyclerView recyclerViewConversations;
    private EditText editTextMessage;
    private ImageButton btnSend;
    private ImageButton btnStop;
    private ImageButton btnNewChat;
    private LinearLayout layoutEmpty;

    private ChatAdapter chatAdapter;
    private ConversationAdapter conversationAdapter;
    private AppDatabase database;
    private ExecutorService executor;

    private long currentConversationId = -1;
    private Call currentCall = null;
    private boolean isStreaming = false;
    private StringBuilder streamingContent = new StringBuilder();

    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ThemeHelper.applyTheme(SharedPreferencesUtil.getThemeMode(this));

        setContentView(R.layout.activity_main);

        initViews();
        initDatabase();
        initAdapters();
        initClickListeners();
        loadConversations();

        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        recyclerViewChat = findViewById(R.id.recycler_view_chat);
        recyclerViewConversations = findViewById(R.id.recycler_view_conversations);
        editTextMessage = findViewById(R.id.edit_text_message);
        btnSend = findViewById(R.id.btn_send);
        btnStop = findViewById(R.id.btn_stop);
        btnNewChat = findViewById(R.id.btn_new_chat);
        layoutEmpty = findViewById(R.id.layout_empty);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(Gravity.START));

        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutManager convLayoutManager = new LinearLayoutManager(this);
        recyclerViewConversations.setLayoutManager(convLayoutManager);
    }

    private void initDatabase() {
        database = AppDatabase.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
    }

    private void initAdapters() {
        chatAdapter = new ChatAdapter(this, this);
        recyclerViewChat.setAdapter(chatAdapter);

        conversationAdapter = new ConversationAdapter(this);
        recyclerViewConversations.setAdapter(conversationAdapter);
    }

    private void initClickListeners() {
        btnSend.setOnClickListener(v -> sendMessage());
        btnStop.setOnClickListener(v -> stopGeneration());
        btnNewChat.setOnClickListener(v -> createNewConversation());

        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                btnSend.setVisibility(s.toString().trim().isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.action_theme_light) {
                SharedPreferencesUtil.saveThemeMode(this, ThemeHelper.MODE_LIGHT);
                ThemeHelper.applyTheme(ThemeHelper.MODE_LIGHT);
            } else if (id == R.id.action_theme_dark) {
                SharedPreferencesUtil.saveThemeMode(this, ThemeHelper.MODE_DARK);
                ThemeHelper.applyTheme(ThemeHelper.MODE_DARK);
            } else if (id == R.id.action_theme_system) {
                SharedPreferencesUtil.saveThemeMode(this, ThemeHelper.MODE_SYSTEM);
                ThemeHelper.applyTheme(ThemeHelper.MODE_SYSTEM);
            } else if (id == R.id.action_export) {
                exportCurrentConversation();
            }
            drawerLayout.closeDrawer(Gravity.START);
            return true;
        });
    }

    private void loadConversations() {
        executor.execute(() -> {
            List<Conversation> convs = database.conversationDao().getAllConversations();
            runOnUiThread(() -> conversationAdapter.setConversations(convs));
        });
    }

    private void createNewConversation() {
        currentConversationId = -1;
        chatAdapter.setMessages(new ArrayList<>());
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerViewChat.setVisibility(View.GONE);
        conversationAdapter.setSelectedConversationId(-1);
        toolbar.setSubtitle("");
    }

    private void loadConversation(Conversation conversation) {
        currentConversationId = conversation.id;
        conversationAdapter.setSelectedConversationId(conversation.id);
        toolbar.setSubtitle(conversation.title);

        executor.execute(() -> {
            List<Message> msgs = database.messageDao().getMessagesByConversationId(conversation.id);
            runOnUiThread(() -> {
                chatAdapter.setMessages(msgs);
                layoutEmpty.setVisibility(msgs.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerViewChat.setVisibility(msgs.isEmpty() ? View.GONE : View.VISIBLE);
                if (!msgs.isEmpty()) {
                    recyclerViewChat.smoothScrollToPosition(msgs.size() - 1);
                }
            });
        });

        drawerLayout.closeDrawer(Gravity.START);
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (text.isEmpty() || isStreaming) return;

        if (currentConversationId == -1) {
            createConversationAndSend(text);
            return;
        }

        editTextMessage.setText("");
        hideKeyboard();

        Message userMsg = new Message(currentConversationId, "user", text, System.currentTimeMillis());
        executor.execute(() -> {
            database.messageDao().insert(userMsg);
            updateConversationTimestamp(currentConversationId);
        });

        chatAdapter.addMessage(userMsg);
        layoutEmpty.setVisibility(View.GONE);
        recyclerViewChat.setVisibility(View.VISIBLE);
        recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

        requestAIResponse();
    }

    private void createConversationAndSend(String firstMessage) {
        executor.execute(() -> {
            String title = firstMessage.length() > 30 ? firstMessage.substring(0, 30) + "..." : firstMessage;
            long now = System.currentTimeMillis();
            Conversation conv = new Conversation(title, now, now);
            long convId = database.conversationDao().insert(conv);
            currentConversationId = convId;

            Message userMsg = new Message(convId, "user", firstMessage, now);
            database.messageDao().insert(userMsg);

            List<Conversation> convs = database.conversationDao().getAllConversations();
            runOnUiThread(() -> {
                conversationAdapter.setConversations(convs);
                conversationAdapter.setSelectedConversationId(convId);
                toolbar.setSubtitle(title);
                chatAdapter.addMessage(userMsg);
                layoutEmpty.setVisibility(View.GONE);
                recyclerViewChat.setVisibility(View.VISIBLE);
                editTextMessage.setText("");
                hideKeyboard();
                recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                requestAIResponse();
            });
        });
    }

    private void requestAIResponse() {
        ApiConfig config = SharedPreferencesUtil.getApiConfig(this);
        if (config.apiKey.isEmpty()) {
            Toast.makeText(this, R.string.error_no_api_key, Toast.LENGTH_LONG).show();
            return;
        }

        isStreaming = true;
        streamingContent = new StringBuilder();
        chatAdapter.setStreaming(true);
        btnStop.setVisibility(View.VISIBLE);
        btnSend.setVisibility(View.GONE);

        Message aiMsg = new Message(currentConversationId, "assistant", "", System.currentTimeMillis());
        chatAdapter.addMessage(aiMsg);

        executor.execute(() -> {
            long aiMsgId = database.messageDao().insert(aiMsg);
            aiMsg.id = aiMsgId;

            List<Message> history = database.messageDao().getMessagesByConversationId(currentConversationId);
            List<ChatRequest.ChatMessage> apiMessages = buildApiMessages(history, config);

            runOnUiThread(() -> sendStreamRequest(config, apiMessages, aiMsg, aiMsgId));
        });
    }

    private List<ChatRequest.ChatMessage> buildApiMessages(List<Message> history, ApiConfig config) {
        List<ChatRequest.ChatMessage> apiMessages = new ArrayList<>();
        apiMessages.add(new ChatRequest.ChatMessage("system", SystemPrompt.getSystemPrompt(this)));

        int startIndex = Math.max(0, history.size() - 20);
        for (int i = startIndex; i < history.size(); i++) {
            Message msg = history.get(i);
            if (!msg.content.isEmpty()) {
                apiMessages.add(new ChatRequest.ChatMessage(msg.role, msg.content));
            }
        }
        return apiMessages;
    }

    private void sendStreamRequest(ApiConfig config, List<ChatRequest.ChatMessage> apiMessages, Message aiMsg, long aiMsgId) {
        try {
            ChatRequest chatRequest = new ChatRequest(config.model, apiMessages, true, config.maxTokens, config.temperature);
            String jsonBody = new com.google.gson.Gson().toJson(chatRequest);

            RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(config.getChatUrl())
                    .addHeader("Authorization", "Bearer " + config.apiKey)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "text/event-stream")
                    .post(body)
                    .build();

            currentCall = okHttpClient.newCall(request);
            currentCall.enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        finishStreaming(aiMsg, aiMsgId);
                        if (!call.isCanceled()) {
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.error_request_failed) + ": " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (!response.isSuccessful()) {
                        String errorBody = "";
                        try {
                            if (response.body() != null) {
                                errorBody = response.body().string();
                            }
                        } catch (IOException ignored) {}
                        String finalError = errorBody;
                        runOnUiThread(() -> {
                            finishStreaming(aiMsg, aiMsgId);
                            Toast.makeText(MainActivity.this,
                                    getString(R.string.error_request_failed) + ": " + response.code() + " " + finalError,
                                    Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    StreamProcessor.processStream(response, new StreamProcessor.StreamListener() {
                        @Override
                        public void onToken(String token) {
                            streamingContent.append(token);
                            runOnUiThread(() -> {
                                chatAdapter.updateLastMessage(streamingContent.toString());
                                recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                            });
                        }

                        @Override
                        public void onComplete() {
                            runOnUiThread(() -> finishStreaming(aiMsg, aiMsgId));
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                finishStreaming(aiMsg, aiMsgId);
                                Toast.makeText(MainActivity.this,
                                        getString(R.string.error_stream) + ": " + error,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                }
            });
        } catch (Exception e) {
            finishStreaming(aiMsg, aiMsgId);
            Toast.makeText(this, getString(R.string.error_request_failed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void finishStreaming(Message aiMsg, long aiMsgId) {
        isStreaming = false;
        chatAdapter.setStreaming(false);
        btnStop.setVisibility(View.GONE);
        btnSend.setVisibility(View.VISIBLE);
        currentCall = null;

        String content = streamingContent.toString();
        if (!content.isEmpty()) {
            aiMsg.content = content;
            executor.execute(() -> {
                database.messageDao().updateContent(aiMsgId, content);
                updateConversationTimestamp(currentConversationId);
            });
        }
    }

    private void stopGeneration() {
        if (currentCall != null) {
            currentCall.cancel();
            currentCall = null;
        }
        Message lastMsg = chatAdapter.getLastMessage();
        if (lastMsg != null && "assistant".equals(lastMsg.role)) {
            finishStreaming(lastMsg, lastMsg.id);
        }
    }

    private void updateConversationTimestamp(long convId) {
        executor.execute(() -> {
            Conversation conv = database.conversationDao().getConversationById(convId);
            if (conv != null) {
                conv.updatedAt = System.currentTimeMillis();
                database.conversationDao().update(conv);
                List<Conversation> convs = database.conversationDao().getAllConversations();
                runOnUiThread(() -> conversationAdapter.setConversations(convs));
            }
        });
    }

    @Override
    public void onCopyMessage(String content) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("message", content);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRegenerateMessage(long messageId) {
        if (isStreaming) return;

        executor.execute(() -> {
            List<Message> msgs = database.messageDao().getMessagesByConversationId(currentConversationId);
            Message lastAiMsg = null;
            for (int i = msgs.size() - 1; i >= 0; i--) {
                if ("assistant".equals(msgs.get(i).role)) {
                    lastAiMsg = msgs.get(i);
                    break;
                }
            }
            if (lastAiMsg != null) {
                database.messageDao().updateContent(lastAiMsg.id, "");
                List<Message> updatedMsgs = database.messageDao().getMessagesByConversationId(currentConversationId);
                runOnUiThread(() -> {
                    chatAdapter.setMessages(updatedMsgs);
                    requestAIResponse();
                });
            }
        });
    }

    @Override
    public void onConversationClick(Conversation conversation) {
        loadConversation(conversation);
    }

    @Override
    public void onConversationDelete(Conversation conversation) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    executor.execute(() -> {
                        database.conversationDao().delete(conversation);
                        List<Conversation> convs = database.conversationDao().getAllConversations();
                        runOnUiThread(() -> {
                            conversationAdapter.setConversations(convs);
                            if (conversation.id == currentConversationId) {
                                createNewConversation();
                            }
                        });
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onConversationRename(Conversation conversation) {
        EditText input = new EditText(this);
        input.setText(conversation.title);
        new AlertDialog.Builder(this)
                .setTitle(R.string.rename_conversation)
                .setView(input)
                .setPositiveButton(R.string.save, (d, w) -> {
                    String newTitle = input.getText().toString().trim();
                    if (!newTitle.isEmpty()) {
                        executor.execute(() -> {
                            database.conversationDao().updateTitle(conversation.id, newTitle, System.currentTimeMillis());
                            List<Conversation> convs = database.conversationDao().getAllConversations();
                            runOnUiThread(() -> {
                                conversationAdapter.setConversations(convs);
                                if (conversation.id == currentConversationId) {
                                    toolbar.setSubtitle(newTitle);
                                }
                            });
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void exportCurrentConversation() {
        if (currentConversationId == -1) {
            Toast.makeText(this, R.string.no_conversation_to_export, Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            Conversation conv = database.conversationDao().getConversationById(currentConversationId);
            List<Message> msgs = database.messageDao().getMessagesByConversationId(currentConversationId);
            StringBuilder sb = new StringBuilder();
            sb.append("# ").append(conv.title).append("\n\n");
            for (Message msg : msgs) {
                String role = "user".equals(msg.role) ? "You" : "AI";
                sb.append("**").append(role).append(":**\n").append(msg.content).append("\n\n");
            }

            String filename = "GPTClone_" + conv.title.replaceAll("[^a-zA-Z0-9]", "_") + ".txt";
            java.io.File dir = new java.io.File(getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS), "GPTClone");
            dir.mkdirs();
            java.io.File file = new java.io.File(dir, filename);
            try {
                java.io.FileWriter writer = new java.io.FileWriter(file);
                writer.write(sb.toString());
                writer.close();
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.exported_to) + " " + file.getAbsolutePath(),
                        Toast.LENGTH_LONG).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this,
                        getString(R.string.export_failed) + ": " + e.getMessage(),
                        Toast.LENGTH_LONG).show());
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextMessage.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadConversations();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentCall != null) {
            currentCall.cancel();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
