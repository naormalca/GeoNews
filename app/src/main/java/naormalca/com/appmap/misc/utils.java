package naormalca.com.appmap.misc;



public class utils {
    public static String[] parseFullName(String fullName){
        fullName = fullName.trim();
        return fullName.split(" ");
    }

    /**
     * parse string time to integer for sorting
     * @param time report time
     * @return
     */
    public static int parseTimeToInt(String time){
        time = time.replaceAll("\\:","");
        return Integer.parseInt(time);
    }

}
