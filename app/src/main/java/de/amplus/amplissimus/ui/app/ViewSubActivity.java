package de.amplus.amplissimus.ui.app;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;

import de.amplus.amplissimus.R;
import de.amplus.amplissimus.services.DSBService;

public class ViewSubActivity extends AppCompatActivity {

    private DSBService.Substitution sub;
    private ConstraintLayout constraintLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_sub);
        sub = new Gson()
                .fromJson(getIntent().getStringExtra("sub"), DSBService.Substitution.class);
        constraintLayout = findViewById(R.id.view_subs_constraint_layout);
        if(sub.isFree()) {
            constraintLayout.setBackgroundColor(Color.rgb(0, 155, 3));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(Color.rgb(0, 155, 3));
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(Color.rgb(255, 136, 47));
        }

        ((TextView)findViewById(R.id.affected_class)).setText(sub.getAffectedClass());
        ((TextView)findViewById(R.id.subject)).setText(sub.getSubject());
        ((TextView)findViewById(R.id.orig_teacher)).setText(sub.getOrigTeacher());
        ((TextView)findViewById(R.id.new_teacher))
                .setText(sub.isFree() ? "Keine Vertretung (Entfall)" : sub.getNewTeacher());
        ((TextView)findViewById(R.id.notes))
                .setText(sub.hasNotes() ? sub.getNotes() : "Keine Bemerkung");

        findViewById(R.id.root_card_view).setBackgroundResource(R.drawable.card_top_corners);

    }
}
