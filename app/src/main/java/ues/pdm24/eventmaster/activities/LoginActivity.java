package ues.pdm24.eventmaster.activities;

import android.app.Activity;
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

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.firebasedatacollection.FirebaseDataCollection;
import ues.pdm24.eventmaster.validations.NetworkChecker;
import ues.pdm24.eventmaster.validations.UserValidator;

public class LoginActivity extends AppCompatActivity {

    SpannableString spannableString;
    TextView lbl_login_aActivitySignup, lbl_password_forgotten;

    EditText txtUsuarioLogin, txtConstrasenaLogin;
    Button btn_ingresar;

    FirebaseAuth mAuth;

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

        mAuth = FirebaseAuth.getInstance();

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

            if (NetworkChecker.checkInternetConnection(this)) {
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
                    mostrarMensajeError("El correo ingresado no está registrado");
                }
            });
        });
    }

    private void authentification(String email, String password) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            // No hay ningún usuario autenticado, crea uno nuevo
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Usuario registrado y autenticado exitosamente", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error al registrar usuario:: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            // Hay un usuario autenticado, verificar si es el mismo correo
            if (user.getEmail().equals(email)) {
                // Usuario ya autenticado con el mismo correo
                Toast.makeText(LoginActivity.this, "Usuario ya autenticado", Toast.LENGTH_SHORT).show();
            } else {
                // Usuario autenticado con un correo diferente, cerrar sesión
                mAuth.signOut();
                // Intentar autenticar al nuevo usuario
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();
                            } else {
                                // Si el usuario no está registrado, registrarlo
                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(createTask -> {
                                            if (createTask.isSuccessful()) {
                                                Toast.makeText(LoginActivity.this, "Usuario registrado y autenticado exitosamente", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Error al registrar usuario", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error al registrar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error al iniciar sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }
    }


    private void iniciarSesion(String email, String password) {
        String username = email.split("@")[0];

        FirebaseDataCollection.obtenerIdFirebase(email, firebaseId -> {
            if (firebaseId != null) {
                /*SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isUserRegistered", true);
                editor.putString("userFirebaseId", firebaseId);
                editor.putString("username", username);
                editor.apply();*/
                authentification(email, password);
                SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("userFirebaseId", firebaseId);
                editor.putString("email", email);
                editor.putString("username", username);
                editor.apply();
                iniciarListaDestinosActivity();
                finish();
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

    private void onComplete(Task<AuthResult> task) {
        if (task.isSuccessful()) {
            mostrarMensajeError("Inicio de sesión exitoso");
        } else {
            mostrarMensajeError("Correo o contraseña incorrectos");
        }
    }
}
