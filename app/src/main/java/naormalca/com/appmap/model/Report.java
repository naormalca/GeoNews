package naormalca.com.appmap.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import naormalca.com.appmap.misc.utils;

/**
 * Created by Naor on 21/02/2018.
 */
public class Report implements Parcelable{
    private String mTitle;
    private String mDescription;
    private double mLatitude;
    private double mLongitude;
    private int mId;
    private String mTime;
    private int mType;
    private String mUserID;
    private String mUserFullName;
    private String mUrlImage;
    //
    private boolean show = true ;
    /**type by int
     * 1- Military or Security - RED
     * 2- Economy - CYAN
     * 3- Social - ROSE
     * 4- Crime - BLUE
     * 5- Accident
     * 6- explosive
     */
    public static final String[] iconColors = {
            "military","economy","social","policemen","accident","explosive"
    };
    public Report(){
        //Default
    }
    public boolean isShow() {
        return show;
    }
    /*
    Every marker has a report, marker with out report no needed to show.
     */
    public Report(boolean show){
        this.show = show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public Report(String title, String description, double latitude,
                  double longitude, int id, String time, int type,
                  String userID, String urlImage) {
        mTitle = title;
        mDescription = description;
        mLatitude = latitude;
        mLongitude = longitude;
        mId = id;
        mTime = time;
        mType = type;
      //  mUserFullName = userFullName;
        mUserID = userID;
        mUrlImage = urlImage;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getUserID() {
        return mUserID;
    }

    public void setUserID(String userID) {
        mUserID = userID;
    }

    public String getUrlImage() {
        return mUrlImage;
    }

    public void setUrlImage(String urlImage) {
        mUrlImage = urlImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeString(mDescription);
        parcel.writeDouble(mLatitude);
        parcel.writeDouble(mLongitude);
        parcel.writeInt(mId);
        parcel.writeString(mTime);
        parcel.writeInt(mType);
        parcel.writeString(mUserID);
        parcel.writeString(mUrlImage);

    }
    private Report(Parcel in){
        mTitle = in.readString();
        mDescription = in.readString();
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
        mId = in.readInt();
        mTime = in.readString();
        mType = in.readInt();
        mUserID = in.readString();
        mUrlImage = in.readString();
    }
    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel parcel) {
            return new Report(parcel);
        }

        @Override
        public Report[] newArray(int i) {
            return new Report[i];
        }
    };


    /* public String getUserFullName() {
        return mUserFullName;
    }

   public void setUserFullName(String userFullName) {
        mUserFullName = userFullName;
    }*/
}
