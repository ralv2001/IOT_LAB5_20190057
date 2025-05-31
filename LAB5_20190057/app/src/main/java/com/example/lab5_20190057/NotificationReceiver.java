package com.example.lab5_20190057;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String EXTRA_HABIT_NAME = "habit_name";
    public static final String EXTRA_HABIT_CATEGORY = "habit_category";
    public static final String EXTRA_NOTIFICATION_TYPE = "notification_type";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_CHANNEL_ID = "channel_id";

    public static final String TYPE_HABIT = "habit";
    public static final String TYPE_MOTIVATIONAL = "motivational";

    @Override
    public void onReceive(Context context, Intent intent) {
        String notificationType = intent.getStringExtra(EXTRA_NOTIFICATION_TYPE);

        if (TYPE_HABIT.equals(notificationType)) {
            showHabitNotification(context, intent);
        } else if (TYPE_MOTIVATIONAL.equals(notificationType)) {
            showMotivationalNotification(context, intent);
        }
    }

    private void showHabitNotification(Context context, Intent intent) {
        String habitName = intent.getStringExtra(EXTRA_HABIT_NAME);
        String category = intent.getStringExtra(EXTRA_HABIT_CATEGORY);
        String channelId = intent.getStringExtra(EXTRA_CHANNEL_ID);

        String actionSuggestion = getActionSuggestion(habitName, category);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent para abrir la app al hacer clic en la notificación
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(getCategoryIcon(category))
                .setContentTitle("Recordatorio: " + habitName)
                .setContentText(actionSuggestion)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(actionSuggestion))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 250, 500});

        int notificationId = habitName.hashCode();
        notificationManager.notify(notificationId, builder.build());
    }

    private void showMotivationalNotification(Context context, Intent intent) {
        String message = intent.getStringExtra(EXTRA_MESSAGE);

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Intent para abrir la app
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                NotificationHelper.CHANNEL_MOTIVATIONAL)
                .setSmallIcon(R.drawable.ic_motivational)
                .setContentTitle("Mensaje Motivacional")
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(9999, builder.build());
    }

    private String getActionSuggestion(String habitName, String category) {
        switch (category.toLowerCase()) {
            case "ejercicio":
                return "¡Es hora de " + habitName + "! 💪 Mantente activo y fuerte.";
            case "alimentación":
                return "Recordatorio: " + habitName + " 🥗 Cuida tu alimentación.";
            case "sueño":
                return "Es momento de " + habitName + " 😴 Descansa bien.";
            case "lectura":
                return "Tiempo de " + habitName + " 📚 Alimenta tu mente.";
            case "trabajo":
                return "Recordatorio: " + habitName + " 💼 ¡A ser productivo!";
            case "salud":
                return "No olvides " + habitName + " 🏥 Tu salud es prioridad.";
            case "meditación":
                return "Momento de " + habitName + " 🧘 Encuentra tu paz interior.";
            case "estudio":
                return "Hora de " + habitName + " 📖 El conocimiento es poder.";
            case "creatividad":
                return "Es tiempo de " + habitName + " 🎨 Deja fluir tu creatividad.";
            case "social":
                return "Recordatorio: " + habitName + " 👥 Conecta con otros.";
            default:
                return "¡Es hora de practicar tu hábito: " + habitName + "!";
        }
    }

    private int getCategoryIcon(String category) {
        switch (category.toLowerCase()) {
            case "ejercicio":
                return R.drawable.ic_exercise;
            case "alimentación":
                return R.drawable.ic_food;
            case "sueño":
                return R.drawable.ic_sleep;
            case "lectura":
                return R.drawable.ic_book;
            case "trabajo":
                return R.drawable.ic_work;
            case "salud":
                return R.drawable.ic_health;
            default:
                return R.drawable.ic_default_habit;
        }
    }
}