package com.example.lab5_20190057;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class SettingsActivity extends AppCompatActivity {

    private TextInputEditText etUserName;
    private TextInputEditText etMotivationalMessage;
    private TextInputEditText etNotificationMessage;
    private TextInputEditText etNotificationFrequency;
    private Button btnSaveSettings;
    private Button btnResetDefaults;
    private ImageView btnBack;
    private NotificationHelper notificationHelper;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "HabitsAppPrefs";

    // Keys para SharedPreferences
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_MOTIVATIONAL_MESSAGE = "motivational_message";
    private static final String KEY_NOTIFICATION_MESSAGE = "notification_message";
    private static final String KEY_NOTIFICATION_FREQUENCY = "notification_frequency";

    // Valores por defecto
    private static final String DEFAULT_USER_NAME = "Usuario";
    private static final String DEFAULT_MOTIVATIONAL_MESSAGE = "¡Hoy es un gran día para formar buenos hábitos!";
    private static final String DEFAULT_NOTIFICATION_MESSAGE = "¡Es hora de trabajar en tus hábitos! 💪";
    private static final int DEFAULT_NOTIFICATION_FREQUENCY = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        initializeSharedPreferences();
        loadCurrentSettings();
        setupClickListeners();
    }

    private void initializeViews() {
        etUserName = findViewById(R.id.etUserName);
        etMotivationalMessage = findViewById(R.id.etMotivationalMessage);
        etNotificationMessage = findViewById(R.id.etNotificationMessage);
        etNotificationFrequency = findViewById(R.id.etNotificationFrequency);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);
        btnResetDefaults = findViewById(R.id.btnResetDefaults);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        notificationHelper = new NotificationHelper(this);
    }

    private void loadCurrentSettings() {
        // Cargar configuraciones actuales
        String userName = sharedPreferences.getString(KEY_USER_NAME, DEFAULT_USER_NAME);
        String motivationalMessage = sharedPreferences.getString(KEY_MOTIVATIONAL_MESSAGE, DEFAULT_MOTIVATIONAL_MESSAGE);
        String notificationMessage = sharedPreferences.getString(KEY_NOTIFICATION_MESSAGE, DEFAULT_NOTIFICATION_MESSAGE);
        int notificationFrequency = sharedPreferences.getInt(KEY_NOTIFICATION_FREQUENCY, DEFAULT_NOTIFICATION_FREQUENCY);

        // Establecer valores en los campos
        etUserName.setText(userName);
        etMotivationalMessage.setText(motivationalMessage);
        etNotificationMessage.setText(notificationMessage);
        etNotificationFrequency.setText(String.valueOf(notificationFrequency));
    }

    private void setupClickListeners() {
        // Botón de regreso
        btnBack.setOnClickListener(v -> finish());

        // Botón guardar configuraciones
        btnSaveSettings.setOnClickListener(v -> saveSettings());

        // Botón valores por defecto
        btnResetDefaults.setOnClickListener(v -> resetToDefaults());
    }

    private void saveSettings() {
        // Validar campos
        if (!validateFields()) {
            return;
        }

        // Obtener valores de los campos
        String userName = etUserName.getText().toString().trim();
        String motivationalMessage = etMotivationalMessage.getText().toString().trim();
        String notificationMessage = etNotificationMessage.getText().toString().trim();
        int notificationFrequency = Integer.parseInt(etNotificationFrequency.getText().toString().trim());

        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_MOTIVATIONAL_MESSAGE, motivationalMessage);
        editor.putString(KEY_NOTIFICATION_MESSAGE, notificationMessage);
        editor.putInt(KEY_NOTIFICATION_FREQUENCY, notificationFrequency);
        editor.apply();

        // Programar notificaciones motivacionales con la nueva configuración
        notificationHelper.scheduleMotivationalNotifications(notificationMessage, notificationFrequency);

        Toast.makeText(this, "Configuraciones guardadas exitosamente", Toast.LENGTH_SHORT).show();

        // Finalizar actividad para que MainActivity se actualice
        finish();
    }

    private boolean validateFields() {
        // Validar nombre de usuario
        if (etUserName.getText().toString().trim().isEmpty()) {
            etUserName.setError("Por favor ingresa tu nombre");
            etUserName.requestFocus();
            return false;
        }

        // Validar mensaje motivacional
        if (etMotivationalMessage.getText().toString().trim().isEmpty()) {
            etMotivationalMessage.setError("Por favor ingresa un mensaje motivacional");
            etMotivationalMessage.requestFocus();
            return false;
        }

        // Validar mensaje de notificación
        if (etNotificationMessage.getText().toString().trim().isEmpty()) {
            etNotificationMessage.setError("Por favor ingresa un mensaje para las notificaciones");
            etNotificationMessage.requestFocus();
            return false;
        }

        // Validar frecuencia de notificaciones
        String frequencyText = etNotificationFrequency.getText().toString().trim();
        if (frequencyText.isEmpty()) {
            etNotificationFrequency.setError("Por favor ingresa la frecuencia");
            etNotificationFrequency.requestFocus();
            return false;
        }

        try {
            int frequency = Integer.parseInt(frequencyText);
            if (frequency <= 0 || frequency > 24) {
                etNotificationFrequency.setError("La frecuencia debe estar entre 1 y 24 horas");
                etNotificationFrequency.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etNotificationFrequency.setError("Por favor ingresa un número válido");
            etNotificationFrequency.requestFocus();
            return false;
        }

        return true;
    }

    private void resetToDefaults() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Restaurar valores por defecto")
                .setMessage("¿Estás seguro de que quieres restaurar todos los valores por defecto?")
                .setPositiveButton("Restaurar", (dialog, which) -> {
                    etUserName.setText(DEFAULT_USER_NAME);
                    etMotivationalMessage.setText(DEFAULT_MOTIVATIONAL_MESSAGE);
                    etNotificationMessage.setText(DEFAULT_NOTIFICATION_MESSAGE);
                    etNotificationFrequency.setText(String.valueOf(DEFAULT_NOTIFICATION_FREQUENCY));

                    Toast.makeText(this, "Valores restaurados", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Método para obtener configuraciones actuales (útil para otras actividades)
    public static String getUserName(SharedPreferences prefs) {
        return prefs.getString(KEY_USER_NAME, DEFAULT_USER_NAME);
    }

    public static String getMotivationalMessage(SharedPreferences prefs) {
        return prefs.getString(KEY_MOTIVATIONAL_MESSAGE, DEFAULT_MOTIVATIONAL_MESSAGE);
    }

    public static String getNotificationMessage(SharedPreferences prefs) {
        return prefs.getString(KEY_NOTIFICATION_MESSAGE, DEFAULT_NOTIFICATION_MESSAGE);
    }

    public static int getNotificationFrequency(SharedPreferences prefs) {
        return prefs.getInt(KEY_NOTIFICATION_FREQUENCY, DEFAULT_NOTIFICATION_FREQUENCY);
    }
}