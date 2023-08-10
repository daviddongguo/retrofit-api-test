package xyz.dongguo.retrofit;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.dongguo.retrofit.model.APIClient;
import xyz.dongguo.retrofit.model.APIInterface;
import xyz.dongguo.retrofit.model.CalorieResults;
import xyz.dongguo.retrofit.model.Ingredient;

public class MainActivity extends AppCompatActivity {

    APIInterface apiInterface;
    TextView responseText;
    EditText foodQueryEditText;
    Button searchButton;
    String serverUrl = "https://api.calorieninjas.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Init
        apiInterface = APIClient.getClient(serverUrl).create(APIInterface.class);
        responseText = (TextView) findViewById(R.id.textView_result);
        foodQueryEditText = findViewById(R.id.edit_text_food_query);
        searchButton = findViewById(R.id.button);


        // Button click
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apiKey = getResources().getString(R.string.apikey);
                String queryString = foodQueryEditText.getText().toString();
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