package xyz.dongguo.retrofit.model;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface APIInterface {
    @Headers({
            "X-Api-Key", "YOUR_API_KEY"
    })
    @GET("/nutrition")
    Call<CalorieResults> doSearchIngredient(@Query(value = "query") String foodQueryString);
}
