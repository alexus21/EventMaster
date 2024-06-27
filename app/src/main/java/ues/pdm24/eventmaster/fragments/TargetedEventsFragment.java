package ues.pdm24.eventmaster.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.adapters.TargetedEventsListAdapter;
import ues.pdm24.eventmaster.api.instances.RetrofitClient;
import ues.pdm24.eventmaster.api.interfaces.EventsApi;
import ues.pdm24.eventmaster.api.models.Event;

public class TargetedEventsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ListView listViewTodosProximosEventos;
    private ArrayList<Event> listEvents;

    private String textSearch = "";
    private boolean isSearching = false;

    public TargetedEventsFragment() {
        // Required empty public constructor
    }

    public TargetedEventsFragment(String textSearch, boolean isSearching) {
        this.textSearch = textSearch;
        this.isSearching = isSearching;
    }

    public static TargetedEventsFragment newInstance(String param1, String param2) {
        TargetedEventsFragment fragment = new TargetedEventsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_targeted_events, container, false);

        listEvents = new ArrayList<>();

        listViewTodosProximosEventos = root.findViewById(R.id.listViewTodosProximosEventos);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Retrofit retrofit = RetrofitClient.getInstance();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userFirebaseId", "1234");

        EventsApi eventsApi = retrofit.create(EventsApi.class);

        databaseReference
                .child("attendances")
                .orderByChild(userId)
                .equalTo(true)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listEvents.clear();
                        Log.d("Snapshot: ", snapshot.toString());

                        AtomicInteger pendingCalls = new AtomicInteger((int) snapshot.getChildrenCount());

                        for (DataSnapshot attendanceSnapshot : snapshot.getChildren()) {
                            String eventId = attendanceSnapshot.getKey();
                            Call<Event> call = eventsApi.getEventById(eventId);

                            call.enqueue(new Callback<Event>() {
                                @Override
                                public void onResponse(Call<Event> call, Response<Event> response) {
                                    if (!response.isSuccessful()) {
                                        Log.e("Error", "Error al obtener los eventos");
                                        checkAndUpdateAdapter(pendingCalls);
                                        return;
                                    }

                                    Event event = response.body();
                                    listEvents.add(event);
                                    Toast.makeText(getContext(), event.getName() + ", " + listEvents.size(), Toast.LENGTH_SHORT).show();
                                    checkAndUpdateAdapter(pendingCalls);
                                }

                                @Override
                                public void onFailure(Call<Event> call, Throwable throwable) {
                                    Log.e("Error BY child", "Error al obtener los eventos: " + throwable.getMessage());
                                    checkAndUpdateAdapter(pendingCalls);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Error", error.getMessage());
                        Toast.makeText(getContext(), "Error al cargar los eventos", Toast.LENGTH_SHORT).show();
                    }
                });

        return root;
    }

    private void checkAndUpdateAdapter(AtomicInteger pendingCalls) {
        if (pendingCalls.decrementAndGet() == 0) {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Size final: " + listEvents.size(), Toast.LENGTH_SHORT).show();
                TargetedEventsListAdapter adapter = new TargetedEventsListAdapter(requireContext(), listEvents);
                listViewTodosProximosEventos.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            });
        }
    }
}