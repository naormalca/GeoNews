package naormalca.com.appmap;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akhgupta.easylocation.EasyLocationAppCompatActivity;
import com.akhgupta.easylocation.EasyLocationRequest;
import com.akhgupta.easylocation.EasyLocationRequestBuilder;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import naormalca.com.appmap.Firebase.FirebaseDB;
import naormalca.com.appmap.misc.GPSTracker;
import naormalca.com.appmap.model.Report;
import naormalca.com.appmap.model.Users;
import naormalca.com.appmap.ui.LoginActivity;
import naormalca.com.appmap.ui.RegisterActivity;
import naormalca.com.appmap.ui.ReportActivity;
import naormalca.com.appmap.ui.ReportListViewActivity;
import naormalca.com.appmap.ui.ShowReportFragment;

import static naormalca.com.appmap.Firebase.FirebaseDB.DB_REPORTS;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_ACCIDENT;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_CRIMINAL;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_ECONOMY;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_EXPLOSIVE;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_SECURITY;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_SOCIAL;
import static naormalca.com.appmap.misc.Constant.REPORT_FRAGMENT_TAG;
import static naormalca.com.appmap.misc.Constant.REPORT_LAT;
import static naormalca.com.appmap.misc.Constant.REPORT_LNG;
import static naormalca.com.appmap.misc.Constant.IL_LAT;
import static naormalca.com.appmap.misc.Constant.IL_LNG;

public class MainActivity extends EasyLocationAppCompatActivity
implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback
        , GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnMarkerClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    // Fire base members
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;


    // Map members
    GoogleMap mMap;
    private GPSTracker gps;
    private double mLatitudeClick;
    private double mLongitudeClick;
    private Marker mTempMarkerTarget;
    private boolean isMapLoaded = false;

    // Model members
    private boolean mIsReportTypeFilter;
    private ArrayList<Report> mReports = new ArrayList<>();
    private int mCurrentReportTypeShow;
    private Users currentUserData;

    // Report fragment
    private ShowReportFragment mShowReportFragment;
    // Views
    private TextView mUserHelloMsg;
    //New Location
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    private Location mCurrentLocation;
    private SupportMapFragment supportMapFragment;


    /*
    *
    * Life-Cycle functions
    *
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //  setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Map content fragment
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);

        //First check for location permission,
        // after kick off the map and location service
        checkLocationPermission();

        //async the map



        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup floating report button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMapLoaded && mCurrentLocation != null) {
                    if (radiusCheck(new LatLng(mLatitudeClick, mLongitudeClick))) {
                        //pass the location to ReportActivity
                        Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                        intent.putExtra(REPORT_LAT, mLatitudeClick);
                        intent.putExtra(REPORT_LNG, mLongitudeClick);
                        startActivity(intent);
                    } else {
                        // Cant report radius check fail
                        Snackbar snackbar = Snackbar
                                .make(view, "אינך נמצא ברדיוס הדיווח!", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                } else if (!isMapLoaded){
                    Snackbar snackbar = Snackbar
                            .make(view, "The map not render!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        });

        // Setup action bar
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Setup navigation bar and listener
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Hello navigation bar user message
        LinearLayout linearLayout = (LinearLayout) navigationView.getHeaderView(0);
        mUserHelloMsg = linearLayout.findViewById(R.id.helloMsgItem);


        // Authentication
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // Update the UI match the user connected or not
                updateUI(user);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Here","on resume!!!");
        mDatabase = FirebaseDatabase.getInstance().getReference(DB_REPORTS);
        mDatabase.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            ArrayList<Report> currentReport = new ArrayList<>();
            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                // Add each report to array list
                Report report = postSnapshot.getValue(Report.class);
                currentReport.add(report);
            }
            mReports.clear();
            mReports = currentReport;
            markersSetup();
        }
        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    });
}


    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //TODO: [UX] add a alert message before exit from app
            super.onBackPressed();
        }
    }

    /*
    *
    * Authentication functions
    *
    * */

    /*TODO: Change function first retrieve the user data, after change in ui
    */

    /**
     * If user user auth succeed, This function called by updateUi() function and
     * receive all the users name by userID than fix the UI
     * @param userId
     */
    private void getUserInfoAndUpdate(final String userId){
        // Get the specific reference
        mDatabase = FirebaseDatabase.getInstance().getReference(FirebaseDB.USERS_DB);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    currentUserData = postSnapshot.getValue(Users.class);
                    Log.d(TAG,currentUserData.getFirstName()+" "+currentUserData.getLastName()+"inOnDataChange"+currentUserData.getID());
                    // Check which user id parallel to name
                    if (currentUserData.getID().equals(userId)){
                        // Change the hello msg at navigation bar
                        mUserHelloMsg = findViewById(R.id.helloMsgItem);
                        mUserHelloMsg.setText(" שלום, "+currentUserData.getFirstName());
                        toggleAuthOptions(true);
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    /**
     * Toggle options item visibility
     * @param visible
     */
    private void toggleAuthOptions(boolean visible) {
        // Find the menu item and change visibility
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        MenuItem signInItem = menu.findItem(R.id.signInItem);
        MenuItem signOutItem = menu.findItem(R.id.signOutItem);
        MenuItem signUpItem = menu.findItem(R.id.signUpItem);
        // If @visible is true, there user connected else it`s guest
        signInItem.setVisible(!visible);
        signUpItem.setVisible(!visible);
        signOutItem.setVisible(visible);
    }

    /**
     * Sign out from firebase user with alert box
     */
    private void signOut() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("האם תרצה להתנתק?");
        alert.setCancelable(false);
        alert.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAuth.signOut();
                updateUI(null);
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }
    /*
    *
    * UI Functions
    *
    * */
    private void updateUI(FirebaseUser user) {
        if (user != null){
            Log.d(TAG, user.getEmail()+ user.getUid());
            getUserInfoAndUpdate(user.getUid());
        } else{
            guestInfoUpdate();
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
            mIsReportTypeFilter = false;
        }
        else if (id == R.id.securityItem) {
            mIsReportTypeFilter = true;
            mCurrentReportTypeShow = MARKER_TYPE_SECURITY;
        } else if (id == R.id.economyItem) {
            mIsReportTypeFilter = true;
            mCurrentReportTypeShow = MARKER_TYPE_ECONOMY;
        } else if (id == R.id.socialItem) {
            mIsReportTypeFilter = true;
            mCurrentReportTypeShow = MARKER_TYPE_SOCIAL;
        } else if (id == R.id.criminalItem) {
            mIsReportTypeFilter = true;
            mCurrentReportTypeShow = MARKER_TYPE_CRIMINAL;
        } else if (id == R.id.accidentItem){
            mIsReportTypeFilter = true;
            mCurrentReportTypeShow = MARKER_TYPE_ACCIDENT;
        } else if (id == R.id.explosiveItem){
            mIsReportTypeFilter = true;
            mCurrentReportTypeShow = MARKER_TYPE_EXPLOSIVE;
        } else if (id == R.id.signUpItem){
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.signInItem){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.reportListViewItem){
            Intent intent = new Intent(MainActivity.this, ReportListViewActivity.class);
            intent.putExtra("test", mReports);
            startActivity(intent);
            return true;
        }
        else if (id == R.id.signOutItem){
            signOut();
            return true;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        markersSetup();
        return true;
    }
    private void guestInfoUpdate() {
        mUserHelloMsg.setText("שלום, אורח");
        toggleAuthOptions(false);
    }
    /*
    *
    * Map functions
    *
    *
    * */


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Map ready
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        // Set location icon
        mMap.setMyLocationEnabled(true);
        // Set traffic info by google
        mMap.setTrafficEnabled(true);

        // Call to MapLoaded, MapClickListener, OnMarkerClickListener
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                // A temp marker
                mLatitudeClick = latLng.latitude;
                mLongitudeClick = latLng.longitude;
                // Remove the last temp marker if exist.
                if (mTempMarkerTarget != null)
                    mTempMarkerTarget.remove();
                mTempMarkerTarget = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(latLng.latitude, latLng.longitude)));

                // Remove current report fragment when user click randomly on map.
                if (mShowReportFragment != null){
                    getSupportFragmentManager().beginTransaction()
                            .remove(mShowReportFragment).commit();
                }
            }
        });

        // Retrieve reports from DB
        mDatabase = FirebaseDatabase.getInstance().getReference(DB_REPORTS);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    // Add each report to array list
                    Report report = postSnapshot.getValue(Report.class);
                    mReports.add(report);
                }
                //
                markersSetup();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        // Show report has markers on map
        markersSetup();
    }

    private void markersSetup() {
        if (mReports != null) {
            // Clear map
            mMap.clear();
            for (Report report : mReports) {
                // Check if there report filter
                if (!mIsReportTypeFilter) {
                    // Non-filter - show all reports
                    Log.d("MarkersSetup", "each report : " + report.getTitle());
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(report.getLatitude(), report.getLongitude()))
                            .title(report.getTitle())
                            .snippet(report.getTime())
                            .icon(BitmapDescriptorFactory
                                    .fromBitmap(resizeBitmap(Report.iconColors[report.getType()], 100, 100))))
                            .setTag(report);
                    // .defaultMarker(Report.iconColors[report.getType()])))
                } else if (mCurrentReportTypeShow == report.getType()) {
                    // Show only specific filter
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(report.getLatitude(), report.getLongitude()))
                            .title(report.getTitle())
                            .snippet(report.getTime())
                            .icon(BitmapDescriptorFactory.
                                    fromBitmap(resizeBitmap(Report.iconColors[report.getType()], 100, 100))))
                            .setTag(report);

                }
            }
        }
    }

    public Bitmap resizeBitmap(String drawableName, int width, int height){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources()
                .getIdentifier(drawableName, "drawable", getPackageName()), options);
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false);
    }


    private boolean radiusCheck(LatLng newReport) {
        // Check current position radius 2km around
        Location target = new Location("target");
        target.setLatitude(newReport.latitude);
        target.setLongitude(newReport.longitude);
        return mCurrentLocation.distanceTo(target) < 2000;
    }

    @Override
    public void onMapLoaded() {
        isMapLoaded = true;
        // Focus the camera*/
        Log.d(TAG,mCurrentLatitude+"asdasdasdas"+mCurrentLatitude+"");
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mCurrentLatitude, mCurrentLongitude)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mCurrentLatitude, mCurrentLongitude), 10.0f));
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        // Get the report object from clicked marker
        Report report = (Report) marker.getTag();
        if(report != null) {
            if (report.isShow()) {
                mShowReportFragment = new ShowReportFragment();
                mShowReportFragment.setReport(report);

                // Replace whatever is in the fragment_container view with this mShowReportFragment,
                // and add the mFragmentTransaction to the back stack so the user can navigate back
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, mShowReportFragment, REPORT_FRAGMENT_TAG);
                //mFragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();
            }
        }
        return true;
    }

    /**
     * check locations permission,
     * If it`s granted the map and location service kick off
     */
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Ask for permission and call to @onRequestPermissionsResult
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},2);

        } else{
            // Permission also granted
            kickTheMap();
        }
    }

    /**
     * Deal with permission ask result
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1 : {
                // Location permission
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Move on
                    kickTheMap();
                } else{
                    //TODO:Some manipulation, maybe restart or another activity
                    // Close app
                    finish();
                }
            }
        }
    }

    /**
     * Create the location service, and async the map.
     */
    public void kickTheMap(){
        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(5000);
        EasyLocationRequest easyLocationRequest = new EasyLocationRequestBuilder()
                .setLocationRequest(locationRequest)
                .setFallBackToLastLocationTime(3000)
                .build();
        requestLocationUpdates(easyLocationRequest);
        // Async the map
        supportMapFragment.getMapAsync(this);
    }

    @Override
    public void onLocationPermissionGranted() {

    }

    @Override
    public void onLocationPermissionDenied() {
        checkLocationPermission();
    }

    @Override
    public void onLocationReceived(Location location) {
        mCurrentLocation = location;
        mCurrentLatitude = location.getLatitude();
        mCurrentLongitude = location.getLongitude();
    }

    @Override
    public void onLocationProviderEnabled() {

    }

    @Override
    public void onLocationProviderDisabled() {

    }
}
