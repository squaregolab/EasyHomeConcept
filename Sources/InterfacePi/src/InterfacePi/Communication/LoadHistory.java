package InterfacePi.Communication;

import InterfacePi.Communication.Type.HistoryData;
import InterfacePi.Interface.ExceptionAlert;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.GZIPInputStream;

/**
 * Created by Seb on 17/01/2017.
 */
public class LoadHistory {
    Logger logger = LogManager.getLogger();
    private ArrayList<HistoryData> history = new ArrayList<HistoryData>();
    Response response;

    /**
     * Return history for a Blynk Pin via Resfull request.
     * @param pin
     * @return List<HistoryData>
     */
    public ArrayList<HistoryData> getHistory(String pin) {
        // Create client and send the request
       Client client = ClientBuilder.newClient();
       response = client.target("http://blynk-cloud.com/d6e944e53858428cbb39fe4d1fb6b0dc/data/"+pin)
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get();
        logger.debug("status: " + response.getStatus());
        logger.debug("headers: " + response.getHeaders());
        /*
        Problem: We receive a Gzip File!
        Solution: We create a temp file were we unZip it!
         */
        if(response.getStatus()==200)
        {

            logger.debug("test");
            File path = new File("/home/pi/Interface/temp");
            if(!path.exists())
            {
                path.mkdir();
            }

            try
            {

                GZIPInputStream gzip = new GZIPInputStream((InputStream) response.getEntity());
                FileOutputStream tempOutputStream = new FileOutputStream("/home/pi/Interface/temp/blynkRestfull.txt");
                byte[] buffer = new byte[1024];
                int len;
                while((len = gzip.read(buffer)) != -1){
                    tempOutputStream.write(buffer, 0, len);
                }
                //close resources
                gzip.close();
                tempOutputStream.close();
                logger.debug("File created! Try to read!");

                File tempFile = new File("/home/pi/Interface/temp/blynkRestfull.txt");
                BufferedReader reader = new BufferedReader(new FileReader(tempFile));
                String line;
                while((line = reader.readLine())!=null)
                {
                    String[] splited = line.split(",");
                    Calendar tempCalandar = Calendar.getInstance();
                    tempCalandar.setTimeInMillis(Long.parseLong(splited[1]));
                    HistoryData tempData = new HistoryData(tempCalandar, Float.parseFloat(splited[0]));
                    history.add(tempData);

                }
                logger.debug("Read Finish!");

                reader.close();
                tempFile.delete();

            } catch (FileNotFoundException e) {
                logger.catching(e);
                Platform.runLater(()->
                {
                    Alert alert = new ExceptionAlert().get(e);
                    alert.setHeaderText("Erreur!");
                    alert.setContentText("Erreur lors du chargement de l'historique");
                    alert.show();
                });



            } catch (IOException e) {
                logger.catching(e);
            }
        }
        else
        {

            return null;

        }



        return history;
    }
}
