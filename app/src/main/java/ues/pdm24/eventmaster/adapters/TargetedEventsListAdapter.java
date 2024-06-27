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

import ues.pdm24.eventmaster.R;
import ues.pdm24.eventmaster.api.models.Event;

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

        return convertView;
    }
}
