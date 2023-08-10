package xyz.dongguo.retrofit.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CalorieResults {
    @SerializedName("items")
    public List<Ingredient> items;
}
