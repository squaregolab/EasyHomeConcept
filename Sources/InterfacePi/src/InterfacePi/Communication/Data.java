package InterfacePi.Communication;

/**
 * Created by seb65 on 18/12/2016.
 */
public class Data {
    private String name;
    private String data;
    private String unit;
    private int min;
    private int max;
    private int blynkType;
    private boolean isModifiable;

    public Data(String name, String unit, int min, int max,boolean isModifiable, int blynkType) {
        this.name = name;
        this.unit = unit;
        this.min = min;
        this.max = max;
        this.blynkType =blynkType;
        this.isModifiable=isModifiable;
    }

    public String getName()
    {
       return this.name;
    }

    public String getData()
    {
        return this.data;
    }
    public String getUnit()
    {
        return this.unit;
    }
    public int getMin()
    {
        return this.min;
    }
    public int getMax()
    {
        return this.max;
    }
    public boolean isModifiable()
    {
        return this.isModifiable;
    }
    public int getBlynkType()
    {
        return this.blynkType;
    }



    public void setName(String name)
    {
        this.name=name;
    }
    public void setData(String data)
    {
        this.data=data;
    }
    public void setUnit(String unit)
    {
        this.unit=unit;
    }
    public void setMin(int min)
    {
        this.min=min;
    }
    public void setMax(int max)
    {
        this.max=max;
    }
    public void setModifiable(boolean isModifiable)
    {
        this.isModifiable=isModifiable;
    }
    public void setBlynkType(int blynkType)
    {
        this.blynkType=blynkType;
    }


}
