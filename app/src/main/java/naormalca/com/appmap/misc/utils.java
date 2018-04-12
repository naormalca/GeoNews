package naormalca.com.appmap.misc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by Naor on 29/03/2018.
 */

public class utils {
    public static String[] parseFullName(String fullName){
        fullName = fullName.trim();
        return fullName.split(" ");
    }

}
