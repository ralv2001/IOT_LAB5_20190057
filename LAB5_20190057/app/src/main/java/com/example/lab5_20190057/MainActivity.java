package com.example.lab5_20190057;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.graphics.Matrix;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.exifinterface.media.ExifInterface;
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
                    // Cargar imagen con optimización de memoria
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    options.inSampleSize = 2; // Reduce a la mitad para ahorrar memoria

                    FileInputStream fis = new FileInputStream(imageFile);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
                    fis.close();

                    if (bitmap != null) {
                        ivProfileImage.setImageBitmap(bitmap);
                        ivProfileImage.setPadding(0, 0, 0, 0);
                    } else {
                        // Si falla la carga, resetear a imagen por defecto
                        resetToDefaultImage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                    resetToDefaultImage();
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error de memoria al cargar imagen", Toast.LENGTH_SHORT).show();
                    resetToDefaultImage();
                }
            } else {
                resetToDefaultImage();
            }
        }
    }

    /**
     * Resetea la imagen a la por defecto
     */
    private void resetToDefaultImage() {
        ivProfileImage.setImageResource(R.drawable.ic_add_photo);
        ivProfileImage.setPadding(24, 24, 24, 24);

        // Limpiar la referencia en SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_PROFILE_IMAGE);
        editor.apply();
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

        // Verificar permisos de notificación y mostrar diálogo explicativo
        checkNotificationPermissions();

        // Configurar notificaciones motivacionales si ya están configuradas
        setupMotivationalNotifications();
    }

    private void checkNotificationPermissions() {
        // Para Android 13+ (API 33+) se necesita permiso explícito
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                showNotificationPermissionDialog();
            }
        } else {
            // Para versiones anteriores, verificar si las notificaciones están habilitadas
            if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                showNotificationSettingsDialog();
            }
        }
    }

    private void showNotificationPermissionDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🔔 Permisos de Notificación")
                .setMessage("Esta app necesita enviar notificaciones para recordarte tus hábitos y motivarte.\n\n" +
                        "¿Deseas permitir las notificaciones?")
                .setPositiveButton("Permitir", (dialog, which) -> {
                    // Solicitar permiso de notificaciones
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                NOTIFICATION_PERMISSION_CODE);
                    }
                })
                .setNegativeButton("Ahora no", (dialog, which) -> {
                    Toast.makeText(this, "Puedes habilitar las notificaciones más tarde en Configuración",
                            Toast.LENGTH_LONG).show();
                })
                .setCancelable(false)
                .show();
    }

    private void showNotificationSettingsDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🔔 Notificaciones Deshabilitadas")
                .setMessage("Las notificaciones están deshabilitadas para esta app.\n\n" +
                        "Para recibir recordatorios de tus hábitos, ve a Configuración y habilita las notificaciones.")
                .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                    // Abrir configuración de la app
                    Intent intent = new Intent();
                    intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                    intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    Toast.makeText(this, "Las notificaciones no funcionarán hasta que las habilites",
                            Toast.LENGTH_LONG).show();
                })
                .show();
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
                    // Convertir la imagen seleccionada a bitmap con optimización
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);

                    // Primero obtener las dimensiones sin cargar la imagen completa
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, options);
                    inputStream.close();

                    // Calcular el factor de escala para redimensionar
                    int imageHeight = options.outHeight;
                    int imageWidth = options.outWidth;
                    int scaleFactor = calculateInSampleSize(options, 300, 300); // Máximo 300x300

                    // Cargar la imagen redimensionada
                    inputStream = getContentResolver().openInputStream(imageUri);
                    options.inJustDecodeBounds = false;
                    options.inSampleSize = scaleFactor;
                    options.inPreferredConfig = Bitmap.Config.RGB_565; // Usa menos memoria

                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                    inputStream.close();

                    if (bitmap != null) {
                        // Corregir orientación si es necesario
                        bitmap = correctImageOrientation(imageUri, bitmap);

                        // Guardar imagen en internal storage
                        String fileName = "profile_image_" + System.currentTimeMillis() + ".jpg";
                        saveImageToInternalStorage(bitmap, fileName);

                        // Mostrar imagen en el ImageView
                        ivProfileImage.setImageBitmap(bitmap);
                        ivProfileImage.setPadding(0, 0, 0, 0);

                        // Guardar la ruta en SharedPreferences
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(KEY_PROFILE_IMAGE, fileName);
                        editor.apply();

                        Toast.makeText(this, "Imagen guardada exitosamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Imagen demasiado grande. Intenta con una imagen más pequeña.", Toast.LENGTH_LONG).show();
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

    /**
     * Calcula el factor de escala para redimensionar la imagen
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /**
     * Corrige la orientación de la imagen basada en los datos EXIF
     */
    private Bitmap correctImageOrientation(Uri imageUri, Bitmap bitmap) {
        try {
            InputStream input = getContentResolver().openInputStream(imageUri);
            ExifInterface exif = new ExifInterface(input);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            input.close();

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                    matrix.postScale(-1, 1);
                    break;
                case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                    matrix.postScale(1, -1);
                    break;
                default:
                    return bitmap; // No need to rotate
            }

            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            if (rotatedBitmap != bitmap) {
                bitmap.recycle(); // Liberar memoria del bitmap original
            }
            return rotatedBitmap;

        } catch (IOException e) {
            e.printStackTrace();
            return bitmap; // Return original if rotation fails
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
                Toast.makeText(this, "✅ Permisos de almacenamiento concedidos", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "⚠️ Sin permisos de almacenamiento no podrás cambiar tu foto de perfil",
                        Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "🎉 ¡Genial! Ahora recibirás notificaciones de tus hábitos",
                        Toast.LENGTH_LONG).show();
                setupMotivationalNotifications();
            } else {
                // El usuario negó el permiso
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Notificaciones Denegadas")
                        .setMessage("Sin notificaciones no podrás recibir recordatorios de tus hábitos.\n\n" +
                                "¿Quieres ir a Configuración para habilitarlas?")
                        .setPositiveButton("Ir a Configuración", (dialog, which) -> {
                            Intent intent = new Intent();
                            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
                            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        }
    }
}