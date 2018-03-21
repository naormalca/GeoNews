package naormalca.com.appmap.model;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by Naor on 21/02/2018.
 */
public class Report {
    private String mTitle;
    private String mDescription;
    private double mLatitude;
    private double mLongitude;
    private int mId;
    private String mTime;
    private int mType;
    //
    private boolean show = true ;
    /**type by int
     * 1- Military or Security - RED
     * 2- Economy - CYAN
     * 3- Social - ROSE
     * 4- Crime - BLUE
     */
    public static final float[] iconColors = {
            0.0F,180.0F,330.0F,210.0F
    };
    public Report(){
        //Default
    }
    public Report(boolean show){
        this.show = show;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public Report(String title, String description, double latitude, double longitude, int id, String time, int type) {
        mTitle = title;
        mDescription = description;
        mLatitude = latitude;
        mLongitude = longitude;
        mId = id;
        mTime = time;
        mType = type;
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


}
