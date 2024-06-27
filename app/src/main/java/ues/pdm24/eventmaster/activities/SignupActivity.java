package ues.pdm24.eventmaster.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.firebasedatacollection.FirebaseDataCollection;
import ues.pdm24.eventmaster.models.user.User;
import ues.pdm24.eventmaster.validations.EncryptPassword;
import ues.pdm24.eventmaster.validations.NetworkChecker;
import ues.pdm24.eventmaster.validations.UserValidator;

public class SignupActivity extends AppCompatActivity {

    public DatabaseReference reference;
    private FirebaseFirestore db;
    public EditText txtNameRegistered, txtCorreoRegister, txtPasswordRegister, txtRetypePasswordRegister;
    public Button btnSignUp;

    SpannableString spannableString;
    TextView lbl_register_aActivityLogin;

    FirebaseAuth mAuth;

    private void authentification(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Usuario creado y autenticado exitosamente
                            Toast.makeText(SignupActivity.this, "Usuario registrado y autenticado exitosamente", Toast.LENGTH_SHORT).show();
                            // Aquí puedes agregar código adicional, como actualizar la UI o navegar a otra actividad
                        } else {
                            Toast.makeText(SignupActivity.this, "Error inesperado al obtener el usuario", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Si falla, verifica si el usuario ya existe
                        if (task.getException() != null && task.getException().getMessage() != null &&
                                task.getException().getMessage().contains("The email address is already in use by another account.")) {
                            Toast.makeText(SignupActivity.this, "Este correo electrónico ya está registrado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignupActivity.this, "Error al registrar usuario: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.SignupActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        txtNameRegistered = findViewById(R.id.txtNameRegistered);
        txtCorreoRegister = findViewById(R.id.txtCorreoRegister);
        txtPasswordRegister = findViewById(R.id.txtPasswordRegister);
        txtRetypePasswordRegister = findViewById(R.id.txtRetypePasswordRegister);
        btnSignUp = findViewById(R.id.btnSignUp);
        lbl_register_aActivityLogin = findViewById(R.id.lbl_register_aActivityLogin);

        createSpannableString(lbl_register_aActivityLogin);

        btnSignUp.setOnClickListener(v -> {
            String name = txtNameRegistered.getText().toString().trim();
            String email = txtCorreoRegister.getText().toString().trim();
            String password = txtPasswordRegister.getText().toString().trim();
            String retypePassword = txtRetypePasswordRegister.getText().toString().trim();

            if(NetworkChecker.checkInternetConnection(this)) {
                mostrarMensaje("No hay conexión a internet");
                return;
            }

            boolean isValid = UserValidator.validateLogin(email, password, retypePassword,
                    txtCorreoRegister, txtPasswordRegister, txtRetypePasswordRegister);

            if (!isValid) {
                return;
            }

            // Deshabilitar el botón para evitar múltiples solicitudes
            btnSignUp.setEnabled(false);

            FirebaseDataCollection.checkEmail(email, exists -> {
                if (exists) {
                    txtCorreoRegister.setError("Este correo ya está en uso. Prueba uno diferente.");
                    btnSignUp.setEnabled(true); // Habilitar el botón si el correo ya está registrado
                } else {
                    registerUser(name, email, password);
                    Intent listaDestinos = new Intent(SignupActivity.this, HomeActivity.class);
                    startActivity(listaDestinos);
                }
            });
        });
    }

    void registerUser(String name, String email, String password) {
        reference = FirebaseDatabase.getInstance().getReference();

        String id = reference.push().getKey();

        String passwordFirebase = password;
        password = EncryptPassword.encryptPassword(password);

        User user = new User(id, name, email, password);

        // Insertar a Firebase:
        reference.child("users").child(id).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    mostrarMensaje("Usuario registrado correctamente");
                })
                .addOnFailureListener(e -> {
                    mostrarMensaje("Error al registrar usuario");
                });
        String username = email.split("@")[0];

        authentification(email, passwordFirebase);

        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isUserRegistered", true);
        editor.putString("userFirebaseId", id);
        editor.putString("username", username);
        editor.apply();
        finish();
    }

    void createSpannableString(TextView item) {
        String text = "¿Ya tienes cuenta? ¡Inicia sesión!";
        spannableString = new SpannableString(text);

        int startIndex = 0;
        int endIndex = text.length();

        // Definir el color personalizado
        int customColor = ContextCompat.getColor(this, R.color.blue_rackley);

        // Aplicar el color personalizado a todo el texto
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(customColor);
        spannableString.setSpan(colorSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent registerIntent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(registerIntent);
                finish();
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        item.setText(spannableString);
        item.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
