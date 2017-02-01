package InterfacePi.Communication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * Created by seb65 on 08/12/2016.
 */
public class Traitement {

    private Logger logger = LogManager.getLogger();
    String[] getInfo(String aTraiter)
    {
        logger.debug("Received frame: "+aTraiter);
        if (aTraiter.length()>2)
        {
            if(Objects.equals(aTraiter.charAt(0), '<') && Objects.equals(aTraiter.charAt(aTraiter.length() - 1),'>'))
            {

                aTraiter = aTraiter.substring(1,aTraiter.length()-1);
                String[] part = aTraiter.split(",");
                return part;


            }
            else
                return null;
        }
        else
            return null;
    }
}


