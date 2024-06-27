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
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.adapters.EventsListAdapter;
import ues.pdm24.eventmaster.api.instances.RetrofitClient;
import ues.pdm24.eventmaster.api.interfaces.EventsApi;
import ues.pdm24.eventmaster.api.models.Event;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EventListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EventListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private ListView listaTodosEventos;
    private ArrayList<Event> eventos;
    private EventsListAdapter adapter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public EventListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EventListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EventListFragment newInstance(String param1, String param2) {
        EventListFragment fragment = new EventListFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        listaTodosEventos = view.findViewById(R.id.listaTodosEventos);
        eventos = new ArrayList<>();

        Retrofit retrofit = RetrofitClient.getInstance();

        EventsApi eventsApi = retrofit.create(EventsApi.class);

        Call<List<Event>> call = eventsApi.getEvents();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userFirebaseId", "1234");

        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (!response.isSuccessful()) {
                    Log.e("Error", "Error al obtener los eventos");
                    return;
                }

                List<Event> events = response.body();
                eventos.clear();
                eventos.addAll(events.stream().filter(event -> !event.getUserid().equals(userId)).collect(Collectors.toList()));

                adapter = new EventsListAdapter(requireContext(), eventos);
                listaTodosEventos.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Event>> call, Throwable t) {
                Log.e("Error", "Error al obtener los eventos");
            }
        });

        /*DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Events");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                eventos.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Event evento = ds.getValue(Event.class);
                    if (evento != null) {
                        eventos.add(evento);
                        Log.d("INFO", "Evento: " + evento.getTitle() + ", " + evento.getDescription()); // Agrega más detalles
                    }
                }
                adapter = new EventsListAdapter(requireContext(), eventos); // Usa requireContext()
                listaTodosEventos.setAdapter(adapter);
                adapter.notifyDataSetChanged(); // Notifica al adaptador sobre los cambios
                Log.d("INFO", "Eventos: " + eventos.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ERROR", databaseError.getMessage());
            }
        });*/

        return view;
    }
}