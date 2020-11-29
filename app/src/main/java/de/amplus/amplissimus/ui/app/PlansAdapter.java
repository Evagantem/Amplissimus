package de.amplus.amplissimus.ui.app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import de.amplus.amplissimus.R;
import de.amplus.amplissimus.services.DSBService;

import static android.content.Context.CLIPBOARD_SERVICE;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.ViewHolder> {

    private List<DSBService.Plan> plans;

    public PlansAdapter(List<DSBService.Plan> plans) {
        this.plans = plans;
    }

    @NonNull
    @Override
    public PlansAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.plan_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlansAdapter.ViewHolder holder, int position) {
        DSBService.Plan plan = plans.get(position);
        holder.item = plan;
        if(plan.getSubstitutions().isEmpty()) {
            holder.recyclerView.setVisibility(View.GONE);
            holder.view.findViewById(R.id.no_subs_text).setVisibility(View.VISIBLE);
        } else {
            holder.recyclerView.setVisibility(View.VISIBLE);
            holder.view.findViewById(R.id.no_subs_text).setVisibility(View.GONE);
            holder.recyclerView.setAdapter(new SingleClassSubAdapter(plan.getSubstitutions()));
        }
        holder.planTitleTextView.setText(plan.getTitle());
        holder.copyImageButton.setOnClickListener(v -> {
            ClipboardManager clipboard =
                    (ClipboardManager) holder.view.getContext().getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("DSB-Schedule", holder.item.getUrl());
            clipboard.setPrimaryClip(clip);
            Activity activity = (Activity) holder.view.getContext();
            Snackbar.make(
                    activity.findViewById(android.R.id.content),
                    "URL kopiert!",
                    Snackbar.LENGTH_LONG
            ).setAction("Action", null).show();
        });
    }

    @Override
    public int getItemCount() {
        return plans.size();
    }

    public List<DSBService.Plan> getPlans() {
        return plans;
    }

    public void setPlans(List<DSBService.Plan> plans) {
        this.plans = plans;
    }




    static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView planTitleTextView;
        public final RecyclerView recyclerView;
        public final ImageButton copyImageButton;
        public DSBService.Plan item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            copyImageButton = view.findViewById(R.id.copy_button);
            planTitleTextView = view.findViewById(R.id.item_plan_title);
            recyclerView = view.findViewById(R.id.class_subs_list);
        }
    }
}
