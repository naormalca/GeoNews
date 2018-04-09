package naormalca.com.appmap.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.Calendar;
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

    private double mLatitudeReport;
    private double mLongitudeReport;
    private int mSpinnerChoice;

    private DatabaseReference mDatabaseReports;


    @BindView(R.id.titleEditText) EditText titleEt;
    @BindView(R.id.descriptionEditText) EditText descEditText;
    @BindView(R.id.spinner) Spinner mSpinner;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        ButterKnife.bind(this);
        //get the reference of the reports from database
        mDatabaseReports = FirebaseDatabase.getInstance().getReference(DB_REPORTS);
        /**
         * Retrieve lat lang from MainActivity
         */
        Intent intent = getIntent();
        mLatitudeReport = intent.getDoubleExtra(REPORT_LAT,0);
        mLongitudeReport = intent.getDoubleExtra(REPORT_LNG,0);

        /**
         * Spinner
         */
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
        Report report = new Report(titleEt.getText().toString(),
                descEditText.getText().toString(),
                mLatitudeReport,
                mLongitudeReport,
                0,
                getTime(),
                mSpinnerChoice
                );
        mDatabaseReports.push().setValue(report);
        finish();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        mSpinnerChoice = i;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
