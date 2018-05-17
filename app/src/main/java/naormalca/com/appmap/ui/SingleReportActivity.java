package naormalca.com.appmap.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import naormalca.com.appmap.R;
import naormalca.com.appmap.model.Report;

public class SingleReportActivity extends AppCompatActivity implements View.OnClickListener{
    private Report mReport;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private TextView mTimeTextView;
    private TextView mLikesTextView;
    private TextView mUserNameTextView;
    private ImageView mImageView;
    private Button mLikeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_report);
        mReport = getIntent().getParcelableExtra("my_report");

        // Views
        mTitleTextView = findViewById(R.id.titleTextView);
        mDescriptionTextView = findViewById(R.id.descriptionTextView);
        mTimeTextView = findViewById(R.id.timeTextView);
        mLikesTextView = findViewById(R.id.likesTextView);
        mUserNameTextView = findViewById(R.id.userNameTextView);
        mImageView = findViewById(R.id.imageView);
        mLikeButton = findViewById(R.id.likeButton);
        // Check if user Auth
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // User is signed in, like button visible
            mLikeButton.setVisibility(View.VISIBLE);
        } else {
            // No user is signed in
            mLikeButton.setVisibility(View.INVISIBLE);
        }


        mLikeButton.setOnClickListener(this);

        mTitleTextView.setText(mReport.getTitle());
        mDescriptionTextView.setText(mReport.getDescription());
        mTimeTextView.setText(mReport.getTime());
        mLikesTextView.setText(mReport.getLikes()+"");
        mUserNameTextView.setText(mReport.getUserFullName());

        Picasso.get().load(mReport.getUrlImage()).into(mImageView);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.likeButton){
            Toast.makeText(this, "like!!!!", Toast.LENGTH_SHORT).show();
            int likeUpdate =mReport.getLikes()+1;
            // Increase the value of likes
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.child("Reports")
                    .child(mReport.getId())
                    .child("likes")
                    .setValue(likeUpdate);
            // Refresh the textView of like
            mLikesTextView.setText(likeUpdate+"");
            // Invisible the button
            mLikeButton.setVisibility(View.INVISIBLE);
        }
    }
}
