package xyz.dongguo.retrofit;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.FutureTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.sync.SyncConfiguration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.dongguo.retrofit.model.APIClient;
import xyz.dongguo.retrofit.model.APIInterface;
import xyz.dongguo.retrofit.model.CalorieResults;
import xyz.dongguo.retrofit.model.DateTimeCalorie;
import xyz.dongguo.retrofit.model.Ingredient;

public class MainActivity extends AppCompatActivity {

    Realm uiThreadRealm;
    List<DateTimeCalorie> entries = new ArrayList<DateTimeCalorie>();

    APIInterface apiInterface;
    TextView responseText;
    EditText foodQueryEditText;
    Button searchButton;
    Button saveButton;
    String serverUrl = "https://api.calorieninjas.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // init Realm
        Realm.init(this); // context, usually an Activity or Application
        String realmName = "My Project";
        RealmConfiguration config = new RealmConfiguration.Builder().name(realmName).allowWritesOnUiThread(true).build();
        uiThreadRealm = Realm.getInstance(config);


        // Init
        apiInterface = APIClient.getClient(serverUrl).create(APIInterface.class);
        responseText = (TextView) findViewById(R.id.textView_result);
        foodQueryEditText = findViewById(R.id.edit_text_food_query);
        searchButton = findViewById(R.id.button);
        saveButton = findViewById(R.id.button_save);


        Realm.init(this); // context, usually an Activity or Application

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "save button pressed");
                RealmResults<DateTimeCalorie> results = uiThreadRealm.where(DateTimeCalorie.class).findAll();
                entries.clear();
                for (DateTimeCalorie dateTimeCalorie : results) {
                    entries.add(dateTimeCalorie);
                    Log.d("TAG", "Task: " + dateTimeCalorie.toString());
                }

                try {
                    DateTimeCalorie Task = new DateTimeCalorie(1200.0);
                    uiThreadRealm.executeTransaction (transactionRealm -> {
                        transactionRealm.insert(Task);
                    });
                }catch (Exception ex) {
                    Log.d("ERROR", ex.toString());
                }

            }
        });


        // Button click
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apiKey = getResources().getString(R.string.apikey);
                String queryString = foodQueryEditText.getText().toString();
                queryString = "1 egg, 2 eggs, 3 sandwiches";
                Call<CalorieResults> call = apiInterface.doSearchIngredient(apiKey, queryString);

                call.enqueue(new Callback<CalorieResults>() {
                    @Override
                    public void onResponse(Call<CalorieResults> call, Response<CalorieResults> response) {
                        Log.d("TAG", response.code() + "");
                        String displayResponse = "";
                        CalorieResults results = response.body();
                        for(Ingredient ingredient  : results.items){
                            displayResponse += ingredient.name + " :  " + ingredient.calories + "\n";
                        }

                        responseText.setText(displayResponse);
                    }

                    @Override
                    public void onFailure(Call<CalorieResults> call, Throwable t) {
                        responseText.setText("something error");
                        call.cancel();
                    }
                });
            }
        });

    }



}