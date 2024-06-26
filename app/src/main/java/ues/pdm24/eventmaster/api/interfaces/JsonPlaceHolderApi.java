package ues.pdm24.eventmaster.api.interfaces;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import ues.pdm24.eventmaster.api.models.Posts;

public interface JsonPlaceHolderApi {
    @GET("posts")
    Call<List<Posts>> getPosts();
}
