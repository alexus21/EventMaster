package ues.pdm24.eventmaster.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ues.pdm24.eventmaster.R;

public class NewEventActivity extends AppCompatActivity {
    Spinner spinnerEventCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.newEventActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spinnerEventCategories = findViewById(R.id.spinnerEventCategories);

        // Categoprías de eventos de ejemplo para el spinner
        String[] eventCategories = new String[] {
            "Concierto",
            "Deportivo",
            "Cultural",
            "Social",
            "Religioso",
            "Académico",
            "Empresarial",
            "Familiar",
        };

        // Crear un ArrayAdapter para el spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, eventCategories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventCategories.setAdapter(adapter);
    }
}
