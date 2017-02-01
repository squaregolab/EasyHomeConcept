package InterfacePi.Communication;

import InterfacePi.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by seb65 on 10/01/2017.
 */
public class ModuleLauncher {
    public int etat=0;
    static Logger logger = LogManager.getLogger();
    public void lauchAll()
    {
        new thread().start();
    }
    class thread extends Thread
    {

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.catching(e);
            }
            etat=1;
            SaveManager.load();
            etat=2;
            new Main.LauchScript().start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                logger.catching(e);
            }
            etat=3;
            Communication communication = new Communication();
            communication.start();
            while( communication.endInit!=true);
            etat=4;
        }
    }
}
