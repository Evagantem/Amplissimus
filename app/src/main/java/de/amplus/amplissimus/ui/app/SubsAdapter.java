package de.amplus.amplissimus.ui.app;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import de.amplus.amplissimus.R;
import de.amplus.amplissimus.services.DSBService;

import java.util.List;

public class SubsAdapter extends RecyclerView.Adapter<SubsAdapter.ViewHolder> {

    private final List<DSBService.Substitution> subs;

    public SubsAdapter(List<DSBService.Substitution> items) {
        subs = items;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sub_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NotNull final ViewHolder holder, int position) {
        DSBService.Substitution sub = subs.get(position);
        if(sub.isFree()) ((CardView) holder.view).setCardBackgroundColor(
                Color.rgb(0, 155, 3)
        );
        holder.item = sub;
        holder.hoursTextView.setText(sub.getHours());
        holder.subjectTeacherTextView.setText(
                sub.getOrigTeacher() == null
                        ? sub.getSubject()
                        : String.format("%s - %s", sub.getSubject(), sub.getOrigTeacher())
        );
        holder.substTeacherTextView.setText(
                sub.isFree()
                        ? "Entfall" + sub.getViewableNotes()
                        : String.format("Vertreten durch %s%s", sub.getNewTeacher(), sub.getViewableNotes())
        );
        holder.view.setOnClickListener(v -> {
            Activity activity = (Activity) v.getContext();
            Intent intent = new Intent(activity, ViewSubActivity.class)
                    .putExtra("sub", new Gson().toJson(holder.item));
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return subs.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView hoursTextView;
        public final TextView subjectTeacherTextView;
        public final TextView substTeacherTextView;
        public DSBService.Substitution item;

        public ViewHolder(@NotNull View view) {
            super(view);
            this.view = view;
            hoursTextView = view.findViewById(R.id.item_hours);
            subjectTeacherTextView = view.findViewById(R.id.item_subject_teacher);
            substTeacherTextView = view.findViewById(R.id.item_subst_teacher);
        }
    }
}