package InterfacePi.Communication;

import com.pi4j.platform.PlatformAlreadyAssignedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import InterfacePi.Communication.Type.Blynk;
import InterfacePi.Main;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static InterfacePi.Main.communicationScript;

/**
 *
 *
 *
 */

public class Communication extends Thread {
    public ServerSocket socketServeur;
    public Socket socketClient;
    public Logger logger = LogManager.getLogger();
    public Auth auth = new Auth();
    public boolean endInit=false;

    /**Gestion de la communication general
     *
     * @throws PlatformAlreadyAssignedException
     */
    public void comm() throws PlatformAlreadyAssignedException {

        /****************************
         *     Connection init      *
         ****************************/

        logger.info("Start Serveur...");
        Traitement traitement = new Traitement();
        try {
            socketServeur = new ServerSocket(75);
        } catch (IOException e1) {
            e1.printStackTrace();
        }



        //try to connect to Blynk script

        /************************************************
         *    Tentative de connection au script blynk   *
         ************************************************/
        try {
            Main.socketScript = new Socket(InetAddress.getLocalHost(),5000);
            logger.info("Connection to Script Blynk success.");
            communicationScript.receive.start();
        } catch (IOException e) {
            logger.warn("Connection to Script Blynk fails.");
            logger.catching(e);
        }
        while(true) {
            try {
                /***************************************
                 *           Demmarage du serveur      *
                 ***************************************/
                logger.info("Serveur start at " + socketServeur.getLocalPort() + ".");
                endInit=true;
                socketClient = socketServeur.accept();
                socketClient.setSoTimeout(2000);
                logger.info("Connection to " + socketClient.getInetAddress() + " Ok.");
                auth.newAth(socketClient);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }catch (SocketException e) {
                logger.catching(e);
            } catch (IOException e) {
                logger.catching(e);
            }

        }

    }

    @Override
    public void run() {
        try {
            this.comm();
        } catch (PlatformAlreadyAssignedException e) {
            logger.catching(e);
        }
    }
}
