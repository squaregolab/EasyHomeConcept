package InterfacePi.Communication.Type;

import InterfacePi.Communication.ClientS;
import InterfacePi.Communication.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seb65 on 09/01/2017.
 */
public class StandardClient {
    static Logger logger = LogManager.getLogger();

    public ClientS get(String name,int ID,int type,boolean isGeneral)
    {
        logger.debug("Create standard client: ID: "+ID+" Name: "+name);
        List<Data> temp = new ArrayList<>();
        temp.add(new Data("Temperature","°C",0,40,false,Blynk.GAUGE));
        temp.add(new Data("Temperature cible","°C",10,40,true,Blynk.VALUEDISPLAYANDSLIDER));
        temp.add(new Data("Humidite","%",0,100,false,Blynk.GAUGE));
        temp.add(new Data("Lumiere","",0,1,true,Blynk.BUTTONONLY));
        temp.add(new Data("Volet","",0,4,true,Blynk.BUTTONONLY));
        temp.add(new Data("Consomation","l/h",0,100,false,Blynk.VALUEDISPLAY));
        temp.add(new Data("Chauffage","",0,1,true,Blynk.BUTTONONLY));
        ClientS tempC =new ClientS(name,ID,temp,type,isGeneral);
        return tempC;
    }
}
