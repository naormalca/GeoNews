package naormalca.com.appmap.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import naormalca.com.appmap.R;
import naormalca.com.appmap.model.Report;

import static naormalca.com.appmap.Firebase.FirebaseDB.DB_REPORTS;
import static naormalca.com.appmap.misc.Constant.REPORT_LAT;
import static naormalca.com.appmap.misc.Constant.REPORT_LNG;

public class ReportActivity extends AppCompatActivity
implements AdapterView.OnItemSelectedListener{
    private static final int SELECT_PICTURE = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private static final int MEDIA_TYPE_IMAGE = 4 ;
    private static final String TAG = "ImageTake";

    private double mLatitudeReport;
    private double mLongitudeReport;
    private int mSpinnerChoice;
    private DatabaseReference mDatabaseReports;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private Uri mSelectedImage;
    private String mCurrentPhotoPath;
    private Uri mMediaUri;

    @BindView(R.id.titleEditText) EditText titleEt;
    @BindView(R.id.descriptionEditText) EditText descEditText;
    @BindView(R.id.spinner) Spinner mSpinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);

        // Get the reference of the reports and images from DB
        mDatabaseReports = FirebaseDatabase.getInstance().getReference(DB_REPORTS);
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
        // Retrieve lat lang from MainActivity

        Intent intent = getIntent();
        mLatitudeReport = intent.getDoubleExtra(REPORT_LAT,0);
        mLongitudeReport = intent.getDoubleExtra(REPORT_LNG,0);


        // Spinner

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.planets_array, android.R.layout.simple_spinner_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(this);


    }

    public String getTime(){
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        return hour+":"+minute+":"+second;
    }



    @OnClick(R.id.sendReportButton)
    public void sendReport(View view){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final String userID = user != null ? user.getUid() : null;
        if (mSelectedImage != null){
            StorageReference imageRef = mStorageReference.child("imagesReport/"+mSelectedImage.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(mSelectedImage);
            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {

                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                    Report report = new Report(titleEt.getText().toString(),
                            descEditText.getText().toString(), mLatitudeReport, mLongitudeReport,
                            0, getTime(), mSpinnerChoice,
                            userID, downloadUrl);
                    mDatabaseReports.push().setValue(report);
                    finish();
                }
            });
        } else {
            Report report = new Report(titleEt.getText().toString(),
                    descEditText.getText().toString(), mLatitudeReport, mLongitudeReport,
                    0, getTime(), mSpinnerChoice,
                    userID,null);
            mDatabaseReports.push().setValue(report);
            finish();
        }
    }

    @OnClick(R.id.addPhotoBtn)
    public void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

    }
    @OnClick(R.id.takePhotoBtn)
    public void takeImage(){
       /* Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch (IOException ex){
                // Error occurred while creating the File
            }
            if (photoFile != null) {
               Uri photoURI = FileProvider.getUriForFile(this,
                        "naormalca.com.appmap.android.fileprovider",
                        photoFile);
                //Uri photoURI = Uri.fromFile(photoFile);
                Log.d(TAG, photoURI.getPath());
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        } */
       mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
       if (mMediaUri == null){
           Toast.makeText(this,
                   "There was a problem accessing your device's external storage.",
                   Toast.LENGTH_LONG).show();
       } else{
           /*TODO:This 2 lines are not the best practice!!! it`s just ignore security warrings,
            The right way is with Content Provider has mention here:https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed

           */
           StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
           StrictMode.setVmPolicy(builder.build());

           Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
           takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mMediaUri);
           startActivityForResult(takePhotoIntent, REQUEST_IMAGE_CAPTURE);
       }

    }

    private Uri getOutputMediaFileUri(int mediaType) {
        // Check for external storage
        if (isExternalStorageAvailable()){
            // Get the URI
            // Get the external storage directory
            File mediaStorageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            // Create a unique file name
            String fileName = "";
            String fileType = "";
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

            if (mediaType == MEDIA_TYPE_IMAGE){
                fileName = "IMG_"+ timeStamp;
                fileType = ".jpg";
            } else {
                //TODO: Check about null uri
                return null;
            }

            // Create the file
            File mediaFile;
            try{
                mediaFile = File.createTempFile(fileName, fileType
                ,mediaStorageDir);
                Log.i(TAG, "File: "+Uri.fromFile(mediaFile));

                // Return the file`s URI
                return Uri.fromFile(mediaFile);
            } catch (IOException e){
                Log.e(TAG, "Error creating file: "+
                        mediaStorageDir.getAbsolutePath() + fileName + fileType);
            }
        }

        // Something went wrong
        return null;
    }

    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                // Image selected from gallery
                // Load to imageView
                mSelectedImage = data.getData();
                Picasso.get().load(mSelectedImage).noPlaceholder().centerCrop().fit()
                        .into((ImageView) findViewById(R.id.imageView));
            } else if (requestCode == REQUEST_IMAGE_CAPTURE){
                // Image taking from camera
                mSelectedImage = mMediaUri;
                //Load to imageView
                Picasso.get().load(mSelectedImage).noPlaceholder().centerCrop().fit()
                        .into((ImageView) findViewById(R.id.imageView));
            }

        }
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mSpinnerChoice = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //There default choice
    }
}
