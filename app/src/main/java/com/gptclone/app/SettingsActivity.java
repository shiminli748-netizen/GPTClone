package com.gptclone.app;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.gptclone.app.model.ApiConfig;
import com.gptclone.app.util.SharedPreferencesUtil;
import com.gptclone.app.util.ThemeHelper;

public class SettingsActivity extends AppCompatActivity {

    private EditText editBaseUrl;
    private EditText editApiKey;
    private EditText editModel;
    private EditText editMaxTokens;
    private EditText editTemperature;
    private MaterialButton btnSave;
    private MaterialButton btnThemeLight;
    private MaterialButton btnThemeDark;
    private MaterialButton btnThemeSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings_title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initViews();
        loadSettings();
        initClickListeners();
    }

    private void initViews() {
        editBaseUrl = findViewById(R.id.edit_base_url);
        editApiKey = findViewById(R.id.edit_api_key);
        editModel = findViewById(R.id.edit_model);
        editMaxTokens = findViewById(R.id.edit_max_tokens);
        editTemperature = findViewById(R.id.edit_temperature);
        btnSave = findViewById(R.id.btn_save);
        btnThemeLight = findViewById(R.id.btn_theme_light);
        btnThemeDark = findViewById(R.id.btn_theme_dark);
        btnThemeSystem = findViewById(R.id.btn_theme_system);
    }

    private void loadSettings() {
        ApiConfig config = SharedPreferencesUtil.getApiConfig(this);
        editBaseUrl.setText(config.baseUrl);
        editApiKey.setText(config.apiKey);
        editModel.setText(config.model);
        editMaxTokens.setText(String.valueOf(config.maxTokens));
        editTemperature.setText(String.valueOf(config.temperature));
    }

    private void initClickListeners() {
        btnSave.setOnClickListener(v -> saveSettings());

        btnThemeLight.setOnClickListener(v -> {
            SharedPreferencesUtil.saveThemeMode(this, ThemeHelper.MODE_LIGHT);
            ThemeHelper.applyTheme(ThemeHelper.MODE_LIGHT);
        });

        btnThemeDark.setOnClickListener(v -> {
            SharedPreferencesUtil.saveThemeMode(this, ThemeHelper.MODE_DARK);
            ThemeHelper.applyTheme(ThemeHelper.MODE_DARK);
        });

        btnThemeSystem.setOnClickListener(v -> {
            SharedPreferencesUtil.saveThemeMode(this, ThemeHelper.MODE_SYSTEM);
            ThemeHelper.applyTheme(ThemeHelper.MODE_SYSTEM);
        });
    }

    private void saveSettings() {
        String baseUrl = editBaseUrl.getText().toString().trim();
        String apiKey = editApiKey.getText().toString().trim();
        String model = editModel.getText().toString().trim();
        String maxTokensStr = editMaxTokens.getText().toString().trim();
        String temperatureStr = editTemperature.getText().toString().trim();

        if (baseUrl.isEmpty()) {
            editBaseUrl.setError(getString(R.string.error_field_required));
            return;
        }
        if (model.isEmpty()) {
            editModel.setError(getString(R.string.error_field_required));
            return;
        }

        int maxTokens = 4096;
        try {
            maxTokens = Integer.parseInt(maxTokensStr);
            if (maxTokens < 1 || maxTokens > 128000) {
                editMaxTokens.setError(getString(R.string.error_invalid_range));
                return;
            }
        } catch (NumberFormatException e) {
            editMaxTokens.setError(getString(R.string.error_invalid_number));
            return;
        }

        double temperature = 0.7;
        try {
            temperature = Double.parseDouble(temperatureStr);
            if (temperature < 0.0 || temperature > 2.0) {
                editTemperature.setError(getString(R.string.error_invalid_temperature));
                return;
            }
        } catch (NumberFormatException e) {
            editTemperature.setError(getString(R.string.error_invalid_number));
            return;
        }

        ApiConfig config = new ApiConfig();
        config.baseUrl = baseUrl;
        config.apiKey = apiKey;
        config.model = model;
        config.maxTokens = maxTokens;
        config.temperature = temperature;

        SharedPreferencesUtil.saveApiConfig(this, config);
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
