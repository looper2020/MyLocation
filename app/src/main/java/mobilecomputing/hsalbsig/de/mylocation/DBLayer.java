package mobilecomputing.hsalbsig.de.mylocation;

/**
 * Created by Michael on 18.01.2016.
 */
public class DBLayer {
    private static DBLayer ourInstance = new DBLayer();

    public static DBLayer getInstance() {
        return ourInstance;
    }

    private DBLayer() {
        
    }


}
