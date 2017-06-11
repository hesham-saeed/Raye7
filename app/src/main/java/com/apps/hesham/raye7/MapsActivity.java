package com.apps.hesham.raye7;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Path;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apps.hesham.raye7.DirectionsAPI.DirectionFinder;
import com.apps.hesham.raye7.DirectionsAPI.DirectionFinderListener;
import com.apps.hesham.raye7.DirectionsAPI.Route;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_RED;

/**
 * Created by Hesham on 05/06/2017.
 */

public class MapsActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        DirectionFinderListener,
        RoutePickerFragment.OnCompleteListener,
        TimePickerFragment.OnTimeSetListener,
        DatePickerFragment.OnDateSetListener{

    private static final String TAG = "myMap";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final String DIALOG_ROUTE = "DialogRoute";

    private static final String SOURCE_MARKER_OPTIONS = "SourceMarkerOptions";
    private static final String DESTINATION_MARKER_OPTIONS = "DestinationMarkerOptions";
    private static final String CAMERA_POSITION = "CameraPosition";
    private static final String FIRST_APP_RUN = "FirstAppRun";
    private static final int REQUEST_CODE_AUTOCOMPLETE_SRC = 1;
    private static final int REQUEST_CODE_AUTOCOMPLETE_DEST = 2;

    private GoogleMap mMap;
    private Toolbar toolbar;

    private EditText srcEditText;
    private String srcLatLng;
    private EditText destEditText;
    private String destLatLng;

    private LocationManager mLocationManager;
    private MyLocationListener mLocationListener;
    private Marker sourceMarker;
    private Marker destinationMarker;

    private Button mLocationButton;
    private Button swapButton;
    private Button findRouteButton;
    private Button takeMyCarButton;
    private Button requestPickupButton;
    private Button dateButton;
    private Button timeButton;

    TextView textNotfiItemCount;
    int mNotifItemCount = 30;
    TextView textMsgItemCount;
    int mMsgItemCount = 2;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;

    private List<Polyline> polylinePaths;
    private ProgressDialog progressDialog;

    private CameraPosition mCameraPosition;
    private boolean appFirstRun = true;
    private MarkerOptions srcMarkerOptions;
    private MarkerOptions destMarkerOptions;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Displaying customized toolbar(app bar)
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Adding navigation drawer to the toolbar
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mToggle = new ActionBarDrawerToggle(this,mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Getting references to widgets and setting their listeners
        srcEditText = (EditText) findViewById(R.id.srcEditText);
        srcEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutoCompleteActvity(REQUEST_CODE_AUTOCOMPLETE_SRC,
                        srcEditText.getText().toString());
            }
        });

        destEditText = (EditText) findViewById(R.id.destEditText);
        destEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAutoCompleteActvity(REQUEST_CODE_AUTOCOMPLETE_DEST,
                        destEditText.getText().toString());
            }
        });

        swapButton = (Button) findViewById(R.id.swapButton);
        swapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(srcEditText.getText().toString().equals(""))
                        && !(destEditText.getText().toString().equals("")))
                {
                    String tempAddress = srcEditText.getText().toString();
                    srcEditText.setText(destEditText.getText().toString());
                    destEditText.setText(tempAddress);
                    String tmpLatLng = srcLatLng;
                    srcLatLng = destLatLng;
                    destLatLng = tmpLatLng;
                    cleanPolylines();
                    DirectionFinder.routes.clear();
                    Marker tmp = sourceMarker;
                    sourceMarker = destinationMarker;
                    destinationMarker = tmp;
                    sourceMarker.showInfoWindow();
                    sourceMarker.setIcon(BitmapDescriptorFactory.defaultMarker(HUE_RED));
                    destinationMarker.setIcon(BitmapDescriptorFactory.defaultMarker(HUE_GREEN));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sourceMarker.getPosition(),13));
                } else{
                    Toast.makeText(MapsActivity.this, R.string.check_src_dest,Toast.LENGTH_SHORT).show();
                }

            }
        });

        findRouteButton = (Button) findViewById(R.id.findRouteButton);
        findRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });

        dateButton = (Button)findViewById(R.id.dateButton);
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                DatePickerFragment dialog = new DatePickerFragment();
                dialog.show(fm, DIALOG_DATE);
            }
        });

        timeButton = (Button)findViewById(R.id.timeButton);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                TimePickerFragment dialog = new TimePickerFragment();
                dialog.show(fm, DIALOG_TIME);
            }
        });

        takeMyCarButton = (Button)findViewById(R.id.takeMyCarButton);
        takeMyCarButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        R.string.join_community,Toast.LENGTH_SHORT).show();
            }
        });
        requestPickupButton = (Button)findViewById(R.id.requestPickupButton);
        requestPickupButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        R.string.join_community,Toast.LENGTH_SHORT).show();
            }
        });


        //Requesting permissions for different devices
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();
        if (Build.VERSION.SDK_INT < 23) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mLocationListener);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                // we have permission already
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mLocationListener);
            }
        }

        if (savedInstanceState != null){
            mCameraPosition = savedInstanceState.getParcelable(CAMERA_POSITION);
            srcMarkerOptions = ((MarkerOptions)savedInstanceState.getParcelable(SOURCE_MARKER_OPTIONS));
            destMarkerOptions = ((MarkerOptions)savedInstanceState.getParcelable(DESTINATION_MARKER_OPTIONS));
            appFirstRun = false;
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, mLocationListener);
        }
    }

    //Using Google Places API to retrieve search suggestions
    private void startAutoCompleteActvity(int REQUEST_CODE, String currentText) {
        PlaceAutocomplete.IntentBuilder intentBuilder =
                new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY);
        try{
            Intent intent = intentBuilder.zzeZ(currentText).build(this);
            startActivityForResult(intent, REQUEST_CODE);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }


    //Helper function
    private void setupBadge(TextView badge, int count){
        if (badge != null) {
            if (count == 0) {
                if (badge.getVisibility() != View.INVISIBLE) {
                    badge.setVisibility(View.INVISIBLE);
                }
            } else {
                badge.setText(String.valueOf(Math.min(count, 999)));
                if (badge.getVisibility() != View.VISIBLE) {
                    badge.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    //Menu Inflating
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);

        final MenuItem menuItemNotification = menu.findItem(R.id.action_notif);
        View v1 = MenuItemCompat.getActionView(menuItemNotification);
        textNotfiItemCount = (TextView) v1.findViewById(R.id.notif_badge);
        setupBadge(textNotfiItemCount, mNotifItemCount);

        final MenuItem menuItemMessage = menu.findItem(R.id.action_msgs);
        View v2 = MenuItemCompat.getActionView(menuItemMessage);
        textMsgItemCount = (TextView) v2.findViewById(R.id.msg_badge);
        setupBadge(textMsgItemCount, mMsgItemCount);

        v1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItemNotification);
            }
        });

        v2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItemMessage);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) //Action item of the Sidebar view
            return true;
        Intent intent;
        switch(item.getItemId()){

            case R.id.action_notif:
                textNotfiItemCount.setText(getString(R.string.zero));
                textNotfiItemCount.setVisibility(View.INVISIBLE);
                intent = new Intent(this, NotificationsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_msgs:
                textMsgItemCount.setText(getString(R.string.zero));
                textMsgItemCount.setVisibility(View.INVISIBLE);
                intent = new Intent(this, MessagesActivity.class);
                startActivity(intent);
                return true;
            //On receiving a message or a notification the count variable should be incremented
            //for both notification badge and message badge

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (sourceMarker != null){
            MarkerOptions sourceOptions = new MarkerOptions()
                    .title(sourceMarker.getTitle())
                    .position(sourceMarker.getPosition())
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(HUE_RED));
            outState.putParcelable(SOURCE_MARKER_OPTIONS, sourceOptions);
        }
        if (destinationMarker != null){
            MarkerOptions destinationOptions = new MarkerOptions()
                    .title(destinationMarker.getTitle())
                    .position(destinationMarker.getPosition())
                    .draggable(false)
                    .icon(BitmapDescriptorFactory.defaultMarker(HUE_GREEN));
            outState.putParcelable(DESTINATION_MARKER_OPTIONS, destinationOptions);
        }
        outState.putParcelable(CAMERA_POSITION,mMap.getCameraPosition());
        outState.putBoolean(FIRST_APP_RUN, appFirstRun);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Showing traffic on map
        mMap.setTrafficEnabled(true);

        //Location Button
        mLocationButton = (Button) findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sourceMarker != null){
                    sourceMarker.remove();
                }
                cleanPolylines();
                Location loc = null;
                try{
                    //loc = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (loc == null)
                        loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }catch(SecurityException x){
                    Toast.makeText(getApplicationContext(),
                            R.string.location_disallowed, Toast.LENGTH_LONG).show();
                }
                if (loc != null){
                    LatLng position = new LatLng(loc.getLatitude(), loc.getLongitude());
                    String address = getAddress(position);
                    srcEditText.setText(address);
                    //srcLatLng = position.toString();
                    srcLatLng = String.valueOf(position.latitude) + "," + String.valueOf(position.longitude);
                    sourceMarker = mMap.addMarker(new MarkerOptions().position(position).title(address).draggable(false));
                    sourceMarker.showInfoWindow();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,13));
                } else {
                    Toast.makeText(getApplicationContext(), R.string.gps_disabled, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Map Long click event
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                if (destinationMarker != null){
                    destinationMarker.remove();
                }

                cleanPolylines();
                DirectionFinder.routes.clear();
                destLatLng = String.valueOf(latLng.latitude) + "," + String.valueOf(latLng.longitude);
                String destAddress = getAddress(latLng);
                destEditText.setText(destAddress);
                destinationMarker = addMarkerToMap(false, destAddress, latLng);
                destinationMarker.showInfoWindow();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,13));
            }
        });

        //Initialize the app with a Marker on user's location if possible
        if (appFirstRun == true)
        {
            mLocationButton.performClick();
            Log.d("abd", "MlocationButton perform click()");

        }
        else //Retaining the map state on a screen orientation change
        {
            if (srcMarkerOptions != null){
                sourceMarker = mMap.addMarker(srcMarkerOptions);
                srcLatLng = String.valueOf(sourceMarker.getPosition().latitude) + ","
                        + String.valueOf(sourceMarker.getPosition().longitude);
            }
            if (destMarkerOptions != null){
                destinationMarker = mMap.addMarker(destMarkerOptions);
                destLatLng = String.valueOf(destinationMarker.getPosition().latitude) + ","
                        + String.valueOf(destinationMarker.getPosition().longitude);
            }

            onComplete();
            if (mCameraPosition != null){
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            }
        }

    }

    //Using Geocoding to retrieve address for a given Latitude-Longitude
    private String getAddress(LatLng latLng){
        Geocoder coder = new Geocoder(getApplicationContext());
        List<Address> addressList;
        try
        {
            addressList = coder.getFromLocation(latLng.latitude,
                    latLng.longitude,1);

            if (!addressList.isEmpty()){
                String address = "";
                int size = addressList.get(0).getMaxAddressLineIndex();
                for (int i=0;i<=size; i++)
                    address += addressList.get(0).getAddressLine(i) + ", ";
                return address;
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        return null;
    }

    //Helper function
    private Marker addMarkerToMap(boolean source, String address, LatLng position){
        MarkerOptions markerOptions = new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker(source ? HUE_RED : HUE_GREEN))
                .title(address)
                .draggable(false)
                .position(position);
        return mMap.addMarker(markerOptions);
    }

    //Functions used for Finding routes between two LatLngs:
    //sendRequest()
    //onDirectionFinderStart()
    //onDirectionFinderSuccess()
    //onComplete()
    private void sendRequest() {
        String origin = srcEditText.getText().toString();
        String destination = destEditText.getText().toString();
        if (origin.isEmpty()) {
            Toast.makeText(this,R.string.empty_source , Toast.LENGTH_SHORT).show();
            return;
        }
        if (destination.isEmpty()) {
            Toast.makeText(this, R.string.empty_destination, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            new DirectionFinder(this, srcLatLng, destLatLng).execute();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, getString(R.string.please_wait),
                getString(R.string.finding_direction), true);

        if (sourceMarker != null){
            sourceMarker.remove();
        }
        if (destinationMarker != null){
            destinationMarker.remove();
        }

        cleanPolylines(); //clean drawn routes on the map
        DirectionFinder.routes.clear();
    }

    @Override
    public void onDirectionFinderSuccess() {
        List<Route> routes = DirectionFinder.routes;
        progressDialog.dismiss();

        if (routes.size() > 1){
            FragmentManager fm = getSupportFragmentManager();
            RoutePickerFragment routeDialog = new RoutePickerFragment();
            routeDialog.show(fm, DIALOG_ROUTE); //onComplete will be called implicitly
        }
        else{
            onComplete();
        }
    }


    //Returning from Route Picker Dialog
    @Override
    public void onComplete() {
        List<Route> routes = DirectionFinder.routes;
        if (!routes.isEmpty()){
            polylinePaths = new ArrayList<>();

            if (sourceMarker != null)
                sourceMarker.remove();

            sourceMarker = addMarkerToMap(true, routes.get(0).getStartAddress(), routes.get(0).getStartLocation());
            sourceMarker.showInfoWindow();

            if (destinationMarker!= null)
                destinationMarker.remove();
            destinationMarker = addMarkerToMap(false, routes.get(0).getEndAddress(), routes.get(0).getEndLocation());

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(routes.get(0).getStartLocation(), 14));

            for (Route route : routes) {

                PolylineOptions polylineOptions = new PolylineOptions();
                if (routes.size() > 1){
                    if (!route.isChosen()) {
                        polylineOptions = polylineOptions.geodesic(true).color(Color.GRAY).width(17);
                    }
                    else
                        polylineOptions = polylineOptions.geodesic(true).color(Color.BLUE).width(20);
                }
                else
                    polylineOptions = polylineOptions.geodesic(true).color(Color.BLUE).width(17);

                for (int i = 0; i < route.getPoints().size(); i++)
                    polylineOptions.add(route.getPoints().get(i));

                polylinePaths.add(mMap.addPolyline(polylineOptions));
            }
        }

    }

    //Using results for the different started activities
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_AUTOCOMPLETE_SRC || requestCode == REQUEST_CODE_AUTOCOMPLETE_DEST) {
                cleanPolylines();
                DirectionFinder.routes.clear();
                Place place = PlaceAutocomplete.getPlace(this, data);
                String address = place.getName() +", " + place.getAddress();
                LatLng position = place.getLatLng();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 13));
                if (REQUEST_CODE_AUTOCOMPLETE_DEST == requestCode) {
                    destEditText.setText(address);
                    destLatLng = String.valueOf(position.latitude) + "," + String.valueOf(position.longitude);
                    if (destinationMarker != null) {
                        destinationMarker.remove();
                    }
                    destinationMarker = addMarkerToMap(false, address, position);
                    destinationMarker.showInfoWindow();
                }
                else if (REQUEST_CODE_AUTOCOMPLETE_SRC == requestCode) {
                    srcEditText.setText(address);
                    srcLatLng = String.valueOf(position.latitude) + "," + String.valueOf(position.longitude);
                    if (sourceMarker != null) {
                        sourceMarker.remove();
                    }
                    sourceMarker = addMarkerToMap(true, address, position);
                    sourceMarker.showInfoWindow();
                }
            }
        }
    }

    //Helper function
    private void cleanPolylines(){
        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    //Returning from Time Picker dialog
    @Override
    public void onTimeSet(String time) {
        timeButton.setText(time);
    }

    //Returning from Date Picker dialog
    @Override
    public void onDateSet(String date) {
        dateButton.setText(date);
    }
}
