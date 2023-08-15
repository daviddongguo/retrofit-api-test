package xyz.dongguo.retrofit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
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
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.dongguo.retrofit.model.APIClient;
import xyz.dongguo.retrofit.model.APIInterface;
import xyz.dongguo.retrofit.model.CalorieResults;
import xyz.dongguo.retrofit.model.DateTimeCalorie;
import xyz.dongguo.retrofit.model.Ingredient;

public class MainActivity extends AppCompatActivity {

    
    // Constant Variables
    String REALM_PROJECT_NAME = "My Project";
    double BASE_INTAKE_CALORIES_DAILY = 2400.0;
    int BASE_STEP_CALORIES = 20;
    long SEVEN_DAYS_AGO_MILLIS = 7 * 24 * 60 * 60 * 1000;
    SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.US);

    // Realm
    Realm uiThreadLocalRealm;

    // Calorie API
    APIInterface apiInterface;
    String serverUrl = "https://api.calorieninjas.com/";

    // Calculate Calorie UI
    EditText foodQueryEditText;
    TextView responseText;
    Button calculateButton;

    // Save Calculate UI
    Button dateButton;
    Button timeButton;
    SeekBar intakeCaloriesSeekBar;
    TextView intakeCaloriesTextView;
    Button saveButton;
    Button refreshButton;

    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    int hour = calendar.get(Calendar.HOUR);
    int minute = calendar.get(Calendar.MINUTE);

    AnyChartView anyChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init Realm
        Realm.init(this); // context, usually an Activity or Application
        RealmConfiguration config = new RealmConfiguration.Builder().name(REALM_PROJECT_NAME).allowWritesOnUiThread(true).build();
        uiThreadLocalRealm = Realm.getInstance(config);

        // init api interface
        apiInterface = APIClient.getClient(serverUrl).create(APIInterface.class);

        // init UI
        initUI();
        refreshGraph();

        // set number seekBar for Intake Calories
        intakeCaloriesSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the TextView with the current progress value
                intakeCaloriesTextView.setText(String.format(Locale.US, "%d", progress * BASE_STEP_CALORIES));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // This method is called when the user starts interacting with the slider
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // This method is called when the user stops interacting with the slider
            }
        });

        // save calorie Button
        saveButton.setOnClickListener(v -> {
            try {
                calendar.set(year, month, dayOfMonth, hour, minute, 0);
                Date dateTime = calendar.getTime();
                Double calories = Double.parseDouble(intakeCaloriesTextView.getText().toString());
                DateTimeCalorie dateTimeCalorie = new DateTimeCalorie(dateTime, calories);
                uiThreadLocalRealm.executeTransaction(transactionRealm -> transactionRealm.insert(dateTimeCalorie));
            } catch (Exception ex) {
                Log.d("ERROR", ex.toString());
            }
        });


        // calculate calorie Button
        calculateButton.setOnClickListener(v -> {
            String apiKey = getResources().getString(R.string.apikey);
            String queryString = foodQueryEditText.getText().toString();
            Call<CalorieResults> call = apiInterface.doSearchIngredient(apiKey, queryString);

            call.enqueue(new Callback<CalorieResults>() {
                @Override
                public void onResponse(Call<CalorieResults> call, Response<CalorieResults> response) {
                    Log.d("TAG", response.code() + "");
                    StringBuilder displayResponse = new StringBuilder();
                    CalorieResults results = response.body();
                    for (Ingredient ingredient : results.items) {
                        displayResponse.append(ingredient.name).append(" :  ").append(ingredient.calories).append("\n");
                    }

                    responseText.setText(displayResponse.toString());
                }

                @Override
                public void onFailure(Call<CalorieResults> call, Throwable t) {
                    call.cancel();
                }
            });
        });

        // refresh graph Button
        refreshButton.setOnClickListener(v -> refreshGraph());


    }// end of onCreate

    private void initUI() {
        foodQueryEditText = findViewById(R.id.edit_text_food_query);
        responseText = findViewById(R.id.textView_result);
        calculateButton = findViewById(R.id.button_calculate);

        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        intakeCaloriesSeekBar = findViewById(R.id.numberSlider);
        intakeCaloriesTextView = findViewById(R.id.sliderValueText);
        saveButton = findViewById(R.id.button_save);
        refreshButton = findViewById(R.id.button_refresh);

        anyChartView = findViewById(R.id.any_chart_view);
    }

    private void refreshGraph() {

        List<DataEntry> listOfDataEntry = new ArrayList<>();
        try {
            long sevenDaysAgoMillis = System.currentTimeMillis() - SEVEN_DAYS_AGO_MILLIS;
            // Filter and sort the results to get the latest 7 days of data
            RealmResults<DateTimeCalorie> results = uiThreadLocalRealm
                    .where(DateTimeCalorie.class)
                    .greaterThanOrEqualTo("dateTime", new Date(sevenDaysAgoMillis))
                    .lessThanOrEqualTo("dateTime", new Date())
                    .findAll()
                    .sort("dateTime", Sort.ASCENDING);

            // Group results by day
            Map<String, List<DateTimeCalorie>> groupedResults = new HashMap<>();

            for (DateTimeCalorie dateTimeCalorie : results) {
                String dateString = DATE_FORMAT.format(dateTimeCalorie.getDateTime());
                Log.d("TAG", dateString);

                if (!groupedResults.containsKey(dateString)) {
                    groupedResults.put(dateString, new ArrayList<>());
                }

                Objects.requireNonNull(groupedResults.get(dateString)).add(dateTimeCalorie);
            }

            // Sort the grouped results
            Map<String, List<DateTimeCalorie>> sortedResults = new TreeMap<>(new KeyComparator());
            sortedResults.putAll(groupedResults);


            for (Map.Entry<String, List<DateTimeCalorie>> entry : sortedResults.entrySet()) {
                String dayString = entry.getKey();
                List<DateTimeCalorie> dayData = entry.getValue();

                Log.d("Day: ", dayString); // Print the day's timestamp

                int count = 0; // Counter for processed items
                Double[] calories = {0.0, 0.0, 0.0};
                for (DateTimeCalorie dateTimeCalorie : dayData) {
                    if (count < 3) { // Process only the first 3 items
                        Log.d("DateTime: ", dateTimeCalorie.getDateTime() + ", Calories: " + dateTimeCalorie.getCalories());
                        calories[count] = dateTimeCalorie.getCalories();
                        count++;
                      } else {
                        double percentIntakeCalories = (calories[0] + calories[1] + calories[2]) / BASE_INTAKE_CALORIES_DAILY;
                        listOfDataEntry.add(new CustomDataEntry(dayString, percentIntakeCalories, calories[2], calories[1], calories[0]));
                        break; // Exit the loop once the first 3 items are processed
                    }
                }

                // If there are less than 3 items, fill remaining calories with 0.0
                while (count < 3) {
                    calories[count] = 0.0;
                    count++;
                }

                // Add the data entry to the data list
                double percentIntakeCalories = (calories[0] + calories[1] + calories[2]) / BASE_INTAKE_CALORIES_DAILY * 100;
                listOfDataEntry.add(new CustomDataEntry(dayString, percentIntakeCalories, calories[2], calories[1], calories[0]));
            }
        } catch (Exception ex) {
            Log.d("Error", ex.toString());
        }

        // chart
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

        Set set = Set.instantiate();
        set.data(listOfDataEntry);
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


    }

    public void popDatePicker(View view) {
        // Show a DatePickerDialog
        DatePickerDialog.OnDateSetListener onDateSetListener = (view1, selectedYear, selectedMonth, selectedDayOfMonth) -> {
            year = selectedYear;
            month = selectedMonth;
            dayOfMonth = selectedDayOfMonth;
            dateButton.setText(String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, dayOfMonth));
        };

        // int style = AlertDialog.THEME_HOLO_DARK;

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, /*style,*/ onDateSetListener, year, month, dayOfMonth);

        datePickerDialog.setTitle("Select Date");
        datePickerDialog.show();
    }

    public void popTimePicker(View view) {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = (timePicker, selectedHour, selectedMinute) -> {
            hour = selectedHour;
            minute = selectedMinute;
            timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
        };

        // int style = AlertDialog.THEME_HOLO_DARK;

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, /*style,*/ onTimeSetListener, hour, minute, true);

        timePickerDialog.setTitle("Select Time");
        timePickerDialog.show();
    }

    private static class KeyComparator implements Comparator<String> {
        @Override
        public int compare(String key1, String key2) {
            return key1.compareTo(key2);
        }
    }

    private static class CustomDataEntry extends ValueDataEntry {
        CustomDataEntry(String x, Number value, Number value2, Number value3, Number value4) {
            super(x, value);
            setValue("value2", value2);
            setValue("value3", value3);
            setValue("value4", value4);
        }
    } // end of Custom DataEntry class

}