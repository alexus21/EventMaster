package ues.pdm24.eventmaster.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.api.interfaces.EventsApi;
import ues.pdm24.eventmaster.api.interfaces.JsonPlaceHolderApi;
import ues.pdm24.eventmaster.api.models.Event;
import ues.pdm24.eventmaster.api.models.Posts;
import ues.pdm24.eventmaster.fragments.CreatedEventsFragment;
import ues.pdm24.eventmaster.fragments.EventListFragment;
import ues.pdm24.eventmaster.fragments.TargetedEventsFragment;
import ues.pdm24.eventmaster.validations.NetworkUtils;

public class HomeActivity extends AppCompatActivity {
    Fragment createdEventsFragment, mainFragment;
    FragmentContainerView fragmentContainerView;
    BottomNavigationView bottomNavigation;
    TextView lblnameapp, lblUsuarioLogeado;
    FloatingActionButton btnAgregarEventos;
    CircleImageView btnUser;
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
        lblUsuarioLogeado = findViewById(R.id.lblUsuarioLogeado);
        btnAgregarEventos = findViewById(R.id.btnAgregarEventos);
        btnUser = findViewById(R.id.btnUser);
        mainFragment = new EventListFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, mainFragment)
                .commit();
        lblnameapp.setText("Eventos");

        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        lblUsuarioLogeado.setText(sharedPreferences.getString("username", "Invitado"));

        btnUser.setOnClickListener(v -> {
            startActivity(new Intent(this, UserProfileActivity.class));
        });

        btnAgregarEventos.setOnClickListener(v -> {
            startActivity(new Intent(this, NewEventActivity.class));
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
                nameApp = "Mis Asistencias";
                break;
        }

        //getEvents();

        if (mainFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, mainFragment)
                    .commit();
            lblnameapp.setText(nameApp);
        }
        return true;
    }

    private void getPosts() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jsonplaceholder.typicode.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JsonPlaceHolderApi jsonPlaceHolderApi = retrofit.create(JsonPlaceHolderApi.class);
        Call<List<Posts>> call = jsonPlaceHolderApi.getPosts();

        call.enqueue(new Callback<List<Posts>>() {
            @Override
            public void onResponse(Call<List<Posts>> call, Response<List<Posts>> response) {
                if (!response.isSuccessful()) {
                    Log.e("Error", "Error al obtener los posts");
                    return;
                }

                List<Posts> posts = response.body();
                for (Posts post : posts) {
                    Log.i("Post Obtained", post.getTitle());
                }
            }

            @Override
            public void onFailure(Call<List<Posts>> call, Throwable t) {
                Log.e("Error", "Error al obtener los posts");
            }
        });
    }

    private void getEvents() {

        String ipAddres = NetworkUtils.getIPv4Address();

        if (ipAddres != null) {
            String baseUrl = "http://" + ipAddres + ":3000/api/";
            Toast.makeText(this, "Dirección IP: " + ipAddres, Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(this, "No se pudo obtener la dirección IP", Toast.LENGTH_SHORT).show();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://event-api-cfbb36feb6d3.herokuapp.com/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        EventsApi eventsApi = retrofit.create(EventsApi.class);
        Call<List<Event>> call = eventsApi.getEvents();

        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (!response.isSuccessful()) {
                    Log.e("Error", "Error al obtener los eventos");
                    return;
                }

                List<Event> events = response.body();
                for (Event event : events) {
                    Toast.makeText(getBaseContext(), String.valueOf(event.getCapacity()), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Log.e("Error", "Error al obtener los eventos: " + t.getMessage());
            }
        });
    }
}
