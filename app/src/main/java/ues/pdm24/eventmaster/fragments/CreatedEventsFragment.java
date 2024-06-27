package ues.pdm24.eventmaster.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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
 * Use the {@link CreatedEventsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreatedEventsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private ListView ltsMisEventos;
    private ArrayList<Event> eventos;
    private EventsListAdapter adapter;

    private String textSearch = "";
    private boolean isSearching = false;

    public CreatedEventsFragment() {
        // Required empty public constructor
    }

    public CreatedEventsFragment(String textSearch, boolean isSearching) {
        this.textSearch = textSearch;
        this.isSearching = isSearching;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreatedEventsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreatedEventsFragment newInstance(String param1, String param2) {
        CreatedEventsFragment fragment = new CreatedEventsFragment();
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
        View view = inflater.inflate(R.layout.fragment_created_events, container, false);

        ltsMisEventos = view.findViewById(R.id.ltsMisEventos);

        eventos = new ArrayList<>();

        Retrofit retrofit = RetrofitClient.getInstance();

        EventsApi eventsApi = retrofit.create(EventsApi.class);

        Call<List<Event>> call = eventsApi.getEvents(isSearching ? textSearch : "");

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("userFirebaseId", "1234");

        call.enqueue(new Callback<List<Event>>() {
            @Override
            public void onResponse(Call<List<Event>> call, Response<List<Event>> response) {
                if (!response.isSuccessful()) {
                    Log.e("Error", "Error al obtener los eventos");
                    return;
                }

                for (Event event : response.body()) {
                    Log.d("Event", event.getName());
                }

                List<Event> events = response.body();

                eventos.clear();
                eventos.addAll(events.stream().filter(event -> event.getUserid().equals(userId)).collect(Collectors.toList()));

                Log.d("Eventos", eventos.toString());

                adapter = new EventsListAdapter(getContext(), eventos, false);
                ltsMisEventos.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }
            @Override
            public void onFailure(Call<List<Event>> call, Throwable throwable) {
                Log.e("Error", "Error al obtener los eventos: " + throwable.getMessage());
            }
        });

        return view;
    }
}