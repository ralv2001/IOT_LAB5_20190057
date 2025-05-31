package com.example.lab5_20190057;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HabitsAdapter extends RecyclerView.Adapter<HabitsAdapter.HabitViewHolder> {

    private Context context;
    private List<Habit> habitsList;
    private OnHabitDeleteListener deleteListener;

    public interface OnHabitDeleteListener {
        void onHabitDelete(Habit habit, int position);
    }

    public HabitsAdapter(Context context, List<Habit> habitsList) {
        this.context = context;
        this.habitsList = habitsList;
    }

    public void setOnHabitDeleteListener(OnHabitDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitsList.get(position);

        // Configurar los datos del hábito
        holder.tvHabitName.setText(habit.getName());
        holder.tvCategory.setText(habit.getCategory());

        // Configurar frecuencia
        String frequencyText = "Cada " + habit.getFrequencyHours() + " horas";
        holder.tvFrequency.setText(frequencyText);

        // Configurar fecha y hora de inicio
        String startDateTime = "Inicio: " + habit.getStartDate() + " - " + habit.getStartTime();
        holder.tvStartDateTime.setText(startDateTime);

        // Configurar icono y color de categoría
        holder.ivCategoryIcon.setImageResource(habit.getCategoryIcon());
        int categoryColor = ContextCompat.getColor(context, habit.getCategoryColor());
        holder.ivCategoryBackground.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(categoryColor)
        );

        // Configurar listener para el botón eliminar
        holder.btnDeleteHabit.setOnClickListener(v -> showDeleteConfirmationDialog(habit, position));
    }

    @Override
    public int getItemCount() {
        return habitsList.size();
    }

    private void showDeleteConfirmationDialog(Habit habit, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Eliminar Hábito")
                .setMessage("¿Estás seguro de que quieres eliminar el hábito '" + habit.getName() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    if (deleteListener != null) {
                        deleteListener.onHabitDelete(habit, position);
                    }
                    Toast.makeText(context, "Hábito eliminado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    // Método para actualizar la lista de hábitos
    public void updateHabits(List<Habit> newHabits) {
        this.habitsList = newHabits;
        notifyDataSetChanged();
    }

    // Método para agregar un nuevo hábito
    public void addHabit(Habit habit) {
        habitsList.add(habit);
        notifyItemInserted(habitsList.size() - 1);
    }

    // Método para eliminar un hábito
    public void removeHabit(int position) {
        if (position >= 0 && position < habitsList.size()) {
            habitsList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, habitsList.size());
        }
    }

    // ViewHolder class
    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView tvHabitName;
        TextView tvCategory;
        TextView tvFrequency;
        TextView tvStartDateTime;
        ImageView ivCategoryIcon;
        ImageView ivCategoryBackground;
        ImageButton btnDeleteHabit;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);

            tvHabitName = itemView.findViewById(R.id.tvHabitName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvStartDateTime = itemView.findViewById(R.id.tvStartDateTime);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
            ivCategoryBackground = itemView.findViewById(R.id.ivCategoryBackground);
            btnDeleteHabit = itemView.findViewById(R.id.btnDeleteHabit);
        }
    }
}