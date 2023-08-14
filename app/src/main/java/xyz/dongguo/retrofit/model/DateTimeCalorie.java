package xyz.dongguo.retrofit.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class DateTimeCalorie extends RealmObject {
    @PrimaryKey
    private String _id;

    @Required
    private Date dateTime = new Date();
    @Required
    private Double calories = 50.0;

    private String foodText = "";
    private String imageUrl = "";

    private String ownerId = "a-string-represtening-user";

    public DateTimeCalorie() {}

    public DateTimeCalorie(Date dateTime, Double calories) {
        this.dateTime = dateTime;
        this.calories = calories;
    }

    public DateTimeCalorie(Date dateTime, Double calories, String foodText) {
        this.dateTime = dateTime;
        this.calories = calories;
        this.foodText = foodText;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public String getFoodText() {
        return foodText;
    }

    public void setFoodText(String foodText) {
        this.foodText = foodText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
