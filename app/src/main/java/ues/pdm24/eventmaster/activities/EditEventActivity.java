package ues.pdm24.eventmaster.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import retrofit2.Call;
import retrofit2.Retrofit;
import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.api.instances.RetrofitClient;
import ues.pdm24.eventmaster.api.interfaces.EventsApi;
import ues.pdm24.eventmaster.api.models.Event;
import ues.pdm24.eventmaster.firebasedatacollection.FirebaseDataCollection;
import ues.pdm24.eventmaster.validations.NetworkChecker;
import ues.pdm24.eventmaster.validations.NewPostValidation;

public class EditEventActivity extends AppCompatActivity {

    Button btnEditEvent, btnDeleteEvent;
    EditText editTextEventTitle, editTextEventLocation, editTextEventDescription, editTextEventAssistants;
    Uri selectedImageUri = null;
    Bitmap selectedImageBitmap = null;
    Spinner spinnerEventCategories;
    ImageView imgEvent;
    DatePicker datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_event);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editEventActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Events");
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userFirebaseId", "1234");
        Retrofit retrofit = RetrofitClient.getInstance();
        EventsApi eventsApi = retrofit.create(EventsApi.class);

        btnEditEvent = findViewById(R.id.btnEditEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        editTextEventTitle = findViewById(R.id.editTextEventTitle);
        editTextEventLocation = findViewById(R.id.editTextEventLocation);
        editTextEventDescription = findViewById(R.id.editTextEventDescription);
        editTextEventAssistants = findViewById(R.id.editTextEventAssistants);
        spinnerEventCategories = findViewById(R.id.spinnerEventCategories);
        imgEvent = findViewById(R.id.imgEvent);
        datePicker = findViewById(R.id.datePicker);

        // Categorías de eventos de ejemplo para el spinner
        ArrayAdapter<String> adapter = getStringArrayAdapter();
        spinnerEventCategories.setAdapter(adapter);

        editTextEventTitle.setText(getIntent().getStringExtra("eventName"));
        editTextEventLocation.setText(getIntent().getStringExtra("eventLocation"));
        editTextEventDescription.setText(getIntent().getStringExtra("eventDescription"));
        editTextEventAssistants.setText(String.valueOf(getIntent().getIntExtra("eventAssistants", 0)));
        spinnerEventCategories.setSelection(getIntent().getIntExtra("eventCategory", 0));
        Glide.with(this).load(getIntent().getStringExtra("eventImageUrl")).into(imgEvent);
        datePicker.updateDate(getIntent().getIntExtra("eventYear", 2021),
                getIntent().getIntExtra("eventMonth", 1),
                getIntent().getIntExtra("eventDay", 1));

        btnEditEvent.setOnClickListener(v -> {
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

            /*if (selectedImageBitmap == null && selectedImageUri == null) {
                Toast.makeText(this, "Debe seleccionar una imagen", Toast.LENGTH_SHORT).show();
                return;
            }*/

            // Get the event ID
            int eventId = getIntent().getIntExtra("eventId", 0);

            // Create the event object
            Event event = new Event();
            event.setName(eventTitle);
            event.setDetails(eventDescription);
            event.setLocation(eventLocation);
            event.setCapacity(Integer.parseInt(eventAssistants));
            event.setCategory(spinnerEventCategories.getSelectedItem().toString());
            event.setDate(getDatePicker(datePicker));
            event.setUserid(userId);

            // Update the event in the API
            Call<Event> call = eventsApi.updateEvent(String.valueOf(eventId), event);
            call.enqueue(new retrofit2.Callback<Event>() {
                @Override
                public void onResponse(Call<Event> call, retrofit2.Response<Event> response) {
                    if (!response.isSuccessful()) {
                        mostrarMensaje("Error al actualizar el evento");
                        return;
                    }

                    // Update the event in the database
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Events");
                    reference.child(String.valueOf(eventId)).setValue(event);
                    mostrarMensaje("Evento actualizado correctamente");
                    startActivity(new Intent(EditEventActivity.this, HomeActivity.class));
                    finish();
                }

                @Override
                public void onFailure(Call<Event> call, Throwable t) {
                    mostrarMensaje("Error al actualizar el evento");
                }
            });
        });

        btnDeleteEvent.setOnClickListener(v -> {

            // Eliminar el evento
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirmación");
            builder.setMessage("¿Estás seguro de que deseas eliminar este evento?");
            builder.setPositiveButton("Sí, eliminar", (dialog, which) -> {
                // Get the event ID
                int eventId = getIntent().getIntExtra("eventId", 0);

                // Delete the event from the API
                Call<Void> call = eventsApi.deleteEvent(String.valueOf(eventId));
                call.enqueue(new retrofit2.Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, retrofit2.Response<Void> response) {
                        if (!response.isSuccessful()) {
                            mostrarMensaje("Error al eliminar el evento");
                            return;
                        }

                        // Delete the event from the database
                        reference.child(String.valueOf(eventId)).removeValue();
                        mostrarMensaje("Evento eliminado correctamente");
                        startActivity(new Intent(EditEventActivity.this, HomeActivity.class));
                        finish();
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        mostrarMensaje("Error al eliminar el evento");
                    }
                });

            });
            builder.setNegativeButton("No, cancelar", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    private String getDatePicker(DatePicker myDatePicker) {
        int day = myDatePicker.getDayOfMonth();
        int month = myDatePicker.getMonth() + 1;
        int year = myDatePicker.getYear();

        // Format the date as "year-month-day"
        return year + "-" + (month < 10 ? "0" + month : month) + "-" + (day < 10 ? "0" + day : day);
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

    /*private void showConfirmationDialog(String userId) {
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
    }*/

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void disableUploadButton() {
        btnEditEvent.setEnabled(false);
    }

    private void enableUploadButton() {
        btnEditEvent.setEnabled(true);
    }
}
