package InterfacePi.Communication;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import InterfacePi.Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by seb65 on 13/12/2016.
 */
public class Auth {
    Socket socket;
    static Logger logger = LogManager.getLogger();


    /**
     * Gestion d'authentificaton d'un nouveau client.
     * @param socket
     */
    public void newAth(Socket socket)
    {
        this.socket=socket;
        logger.debug("Wait auth message for "+socket.getInetAddress()+"...");
        Wait wait = new Wait();
        wait.start();
    }

    /**
     *Gestion de la connection avec un client.
     */
    public class Wait extends Thread {

        private boolean denied = false;

        @Override
        public void run() {
            Traitement traitement = new Traitement();

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String[] data = traitement.getInfo(in.readLine());

                if(data.length>=2)
                {
                    if(data[0].equals("a"))
                    {
                        if(Main.clientHashMap.containsKey(Integer.parseInt(data[1])))
                        {
                            denied=false;
                            logger.info("Client identified: "+ Main.clientHashMap .get(Integer.parseInt(data[1])).name+".");
                            logger.debug(Main.clientHashMap.get(Integer.parseInt(data[1])).name);
                            if(Main.clientHashMap.get(Integer.parseInt(data[1])).socket==null)
                            {
                                Main.clientHashMap.get(Integer.parseInt(data[1])).setSocketAndGo(socket);
                            }
                            else
                                denied=true;


                        }
                        else
                        {
                            logger.warn("Authentication Failure for "+socket.getInetAddress()+", not found in DataBase");
                            denied=true;
                        }

                    }
                    else
                    {
                        logger.warn("Authentication Failure for "+socket.getInetAddress()+", not auth frame");
                        denied=true;
                    }


                }
                else
                {
                   logger.warn("Authentication Failure for "+socket.getInetAddress()+", not auth frame");
                   denied=true;
                }


                if(denied)
                {
                    denied=false;
                    logger.warn("Close connection for "+socket.getInetAddress()+" ...");
                    socket.shutdownInput();
                    socket.close();
                    Main.clientHashMap.get(Integer.parseInt(data[1])).socket=null;

                }

            }
            catch (SocketTimeoutException ste) {
                logger.warn("Time out, Authentication Failure for "+socket.getInetAddress()+", close connection...");
                try {
                    socket.shutdownInput();
                    socket.close();
                } catch (IOException e) {
                    logger.catching(e);
                }

            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
