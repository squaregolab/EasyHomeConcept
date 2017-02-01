package InterfacePi.Communication.Type;

import java.util.Calendar;

/**
 * Created by Seb on 17/01/2017.
 */
public class HistoryData {
    private Calendar date;
    private float data;


    public HistoryData(Calendar date, float data) {
        this.date = date;
        this.data = data;
    }

    public Calendar getDate() {
        return date;
    }

    public float getData() {
        return data;
    }
}
