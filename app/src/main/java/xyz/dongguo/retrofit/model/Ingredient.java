package xyz.dongguo.retrofit.model;

import com.google.gson.annotations.SerializedName;

import java.util.Random;

public class Ingredient {
    @SerializedName("id")
    public String id() {
        return randomString(16);
    }

    @SerializedName("name")
    public String name;

    @SerializedName("calories")
    public float calories;

    private String randomString(int length) {
        String letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(letters.length());
            stringBuilder.append(letters.charAt(randomIndex));
        }
        return stringBuilder.toString();
    }
}
