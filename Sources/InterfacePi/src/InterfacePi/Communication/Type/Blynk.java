package InterfacePi.Communication.Type;

/**
 * Created by seb65 on 19/12/2016.
 */
public class Blynk {
    public static final int GAUGE = 1;
    public static final int VALUEDISPLAY = 2;
    public static final int VALUEDISPLAYANDSLIDER = 7;
    public static final int SLIDER = 3;
    public static final int LEDONLY = 4;
    public static final int LEDANDBUTTON = 5;
    public static final int BUTTONONLY = 6;

    public String getNameOf(int typeInt)
    {
        switch (typeInt)
        {
            case 1:
                return "GAUGE";
            case 2:
                return "VALUEDISPLAY";
            case 3:
                return "SLIDER";
            case 4:
                return "LEDONLY";
            case 5:
                return "LEDANDBUTTON";
            case 6:
                return "BUTTONONLY";
            case 7:
                return "VALUEDISPLAYANDSLIDER";
            default:
                return null;
        }
    }

}
