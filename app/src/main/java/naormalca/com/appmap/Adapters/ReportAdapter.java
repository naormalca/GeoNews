package naormalca.com.appmap.Adapters;

import android.content.Context;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Comparator;


import naormalca.com.appmap.R;
import naormalca.com.appmap.misc.utils;
import naormalca.com.appmap.model.Report;

//TODO:DOCS!!!!!!!!!!

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private Context mContext;
    private ArrayList<Report> mReports;

    public ReportAdapter(ArrayList<Report> reports, Context context) {
        // Sorting the arrayList in descending order
        Collections.sort(reports, new Comparator<Report>() {
                    @Override
                    public int compare(Report t1, Report t2) {
                        if (t1 == null || t2 == null)
                            return 0;
                        return utils.parseTimeToInt(t2.getTime()) - utils.parseTimeToInt(t1.getTime());
                    }
                });
        mReports = reports;
        mContext = context;
    }


    @Override
    public ReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.report_list_view_row, parent, false);
        return new ReportViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ReportViewHolder holder, int position) {
        holder.title.setText(mReports.get(position).getTitle());
        holder.description.setText(mReports.get(position).getDescription());
        holder.name.setText(mReports.get(position).getUserID());
        holder.time.setText(mReports.get(position).getTime());
        String url = mReports.get(position).getUrlImage();
        Picasso.get().load(url).into(holder.imageView);
    }


    @Override
    public int getItemCount() {
        return mReports.size();
    }
    public class ReportViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{
        public TextView title;
        public TextView description;
        public TextView name;
        public TextView time;
        public ImageView imageView;

        public ReportViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleTextViewList);
            description = itemView.findViewById(R.id.descriptionTextViewList);
            name = itemView.findViewById(R.id.nameTextViewList);
            time = itemView.findViewById(R.id.timeTextViewList);
            imageView = itemView.findViewById(R.id.imageViewList);
        }
        //TODO: OPEN Activity for each report
        @Override
        public void onClick(View view) {

        }
    }
}
