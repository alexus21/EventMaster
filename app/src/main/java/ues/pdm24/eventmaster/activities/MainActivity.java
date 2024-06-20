package ues.pdm24.eventmaster.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.activities.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Obtener una instancia de SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);

        // Verificar si el usuario está registrado
        boolean isUserRegistered = sharedPreferences.getBoolean("isUserRegistered", false);

        // Si el usuario no está registrado, redirigir a la pantalla de registro
        if (!isUserRegistered) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        startActivity(new Intent(this, HomeActivity.class));
    }
}
