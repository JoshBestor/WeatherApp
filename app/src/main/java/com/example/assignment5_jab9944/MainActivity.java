package com.example.assignment5_jab9944;

import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private String latitude;
    private String longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //make powered by clickable
        Spanned policy = Html.fromHtml(getString(R.string.PoweredBy));
        TextView termsOfUse = findViewById(R.id.PoweredBy);
        termsOfUse.setText(policy);
        termsOfUse.setMovementMethod(LinkMovementMethod.getInstance());

        //get lat and long
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){

                    double lat = round(location.getLatitude(), 4);
                    double log = round(location.getLongitude(), 4);

                    latitude = String.valueOf(lat);
                    longitude = String.valueOf(log);

                    //get json for data from dark sky
                    OkHttpClient client = new OkHttpClient();
                    String url = "https://api.darksky.net/forecast/739777f33fd4b608e1111d1d7add5434/" + latitude + "," + longitude +"?exclude=minutely,alerts,flag";
                    Request request = new Request.Builder().url(url).build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if(response.isSuccessful()){
                                final String myResponse = response.body().string();
                                Log.i("data", myResponse);

                                Weather w = new Weather();

                                ObjectMapper mapper = new ObjectMapper();
                                mapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
                                w = mapper.readValue(myResponse, Weather.class);

                                mapWeatherToScreen(w);

                            }else{
                                Log.i("event", "Failed the request");
                            }
                        }
                    });

                }else{
                    Log.i("event", "Null location");
                }
            }
        });
    }

    //method that allows me to round numbers
    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //puts the value from dark sky into the corresponding text view
    private void mapWeatherToScreen(Weather weather) {
        final Weather w = weather;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                DecimalFormat df = new DecimalFormat("#");
                String s, h, t;
                int day;

                TextView currentTemp = findViewById(R.id.currentTemp);
                currentTemp.setText(df.format(w.getCurrently().getTemperature()) + "°");

                TextView currentFeelsLike = findViewById(R.id.currentFeelsLike);
                currentFeelsLike.setText("Feels like: " + df.format(w.getCurrently().getApparentTemperature()) + "°");

                TextView currentPrecip = findViewById(R.id.currentPrecip);
                s = getProbabilityValue(w.getCurrently().getPrecipProbability().toString());
                currentPrecip.setText("Precipitation: " + s.substring(2) + "%");

                TextView currentWind = findViewById(R.id.currentWind);
                currentWind.setText("Wind: " + df.format(w.getCurrently().getWindSpeed()) + "mph");

                Date date = new Date();
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTime(date);
                int ti = calendar.get(Calendar.HOUR_OF_DAY);
                int hour = calendar.get(Calendar.HOUR);
                calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_WEEK);

                TextView hourly_1 = findViewById(R.id.hourly_1);
                s = getProbabilityValue(w.getHourly().getData().get(1).getPrecipProbability().toString());
                if(((hour + 1) % 12) == 0) {
                    h = String.valueOf(12);
                }else{
                    h = String.valueOf((hour + 1) % 12);
                }
                if((ti + 1) > 11){
                    t = "pm";
                }else{
                    t = "am";
                }
                hourly_1.setText(h + t + "\n" + df.format(w.getHourly().getData().get(1).getTemperature()) + "°" + "\n" + s.substring(2) + "%");

                TextView hourly_2 = findViewById(R.id.hourly_2);
                s = getProbabilityValue(w.getHourly().getData().get(2).getPrecipProbability().toString());
                if(((hour + 2) % 12) == 0) {
                    h = String.valueOf(12);
                }else{
                    h = String.valueOf((hour + 2) % 12);
                }
                if((ti + 2) > 11){
                    t = "pm";
                }else{
                    t = "am";
                }
                hourly_2.setText(h + t + "\n" + df.format(w.getHourly().getData().get(2).getTemperature()) + "°" + "\n" + s.substring(2) + "%");

                TextView hourly_3 = findViewById(R.id.hourly_3);
                s = getProbabilityValue(w.getHourly().getData().get(3).getPrecipProbability().toString());
                if(((hour + 3) % 12) == 0) {
                    h = String.valueOf(12);
                }else{
                    h = String.valueOf((hour + 3) % 12);
                }
                if((ti + 3) > 11){
                    t = "pm";
                }else{
                    t = "am";
                }
                hourly_3.setText(h + t + "\n" + df.format(w.getHourly().getData().get(3).getTemperature()) + "°" + "\n" + s.substring(2) + "%");

                TextView hourly_4 = findViewById(R.id.hourly_4);
                s = getProbabilityValue(w.getHourly().getData().get(4).getPrecipProbability().toString());
                if(((hour + 4) % 12) == 0) {
                    h = String.valueOf(12);
                }else{
                    h = String.valueOf((hour + 4) % 12);
                }
                if((ti + 4) > 11){
                    t = "pm";
                }else{
                    t = "am";
                }
                hourly_4.setText(h + t + "\n" + df.format(w.getHourly().getData().get(4).getTemperature()) + "°" + "\n" + s.substring(2) + "%");

                TextView hourly_5 = findViewById(R.id.hourly_5);
                s = getProbabilityValue(w.getHourly().getData().get(5).getPrecipProbability().toString());
                if(((hour + 5) % 12) == 0) {
                    h = String.valueOf(12);
                }else{
                    h = String.valueOf((hour + 5) % 12);
                }
                if((ti + 5) > 11){
                    t = "pm";
                }else{
                    t = "am";
                }
                hourly_5.setText(h + t + "\n" + df.format(w.getHourly().getData().get(5).getTemperature()) + "°" + "\n" + s.substring(2) + "%");

                TextView day_0_date = findViewById(R.id.day_0_date);
                day_0_date.setText(getDOW(day));

                TextView day_1_date = findViewById(R.id.day_1_date);
                day_1_date.setText(getDOW(day+1));

                TextView day_2_date = findViewById(R.id.day_2_date);
                day_2_date.setText(getDOW(day+2));

                TextView day_3_date = findViewById(R.id.day_3_date);
                day_3_date.setText(getDOW(day+3));

                TextView day_4_date = findViewById(R.id.day_4_date);
                day_4_date.setText(getDOW(day+4));

                TextView day_5_date = findViewById(R.id.day_5_date);
                day_5_date.setText(getDOW(day+5));

                TextView day_6_date = findViewById(R.id.day_6_date);
                day_6_date.setText(getDOW(day+6));

                TextView day_7_date = findViewById(R.id.day_7_date);
                day_7_date.setText(getDOW(day));

                TextView day_0_temp = findViewById(R.id.day_0_temp);
                day_0_temp.setText(df.format(w.getDaily().getData().get(0).getTemperatureHigh()) + "°" + " " + df.format(w.getDaily().getData().get(0).getTemperatureLow()) + "°");

                TextView day_1_temp = findViewById(R.id.day_1_temp);
                day_1_temp.setText(df.format(w.getDaily().getData().get(1).getTemperatureHigh()) + "°" + " " + df.format(w.getDaily().getData().get(1).getTemperatureLow()) + "°");

                TextView day_2_temp = findViewById(R.id.day_2_temp);
                day_2_temp.setText(df.format(w.getDaily().getData().get(2).getTemperatureHigh()) + "°" + " " + df.format(w.getDaily().getData().get(2).getTemperatureLow()) + "°");

                TextView day_3_temp = findViewById(R.id.day_3_temp);
                day_3_temp.setText(df.format(w.getDaily().getData().get(3).getTemperatureHigh()) + "°" + " " + df.format(w.getDaily().getData().get(3).getTemperatureLow()) + "°");

                TextView day_4_temp = findViewById(R.id.day_4_temp);
                day_4_temp.setText(df.format(w.getDaily().getData().get(4).getTemperatureHigh()) + "°" + " " + df.format(w.getDaily().getData().get(4).getTemperatureLow()) + "°");

                TextView day_5_temp = findViewById(R.id.day_5_temp);
                day_5_temp.setText(df.format(w.getDaily().getData().get(5).getTemperatureHigh()) + "°" + " " + df.format(w.getDaily().getData().get(5).getTemperatureLow()) + "°");

                TextView day_6_temp = findViewById(R.id.day_6_temp);
                day_6_temp.setText(df.format(w.getDaily().getData().get(6).getTemperatureHigh()) + "°" + " " + df.format(w.getDaily().getData().get(6).getTemperatureLow()) + "°");

                TextView day_7_temp = findViewById(R.id.day_7_temp);
                day_7_temp.setText(df.format(w.getDaily().getData().get(7).getTemperatureHigh()) + "°" + " " + df.format(w.getDaily().getData().get(7).getTemperatureLow()) + "°");
            }
        });
    }

    private String getProbabilityValue(String prob){
        String s;

        if((prob.charAt(2) == '0')){
            s =  "0";
        }else if(prob.length() == 3) {
            s = prob.substring(2) + "0";
        }else{
            s = prob.substring(2);
        }

        return s;
    }

    private String getDOW(int num){
        num %= 7;
        if(num == 1){
            return "Sun";
        }else if(num == 2){
            return "Mon";
        }else if(num == 3){
            return "Tue";
        }else if(num == 4){
            return "Wed";
        }else if(num == 5){
            return "Thur";
        }else if(num == 6){
            return "Fri";
        }else if(num == 0){
            return "Sat";
        }else{
            return "Sun";
        }
    }
}
