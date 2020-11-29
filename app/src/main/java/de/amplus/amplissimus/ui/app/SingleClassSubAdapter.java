package de.amplus.amplissimus.ui.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.amplus.amplissimus.R;
import de.amplus.amplissimus.services.DSBService;

public class SingleClassSubAdapter extends RecyclerView.Adapter<SingleClassSubAdapter.ViewHolder> {

    private List<DSBService.ClassSubs> classSubsList;

    public SingleClassSubAdapter(List<DSBService.ClassSubs> classSubsList) {
        this.classSubsList = classSubsList;
    }

    @NonNull
    @Override
    public SingleClassSubAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.class_subs_list_item, parent, false);
        return new SingleClassSubAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SingleClassSubAdapter.ViewHolder holder, int position) {
        DSBService.ClassSubs classSubs = classSubsList.get(position);
        holder.item = classSubs;
        holder.recyclerView.setAdapter(new SubsAdapter(classSubs.getSubs()));
        holder.classTextView.setText(classSubs.getClassName());
    }



    @Override
    public int getItemCount() {
        return classSubsList.size();
    }

    public List<DSBService.ClassSubs> getClassSubList() {
        return classSubsList;
    }

    public void setClassSubList(List<DSBService.ClassSubs> plans) {
        this.classSubsList = plans;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView classTextView;
        public final RecyclerView recyclerView;
        public DSBService.ClassSubs item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.classTextView = view.findViewById(R.id.represented_class_name);
            recyclerView = view.findViewById(R.id.subs_list);
        }
    }
}
