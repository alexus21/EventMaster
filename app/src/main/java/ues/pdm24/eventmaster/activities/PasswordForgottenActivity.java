package ues.pdm24.eventmaster.activities;

import android.app.Activity;
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

import com.google.firebase.database.DatabaseReference;

import ues.pdm24.eventmaster.R;

public class PasswordForgottenActivity extends AppCompatActivity {

    EditText txtCorreoRecover, txtPasswordRecover, txtRetypePasswordRegister;
    Button btnRecoverPassword;
    TextView lbl_cancel_action;
    SpannableString spannableString;
    public DatabaseReference reference;

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

        createSpannableString("Cancelar", lbl_cancel_action, LoginActivity.class);
    }

    void createSpannableString(String text, TextView item, Class<? extends Activity> targetActivity) {
        spannableString = new SpannableString(text);

        int startIndex = 0;
        int endIndex = text.length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                Intent registerIntent = new Intent(PasswordForgottenActivity.this, targetActivity);
                startActivity(registerIntent);
                finish();
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, endIndex, 0);
        item.setText(spannableString);
        item.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
