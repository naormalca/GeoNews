package naormalca.com.appmap.ui;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import naormalca.com.appmap.R;
import naormalca.com.appmap.model.Report;

/**
 * Created by Naor on 11/03/2018.
 */

public class ShowReportFragment extends android.support.v4.app.Fragment {
    TextView mTitleTextView;
    TextView mDescriptionTextView;
    TextView mTimeTextView;
    private Report mReport;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.show_report_fragment,container, false);

      mTimeTextView = view.findViewById(R.id.timeTextView);
      mTitleTextView = view.findViewById(R.id.titleTextView);
      mDescriptionTextView = view.findViewById(R.id.descriptionTextView);


      mDescriptionTextView.setText(mReport.getDescription());
      mTitleTextView.setText(mReport.getTitle());
      mTimeTextView.setText(mReport.getTime());

      return view;
    }

    public void setReport(Report report) {
        mReport = report;
    }
}
