package ues.pdm24.eventmaster.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.activities.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private void checkIfUserIsAuthenticated() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", currentUser.getEmail().split("@")[0]);
            editor.putString("username", currentUser.getEmail().split("@")[0]);
            editor.apply();
            startActivity(new Intent(this, HomeActivity.class));
        }else{
            Toast.makeText(this, "No hay usuario autenticado", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

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
        /*SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);

        // Verificar si el usuario está registrado
        boolean isUserRegistered = sharedPreferences.getBoolean("isUserRegistered", false);*/

        mAuth = FirebaseAuth.getInstance();

        // Si el usuario no está registrado, redirigir a la pantalla de registro
        /*if (!isUserRegistered) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        startActivity(new Intent(this, HomeActivity.class));*/

        checkIfUserIsAuthenticated();
    }

    @Override
    protected void onResume() {
        super.onResume();
        finish();
    }
}
