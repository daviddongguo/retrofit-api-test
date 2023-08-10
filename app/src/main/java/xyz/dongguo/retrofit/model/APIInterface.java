package xyz.dongguo.retrofit.model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("v1/nutrition")
    Call<CalorieResults> doSearchIngredient(
            @Header("X-Api-Key") String apiKey,
            @Query(value = "query") String foodQueryString);
}
