package InterfacePi.Communication;

import InterfacePi.Communication.Type.Blynk;
import InterfacePi.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.List;

/**
 * Created by seb65 on 16/12/2016.
 */
public class CommunicationScript {
    static Logger logger = LogManager.getLogger();
    public Receive receive = new Receive();



    public void send(int ID, List<Data> data)
    {
        if(!checkPID())
            Main.socketScript=null;
        if(Main.socketScript==null)
        {
            logger.warn("Script Blynk non connecté.");
            this.reConnect();

        }
        if(Main.socketScript!=null)
        {
            logger.debug("Blynk script connected.");
            String toSend = "";
            for(Data adata : data)
            {
                if(adata.getBlynkType()== Blynk.LEDANDBUTTON || adata.getBlynkType()== Blynk.LEDONLY)
                {
                    toSend=toSend+","+Integer.toString(Integer.parseInt(adata.getData())*1023);
                }
                else
                    toSend=toSend+","+adata.getData();
            }
            try {
                PrintStream outSocket = new PrintStream( Main.socketScript.getOutputStream());
                //BufferedReader in = new BufferedReader(new InputStreamReader(Main.socketScript.getInputStream()));
                outSocket.print(Integer.toString(ID)+toSend);
//                String receve = in.readLine();
//                if(receve==null)
//                {
//                    Main.socketScript=null;
//                }

                Thread.sleep(100);

            }catch (SocketTimeoutException ste) {
                logger.warn("Time out!");
                Main.socketScript=null;
            } catch (IOException e) {
                logger.catching(e);
            } catch (InterruptedException e) {
                logger.catching(e);
            }

        }
    }


    public void reConnect()
    {
        logger.info("Trying to reconnect to BlynkScript...");
        try {
            Main.socketScript = new Socket(InetAddress.getLocalHost(),5000);
            receive = new Receive();
            receive.start();
            logger.info("Reconnection to Blynk Script success!");
        } catch (IOException e) {
            logger.error("Reconnection to Blynk Script failed! Trying to reboot script...");
            Main.socketScript = null;
            Main.LauchScript lauchScript = new Main.LauchScript();
            lauchScript.start();

            try {
                Thread.sleep(2000);
                Main.socketScript = new Socket(InetAddress.getLocalHost(),5000);
                receive = new Receive();
                receive.start();
                logger.info("Script reboot success!");
            } catch (IOException e1) {
                logger.error("Script reboot failed!");
            } catch (InterruptedException e1) {
                logger.catching(e1);
            }


        }
    }


    public class Receive extends Thread
    {
        @Override
        public void run() {


            try {
                Main.socketScript.setSoTimeout(300);
            } catch (SocketException e) {
            }


            while(Main.socketScript!=null)
            {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(Main.socketScript.getInputStream()));
                    String read = in.readLine();
                    logger.debug(read);
                    if(read!=null)
                    {
                        logger.debug("Receive Commande from Script: "+read);
                        String[] splited = read.split(":");
                        int id = Character.getNumericValue(splited[0].charAt(0));
                        int pin = Character.getNumericValue(splited[0].charAt(1));
                        int value = Integer.parseInt(splited[1].replace(".0",""));
                        int commande =  Main.clientHashMap.get(id).getCommandeNumber(pin);
                        if(pin==7&&value!=0)
                        {
                            logger.debug("switch");
                            switch (Main.clientHashMap.get(id).data.get(4).getData())
                            {
                                case "1":
                                case "0":
                                    value=2;
                                    break;
                                case "2":
                                    logger.debug("ok");
                                    value=1;
                                    break;
                                case "3":
                                case "4":
                                    value=0;
                                    break;

                            }
                        }
                        if(!(pin==7&&value==0))
                            Main.clientHashMap.get(id).send("<c,"+commande+","+value+">");
                    }
                    else{
                        Main.socketScript=null;
                        logger.warn("Blynk connection lost...");
                    }
                } catch (SocketTimeoutException e)
                {

                }
                catch (IOException e) {
                    logger.catching(e);
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.catching(e);
            }

        }
    }

    public boolean checkPID()
    {
        String pid=null;
        try{
            ProcessBuilder pb = new ProcessBuilder("pidof", "node");
            Process p = pb.start();
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
                logger.trace(line);
                pid=line;

            }
            p.destroy();


        } catch (IOException e) {
            logger.catching(e);
        }
        if(pid!=null)
            return true;
        else
            return false;
    }
}
