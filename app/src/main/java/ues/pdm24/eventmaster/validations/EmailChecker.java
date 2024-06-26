package ues.pdm24.eventmaster.validations;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailChecker {
    public static boolean isEmailEmpty(String email) {
        return email.isEmpty();
    }

    public static boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    public static boolean validDomainEmail(String email) {
        return email.contains("@gmail.com") || email.contains("@hotmail.com") ||
                email.contains("@yahoo.com") || email.contains("@outlook.com") ||
                email.contains("@live.com") || email.contains("@icloud.com") ||
                email.contains("@aol.com") || email.contains("@protonmail.com") || email.contains("@ues.edu.sv");
    }
}
