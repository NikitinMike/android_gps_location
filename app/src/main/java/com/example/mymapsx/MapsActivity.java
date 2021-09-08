package com.example.mymapsx;

import static java.util.Collections.*;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.example.mymapsx.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    final String LOG_TAG = "myLogs";
    static final ArrayList<String> locations = new ArrayList<>();
    static final ArrayList<LatLng> locationPoints = new ArrayList<>();
    int points = 1000;
    int scale = 10000000;
//    int PRECISION = 1;
    long startDateTime = 0;
    long skipCounter = 0;
    public static final String PREFS_NAME = "MyMapPrefs";
    public static final String PREFS_LOC = "locations";
    int n = 0;
//    SupportMapFragment mapFragment;
//    LatLng base;
    MarkerOptions markerOptions; // = setMarker(10);

    static Window window;
    static Timer timer;
    DBHelper dbHelper;

    @Override
    protected void onResume() {
        restorePrefs();
        super.onResume();
//        freshListView();
    }

    @Override
    protected void onStop() {
        savePrefs();
        super.onStop();
    }

    @Override
    protected void onStart() {
        markerOptions = setMarker(10);
        super.onStart();
//        this.setTitle(MyLocationListener.getLocation());
        dbHelper.getDBData();
//        freshListView();
    }

    void restorePrefs(){
        //restore preferences
        SharedPreferences settings = this.getSharedPreferences(PREFS_NAME, 0);
        locations.clear();
        locations.addAll(settings.getStringSet(PREFS_LOC, new HashSet(locations)));
//        points = locations.size(); System.out.println(points);
//        Log.d(LOG_TAG,"====================================================================");
//        System.out.println(locations);
        locationPoints.clear();
        for (String s : locations) locationPoints.add(getPoint(s));
    }

    void savePrefs(){
//        Log.d(LOG_TAG,"******************************************************************");
//        System.out.println(locations);
        SharedPreferences.Editor editor = this.getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putStringSet(PREFS_LOC, new HashSet(locations));
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        MyLocationListener.SetUpLocationListener(this);

        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        startDateTime = new Date().getTime();

        dbHelper = new DBHelper(this);
//        dbHelper.clearDBData();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        restorePrefs();
        points = locationPoints.size();
        locationPoints.sort(this::compare);

//        Location location = MyLocationListener.imHere;
//        base = new LatLng(location.getLatitude(), location.getLongitude());
//        Log.i(LOG_TAG, base.toString());

        // выполняем задачу MyTimerTask, описание которой будет ниже:
        window = this.getWindow();
        timer = new Timer();
        timer.schedule(new MyTimerTask(), 10000, 10000);
    }

    private void freshListView() {
        ListView positions = findViewById(R.id.map);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, locations.toArray());
        // используем адаптер данных
        positions.setAdapter(adapter);
    }

    int compare(LatLng b, LatLng a) {
        return (int) (((a.latitude == b.latitude) ? a.longitude - b.longitude : a.latitude - b.latitude) * scale);
//        return (int) (((a.longitude == b.longitude) ?a.latitude-b.latitude:a.longitude-b.longitude)*scale);
//        return (int) (Math.abs(a.longitude-b.longitude) + Math.abs(a.latitude-b.latitude)*1000000);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 11.0f));
        refreshMap();
    }

//    private void freshListView() {
//        ListView positions = findViewById(R.id.coordinatesList);
//        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, locations.toArray());
//        // используем адаптер данных
//        positions.setAdapter(adapter);
//    }

    void refreshMap() {
        if (skipCounter == 0) {
            //        PolylineOptions line = new PolylineOptions();
            //        line.width(4f).color(R.color.indigo_900);
            //        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
            for (LatLng point : locationPoints) {
                //            if(point!=null) line.add(point);
                if (point != null) {
                    markerOptions.position(point);
                    mMap.addMarker(markerOptions);
                }
            }
        }
        Location location = MyLocationListener.imHere;
        LatLng me = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(me));
//        10: City
//        15: Streets
//        20: Buildings
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 17.0f));
        mMap.addMarker(new MarkerOptions().position(me).title("It's Me"));
    }

    Double getDouble(String s){
        return Double.parseDouble(Objects.requireNonNull(s).replace(",", "."));
    }

    LatLng getPoint(String o) {
        final Pattern pattern = Pattern.compile("(\\d+,\\d+);(\\d+,\\d+)");
        Matcher matcher = pattern.matcher(o);
        if (matcher.find())
            return new LatLng(getDouble(matcher.group(1)),getDouble(matcher.group(2)));
        return null;
    }

    MarkerOptions setMarker(int bits) {
        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.alpha(0.1F);
        Bitmap bitmap = Bitmap.createBitmap(bits, bits, Bitmap.Config.ARGB_8888);
        for (int x = 0; x < bits; x++)
            for (int y = 0; y < bits; y++) bitmap.setPixel(x, y, Color.BLACK);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
//        markerOptions.icon( BitmapDescriptorFactory.fromAsset("pixel.bmp") );
//                Bitmap.createBitmap(1,1,new Bitmap.Config()));
        return markerOptions;
    }

    // Метод для описания того, что будет происходить при работе таймера (задача для таймера):
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            // Отображаем информацию в текстовом поле count:
            runOnUiThread(() -> {
                String text = MyLocationListener.getLocation();
//                Log.i(LOG_TAG, text);
                locations.add(text);
                LatLng p = getPoint(text);
//                Log.i(LOG_TAG, p.toString());
                long time = new Date().getTime();
//                window.setTitle(time.toString());
//                locationPoints.forEach(lp->Log.i(LOG_TAG, lp.toString()));
                if(dbHelper.saveDBRecord(time,p.latitude,p.longitude)>0L)
                    locationPoints.add(p);
                Log.i(LOG_TAG, p.toString()+":"+locationPoints.size());
//                System.out.println((new Date().getTime()-start)/1000+" "+locationPoints.size()+" "+text);
//                freshListView();
                if ((++skipCounter % 10) == 0) refreshMap();
            });
        }
    }
}