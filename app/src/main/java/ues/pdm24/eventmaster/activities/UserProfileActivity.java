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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.firebasedatacollection.FirebaseDataCollection;
import ues.pdm24.eventmaster.validations.EncryptPassword;
import ues.pdm24.eventmaster.validations.NetworkChecker;
import ues.pdm24.eventmaster.validations.UserValidator;

public class UserProfileActivity extends AppCompatActivity {
    ImageView imgAtras;
    Button btnUpdatePassword, btnEndSession, btnDeleteMyAccount;
    EditText editTextUserEmail, txtPassword, txtRetypedPassword;

    private FirebaseAuth mAuth;

    private void signOut() {
        mAuth.getCurrentUser().delete().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error al cerrar sesión ", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cerrar sesión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        mAuth.signOut();
    }


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
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword);
        btnEndSession = findViewById(R.id.btnEndSession);
        btnDeleteMyAccount = findViewById(R.id.btnDeleteMyAccount);
        editTextUserEmail = findViewById(R.id.editTextUserEmail);
        txtPassword = findViewById(R.id.txtPassword);
        txtRetypedPassword = findViewById(R.id.txtRetypedPassword);
        SharedPreferences preferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();

        String id = preferences.getString("userFirebaseId", "");
        Toast.makeText(this, "ID de Firebase: " + id, Toast.LENGTH_SHORT).show();

        FirebaseDataCollection.obtenerEmailFirebase(id, email -> {
            if (email != null) {
                editTextUserEmail.setText(email);
            } else {
                mostrarMensaje("Error al obtener el ID de Firebase");
            }
        });

//        editTextUserEmail.setText(preferences.getString("email", ""));
        // Hacer el EditText no editable
        editTextUserEmail.setFocusable(false);
        editTextUserEmail.setFocusableInTouchMode(false);
        editTextUserEmail.setClickable(false);
        editTextUserEmail.setLongClickable(false);

        imgAtras.setOnClickListener(v -> {
            startActivity(new Intent(UserProfileActivity.this, HomeActivity.class));
        });

        btnEndSession.setOnClickListener(v -> {
            if(NetworkChecker.checkInternetConnection(this)) {
                mostrarMensaje("No hay conexión a internet");
                return;
            }

            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (currentUser != null) {
                Toast.makeText(this, "usuario autenticado: " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
                signOut();
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Cierra la actividad actual

        });

        btnDeleteMyAccount.setOnClickListener(v -> {
            if(NetworkChecker.checkInternetConnection(this)) {
                mostrarMensaje("No hay conexión a internet");
                return;
            }

            String userId = preferences.getString("firebaseId", "");

            showConfirmationDialog(userId);
        });

        btnUpdatePassword.setOnClickListener(v -> {
            String email = editTextUserEmail.getText().toString().trim();
            String password = txtPassword.getText().toString().trim();
            String retypePassword = txtRetypedPassword.getText().toString().trim();

            if(NetworkChecker.checkInternetConnection(this)) {
                mostrarMensaje("No hay conexión a internet");
                return;
            }

            boolean isValid = UserValidator.validateLogin(email, password, retypePassword, editTextUserEmail, txtPassword, txtRetypedPassword);

            if (!isValid) {
                return;
            }

            btnUpdatePassword.setEnabled(false);

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
                    SharedPreferences.Editor editor = getSharedPreferences("UserPreferences", MODE_PRIVATE).edit();
                    editor.clear();
                    editor.apply();
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
