package ues.pdm24.eventmaster.api.interfaces;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import ues.pdm24.eventmaster.api.models.Event;

public interface EventsApi {
    @GET("events")
    Call<List<Event>> getEvents();


    @POST("events")
    Call<Event> createEvents(@Body Event event);

    @GET("events/{id}")
    Call<Event> getEventById(@Path("id") String id);
}
