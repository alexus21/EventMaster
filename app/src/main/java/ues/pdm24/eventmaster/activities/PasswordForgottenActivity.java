package ues.pdm24.eventmaster.activities;

import android.content.Intent;
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

import com.google.firebase.database.DatabaseReference;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.firebasedatacollection.FirebaseDataCollection;
import ues.pdm24.eventmaster.validations.EncryptPassword;
import ues.pdm24.eventmaster.validations.NetworkChecker;
import ues.pdm24.eventmaster.validations.UserValidator;

public class PasswordForgottenActivity extends AppCompatActivity {

    EditText txtCorreoRecover, txtPasswordRecover, txtRetypePasswordRegister;
    Button btnRecoverPassword;
    TextView lbl_cancel_action;
    SpannableString spannableString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_forgotten);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.PasswordForgottenActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtCorreoRecover = findViewById(R.id.txtCorreoRecover);
        txtPasswordRecover = findViewById(R.id.txtPasswordRecover);
        txtRetypePasswordRegister = findViewById(R.id.txtRetypePasswordRecover);
        btnRecoverPassword = findViewById(R.id.btnRecoverPassword);
        lbl_cancel_action = findViewById(R.id.lbl_cancel_action);

        createSpannableString(lbl_cancel_action);

        btnRecoverPassword.setOnClickListener(v -> {
            String email = txtCorreoRecover.getText().toString().trim();
            String password = txtPasswordRecover.getText().toString().trim();
            String retypePassword = txtRetypePasswordRegister.getText().toString().trim();

            if(NetworkChecker.checkInternetConnection(this)) {
                mostrarMensaje("No hay conexión a internet");
                return;
            }

            boolean isValid = UserValidator.validateLogin(email, password, retypePassword,
                    txtCorreoRecover, txtPasswordRecover, txtRetypePasswordRegister);

            if (!isValid) {
                return;
            }

            // Deshabilitar el botón para evitar múltiples solicitudes
            btnRecoverPassword.setEnabled(false);

            FirebaseDataCollection.checkEmail(email, exists -> {
                if (exists) {
                    updateUserPassword(email, password);
                    startActivity(new Intent(PasswordForgottenActivity.this, LoginActivity.class));
                    finish();
                } else {
                    txtCorreoRecover.setError("El correo proporcionado no se ha encontrado");
                    btnRecoverPassword.setEnabled(true);
                }
            });
        });
    }

    void updateUserPassword(String email, String password) {

        FirebaseDataCollection.updateUserPassword(email, password, new FirebaseDataCollection.UpdatePasswordCallback() {
            @Override
            public void onSuccess() {
                mostrarMensaje("Contraseña actualizada correctamente");
            }

            @Override
            public void onFailure(Exception e) {
                mostrarMensaje("Error al actualizar la contraseña");
            }
        });
    }

    void createSpannableString(TextView item) {
        spannableString = new SpannableString("Cancelar");

        int startIndex = 0;
        int endIndex = "Cancelar".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent registerIntent = new Intent(PasswordForgottenActivity.this, LoginActivity.class);
                startActivity(registerIntent);
                finish();
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, endIndex, 0);
        item.setText(spannableString);
        item.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
