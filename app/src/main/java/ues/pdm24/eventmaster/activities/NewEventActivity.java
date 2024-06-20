package ues.pdm24.eventmaster.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.validations.NetworkChecker;
import ues.pdm24.eventmaster.validations.NewPostValidation;

public class NewEventActivity extends AppCompatActivity {
    Spinner spinnerEventCategories;
    Button btnPublish;
    ImageView imgEvent;
    EditText editTextEventTitle, editTextEventLocation, editTextEventDescription, editTextEventAssistants;
    DatePicker datePicker;
    Bitmap selectedImageBitmap = null;
    Uri selectedImageUri = null;

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
        btnPublish = findViewById(R.id.btnPublish);
        imgEvent = findViewById(R.id.imgEvent);
        editTextEventTitle = findViewById(R.id.editTextEventTitle);
        editTextEventLocation = findViewById(R.id.editTextEventLocation);
        editTextEventDescription = findViewById(R.id.editTextEventDescription);
        editTextEventAssistants = findViewById(R.id.editTextEventAssistants);
        datePicker = findViewById(R.id.datePicker);

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

        imgEvent.setOnClickListener(v -> showConfirmationDialog());

        btnPublish.setOnClickListener(v -> {
            if (NetworkChecker.checkInternetConnection(this)) {
                mostrarMensaje("No hay conexión a internet");
                return;
            }

            String eventTitle = editTextEventTitle.getText().toString();
            String eventDescription = editTextEventDescription.getText().toString();
            String eventLocation = editTextEventLocation.getText().toString();
            String eventAssistants = editTextEventAssistants.getText().toString();

            if (!NewPostValidation.validateNewPost(eventTitle, eventDescription, eventLocation, eventAssistants,
                    editTextEventTitle, editTextEventDescription, editTextEventLocation, editTextEventAssistants)) {
                return;
            }

            if (selectedImageBitmap == null && selectedImageUri == null) {
                Toast.makeText(this, "Debe seleccionar una imagen", Toast.LENGTH_SHORT).show();
                return;
            }

            disableUploadButton();

            if (selectedImageBitmap != null) {
                uploadImageToFirebaseStorage(selectedImageBitmap);
            } else {
                uploadImageToFirebaseStorage(selectedImageUri);
            }
        });
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String imageName = UUID.randomUUID().toString() + ".jpg";
        /*StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + localUserDAO.getUserId() + "/" + imageName);
        UploadTask uploadTask = storageRef.putBytes(imageBytes);

        handleUploadTask(uploadTask, storageRef);*/
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        String imageName = UUID.randomUUID().toString() + ".jpg";
        /*StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + localUserDAO.getUserId() + "/" + imageName);
        UploadTask uploadTask = storageRef.putFile(imageUri);

        handleUploadTask(uploadTask, storageRef);*/
    }

    private void handleUploadTask(UploadTask uploadTask, StorageReference storageRef) {
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
//                saveImageInfoToDatabase(imageUrl);
            }).addOnFailureListener(e -> {
                Log.e("Firebase", "Failed to get download URL", e);
                enableUploadButton();
            });
        }).addOnFailureListener(exception -> {
            Log.e("Firebase", "Upload failed", exception);
            enableUploadButton();
        });
    }

    /*private void saveImageInfoToDatabase(String imageUrl) {
        // Obtener datos de la interfaz de usuario
        String placeName = editTextPlaceName.getText().toString();
        String placeDescription = editTextPlaceDescription.getText().toString();
        String placeLocation = editTextPlaceLocation.getText().toString();
        String userId = localUserDAO.getUserId();

        // Obtener una referencia a Firebase Database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        String uuid = reference.push().getKey();

        // Crear un nuevo destino
        ListaDestinos destino = new ListaDestinos(
                placeDescription,
                imageUrl,
                placeLocation,
                placeName,
                userId,
                uuid
        );

        // Insertar destino en Firebase
        reference.child("destination").child(uuid).setValue(destino)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Database entry created successfully");
                    mostrarMensaje("Publicación exitosa");
                    enableUploadButton();
                    startActivity(new Intent(NuevoPostActivity.this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to create database entry", e);
                    enableUploadButton();
                });
    }*/

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona una acción");
        builder.setMessage("¿Cómo deseas continuar?");

        builder.setPositiveButton("Cámara", (dialog, which) -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, 1);
        });

        builder.setNegativeButton("Galería", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, 2);
        });

        builder.setNeutralButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void disableUploadButton() {
        btnPublish.setEnabled(false);
    }

    private void enableUploadButton() {
        btnPublish.setEnabled(true);
    }


}
