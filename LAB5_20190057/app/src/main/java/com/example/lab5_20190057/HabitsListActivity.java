package com.example.lab5_20190057;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HabitsListActivity extends AppCompatActivity implements HabitsAdapter.OnHabitDeleteListener {

    private RecyclerView recyclerViewHabits;
    private LinearLayout layoutEmptyState;
    private FloatingActionButton btnAddHabit;
    private ImageView btnBack;

    private HabitsAdapter habitsAdapter;
    private List<Habit> habitsList;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "HabitsAppPrefs";
    private static final String KEY_HABITS_LIST = "habits_list";

    private NotificationHelper notificationHelper;
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habits_list);

        initializeViews();
        initializeData();
        setupRecyclerView();
        setupClickListeners();
        loadHabits();
        updateUI();
    }

    private void initializeViews() {
        recyclerViewHabits = findViewById(R.id.recyclerViewHabits);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnAddHabit = findViewById(R.id.btnAddHabit);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initializeData() {
        habitsList = new ArrayList<>();
        gson = new Gson();
        notificationHelper = new NotificationHelper(this);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    private void setupRecyclerView() {
        habitsAdapter = new HabitsAdapter(this, habitsList);
        habitsAdapter.setOnHabitDeleteListener(this);

        recyclerViewHabits.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHabits.setAdapter(habitsAdapter);
    }

    private void setupClickListeners() {
        // Botón de regreso
        btnBack.setOnClickListener(v -> finish());

        // Botón agregar nuevo hábito
        btnAddHabit.setOnClickListener(v -> {
            Intent intent = new Intent(HabitsListActivity.this, CreateHabitActivity.class);
            startActivityForResult(intent, 1001);
        });
    }

    private void loadHabits() {
        String habitsJson = sharedPreferences.getString(KEY_HABITS_LIST, "");

        if (!habitsJson.isEmpty()) {
            Type listType = new TypeToken<List<Habit>>(){}.getType();
            List<Habit> loadedHabits = gson.fromJson(habitsJson, listType);

            if (loadedHabits != null) {
                habitsList.clear();
                habitsList.addAll(loadedHabits);
                habitsAdapter.notifyDataSetChanged();

                // Programar notificaciones para todos los hábitos cargados
                for (Habit habit : habitsList) {
                    if (habit.isActive()) {
                        notificationHelper.scheduleHabitNotification(habit);
                    }
                }
            }
        }
    }

    private void saveHabits() {
        String habitsJson = gson.toJson(habitsList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_HABITS_LIST, habitsJson);
        editor.apply();
    }

    private void updateUI() {
        if (habitsList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerViewHabits.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerViewHabits.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onHabitDelete(Habit habit, int position) {
        // Cancelar las notificaciones del hábito
        notificationHelper.cancelHabitNotification(habit.getId());

        // Eliminar el hábito de la lista
        habitsList.remove(position);
        habitsAdapter.notifyItemRemoved(position);
        habitsAdapter.notifyItemRangeChanged(position, habitsList.size());

        // Guardar cambios
        saveHabits();

        // Actualizar UI
        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            // Recibir el nuevo hábito desde CreateHabitActivity
            Habit newHabit = (Habit) data.getSerializableExtra("new_habit");

            if (newHabit != null) {
                // Agregar el nuevo hábito a la lista
                habitsList.add(newHabit);
                habitsAdapter.notifyItemInserted(habitsList.size() - 1);

                // *** ESTE ES EL GUARDADO AUTOMÁTICO ***
                saveHabits();

                // Programar notificación para el nuevo hábito
                notificationHelper.scheduleHabitNotification(newHabit);

                // Actualizar UI
                updateUI();

                // Scroll al nuevo item
                recyclerViewHabits.smoothScrollToPosition(habitsList.size() - 1);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar hábitos cuando regresemos a esta actividad
        loadHabits();
        updateUI();

        // Reprogramar notificaciones por si se reinició el dispositivo
        rescheduleAllNotifications();
    }

    // Método público para agregar hábitos (útil para testing o llamadas externas)
    public void addHabit(Habit habit) {
        habitsList.add(habit);
        habitsAdapter.notifyItemInserted(habitsList.size() - 1);
        saveHabits();
        updateUI();
    }

    // Método para obtener la lista de hábitos
    public List<Habit> getHabits() {
        return new ArrayList<>(habitsList);
    }

    // Método para verificar si hay hábitos
    public boolean hasHabits() {
        return !habitsList.isEmpty();
    }

    // Método para reprogramar todas las notificaciones (útil cuando se reinicia la app)
    private void rescheduleAllNotifications() {
        for (Habit habit : habitsList) {
            if (habit.isActive()) {
                // Cancelar notificación existente y reprogramar
                notificationHelper.cancelHabitNotification(habit.getId());
                notificationHelper.scheduleHabitNotification(habit);
            }
        }
    }
}