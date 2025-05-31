package com.example.lab5_20190057;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateHabitActivity extends AppCompatActivity {

    private TextInputEditText etHabitName;
    private Spinner spinnerCategory;
    private TextInputEditText etFrequencyHours;
    private Button btnSelectDate;
    private Button btnSelectTime;
    private Button btnCancel;
    private Button btnSaveHabit;
    private ImageView btnBack;

    private Calendar selectedDate;
    private Calendar selectedTime;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_habit);

        initializeViews();
        initializeDateTimeFormatters();
        setupSpinner();
        setupClickListeners();
        setDefaultDateTime();
    }

    private void initializeViews() {
        etHabitName = findViewById(R.id.etHabitName);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        etFrequencyHours = findViewById(R.id.etFrequencyHours);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnSelectTime = findViewById(R.id.btnSelectTime);
        btnCancel = findViewById(R.id.btnCancel);
        btnSaveHabit = findViewById(R.id.btnSaveHabit);
        btnBack = findViewById(R.id.btnBack);
    }

    private void initializeDateTimeFormatters() {
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        selectedDate = Calendar.getInstance();
        selectedTime = Calendar.getInstance();
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.habit_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Botón de regreso
        btnBack.setOnClickListener(v -> finish());

        // Botón cancelar
        btnCancel.setOnClickListener(v -> finish());

        // Botón seleccionar fecha
        btnSelectDate.setOnClickListener(v -> showDatePicker());

        // Botón seleccionar hora
        btnSelectTime.setOnClickListener(v -> showTimePicker());

        // Botón guardar hábito
        btnSaveHabit.setOnClickListener(v -> saveHabit());
    }

    private void setDefaultDateTime() {
        btnSelectDate.setText(dateFormat.format(selectedDate.getTime()));
        btnSelectTime.setText(timeFormat.format(selectedTime.getTime()));
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(Calendar.YEAR, year);
                    selectedDate.set(Calendar.MONTH, month);
                    selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    btnSelectDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);

                    btnSelectTime.setText(timeFormat.format(selectedTime.getTime()));
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                true // Formato 24 horas
        );

        timePickerDialog.show();
    }

    private void saveHabit() {
        // Validar campos
        if (!validateFields()) {
            return;
        }

        // Obtener datos del formulario
        String habitName = etHabitName.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        int frequencyHours = Integer.parseInt(etFrequencyHours.getText().toString().trim());
        String startDate = dateFormat.format(selectedDate.getTime());
        String startTime = timeFormat.format(selectedTime.getTime());

        // Crear objeto Habit
        Habit newHabit = new Habit(habitName, category, frequencyHours, startDate, startTime);

        // Devolver el resultado a HabitsListActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_habit", newHabit);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Hábito creado exitosamente", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean validateFields() {
        // Validar nombre del hábito
        if (etHabitName.getText().toString().trim().isEmpty()) {
            etHabitName.setError("Por favor ingresa el nombre del hábito");
            etHabitName.requestFocus();
            return false;
        }

        // Validar frecuencia
        String frequencyText = etFrequencyHours.getText().toString().trim();
        if (frequencyText.isEmpty()) {
            etFrequencyHours.setError("Por favor ingresa la frecuencia");
            etFrequencyHours.requestFocus();
            return false;
        }

        try {
            int frequency = Integer.parseInt(frequencyText);
            if (frequency <= 0 || frequency > 168) { // Máximo 168 horas = 1 semana
                etFrequencyHours.setError("La frecuencia debe estar entre 1 y 168 horas");
                etFrequencyHours.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etFrequencyHours.setError("Por favor ingresa un número válido");
            etFrequencyHours.requestFocus();
            return false;
        }

        // Validar que se haya seleccionado una categoría
        if (spinnerCategory.getSelectedItem() == null) {
            Toast.makeText(this, "Por favor selecciona una categoría", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar que la fecha y hora no sean del pasado
        Calendar now = Calendar.getInstance();
        Calendar selectedDateTime = Calendar.getInstance();
        selectedDateTime.set(
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH),
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                0
        );

        if (selectedDateTime.before(now)) {
            Toast.makeText(this, "La fecha y hora de inicio no pueden ser del pasado", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void clearFields() {
        etHabitName.setText("");
        etFrequencyHours.setText("");
        spinnerCategory.setSelection(0);
        setDefaultDateTime();
    }

    @Override
    public void onBackPressed() {
        // Mostrar confirmación si hay datos ingresados
        if (hasUnsavedChanges()) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("¿Descartar cambios?")
                    .setMessage("Tienes cambios sin guardar. ¿Estás seguro de que quieres salir?")
                    .setPositiveButton("Salir", (dialog, which) -> finish())
                    .setNegativeButton("Cancelar", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    private boolean hasUnsavedChanges() {
        return !etHabitName.getText().toString().trim().isEmpty() ||
                !etFrequencyHours.getText().toString().trim().isEmpty();
    }
}