package com.unipi.p19216.mypoi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements LocationListener {
    LocationManager locationManager;
    TextView textView;
    String cords = null;

    SQLiteDatabase db;

    // timestamp

    String timestamp = String.valueOf(Calendar.getInstance().getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //get the spinner from the xml.
        Spinner dropdown = findViewById(R.id.spinner2);

        //create a list of items for the spinner.
        String[] items = new String[]{"Restaurant", "Museum", "Cafe", "Hotel", "Library"};

        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);

        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);

        // ask permission for location access
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);

            //return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        // create or open database
        db = openOrCreateDatabase("DB1.db",MODE_PRIVATE,null);

        db.execSQL("CREATE TABLE IF NOT EXISTS POI(" +
                "title TEXT PRIMARY KEY," +
                "timestamp TEXT," +
                "location TEXT," +
                "category TEXT," +
                "description TEXT)");

    }

    // on click add poi button
    public void addPOI(View view){
        TextView loading_message = findViewById(R.id.loading_message3);
        View panel  = findViewById(R.id.view3);
        View progress_bar = findViewById(R.id.progressBar4);
        TextView title = findViewById(R.id.editText3);
        Spinner category = findViewById(R.id.spinner2);
        TextView description = findViewById(R.id.editTextTextMultiLine3);
        Button btn = findViewById(R.id.button);

        // if all edit texts are filled in then insert into the database the new POI
        if(!title.getText().toString().trim().isEmpty() && cords != null && category.getSelectedItem().toString() != null && !description.getText().toString().trim().isEmpty()){
            db.execSQL("INSERT OR IGNORE INTO POI VALUES(?,?,?,?,?)", new String[]{title.getText().toString(),timestamp ,cords.toString() ,category.getSelectedItem().toString(), description.getText().toString()});
            Toast.makeText(MainActivity.this, "POI has been added successfully!", Toast.LENGTH_LONG).show();
        }
        // else if any input is empty then tell user to fill all of them
        else{
            Toast.makeText(MainActivity.this, "Please fill in all the required inputs!", Toast.LENGTH_LONG).show();
        }


    }

    // on click show all button
    public void showAll(View view){
        Intent intent = new Intent(this, showAllPOI.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        TextView loading_message = findViewById(R.id.loading_message3);
        TextView loc = findViewById(R.id.textView8);
        View panel  = findViewById(R.id.view3);
        View progress_bar = findViewById(R.id.progressBar4);
        TextView title = findViewById(R.id.editText3);
        Spinner category = findViewById(R.id.spinner2);
        TextView description = findViewById(R.id.editTextTextMultiLine3);
        loc.setText(location.getLatitude()+","+location.getLongitude());
        Button btn = findViewById(R.id.button);
        Button btn2 = findViewById(R.id.button5);

        // cords gets the location coordinates as string
        // while app is loading the current coordinates, variable cords is null
        // while cords is null set visible a new view which informs the user to wait until his
        // current location loads up.
        if(cords==null){
            loading_message.setVisibility(View.VISIBLE);
            progress_bar.setVisibility(View.VISIBLE);
            //loc.setVisibility(View.INVISIBLE);
            panel.setVisibility(View.VISIBLE);
            title.setEnabled(false);
            category.setEnabled(false);
            description.setEnabled(false);
            btn.setClickable(false);
            btn.setEnabled(false);
            btn2.setClickable(false);
            btn2.setEnabled(false);

        }
        // when cords get user's current location set loading view invisible
        else{
            loading_message.setVisibility(View.INVISIBLE);
            progress_bar.setVisibility(View.INVISIBLE);
            //loc.setVisibility(View.VISIBLE);
            panel.setVisibility(View.INVISIBLE);
            title.setEnabled(true);
            category.setEnabled(true);
            description.setEnabled(true);
            btn.setClickable(true);
            btn2.setClickable(true);
            btn.setEnabled(true);
            btn2.setEnabled(true);

        }
        cords = location.getLatitude()+","+location.getLongitude();
    }

    public void showMessage(String title, String text){
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(text)
                .show();
    }
}