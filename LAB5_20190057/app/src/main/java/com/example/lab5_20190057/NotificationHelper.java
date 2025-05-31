package com.example.lab5_20190057;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {

    // Canales de notificación
    public static final String CHANNEL_EXERCISE = "exercise_channel";
    public static final String CHANNEL_FOOD = "food_channel";
    public static final String CHANNEL_SLEEP = "sleep_channel";
    public static final String CHANNEL_READING = "reading_channel";
    public static final String CHANNEL_WORK = "work_channel";
    public static final String CHANNEL_HEALTH = "health_channel";
    public static final String CHANNEL_MEDITATION = "meditation_channel";
    public static final String CHANNEL_STUDY = "study_channel";
    public static final String CHANNEL_CREATIVITY = "creativity_channel";
    public static final String CHANNEL_SOCIAL = "social_channel";
    public static final String CHANNEL_MOTIVATIONAL = "motivational_channel";

    private Context context;
    private NotificationManager notificationManager;
    private AlarmManager alarmManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // Canal para ejercicio
            NotificationChannel exerciseChannel = new NotificationChannel(
                    CHANNEL_EXERCISE,
                    "Ejercicio",
                    NotificationManager.IMPORTANCE_HIGH
            );
            exerciseChannel.setDescription("Notificaciones para hábitos de ejercicio");
            exerciseChannel.enableVibration(true);
            exerciseChannel.setVibrationPattern(new long[]{0, 500, 250, 500});
            notificationManager.createNotificationChannel(exerciseChannel);

            // Canal para alimentación
            NotificationChannel foodChannel = new NotificationChannel(
                    CHANNEL_FOOD,
                    "Alimentación",
                    NotificationManager.IMPORTANCE_HIGH
            );
            foodChannel.setDescription("Notificaciones para hábitos de alimentación");
            foodChannel.enableVibration(true);
            notificationManager.createNotificationChannel(foodChannel);

            // Canal para sueño
            NotificationChannel sleepChannel = new NotificationChannel(
                    CHANNEL_SLEEP,
                    "Sueño",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            sleepChannel.setDescription("Notificaciones para hábitos de sueño");
            sleepChannel.enableVibration(false);
            notificationManager.createNotificationChannel(sleepChannel);

            // Canal para lectura
            NotificationChannel readingChannel = new NotificationChannel(
                    CHANNEL_READING,
                    "Lectura",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            readingChannel.setDescription("Notificaciones para hábitos de lectura");
            notificationManager.createNotificationChannel(readingChannel);

            // Canal para trabajo
            NotificationChannel workChannel = new NotificationChannel(
                    CHANNEL_WORK,
                    "Trabajo",
                    NotificationManager.IMPORTANCE_HIGH
            );
            workChannel.setDescription("Notificaciones para hábitos de trabajo");
            workChannel.enableVibration(true);
            notificationManager.createNotificationChannel(workChannel);

            // Canal para salud
            NotificationChannel healthChannel = new NotificationChannel(
                    CHANNEL_HEALTH,
                    "Salud",
                    NotificationManager.IMPORTANCE_HIGH
            );
            healthChannel.setDescription("Notificaciones para hábitos de salud");
            healthChannel.enableVibration(true);
            notificationManager.createNotificationChannel(healthChannel);

            // Canal para meditación
            NotificationChannel meditationChannel = new NotificationChannel(
                    CHANNEL_MEDITATION,
                    "Meditación",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            meditationChannel.setDescription("Notificaciones para hábitos de meditación");
            meditationChannel.enableVibration(false);
            notificationManager.createNotificationChannel(meditationChannel);

            // Canal para estudio
            NotificationChannel studyChannel = new NotificationChannel(
                    CHANNEL_STUDY,
                    "Estudio",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            studyChannel.setDescription("Notificaciones para hábitos de estudio");
            notificationManager.createNotificationChannel(studyChannel);

            // Canal para creatividad
            NotificationChannel creativityChannel = new NotificationChannel(
                    CHANNEL_CREATIVITY,
                    "Creatividad",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            creativityChannel.setDescription("Notificaciones para hábitos creativos");
            notificationManager.createNotificationChannel(creativityChannel);

            // Canal para social
            NotificationChannel socialChannel = new NotificationChannel(
                    CHANNEL_SOCIAL,
                    "Social",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            socialChannel.setDescription("Notificaciones para hábitos sociales");
            notificationManager.createNotificationChannel(socialChannel);

            // Canal motivacional
            NotificationChannel motivationalChannel = new NotificationChannel(
                    CHANNEL_MOTIVATIONAL,
                    "Motivacional",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            motivationalChannel.setDescription("Notificaciones motivacionales");
            notificationManager.createNotificationChannel(motivationalChannel);
        }
    }

    public void scheduleHabitNotification(Habit habit) {
        String channelId = getCategoryChannelId(habit.getCategory());

        // Calcular el tiempo para la primera notificación
        Calendar firstNotification = getFirstNotificationTime(habit);

        // Crear intent para la notificación
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_NOTIFICATION_TYPE, NotificationReceiver.TYPE_HABIT);
        intent.putExtra(NotificationReceiver.EXTRA_HABIT_NAME, habit.getName());
        intent.putExtra(NotificationReceiver.EXTRA_HABIT_CATEGORY, habit.getCategory());
        intent.putExtra(NotificationReceiver.EXTRA_CHANNEL_ID, channelId);

        int requestCode = habit.getId().hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Programar alarma repetitiva
        long intervalMillis = habit.getFrequencyHours() * 60 * 60 * 1000L;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Para Android 6.0+ usar setExactAndAllowWhileIdle para mayor precisión
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    firstNotification.getTimeInMillis(),
                    intervalMillis,
                    pendingIntent
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Para Android 4.4+ usar setExact
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    firstNotification.getTimeInMillis(),
                    intervalMillis,
                    pendingIntent
            );
        } else {
            // Para versiones anteriores usar set normal
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    firstNotification.getTimeInMillis(),
                    intervalMillis,
                    pendingIntent
            );
        }

        // Log para debugging
        Log.d("NotificationHelper", "Notificación programada para: " + habit.getName() +
                " - Primera notificación: " + firstNotification.getTime() +
                " - Frecuencia: cada " + habit.getFrequencyHours() + " horas");
    }

    public void cancelHabitNotification(String habitId) {
        Intent intent = new Intent(context, NotificationReceiver.class);
        int requestCode = habitId.hashCode();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    public void scheduleMotivationalNotifications(String message, int frequencyHours) {
        // Cancelar notificaciones previas
        cancelMotivationalNotifications();

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra(NotificationReceiver.EXTRA_NOTIFICATION_TYPE, NotificationReceiver.TYPE_MOTIVATIONAL);
        intent.putExtra(NotificationReceiver.EXTRA_MESSAGE, message);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                10000, // ID único para notificaciones motivacionales
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Programar primera notificación en X horas
        Calendar firstTime = Calendar.getInstance();
        firstTime.add(Calendar.HOUR_OF_DAY, frequencyHours);

        long intervalMillis = frequencyHours * 60 * 60 * 1000L;

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                firstTime.getTimeInMillis(),
                intervalMillis,
                pendingIntent
        );

        // Log para debugging
        Log.d("NotificationHelper", "Notificaciones motivacionales programadas - " +
                "Mensaje: " + message + " - Cada " + frequencyHours + " horas" +
                " - Primera notificación: " + firstTime.getTime());
    }

    public void cancelMotivationalNotifications() {
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                10000,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    private Calendar getFirstNotificationTime(Habit habit) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        Calendar calendar = Calendar.getInstance();

        try {
            Date startDate = dateFormat.parse(habit.getStartDate());
            Date startTime = timeFormat.parse(habit.getStartTime());

            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(startDate);

            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(startTime);

            calendar.set(Calendar.YEAR, dateCalendar.get(Calendar.YEAR));
            calendar.set(Calendar.MONTH, dateCalendar.get(Calendar.MONTH));
            calendar.set(Calendar.DAY_OF_MONTH, dateCalendar.get(Calendar.DAY_OF_MONTH));
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);

            // Si la hora ya pasó para hoy, programar para la próxima repetición
            Calendar now = Calendar.getInstance();
            if (calendar.before(now)) {
                long frequencyMillis = habit.getFrequencyHours() * 60 * 60 * 1000L;
                while (calendar.before(now)) {
                    calendar.setTimeInMillis(calendar.getTimeInMillis() + frequencyMillis);
                }
            }

        } catch (ParseException e) {
            e.printStackTrace();
            // Si hay error, programar para una hora desde ahora
            calendar.add(Calendar.HOUR_OF_DAY, 1);
        }

        return calendar;
    }

    private String getCategoryChannelId(String category) {
        switch (category.toLowerCase()) {
            case "ejercicio":
                return CHANNEL_EXERCISE;
            case "alimentación":
                return CHANNEL_FOOD;
            case "sueño":
                return CHANNEL_SLEEP;
            case "lectura":
                return CHANNEL_READING;
            case "trabajo":
                return CHANNEL_WORK;
            case "salud":
                return CHANNEL_HEALTH;
            case "meditación":
                return CHANNEL_MEDITATION;
            case "estudio":
                return CHANNEL_STUDY;
            case "creatividad":
                return CHANNEL_CREATIVITY;
            case "social":
                return CHANNEL_SOCIAL;
            default:
                return CHANNEL_EXERCISE;
        }
    }
}