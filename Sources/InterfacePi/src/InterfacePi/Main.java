package InterfacePi;


import InterfacePi.Communication.ClientS;
import InterfacePi.Communication.CommunicationScript;
import InterfacePi.Communication.ModuleLauncher;
import com.pi4j.io.gpio.*;
import com.pi4j.system.SystemInfo;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class Main extends Application {

    /******************
     *  StandBy Screen=
     ******************/
    public double timeForSleep=10;
    public int compteur;
    public boolean screenClicked=false;

    /***********************
     *  Client management
     ***********************/
    public static HashMap<Integer, ClientS> clientHashMap = new HashMap<>();   /*<!Table qui contient les diferent client configurer*/
    public static Socket socketScript;   /*<!Socket de connection au script Blynk*/
    public static CommunicationScript communicationScript =new CommunicationScript();   /*<!Class de getioon de la communication avec le script*/

    /***********************
     *  Screen management
     ***********************/
    public static int currentRoom =-1;
    public static int generalScreen;



    public static ModuleLauncher launcher = new ModuleLauncher();
    public static GpioPinDigitalOutput ventiloPin;

    /***********************
     *  Private Variable
     ***********************/
    private static Logger logger = LogManager.getLogger();
    private Stage primaryStageSave;



    @Override
    public void init() throws Exception {

    }

    @Override
    public void start(Stage primaryStage) throws Exception{



        Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Home/home.fxml"));

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 1024, 600));
        primaryStage.show();
        primaryStage.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                screenClicked=true;
            }
        });
        primaryStageSave=primaryStage;
        new StandByScreen().start();



        launcher.lauchAll();


    }


    public static void main(String[] args) {

        GpioController gpio = GpioFactory.getInstance();
        ventiloPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, PinState.HIGH);
        ventiloPin.high();
        new GestionTemp().start();
        logger.info("**********************************************************************");
        logger.info("**********************************************************************");
        logger.info("**                           Welcome !                              **");
        logger.info("**                     This is a new start !                        **");
        logger.info("**********************************************************************");
        logger.info("**********************************************************************");
        logger.info("");
        logger.info("Yes, yes i now this is an awesome app made by un awesome guy! ;)");
        logger.info("");
        logger.info("Special thanks to SquareGoLab and IMERIR and Clement Rey for all their help!");
        logger.info("");
        logger.info("Powered by:");
        logger.info("");
        logger.info("");
        File file = new File("/home/pi/Interface/rasp");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine())!=null) {
                logger.info(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            logger.catching(e);
        } catch (IOException e) {
            logger.catching(e);
        }
        logger.info("");
        logger.info("");
        logger.info("And:");
        logger.info("");
        logger.info("");
        file = new File("/home/pi/Interface/java");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine())!=null) {
                logger.info(line);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            logger.catching(e);
        } catch (IOException e) {
            logger.catching(e);
        }

        logger.info("");
        logger.info("");


        launch(args);
    }


        public static class LauchScript extends Thread
    {
        Logger loggerScript = LogManager.getLogger(this);

        @Override
        public void run() {
            /*while(true)
            {*/
                logger.info("Starting Blynk Script...");
                try {
                    loggerScript.trace("Kill script if already start...");
                    ProcessBuilder killer = new ProcessBuilder("sudo", "killall","node");
                    Process processKiller = killer.start();
                    processKiller.destroy();
                } catch (IOException e) {
                    logger.catching(e);
                }
                try{
                    ProcessBuilder pb = new ProcessBuilder("node", "/home/pi/Interface/blynkScript.js");
                    Process p = pb.start();
                    InputStream is = p.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        loggerScript.trace(line);

                    }
                    p.destroy();


                } catch (IOException e) {
                    logger.catching(e);
                }
        }
    }

    /**
     * Class for manage the case Fan (Thread)
     */
    public static class GestionTemp extends Thread
    {
        float temp=0;
        @Override
        public void run() {
            while(true)
            {

                try {
                    Thread.sleep(2000);
                    temp=SystemInfo.getCpuTemperature();
                    if(ventiloPin.isHigh())
                    {
                        if(temp>=70)
                        {
                            ventiloPin.low();
                            logger.debug("Starting fan!");
                        }

                    }
                    else
                    {
                        if(temp<50)
                        {
                            ventiloPin.high();
                            logger.debug("Stop fan!");
                        }

                    }

                } catch (InterruptedException e) {
                    logger.catching(e);
                } catch (IOException e) {
                    logger.catching(e);
                }
            }
        }
    }


    /**
     * Class for manage the StandByScreen (Thread)
     */
    public class StandByScreen extends Thread
    {
        Logger loggerSleep = LogManager.getLogger();
        @Override
        public void run() {
            while(true)
            {
                if(!screenClicked)
                {
                    if(compteur>=300*timeForSleep)
                    {
                        primaryStageSave.getScene().setFill(Color.BLACK);
                        while( primaryStageSave.getScene().getRoot().getOpacity()>0.00)
                        {
                            primaryStageSave.getScene().getRoot().setOpacity(primaryStageSave.getScene().getRoot().getOpacity()-0.01);
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                loggerSleep.catching(e);
                            }
                        }
                        primaryStageSave.getScene().getRoot().setDisable(true);
                    }
                    else
                    {
                        compteur++;
                    }
                }
                else{
                    compteur=0;
                    primaryStageSave.getScene().setFill(Color.BLACK);
                    while( primaryStageSave.getScene().getRoot().getOpacity()<=1)
                    {
                        primaryStageSave.getScene().getRoot().setDisable(false);
                        primaryStageSave.getScene().getRoot().setOpacity(primaryStageSave.getScene().getRoot().getOpacity()+0.01);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            loggerSleep.catching(e);
                        }
                    }
                    screenClicked=false;
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    loggerSleep.catching(e);
                }
            }
        }
    }



}
