package ues.pdm24.eventmaster.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.fragments.CreatedEventsFragment;
import ues.pdm24.eventmaster.fragments.EventListFragment;
import ues.pdm24.eventmaster.fragments.TargetedEventsFragment;

public class HomeActivity extends AppCompatActivity {
    Fragment createdEventsFragment, mainFragment;
    FragmentContainerView fragmentContainerView;
    BottomNavigationView bottomNavigation;
    TextView lblnameapp;
    FloatingActionButton btnAgregarEventos;
    String nameApp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homeActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        createdEventsFragment = new CreatedEventsFragment();
        fragmentContainerView = findViewById(R.id.fragmentContainerView);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        lblnameapp = findViewById(R.id.lblnameapp);
        btnAgregarEventos = findViewById(R.id.btnAgregarEventos);
        mainFragment = null;

        btnAgregarEventos.setOnClickListener(v -> {
            Toast.makeText(this, "No sirve esta mierda", Toast.LENGTH_SHORT).show();
        });

        bottomNavigation.setOnNavigationItemSelectedListener(this::handleNavigationItemSelected);

    }

    @SuppressLint("NonConstantResourceId")
    private boolean handleNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnxAllEvents:
                btnAgregarEventos.show();
                mainFragment = new EventListFragment();
                nameApp = "Eventos";
                break;
            case R.id.mnxMyEvents:
                btnAgregarEventos.hide();
                mainFragment = new CreatedEventsFragment();
                nameApp = "Mis eventos";
                break;
            case R.id.mnxTargetedEvents:
                btnAgregarEventos.hide();
                mainFragment = new TargetedEventsFragment();
                nameApp = "Mis intereses";
                break;
        }

        if (mainFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, mainFragment)
                    .commit();
            lblnameapp.setText(nameApp);
        }
        return true;
    }
}
