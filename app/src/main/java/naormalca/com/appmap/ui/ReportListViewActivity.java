package naormalca.com.appmap.ui;

import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import naormalca.com.appmap.Adapters.ReportAdapter;
import naormalca.com.appmap.MainActivity;
import naormalca.com.appmap.R;
import naormalca.com.appmap.model.Report;

public class ReportListViewActivity extends AppCompatActivity {
    ArrayList<Report> mReports;
    RecyclerView mRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list_view);
        mRecyclerView = findViewById(R.id.recyclerView);
        mReports = getIntent().getParcelableArrayListExtra("test");
        for (int i = 0; i < mReports.size() ; i++) {
            Log.d("oryan'",mReports.get(i).getTitle());
        }
        ReportAdapter adapter = new ReportAdapter(mReports, this);
        mRecyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
    }
}
