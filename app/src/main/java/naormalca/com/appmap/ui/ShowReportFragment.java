package naormalca.com.appmap.ui;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import naormalca.com.appmap.MainActivity;
import naormalca.com.appmap.R;
import naormalca.com.appmap.model.Report;

public class ShowReportFragment extends android.support.v4.app.Fragment implements View.OnClickListener {
    TextView mTitleTextView;
    TextView mDescriptionTextView;
    TextView mTimeTextView;
    TextView mLikesTextView;
    private Report mReport;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.show_report_fragment,container, false);
      view.setOnClickListener(this);
      mTimeTextView = view.findViewById(R.id.timeTextView);
      mTitleTextView = view.findViewById(R.id.titleTextView);
      mDescriptionTextView = view.findViewById(R.id.descriptionTextView);
      mLikesTextView = view.findViewById(R.id.likesTextView);


      mDescriptionTextView.setText(mReport.getDescription());
      mTitleTextView.setText(mReport.getTitle());
      mTimeTextView.setText(mReport.getTime());
      mLikesTextView.setText(mReport.getLikes()+"");
      return view;
    }

    public void setReport(Report report) {
        mReport = report;
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(getActivity(),"WHOOOO",Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getActivity(), SingleReportActivity.class);
        intent.putExtra("my_report",mReport);
        startActivity(intent);
    }
}
