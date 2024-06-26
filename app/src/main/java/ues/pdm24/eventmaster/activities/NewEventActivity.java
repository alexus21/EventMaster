package ues.pdm24.eventmaster.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import java.io.IOException;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ues.pdm24.eventmaster.R;
/*import ues.pdm24.eventmaster.models.events.Event;*/
import ues.pdm24.eventmaster.api.interfaces.EventsApi;
import ues.pdm24.eventmaster.api.models.Event;
import ues.pdm24.eventmaster.validations.NetworkChecker;
import ues.pdm24.eventmaster.validations.NewPostValidation;

public class NewEventActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_PICK = 2;

    Spinner spinnerEventCategories;
    Button btnPublish;
    ImageView imgEvent;
    EditText editTextEventTitle, editTextEventLocation, editTextEventDescription, editTextEventAssistants;
    DatePicker datePicker;
    Bitmap selectedImageBitmap = null;
    Uri selectedImageUri = null;

    private void uploadEvent(String image, String title, String location, String category, String date, String description, int assistants) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Events");
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userFirebaseId", "1234");

        Event event = new Event(0, title, location, category, date, description, assistants, userId, image);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://event-api-cfbb36feb6d3.herokuapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventsApi eventsApi = retrofit.create(EventsApi.class);

        Call<Event> call = eventsApi.createEvents(event);

        call.enqueue(new Callback<Event>() {
            @Override
            public void onResponse(Call<Event> call, Response<Event> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                Event eventResponse = response.body();
                Log.d("Event Response", eventResponse.toString());
                mostrarMensaje("Publicación exitosa");
                enableUploadButton();
                startActivity(new Intent(NewEventActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onFailure(Call<Event> call, Throwable t) {
                Log.e("Error", "Error al obtener los eventos: " + t.getMessage());
                enableUploadButton();
            }
        });

        /*Event event = new Event(image, title, location, category, date, description, assistants);

        reference.push().setValue(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firebase", "Database entry created successfully");
                    mostrarMensaje("Publicación exitosa");
                    enableUploadButton();
                    startActivity(new Intent(NewEventActivity.this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Failed to create database entry", e);
                    enableUploadButton();
                });*/
    }

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

        // Categorías de eventos de ejemplo para el spinner
        ArrayAdapter<String> adapter = getStringArrayAdapter();
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
                uploadImageToFirebaseStorage(selectedImageBitmap, eventTitle, eventLocation, eventDescription, eventAssistants);
            } else {
                uploadImageToFirebaseStorage(selectedImageUri, eventTitle, eventLocation, eventDescription, eventAssistants);
            }
        });
    }

    @NonNull
    private ArrayAdapter<String> getStringArrayAdapter() {
        String[] eventCategories = new String[]{
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
        return adapter;
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap, String eventTitle, String eventLocation, String eventDescription, String eventAssistants) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        String imageName = UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + imageName);
        UploadTask uploadTask = storageRef.putBytes(imageBytes);

        handleUploadTask(uploadTask, storageRef, eventTitle, eventLocation, eventDescription, eventAssistants);
    }

    private void uploadImageToFirebaseStorage(Uri imageUri, String eventTitle, String eventLocation, String eventDescription, String eventAssistants) {
        String imageName = UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + imageName);
        UploadTask uploadTask = storageRef.putFile(imageUri);

        handleUploadTask(uploadTask, storageRef, eventTitle, eventLocation, eventDescription, eventAssistants);
    }

    private void handleUploadTask(UploadTask uploadTask, StorageReference storageRef, String eventTitle, String eventLocation, String eventDescription, String eventAssistants) {
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                //saveImageInfoToDatabase(imageUrl);
                System.out.println(getDatePicker(datePicker));
                uploadEvent(imageUrl, eventTitle, eventLocation, spinnerEventCategories.getSelectedItem().toString(),
                        getDatePicker(datePicker),
                        eventDescription, Integer.parseInt(eventAssistants));
            }).addOnFailureListener(e -> {
                Log.e("Firebase", "Failed to get download URL", e);
                enableUploadButton();
            });
        }).addOnFailureListener(exception -> {
            Log.e("Firebase", "Upload failed", exception);
            enableUploadButton();
        });
    }

    private String getDatePicker(DatePicker myDatePicker) {
        int day = myDatePicker.getDayOfMonth();
        int month = myDatePicker.getMonth() + 1;
        int year = myDatePicker.getYear();

        // Format the date as "year-month-day"
        return year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
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
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        });

        builder.setNegativeButton("Galería", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        });

        builder.setNeutralButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                Bundle extras = data.getExtras();
                selectedImageBitmap = (Bitmap) extras.get("data");
                imgEvent.setImageBitmap(selectedImageBitmap);
                selectedImageUri = null; // Clear Uri
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                selectedImageUri = data.getData();
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imgEvent.setImageBitmap(selectedImageBitmap);
                } catch (IOException e) {
                    Log.e("ImagePick", "Failed to get image", e);
                }
            }
        }
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
