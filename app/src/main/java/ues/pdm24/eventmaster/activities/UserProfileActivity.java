package ues.pdm24.eventmaster.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.firebasedatacollection.FirebaseDataCollection;
import ues.pdm24.eventmaster.validations.EncryptPassword;
import ues.pdm24.eventmaster.validations.NetworkChecker;
import ues.pdm24.eventmaster.validations.UserValidator;

public class UserProfileActivity extends AppCompatActivity {
    ImageView imgAtras;
    Button btnUpdateMyPassword, btnEndSession, btnDeleteMyAccount;
    EditText txtEmail, txtPassword, txtRetypedPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.UserProfileActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imgAtras = findViewById(R.id.imgAtras);
        btnUpdateMyPassword = findViewById(R.id.btnSetRating);
        btnEndSession = findViewById(R.id.btnEndSession);
        btnDeleteMyAccount = findViewById(R.id.btnDeleteMyAccount);
        txtEmail = findViewById(R.id.editTextAddComments);
        txtPassword = findViewById(R.id.txtPassword);
        txtRetypedPassword = findViewById(R.id.txtRetypedPassword);
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);

        txtEmail.setText(preferences.getString("email", ""));
        // Hacer el EditText no editable
        txtEmail.setFocusable(false);
        txtEmail.setFocusableInTouchMode(false);
        txtEmail.setClickable(false);
        txtEmail.setLongClickable(false);

        imgAtras.setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, HomeActivity.class));
        });

        btnEndSession.setOnClickListener(v -> {
            if(NetworkChecker.checkInternetConnection(this)) {
                mostrarMensaje("No hay conexión a internet");
                return;
            }

//            localUserDAO.logout(userId);
//            SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = getSharedPreferences("UserPreferences", MODE_PRIVATE).edit();
            editor.clear();
            editor.apply();
            startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
        });

        btnDeleteMyAccount.setOnClickListener(v -> {
            if(NetworkChecker.checkInternetConnection(this)) {
                mostrarMensaje("No hay conexión a internet");
                return;
            }

//            showConfirmationDialog(userId);
        });

        btnUpdateMyPassword.setOnClickListener(v -> {
            String email = txtEmail.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();
            String retypePassword = txtRetypedPassword.getText().toString().trim();

            if(NetworkChecker.checkInternetConnection(this)) {
                mostrarMensaje("No hay conexión a internet");
                return;
            }

            boolean isValid = UserValidator.validateLogin(email, password, retypePassword, txtEmail, txtPassword, txtRetypedPassword);

            if (!isValid) {
                return;
            }

            btnUpdateMyPassword.setEnabled(false);

//            localUserDAO = DatabaseSingleton.getDatabase(this).localUserDAO();

            FirebaseDataCollection.updateUserPassword(email, password, new FirebaseDataCollection.UpdatePasswordCallback() {
                @Override
                public void onSuccess() {
//                    localUserDAO.updateUserPassword(email, EncryptPassword.encryptPassword(password));
                    mostrarMensaje("Contraseña actualizada correctamente");
                }

                @Override
                public void onFailure(Exception e) {
                    mostrarMensaje("Error al actualizar la contraseña");
                }
            });

//            localUserDAO.updateUserPassword(email, EncryptPassword.encryptPassword(password));
            startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
        });
    }

    private void showConfirmationDialog(String userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmación");
        builder.setMessage("¿Estás seguro de que deseas eliminar tu cuenta?");
        System.out.println("ID del usuario: " + userId);

        // Botón de confirmación
        builder.setPositiveButton("Sí, eliminar", (dialog, which) -> {
//            localUserDAO.delete(userId);
            FirebaseDataCollection.deleteUserById(userId, new FirebaseDataCollection.DeleteUserCallback() {
                @Override
                public void onSuccess() {
                    mostrarMensaje("Cuenta eliminada correctamente");
                    startActivity(new Intent(UserProfileActivity.this, LoginActivity.class));
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    mostrarMensaje("Ocurió un error al eliminar tu cuenta: ");
                    System.out.println("Error al eliminar cuenta: " + e.getMessage());
                }
            });
        });

        // Botón de cancelación
        builder.setNegativeButton("No, cancelar", (dialog, which) -> dialog.dismiss());

        // Crear y mostrar el AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }
}
