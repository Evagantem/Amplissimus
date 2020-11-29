package de.amplus.amplissimus.ui.login;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import de.amplus.amplissimus.R;

public class SaveDataModalBottomSheetDialog extends BottomSheetDialogFragment {

    private BottomSheetListener bottomSheetListener;
    private boolean clickedAny = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_save_data, container, false);

        Button btnCancel = view.findViewById(R.id.cancel_button);
        Button btnSave = view.findViewById(R.id.save_button);

        btnCancel.setOnClickListener(v -> {
            bottomSheetListener.dialogAnswered(0);
            clickedAny = true;
            dismiss();
        });

        btnSave.setOnClickListener(v -> {
            bottomSheetListener.dialogAnswered(1);
            clickedAny = true;
            dismiss();
        });

        return view;
    }

    public interface BottomSheetListener {
        void dialogAnswered(int i);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        System.out.println("Dismissing...");
        if(!clickedAny) {
            bottomSheetListener.dialogAnswered(-1);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            bottomSheetListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }
}
