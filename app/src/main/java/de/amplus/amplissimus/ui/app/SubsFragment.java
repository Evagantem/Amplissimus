package de.amplus.amplissimus.ui.app;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import de.amplus.amplissimus.R;
import de.amplus.amplissimus.data.Functions;
import de.amplus.amplissimus.services.DSBService;
import de.amplus.amplissimus.services.Prefs;

public class SubsFragment extends Fragment {

    private List<DSBService.Plan> plans = new ArrayList<>();
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean portableSession = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.plans.addAll(DSBService.getFilteredPlans());
            if (!portableSession) new Prefs(requireActivity()).setPlans(plans);
        } catch (Exception e) {
            e.printStackTrace();
            this.plans = new ArrayList<>();
        }
        if(Functions.isOffline(requireActivity()))
            Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    getString(R.string.connection_failed),
                    Snackbar.LENGTH_LONG
            ).setAction("Action", null).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subs, container, false);
        portableSession = ((MainActivity) requireActivity()).isPortableSession();

        swipeRefreshLayout = (SwipeRefreshLayout) view;
        swipeRefreshLayout.setOnRefreshListener(this::updatePlans);

        recyclerView = view.findViewById(R.id.plan_list);
        recyclerView.setAdapter(new PlansAdapter(plans));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return view;
    }

    private void updatePlans() {
        if(Functions.isOffline(requireActivity())) {
            swipeRefreshLayout.setRefreshing(false);
            Functions.makeSnackbar(
                    requireActivity(),
                    "Internetverbindung fehlgeschlagen!"
            );
        }
        else {
            new Thread(() -> {
                try {
                    List<DSBService.Plan> plans = new DSBService().parseTimetables();
                    if(!portableSession) new Prefs(requireActivity()).setPlans(plans);
                    DSBService.setPlans(plans);
                } catch (Exception e) {
                    e.printStackTrace();
                    Functions.makeSnackbar(
                            requireActivity(),
                            "Fehler beim Interpretieren des Vertretungsplans!"
                    );
                }
                requireActivity().runOnUiThread(() -> {
                    updateRecyclerView();
                    stopRefreshing();
                });
            }).start();
        }
    }

    public void updateRecyclerView() {
        plans.clear();
        plans.addAll(DSBService.getFilteredPlans());
        if(recyclerView.getAdapter() == null) return;
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.getAdapter().notifyItemChanged(0);
    }

    private void stopRefreshing() {
        if(swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
    }
}

