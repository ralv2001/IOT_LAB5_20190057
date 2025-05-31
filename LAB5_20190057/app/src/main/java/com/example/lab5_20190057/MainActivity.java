package com.example.lab5_20190057;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 1001;
    private static final int PERMISSION_REQUEST_CODE = 1002;

    private TextView tvGreeting;
    private TextView tvMotivationalMessage;
    private ImageView ivProfileImage;
    private Button btnViewHabits;
    private Button btnSettings;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "HabitsAppPrefs";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_MOTIVATIONAL_MESSAGE = "motivational_message";
    private static final String KEY_PROFILE_IMAGE = "profile_image_path";

    private NotificationHelper notificationHelper;
    private static final int NOTIFICATION_PERMISSION_CODE = 1003;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeSharedPreferences();
        loadUserData();
        setupClickListeners();
        requestPermissions();
    }

    private void initializeViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        tvMotivationalMessage = findViewById(R.id.tvMotivationalMessage);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        btnViewHabits = findViewById(R.id.btnViewHabits);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        notificationHelper = new NotificationHelper(this);
    }

    private void loadUserData() {
        // Cargar nombre del usuario
        String userName = sharedPreferences.getString(KEY_USER_NAME, "Usuario");
        tvGreeting.setText("¡Hola, " + userName + "!");

        // Cargar mensaje motivacional
        String motivationalMessage = sharedPreferences.getString(KEY_MOTIVATIONAL_MESSAGE,
                "¡Hoy es un gran día para formar buenos hábitos!");
        tvMotivationalMessage.setText(motivationalMessage);

        // Cargar imagen de perfil
        loadProfileImage();
    }

    private void loadProfileImage() {
        String imagePath = sharedPreferences.getString(KEY_PROFILE_IMAGE, "");
        if (!imagePath.isEmpty()) {
            File imageFile = new File(getFilesDir(), imagePath);
            if (imageFile.exists()) {
                try {
                    FileInputStream fis = new FileInputStream(imageFile);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    ivProfileImage.setImageBitmap(bitmap);
                    ivProfileImage.setPadding(0, 0, 0, 0);
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void setupClickListeners() {
        // Click en imagen de perfil para cambiar foto
        ivProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // Click en botón "Ver mis hábitos"
        btnViewHabits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HabitsListActivity.class);
                startActivity(intent);
            }
        });

        // Click en botón "Configuraciones"
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    private void requestPermissions() {
        // Permisos de almacenamiento
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }

        // Permisos de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }

        // Configurar notificaciones motivacionales si ya están configuradas
        setupMotivationalNotifications();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    // Convertir la imagen seleccionada a bitmap
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();

                    // Guardar imagen en internal storage
                    String fileName = "profile_image_" + System.currentTimeMillis() + ".jpg";
                    saveImageToInternalStorage(bitmap, fileName);

                    // Mostrar imagen en el ImageView
                    ivProfileImage.setImageBitmap(bitmap);

                    // Guardar la ruta en SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(KEY_PROFILE_IMAGE, fileName);
                    editor.apply();

                    Toast.makeText(this, "Imagen guardada exitosamente", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveImageToInternalStorage(Bitmap bitmap, String fileName) {
        try {
            FileOutputStream fos = openFileOutput(fileName, MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar datos cuando regresemos a esta actividad (por si se cambiaron en settings)
        loadUserData();
    }

    // Método para actualizar el nombre del usuario (será llamado desde SettingsActivity)
    public void updateUserName(String newName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_NAME, newName);
        editor.apply();
        tvGreeting.setText("¡Hola, " + newName + "!");
    }

    // Método para actualizar el mensaje motivacional (será llamado desde SettingsActivity)
    public void updateMotivationalMessage(String newMessage) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_MOTIVATIONAL_MESSAGE, newMessage);
        editor.apply();
        tvMotivationalMessage.setText(newMessage);
    }

    private void setupMotivationalNotifications() {
        String notificationMessage = sharedPreferences.getString("notification_message", "");
        int notificationFrequency = sharedPreferences.getInt("notification_frequency", 0);

        if (!notificationMessage.isEmpty() && notificationFrequency > 0) {
            notificationHelper.scheduleMotivationalNotifications(notificationMessage, notificationFrequency);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos de almacenamiento concedidos", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permisos de notificaciones concedidos", Toast.LENGTH_SHORT).show();
                setupMotivationalNotifications();
            } else {
                Toast.makeText(this, "Las notificaciones están deshabilitadas. Puedes habilitarlas en Configuración.", Toast.LENGTH_LONG).show();
            }
        }
    }
}