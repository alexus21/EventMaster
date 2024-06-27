package ues.pdm24.eventmaster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.zip.Inflater;

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.api.models.Event;

public class EventsListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Event> listEvents;

    public EventsListAdapter(Context context, ArrayList<Event> listEvents) {
        this.context = context;
        this.listEvents = listEvents;
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

        ImageView imgEvent = convertView.findViewById(R.id.imageView);
        TextView txtEventName = convertView.findViewById(R.id.textViewPlaceName);
        TextView textViewPublishedBy = convertView.findViewById(R.id.textViewPublishedBy);

        Event event = listEvents.get(position);

        Glide.with(context).load(event.getImageUrl()).into(imgEvent);

        txtEventName.setText(event.getName());
        textViewPublishedBy.setText(event.getDetails());

        return convertView;
    }
}
