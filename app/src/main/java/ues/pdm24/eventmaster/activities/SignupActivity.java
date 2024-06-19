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

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.FirebaseFirestore;

import ues.pdm24.eventmaster.R;

public class SignupActivity extends AppCompatActivity {

    public DatabaseReference reference;
    private FirebaseFirestore db;
    public EditText txtNameRegistered, txtCorreoRegister, txtPasswordRegister, txtRetypePasswordRegister;
    public Button btnSignUp;

    SpannableString spannableString;
    TextView lbl_register_aActivityLogin;

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

        txtNameRegistered = findViewById(R.id.txtNameRegistered);
        txtCorreoRegister = findViewById(R.id.txtCorreoRegister);
        txtPasswordRegister = findViewById(R.id.txtPasswordRegister);
        txtRetypePasswordRegister = findViewById(R.id.txtRetypePasswordRegister);
        btnSignUp = findViewById(R.id.btnSignUp);
        lbl_register_aActivityLogin = findViewById(R.id.lbl_register_aActivityLogin);

        createSpannableString(lbl_register_aActivityLogin);
    }

    void createSpannableString(TextView item) {
        spannableString = new SpannableString("¿Ya tienes cuenta? ¡Inicia sesión!");

        int startIndex = 0;
        int endIndex = "¿Ya tienes cuenta? ¡Inicia sesión!".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent registerIntent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(registerIntent);
                finish();
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, endIndex, 0);
        item.setText(spannableString);
        item.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
