package InterfacePi.Communication.Type;

import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.MaskerPane;

import java.io.*;
import java.util.*;

/**
 * Created by seb65 on 12/01/2017.
 */
public class Wifi {
    static Logger logger = LogManager.getLogger();
    public List<Chanel> chanelsList = new ArrayList<>();
    public Scan  scan;
    private int selected =-1;

    public void setSelected(int selected) {
        this.selected = selected;
    }




    public String getSelectedSSID() {
        return chanelsList.get(selected).getSSID();
    }

    public boolean changeConfig(String psk, MaskerPane maskerPane)
    {
        ChangeConfig changeConfig = new ChangeConfig(chanelsList.get(selected).getSSID(),psk,maskerPane);
        return changeConfig.doYourJob();

    }

    public void scan(){
        scan=new Scan();
        scan.finish=false;
        scan.start();
    }

    public class Scan extends Thread
    {
        Logger loggerScan = LogManager.getLogger();
        private boolean finish =false;
        @Override
        public void run() {
            try {
                finish=false;
                ProcessBuilder wifiScan = new ProcessBuilder("sudo", "iwlist", "wlan0", "scan");
                Process processWifiScan = wifiScan.start();
                InputStreamReader isrWifiScan = new InputStreamReader(processWifiScan.getInputStream());
                BufferedReader brWifiScan = new BufferedReader(isrWifiScan);
                String lineWifiScan;
                Chanel tempWifi=null;

                while ((lineWifiScan = brWifiScan.readLine()) != null) {
                    String[] splited = null;
                    if (lineWifiScan.contains("Quality")) {
                            tempWifi = null;
                            tempWifi = new Chanel();
                            loggerScan.debug("Quality detected");
                            lineWifiScan = lineWifiScan.replace("Quality=", "");

                            splited = lineWifiScan.split("/");
                            splited[0] = splited[0].replaceAll(" ", "");
                            splited[0] = splited[0].replaceAll("\t", "");
                            tempWifi.setQuality(Integer.parseInt(splited[0]));
                            loggerScan.debug("Quality: " + tempWifi.getQuality());
                    }
                    if (lineWifiScan.contains("ESSID") && tempWifi.getQuality() != -1 && tempWifi!=null) {
                        loggerScan.debug("ESSID detected");
                        splited = null;
                        splited = lineWifiScan.split("\"");
                        if (splited.length > 1) {
                            tempWifi.setSSID(splited[1]);
                            loggerScan.debug("SSID: " + tempWifi.getSSID());
                            chanelsList.add(tempWifi);
                        } else {
                            loggerScan.debug("Masked network, ignored it");
                        }


                    }
                    if(lineWifiScan.contains("Cell")) tempWifi = new Chanel();
                }

            }catch (IOException e) {
                e.printStackTrace();
            }
            Collections.sort(chanelsList,new Comparator<Chanel>() {
                @Override
                public int compare(Chanel tc1, Chanel tc2) {
                    return Integer.compare(tc2.getQuality(),tc1.getQuality());
                }
            });
            finish=true;

        }

        public boolean isFinish() {
            return finish;
        }
    }

    public WifiState checkConnection(boolean skiptLoadConfig,WifiState oldState)
    {
        if(!skiptLoadConfig)
        {
            LoadWifiConfig loadWifiConfig = new LoadWifiConfig();
            loadWifiConfig.doYourJob();
            String checkIp = this.checkIp();
            return new WifiState(loadWifiConfig.isEmpty(),checkIp!=null,loadWifiConfig.getSsid(),loadWifiConfig.getPsk(),checkIp);
        }
        else
        {
            String checkIp = this.checkIp();
            return new WifiState(oldState.isEmpty(),checkIp!=null,oldState.getSsid(),oldState.getPsk(),checkIp);
        }


    }

    public class ChangeConfig
    {
        Logger loggerLoad = LogManager.getLogger();
        private String ssid;
        private String psk;
        private MaskerPane maskerPane;


        public ChangeConfig(String ssid, String psk, MaskerPane maskerPane) {
            this.ssid = ssid;
            this.psk = psk;
            this.maskerPane = maskerPane;
        }


        public boolean doYourJob() {
            try {
                Platform.runLater(()-> maskerPane.setText("Application des modifications..."));

                File in = new File("/etc/wpa_supplicant/wpa_supplicant.conf");
                File out = new File("/etc/wpa_supplicant/temp");
                out.createNewFile();
                String line=null;
                BufferedWriter bw = new BufferedWriter(new FileWriter(out));

                if(in.exists())
                {
                    logger.debug("Config file exist");
                    BufferedReader reader = new BufferedReader(new FileReader(in));


                    while ((line = reader.readLine()) != null&&!line.startsWith("update")) {
                        loggerLoad.debug(line);
                        bw.write(line+"\n");
                        bw.flush();
                    }
                    reader.close();
                }
                else{
                    logger.debug("Config file not found!");
                    in.createNewFile();
                    bw.write("ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev\n" +
                            "update_config=1");
                    line="";
                }

                if(line!=null)
                {
                    bw.write(line+"\n\nnetwork={\n\tssid=\"" + ssid+"\"\n\tpsk=\"" + psk + "\"\n}");
                    bw.flush();

                }
                bw.close();

                in.renameTo(new File("/etc/wpa_supplicant/trash"));
                //in.delete();
                out.renameTo(new File("/etc/wpa_supplicant/wpa_supplicant.conf"));

                Platform.runLater(()-> maskerPane.setText("Tentative de connection...."));
                ProcessBuilder wifiStop = new ProcessBuilder("sudo", "ifdown", "wlan0");
                Process processWifiStop = wifiStop.start();
                Thread.sleep(2000);
                processWifiStop.destroy();

                ProcessBuilder wifiStart = new ProcessBuilder("sudo", "ifup", "wlan0");
                Process processWifiStart = wifiStart.start();
                Thread.sleep(2000);
                processWifiStart.destroy();


                for(int i=0; i<60;i++)
                {
                    String resultIp=checkIp();
                    if(resultIp!=null)
                    {
                        return true;
                    }
                    Thread.sleep(1000);
                }



            } catch (FileNotFoundException e) {
                loggerLoad.catching(e);
            } catch (IOException e) {
                loggerLoad.catching(e);
            } catch (InterruptedException e) {
                loggerLoad.catching(e);
            }
            return false;

        }
    }

    private class LoadWifiConfig
    {
        Logger loggerloadWifiConf = LogManager.getLogger();
        private boolean empty;
        private String ssid=null;
        private String psk=null;

        public String getPsk() {
            return psk;
        }

        public String getSsid() {
            return ssid;
        }

        public boolean isEmpty() {
            return empty;
        }

        public void doYourJob()
        {
            loggerloadWifiConf.debug("Trying to load Wifi config");
            File in = new File("/etc/wpa_supplicant/wpa_supplicant.conf");
            if(in.exists())
            {
                try
                {
                    empty=false;
                    loggerloadWifiConf.debug("Config file exist");
                    BufferedReader reader = new BufferedReader(new FileReader(in));
                    String line=null;

                    while ((line = reader.readLine()) != null&&!line.contains("ssid"));
                    if(line!=null)
                    {
                        String[] splited = line.split("\"");
                        if (splited.length > 1) {
                            ssid=splited[1];
                        } else {
                            empty=true;
                        }
                    }
                    if(ssid!=null)
                    {
                        while ((line = reader.readLine()) != null&&!line.contains("psk"));
                        if(line!=null)
                        {
                            String[] splited = line.split("\"");
                            if (splited.length > 1) {
                                psk=splited[1];
                            } else {
                                empty=true;
                            }
                        }
                        else
                        {
                            loggerloadWifiConf.info("PassKey not found...");
                            empty=true;
                        }



                    }
                    else
                    {
                        loggerloadWifiConf.info("SSID not found...");
                        empty=true;
                    }
                    reader.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else
            {
                loggerloadWifiConf.info("Wifi config file not found");
                empty=true;
            }

        }


    }

    private String checkIp()
    {

        try {
            ProcessBuilder wifiIp = new ProcessBuilder("ifconfig", "wlan0");
            Process processWifiIp = wifiIp.start();
            InputStreamReader isrWifiIp = new InputStreamReader(processWifiIp.getInputStream());
            BufferedReader brWifiIp = new BufferedReader(isrWifiIp);
            String lineWifiIp;


            while ((lineWifiIp = brWifiIp.readLine()) != null && !lineWifiIp.contains("inet "));
            if(lineWifiIp!=null)
            {
                String[] splited = lineWifiIp.split(" ");
                for(String aString : splited)
                {
                    if(aString.startsWith("adr"))
                    {
                        return aString.replace("adr:","");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***************************
     *      Type Class         *
     ***************************/
    public class WifiState
    {
        private boolean empty;
        private boolean connected;
        private String ssid;
        private String psk;
        private String ip;


        public WifiState(boolean empty, boolean connected, String ssid, String psk, String ip) {
            this.empty = empty;
            this.connected = connected;
            this.ssid = ssid;
            this.psk = psk;
            this.ip = ip;
        }


        public void setConnected(boolean connected) {
            this.connected = connected;
        }



        public boolean isEmpty() {
            return empty;
        }

        public boolean isConnected() {
            return connected;
        }

        public String getSsid() {
            return ssid;
        }

        public String getPsk() {
            return psk;
        }

        public String getIp() {
            return ip;
        }


    }

    public class Chanel
    {
        private String SSID;
        private String PSK;
        private int quality=-1;
        private boolean isConnected;

        public void setSSID(String SSID) {
            this.SSID = SSID;
        }

        public void setPSK(String PSK) {
            this.PSK = PSK;
        }

        public void setQuality(int quality) {
            this.quality = quality;
        }

        public void setConnected(boolean connected) {
            isConnected = connected;
        }

        public int getQuality() {
            return quality;
        }

        public String getPSK() {
            return PSK;
        }

        public String getSSID() {
            return SSID;
        }

        public boolean isConnected() {
            return isConnected;
        }
    }


}
