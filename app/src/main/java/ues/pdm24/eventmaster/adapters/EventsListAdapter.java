package ues.pdm24.eventmaster.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.zip.Inflater;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.activities.EditEventActivity;
import ues.pdm24.eventmaster.api.models.Event;
import ues.pdm24.eventmaster.models.events.EventCount;

public class EventsListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Event> listEvents;
    private boolean showAttendancesButton;

    public EventsListAdapter(Context context, ArrayList<Event> listEvents) {
        this.context = context;
        this.listEvents = listEvents;
        this.showAttendancesButton = true;
    }

    public EventsListAdapter(Context context, ArrayList<Event> listEvents, boolean showAttendancesButton) {
        this.context = context;
        this.listEvents = listEvents;
        this.showAttendancesButton = showAttendancesButton;
    }

    @Override
    public int getCount() {
        return listEvents.size();
    }

    @Override
    public Object getItem(int position) {
        return listEvents.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_adapter_events, null);
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        ImageView imgEvent = convertView.findViewById(R.id.imageView);
        TextView txtEventName = convertView.findViewById(R.id.textViewPlaceName);
        TextView textViewPublishedBy = convertView.findViewById(R.id.textViewPublishedBy);
        ImageView imageViewFavouritePlaceMark = convertView.findViewById(R.id.imageViewFavouritePlaceMark);

        SharedPreferences preferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String userId = preferences.getString("userFirebaseId", "1234");

        if (this.showAttendancesButton) {
            imageViewFavouritePlaceMark.setVisibility(View.VISIBLE);

            reference.child("attendances")
                    .child(String.valueOf(listEvents.get(position).getId()))
                    .child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                imageViewFavouritePlaceMark.setVisibility(View.GONE);
                                Log.d("EventsListAdapter", "User is attending event: " + snapshot.getValue().toString());
                            } else {
                                Log.d("EventsListAdapter", "User is not attending event");
                                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

                                databaseReference.child("Events")
                                        .child(String.valueOf(listEvents.get(position).getId()))
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    EventCount eventCount = snapshot.getValue(EventCount.class);

                                                    if (eventCount.getLibre() == 0) {
                                                        imageViewFavouritePlaceMark.setVisibility(View.GONE);
                                                    }
                                                } else {
                                                    Log.e("EventsListAdapter", "Event does not exist");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("EventsListAdapter", "Error checking attendance", error.toException());
                        }
                    });

            imageViewFavouritePlaceMark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                    databaseReference
                            .child("attendances")
                            .child(String.valueOf(listEvents.get(position).getId()))
                            .child(userId)
                            .setValue(true);

                    DatabaseReference eventReference = FirebaseDatabase.getInstance().getReference();

                    eventReference
                            .child("Events")
                            .child(String.valueOf(listEvents.get(position).getId()))
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        EventCount eventCount = snapshot.getValue(EventCount.class);
                                        snapshot.getRef().removeValue();
                                        eventCount.setLibre(eventCount.getLibre() - 1);
                                        eventCount.setAgendado(eventCount.getAgendado() + 1);
                                        snapshot.getRef().setValue(eventCount);
                                    } else {
                                        EventCount eventCount = new
                                                EventCount(listEvents.get(position).getCapacity(),
                                                listEvents.get(position).getCapacity() - 1, 1);
                                        snapshot.getRef().setValue(eventCount);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("EventsListAdapter", "Error updating event count", error.toException());
                                    Toast.makeText(context, "Error al confirmar asistencia", Toast.LENGTH_SHORT).show();
                                }
                            });


                    imageViewFavouritePlaceMark.setVisibility(View.GONE);
                    Toast.makeText(context, "Asistencia confirmada", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            imageViewFavouritePlaceMark.setVisibility(View.GONE);
        }

        Event event = listEvents.get(position);

        Glide.with(context).load(event.getImageUrl()).into(imgEvent);

        txtEventName.setText(event.getName());
        textViewPublishedBy.setText(event.getDetails());

        imgEvent.setOnClickListener(v -> {
            System.out.println("ID evento seleccionado: " + event.getId());
            Intent editEventIntent = new Intent(this.context, EditEventActivity.class);
            editEventIntent.putExtra("eventId", event.getId());
            editEventIntent.putExtra("eventName", event.getName());
            editEventIntent.putExtra("eventLocation", event.getLocation());
            editEventIntent.putExtra("eventDescription", event.getDetails());
            editEventIntent.putExtra("eventAssistants", event.getCapacity());
            editEventIntent.putExtra("eventCategory", event.getCategory());
            editEventIntent.putExtra("eventImageUrl", event.getImageUrl());
            editEventIntent.putExtra("eventDate", event.getDate());

            context.startActivity(editEventIntent);
        });

        return convertView;
    }
}
