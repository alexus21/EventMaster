package ues.pdm24.eventmaster.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.firebasedatacollection.FirebaseDataCollection;
import ues.pdm24.eventmaster.validations.NetworkChecker;
import ues.pdm24.eventmaster.validations.UserValidator;

public class LoginActivity extends AppCompatActivity {

    SpannableString spannableString;
    TextView lbl_login_aActivitySignup, lbl_password_forgotten;

    EditText txtUsuarioLogin, txtConstrasenaLogin;
    Button btn_ingresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.LoginActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        lbl_login_aActivitySignup = findViewById(R.id.lbl_login_aActivitySignup);
        lbl_password_forgotten = findViewById(R.id.lbl_password_forgotten);
        txtUsuarioLogin = findViewById(R.id.txtUsuarioLogin);
        txtConstrasenaLogin = findViewById(R.id.txtConstrasenaLogin);
        btn_ingresar = findViewById(R.id.btn_ingresar);

        createSpannableString("¿No tienes cuenta? ¡Regístrate!", lbl_login_aActivitySignup, SignupActivity.class);
        createSpannableString("¿Olvidaste tu contraseña?", lbl_password_forgotten, PasswordForgottenActivity.class);

        btn_ingresar.setOnClickListener(v -> {
            String email = txtUsuarioLogin.getText().toString().trim();
            String password = txtConstrasenaLogin.getText().toString().trim();

            if(NetworkChecker.checkInternetConnection(this)) {
                mostrarMensajeError("No hay conexión a internet");
                return;
            }

            boolean isValid = UserValidator.validateLogin(email, password,
                    txtUsuarioLogin, txtConstrasenaLogin);

            if (!isValid) {
                return;
            }

            FirebaseDataCollection.checkEmail(email, exists -> {
                if (exists) {
                    FirebaseDataCollection.login(email, password, (success) -> {
                        if (success) {
                            iniciarSesion(email, password);
                        } else {
                            mostrarMensajeError("Correo o contraseña incorrectos");
                        }
                    });
                } else {
                    mostrarMensajeError("Correo o contraseña incorrectos");
                }
            });
        });
    }
    private void iniciarSesion(String email, String password) {
        FirebaseDataCollection.obtenerIdFirebase(email, firebaseId -> {
            if (firebaseId != null) {
                SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isUserRegistered", true);
                editor.apply();
                iniciarListaDestinosActivity();
            } else {
                mostrarMensajeError("Error al obtener el ID de Firebase");
            }
        });
    }

    private void iniciarListaDestinosActivity() {
        startActivity(new Intent(this, HomeActivity.class));
    }

    private void mostrarMensajeError(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    void createSpannableString(String text, TextView item, Class<? extends Activity> targetActivity) {
        spannableString = new SpannableString(text);

        int startIndex = 0;
        int endIndex = text.length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent registerIntent = new Intent(LoginActivity.this, targetActivity);
                startActivity(registerIntent);
                finish();
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, endIndex, 0);
        item.setText(spannableString);
        item.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
