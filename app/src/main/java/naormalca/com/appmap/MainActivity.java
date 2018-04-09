package naormalca.com.appmap;


import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import naormalca.com.appmap.ui.ShowReportFragment;

import static naormalca.com.appmap.Firebase.FirebaseDB.DB_REPORTS;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_CRIMINAL;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_ECONOMY;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_SECURITY;
import static naormalca.com.appmap.misc.Constant.MARKER_TYPE_SOCIAL;
import static naormalca.com.appmap.misc.Constant.REPORT_FRAGMENT_TAG;
import static naormalca.com.appmap.misc.Constant.REPORT_LAT;
import static naormalca.com.appmap.misc.Constant.REPORT_LNG;
import static naormalca.com.appmap.misc.Constant.TLV_LAT;
import static naormalca.com.appmap.misc.Constant.TLV_LNG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback
        ,GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnMarkerClickListener
{

    private static final String TAG = MainActivity.class.getSimpleName();
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Users currentUserData;

    private SupportMapFragment mSupportMapFragment;
    GoogleMap mMap;
    private GPSTracker gps;

    private double mLatitudeClick;
    private double mLongitudeClick;
    private Marker mTempMarkerTarget;
    private Marker mCurrentPositionMarker;

    private boolean isMapLoaded = false;
    private int mCurrentReportTypeShow;

    private boolean mIsReportTypeFilter;
    private ArrayList<Report> mReports = new ArrayList<>();

    // Report fragment
    private ShowReportFragment mShowReportFragment;
    private FragmentTransaction mFragmentTransaction;
    private NavigationView navigationView;

    private TextView mUserHelloMsg;
    /*
    *
    * Life-Cycle functions
    *
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        //async the map
        mSupportMapFragment.getMapAsync(this);
        gps = new GPSTracker(MainActivity.this);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isMapLoaded && gps.isCanGetLocation() && gps.getLocation() != null) {
                    if (radiusCheck(new LatLng(mLatitudeClick, mLongitudeClick))) {
                        //pass the point to ReportActivity
                        Intent intent = new Intent(MainActivity.this, ReportActivity.class);
                        intent.putExtra(REPORT_LAT, mLatitudeClick);
                        intent.putExtra(REPORT_LNG, mLongitudeClick);
                        startActivity(intent);
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(view, "אינך נמצא ברדיוס הדיווח!", Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                } else if (!isMapLoaded){
                    Snackbar snackbar = Snackbar
                            .make(view, "The map not render!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (!gps.isCanGetLocation()){
                    Snackbar snackbar = Snackbar
                            .make(view, "GPS IS NOT WORKING", Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else if (gps.getLocation() == null){
                    Snackbar snackbar = Snackbar
                            .make(view, "gps.location", Snackbar.LENGTH_LONG);
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
        // Hello navigation bar user message
        LinearLayout linearLayout = (LinearLayout) navigationView.getHeaderView(0);
        mUserHelloMsg = linearLayout.findViewById(R.id.helloMsgItem);



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
                updateUI(user);
            }
        };
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
    private void getUserInfoAndUpdate(final String userId){

        mDatabase = FirebaseDatabase.getInstance().getReference(FirebaseDB.USERS_DB);
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    currentUserData = postSnapshot.getValue(Users.class);
                    Log.d(TAG,currentUserData.getFirstName()+" "+currentUserData.getLastName()+"inOnDataChange"+currentUserData.getID());
                    if (currentUserData.getID().equals(userId)){
                        mUserHelloMsg = findViewById(R.id.helloMsgItem);
                        mUserHelloMsg.setText(currentUserData.getFirstName()+"שלום, ");
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

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
        } else if (id == R.id.signUpItem){
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        } else if (id == R.id.signInItem){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (id == R.id.signOutItem){
            signOut();
        }

        markersSetup();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateUI(FirebaseUser user) {
        if (user != null){
            Log.d(TAG, user.getEmail()+ user.getUid());
            getUserInfoAndUpdate(user.getUid());
        } else{
            guestInfoUpdate();
        }
    }
    private void guestInfoUpdate() {
        mUserHelloMsg.setText("שלום, אורח");
    }
    /*
    *
    * Map functions
    *
    *
    * */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //map ready
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;
        //focus the camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(TLV_LAT,TLV_LNG)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(TLV_LAT,TLV_LNG), 10.0f));
        showCurrentPositionOnMap(gps);
        //call to MapLoaded, MapClickListener, OnMarkerClickListener
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
        Log.d("MarkersSetup","isReportTYPEFilter : "+ mIsReportTypeFilter);
        for (Report report: mReports) {
            if (!mIsReportTypeFilter) {
                Log.d("MarkersSetup","each report : "+report.getTitle());
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(report.getLatitude(), report.getLongitude()))
                        .title(report.getTitle())
                        .snippet(report.getTime())
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(Report.iconColors[report.getType()])))
                        .setTag(report);
            } else if(mCurrentReportTypeShow == report.getType()){
                mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(report.getLatitude(), report.getLongitude()))
                        .title(report.getTitle())
                        .snippet(report.getTime())
                        .icon(BitmapDescriptorFactory
                                .defaultMarker(Report.iconColors[report.getType()])))
                .setTag(report);

            }
        }
    }

    public void showCurrentPositionOnMap(GPSTracker gps){
        double currentLongitude, currentLatitude;
        if(gps.isCanGetLocation()) {
            Log.d("gpstracker","showCurrentPosition");
            currentLatitude = gps.getLatitude();
            currentLongitude = gps.getLongitude();

            mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude))
                    .title(currentLatitude+"/"+currentLongitude)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)))
                    .setTag(new Report(false));
        }
    }
    private boolean radiusCheck(LatLng newReport) {
        Location target = new Location("target");
        target.setLatitude(newReport.latitude);
        target.setLongitude(newReport.longitude);
        return gps.getLocation().distanceTo(target) < 2000;
    }

    @Override
    public void onMapLoaded() {
        isMapLoaded = true;
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
                mFragmentTransaction = fragmentManager.beginTransaction();
                mFragmentTransaction.replace(R.id.fragment_container, mShowReportFragment, REPORT_FRAGMENT_TAG);
                //mFragmentTransaction.addToBackStack(null);

                mFragmentTransaction.commit();
            }
        }
        return true;
    }
}
