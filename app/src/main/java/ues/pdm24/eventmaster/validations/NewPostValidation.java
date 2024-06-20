package ues.pdm24.eventmaster.validations;

import android.widget.EditText;

public class NewPostValidation {
    public static boolean validateNewPost(String eventTitle, String eventDescription, String eventLocation, String eventAssistants,
                                          EditText editTextEventTitle, EditText editTextEventDescription, EditText editTextEventLocation, EditText editTextEventAssistants) {

        if (eventTitle.isEmpty()) {
            editTextEventTitle.setError("El nombre del lugar es requerido");
            editTextEventTitle.requestFocus();
            return false;
        }

        if (eventDescription.isEmpty()) {
            editTextEventDescription.setError("La descripción del lugar es requerida");
            editTextEventDescription.requestFocus();
            return false;
        }

        if (eventLocation.isEmpty()) {
            editTextEventLocation.setError("La ubicación del lugar es requerida");
            editTextEventLocation.requestFocus();
            return false;
        }

        if (eventAssistants.isEmpty()) {
            editTextEventAssistants.setError("El número de asistentes es requerido");
            editTextEventAssistants.requestFocus();
            return false;
        }

        return true;
    }
}
