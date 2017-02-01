package InterfacePi.Communication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import InterfacePi.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by seb65 on 09/12/2016.
 */
public class ClientS {
    public String name;
    public int ID;
    InetAddress IP;
    public Socket socket=null;
    static Logger logger = LogManager.getLogger();
    String commande=null;
    public boolean sendCommande = false;
    public boolean waitAfterCommande =false;
    public List<Data> data = new ArrayList<>();
    int timedOut=0;
    int tempo=12;
    boolean isGeneral;
    public int type;


    public ClientS(String name, int ID,List<Data> data,int type,boolean isGeneral) {
        logger.trace("Constructeur.");
        logger.debug("Create client \""+name+"\" whit id #"+ID+".");
        this.name = name;
        this.ID = ID;
        this.sendCommande=false;
        this.data=data;
        this.isGeneral=isGeneral;
        this.type=type;

    }

    /**
     * Store socket for client.
     * @param socket
     */
    public void setSocket(Socket socket) {
        this.socket = socket;
        this.IP=socket.getInetAddress();
    }

    /**
     * Send message to this client.
     * @param message
     */
    public void send(String message) {
        logger.info("Sending commande \""+message+"\" to "+name);
        this.sendCommande=true;
        tempo=10000;
        this.commande=message;

    }


    /**
     * Set socket to client et launch this connectionManager.
     * @param socket
     */
    public void setSocketAndGo(Socket socket) {
        this.socket = socket;
        this.IP=socket.getInetAddress();
        ConnectionManager connectionManager = new ConnectionManager();
        connectionManager.start();


    }

    /**
     * Manage the connection for this client (send and reception)
     */
    class ConnectionManager extends Thread {


        @Override
        public void run() {
            logger.debug("Communication manager for "+socket.getInetAddress()+" started...");
            try {


                while(socket!=null) {
                    try
                    {

                        if(timedOut<5)
                        {
                            //Get all stream
                            PrintStream outSocket = new PrintStream(socket.getOutputStream());

                            //A command is pending?
                            if(sendCommande)
                            {
                                //yes, request is pending and we send the command
                                logger.debug("Sending command :"+commande+" to "+IP.toString()+"...");
                                outSocket.print(commande);
                                sendCommande=false;
                                waitAfterCommande = true;
                                receive();
                            }
                            else{
                                //no, we keep send request.
                                if(tempo<=0)
                                {
                                    sendCommande=false;
                                    waitAfterCommande = true;
                                    logger.debug("Send request to "+socket.getInetAddress()+"...");
                                    outSocket.print("<q>");
                                    outSocket.flush();
                                    receive();


                                }
                                else
                                    tempo--;
                            }
                            Thread.sleep(250);
                        }
                        else
                        {
                            logger.error("5 Time out, close connection for "+name+"...");
                            if(socket!=null)
                            {
                                socket.shutdownInput();
                                socket.shutdownOutput();
                                socket.close();
                            }

                            socket=null;
                            timedOut=0;
                            Thread.sleep(1000);


                        }
                    }catch (SocketTimeoutException ste)
                    {
                        logger.warn("Time out for "+name+"!");
                        timedOut++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }catch (SocketException e) {
                logger.catching(e);
                if(socket!=null)
                {
                    if(socket.isConnected()||!socket.isClosed())
                    {
                        try {
                            socket.shutdownInput();
                            socket.shutdownOutput();
                            socket.close();
                            socket=null;
                        } catch (IOException e1) {logger.catching(e1);}
                    }
                }

            }catch (IOException e) {
                logger.catching(e);
                //e.printStackTrace();
            }
            socket=null;

            logger.warn("Disconected.");
        }

    }
    public int getCommandeNumber(int pin)
    {
        switch (pin)
        {
            case 1:
                return 3;
            case 3:
                return 1;
            case 7:
                return 2;
            case 6:
                return 4;

        }
        return -1;
    }

    private void receive() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Traitement traitement = new Traitement();
        String recuTemp = in.readLine();
        logger.debug("Frame received from "+socket.getInetAddress()+": "+recuTemp);
        if(recuTemp==null)
        {
            logger.warn("Connection to "+name+" lost...");
            socket=null;
        }
        else
        {
            if(recuTemp.contains("nan"))
            {
                logger.warn("Failed to read from DHT sensor for "+name+"!");
            }
            String[] dataTemp = traitement.getInfo(recuTemp);
            if(dataTemp!=null)
            {
                if(dataTemp[0]!=null)
                {
                    if(dataTemp[0].equals("r"))
                    {
                        for(int i = 0; i<dataTemp.length-2; i++)
                        {
                            data.get(i).setData(dataTemp[i+2]);
                        }
                    }
                    timedOut=0;
                    waitAfterCommande=false;

                    logger.debug("Message in comming from"+socket.getInetAddress()+".");
                    logger.trace("Data form \""+name+"\":");
                    for(Data aData : data)
                    {
                        logger.trace("\t*"+aData.getName()+": "+aData.getData());
                    }
                    Main.communicationScript.send(ID,data);
                }
                tempo=10;
            }




        }
    }





}
