package ues.pdm24.eventmaster.adapters;

import android.content.Context;
import android.content.SharedPreferences;
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

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.api.models.Event;
import ues.pdm24.eventmaster.models.events.EventCount;

public class TargetedEventsListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Event> eventList;

    public TargetedEventsListAdapter(Context context, ArrayList<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }
    @Override
    public int getCount() {
        return eventList.size();
    }

    @Override
    public Object getItem(int position) {
        return eventList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_list_adapter_targeted_events, null);
        }

        ImageView imgEvent = convertView.findViewById(R.id.imageView);
        TextView lblEventName = convertView.findViewById(R.id.textViewEventTitle);
        TextView lblEventDate = convertView.findViewById(R.id.textViewEventDate);
        TextView lblEventLocation = convertView.findViewById(R.id.textViewEventLocation);
        ImageView imageViewFavouritePlaceMark = convertView.findViewById(R.id.imageViewFavouritePlaceMark);

        Event event = eventList.get(position);

        lblEventName.setText(event.getName());
        lblEventDate.setText(event.getDate());
        lblEventLocation.setText(event.getLocation());
        Glide.with(context).load(event.getImageUrl()).into(imgEvent);

        imageViewFavouritePlaceMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
                String userId = sharedPreferences.getString("userFirebaseId", "1234");

                databaseReference
                        .child("attendances")
                        .child(String.valueOf(event.getId()))
                        .child(userId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    snapshot.getRef().removeValue();
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                    ref
                                            .child("Events")
                                            .child(String.valueOf(event.getId()))
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                            if (snapshot.exists()) {
                                                                EventCount eventCount = snapshot.getValue(EventCount.class);
                                                                snapshot.getRef().removeValue();
                                                                eventCount.setAgendado(eventCount.getAgendado() - 1);
                                                                eventCount.setLibre(eventCount.getLibre() + 1);
                                                                if (eventCount.getAgendado() != 0) {
                                                                    snapshot.getRef().setValue(eventCount);
                                                                    return;
                                                                }

                                                                DatabaseReference refDeleteEvent = FirebaseDatabase.getInstance().getReference();
                                                                refDeleteEvent
                                                                        .child("Events")
                                                                        .child(String.valueOf(event.getId()))
                                                                        .removeValue();
                                                                removeEventFromList(event);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError error) {
                                                            Toast.makeText(context, "Error al eliminar el evento de favoritos", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                    Toast.makeText(context, "Evento eliminado", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "Error al eliminar el evento de favoritos", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });


        return convertView;
    }

    private void removeEventFromList(Event event) {
        int position = eventList.indexOf(event);
        if (position != -1) {
            eventList.remove(position);
            notifyDataSetChanged();
        }
    }
}
