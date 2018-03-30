package naormalca.com.appmap.model;

/**
 * Created by Naor on 29/03/2018.
 */

public class Users {
    private String mFirstName;
    private String mLastName;
    private String mEmail;
    private String mID;
    public Users(){

    }
    public Users(String firstName, String lastName, String email, String id) {
        mFirstName = firstName;
        mLastName = lastName;
        mEmail = email;
        mID = id;

    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getID() {
        return mID;
    }

    public void setID(String ID) {
        mID = ID;
    }
}
