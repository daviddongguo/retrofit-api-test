package xyz.dongguo.retrofit;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Orientation;
import com.anychart.enums.ScaleStackMode;
import com.anychart.scales.Linear;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.US);
        List<DataEntry> data = new ArrayList<>();
        try {
            RealmResults<DateTimeCalorie> results = uiThreadRealm.where(DateTimeCalorie.class).findAll();
            entries.clear();
            for (DateTimeCalorie dateTimeCalorie : results) {
                Log.d("TAG", "Task: " );
                Log.d("TAG", "Task: " + dateTimeCalorie.getDateTime() + dateTimeCalorie.getCalories().toString());
                data.add(new CustomDataEntry(dateFormat.format(dateTimeCalorie.getDateTime()), 50, 250, 500, dateTimeCalorie.getCalories()));
            }
        } catch (Exception ex) {
            Log.d("Error", ex.toString());
        }

        // chart
        AnyChartView anyChartView = findViewById(R.id.any_chart_view);

        Cartesian cartesian = AnyChart.cartesian();

        cartesian.animation(true);

        cartesian.title("Combination of Stacked Column and Line Chart (Dual Y-Axis)");

        cartesian.yScale().stackMode(ScaleStackMode.VALUE);

        Linear scalesLinear = Linear.instantiate();
        scalesLinear.minimum(0d);
        scalesLinear.maximum(100d);
        scalesLinear.ticks("{ interval: 20 }");

        com.anychart.core.axes.Linear extraYAxis = cartesian.yAxis(1d);
        extraYAxis.orientation(Orientation.RIGHT)
                .scale(scalesLinear);
        extraYAxis.labels()
                .padding(0d, 0d, 0d, 5d)
                .format("{%Value}%");

//        List<DataEntry> data = new ArrayList<>();
        data.add(new CustomDataEntry("Aug 15", 96.5, 2040, 1200, 1600));
        data.add(new CustomDataEntry("Aug 16", 50.1, 1794, 1124, 1724));
        data.add(new CustomDataEntry("Aug 17", 73.2, 2026, 1006, 1806));
        data.add(new CustomDataEntry("Aug 18", 50.1, 2341, 921, 1621));
        data.add(new CustomDataEntry("Aug 19", 70.0, 1800, 1500, 1700));
//        data.add(new CustomDataEntry("Aug 6", 60.7, 1507, 1007, 1907));
//        data.add(new CustomDataEntry("Aug 7", 62.1, 2701, 921, 1821));
//        data.add(new CustomDataEntry("Aug 8", 75.1, 1671, 971, 1671));
//        data.add(new CustomDataEntry("Aug 9", 80.0, 1980, 1080, 1880));
//        data.add(new CustomDataEntry("Aug 10", 54.1, 1041, 1041, 1641));
//        data.add(new CustomDataEntry("Aug 11", 51.3, 813, 1113, 1913));
//        data.add(new CustomDataEntry("Aug 12", 59.1, 691, 1091, 1691));
//        data.add(new CustomDataEntry("Aug 15", 96.5, 2040, 1200, 1600));

        Set set = Set.instantiate();
        set.data(data);
        Mapping lineData = set.mapAs("{ x: 'x', value: 'value' }");
        Mapping column1Data = set.mapAs("{ x: 'x', value: 'value2' }");
        Mapping column2Data = set.mapAs("{ x: 'x', value: 'value3' }");
        Mapping column3Data = set.mapAs("{ x: 'x', value: 'value4' }");

        cartesian.column(column1Data);
        cartesian.crosshair(true);

        Line line = cartesian.line(lineData);
        line.yScale(scalesLinear);

        cartesian.column(column2Data);

        cartesian.column(column3Data);

        anyChartView.setChart(cartesian);

        // end of char




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


                try {
                    DateTimeCalorie Task = new DateTimeCalorie(1200.0);
                    uiThreadRealm.executeTransaction(transactionRealm -> {
                        transactionRealm.insert(Task);
                    });
                } catch (Exception ex) {
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
                        for (Ingredient ingredient : results.items) {
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

    }// end of onCreate

    private class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2, Number value3, Number value4) {
            super(x, value);
            setValue("value2", value2);
            setValue("value3", value3);
            setValue("value4", value4);
        }
    } // end of dataentry class


}