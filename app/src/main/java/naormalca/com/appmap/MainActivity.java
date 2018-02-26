package naormalca.com.appmap;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import naormalca.com.appmap.model.Report;

import static naormalca.com.appmap.ReportActivity.DB_REPORTS;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_CRIMINAL;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_ECONOMY;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_SECURITY;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_SOCIAL;
import static naormalca.com.appmap.misc.Constant.REPORT_LAT;
import static naormalca.com.appmap.misc.Constant.REPORT_LNG;
import static naormalca.com.appmap.misc.Constant.TLV_LAT;
import static naormalca.com.appmap.misc.Constant.TLV_LNG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback
        ,GoogleMap.OnMapLoadedCallback
{

    private DatabaseReference mDatabase;
    SupportMapFragment mSupportMapFragment;

    GoogleMap mMap;
    private GPSTracker gps;

    private double mLatitudeClick;
    private double mLongitudeClick;
    private Marker currentPositionMarker;
    private boolean isMapLoaded = false;

    private int currentReportTypeShow;
    private boolean isReportTypeFilter;

    ArrayList<Report> mReports = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mSupportMapFragment.getMapAsync(this);
        gps = new GPSTracker(MainActivity.this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMapLoaded) {
                    if (radiusCheck(new LatLng(mLatitudeClick, mLongitudeClick))) {
                        Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                        intent.putExtra(REPORT_LAT, mLatitudeClick);
                        intent.putExtra(REPORT_LNG, mLongitudeClick);
                        startActivity(intent);
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(view, "אינך נמצא ברדיוס הדיווח!", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                } else{
                    Snackbar snackbar = Snackbar
                            .make(view, "The map not render!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.noFilter){
            isReportTypeFilter = false;
        }
        else if (id == R.id.securityItem) {
            isReportTypeFilter = true;
            currentReportTypeShow = MARKER_TYPE_SECURITY;
        } else if (id == R.id.economyItem) {
            isReportTypeFilter = true;
            currentReportTypeShow = MARKER_TYPE_ECONOMY;
        } else if (id == R.id.socialItem) {
            isReportTypeFilter = true;
            currentReportTypeShow = MARKER_TYPE_SOCIAL;
        } else if (id == R.id.criminalItem) {
            isReportTypeFilter = true;
            currentReportTypeShow = MARKER_TYPE_CRIMINAL;
        }
        markersSetup();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //map ready
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        //focus the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(TLV_LAT,TLV_LNG)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(TLV_LAT,TLV_LNG), 10.0f));
        showCurrentPositionOnMap(gps);
        //call to MapLoaded
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // A temp marker
                mLatitudeClick = latLng.latitude;
                mLongitudeClick = latLng.longitude;
                if (currentPositionMarker != null)
                    currentPositionMarker.remove();
                currentPositionMarker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latLng.latitude, latLng.longitude)));

            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference(DB_REPORTS);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Report report = postSnapshot.getValue(Report.class);
                    mReports.add(report);
                }
                markersSetup();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        markersSetup();
    }


    private void markersSetup() {
        mMap.clear();
        showCurrentPositionOnMap(gps);
        Log.d("MarkersSetup","isReportTYPEFilter : "+isReportTypeFilter);
        for (Report report: mReports) {
            if (!isReportTypeFilter) {
                Log.d("MarkersSetup","each report : "+report.getTitle());
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(report.getLatitude(), report.getLongitude()))
                        .title(report.getTitle())
                        .snippet(report.getTime())
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(Report.iconColors[report.getType()])));
            } else if(currentReportTypeShow == report.getType()){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(report.getLatitude(), report.getLongitude()))
                        .title(report.getTitle())
                        .snippet(report.getTime())
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(Report.iconColors[report.getType()]))).showInfoWindow();

            }
        }
    }

    public void showCurrentPositionOnMap(GPSTracker gps)
    {
        double currentLongitude, currentLatitude;
        if(gps.isCanGetLocation()) {
            Log.d("gpstracker","showCurrentPosition");
            currentLatitude = gps.getLatitude();
            currentLongitude = gps.getLongitude();

            mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude))
                    .title(currentLatitude+"/"+currentLongitude)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        }
    }
    private boolean radiusCheck(LatLng newReport) {
        Location target = new Location("target");
        target.setLatitude(newReport.latitude);
        target.setLongitude(newReport.longitude);
        if (gps.location.distanceTo(target) < 2000){
            return true;
        }
        return false;
    }

    @Override
    public void onMapLoaded() {
        isMapLoaded = true;
    }
}
