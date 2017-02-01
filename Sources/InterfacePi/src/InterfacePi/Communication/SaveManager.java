package InterfacePi.Communication;

import InterfacePi.Communication.Type.StandardClient;
import InterfacePi.Interface.Setting.ModuleController;
import InterfacePi.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Seb on 23/01/2017.
 */
public class SaveManager {
    static Logger logger = LogManager.getLogger();

    public static boolean load()
    {
        logger.info("Loading save...");
        Main.clientHashMap= new HashMap<>();
        boolean firstLoad = true;
        Path path = Paths.get("/home/pi/Interface/config");
        if (Files.exists(path)) {
            logger.info("Save folder exist.");
            File folder = path.toFile();
            folder.setExecutable(true,false);
            folder.setReadable(true,false);
            folder.setWritable(true,false);
            File[] filesList = folder.listFiles();
            logger.info("Found "+filesList.length+" save file");
            BufferedReader reader;

            for(File aFile:filesList)
            {
                boolean error = false;
                ArrayList temp = new ArrayList();
                String name="";
                int id = -1;

                logger.debug("File Name: "+aFile.getName());

                id = Integer.parseInt(aFile.getName().replaceAll(".config",""));
                logger.debug("Content:");
                try {
                    reader = new BufferedReader(new FileReader(aFile));
                    String line;
                    while((line = reader.readLine())!=null)
                    {

                        line=line.replace(" ","");
                        line=line.replace("\t","");
                        logger.debug(line);
                        if(line.equals("name:"))
                        {
                            logger.debug("name detected:");

                            name=reader.readLine().replace("\t","");
                            logger.debug(name);
                            line=reader.readLine();
                            while(line!=null&&!line.equals("type:"))
                                line=reader.readLine();
                            if(line!=null)
                            {
                                logger.debug("Type detected.");
                                String temps = reader.readLine();
                                temps=temps.replace(" ","");
                                temps=temps.replace("\t","");
                                logger.debug(temps);
                                if(temps.equals("0")||temps.equals("1"))
                                {
                                    int type = Integer.parseInt(temps);
                                    line=reader.readLine();
                                    while(line!=null&&!line.equals("general:"))
                                        line=reader.readLine();
                                    if(line!=null)
                                    {
                                        logger.debug("general detected:");

                                        String generalS = reader.readLine().replace(" ", "").replace("\t", "");
                                        logger.debug(generalS);
                                        if(generalS.equals("1"))
                                        {
                                            ClientS tempC = new StandardClient().get(name, id,type,true);
                                            Main.clientHashMap.put(id,tempC);
                                            Main.generalScreen =id;
                                        }
                                        else
                                        {
                                            ClientS tempC = new StandardClient().get(name, id,type, false);
                                            Main.clientHashMap.put(id,tempC);
                                        }

                                    }


                                }
                            }
                            else
                            {
                                error=true;
                                break;
                            }
                        }
                    }

                } catch (FileNotFoundException e) {
                    logger.catching(e);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            firstLoad=false;
        }
        else
        {
            logger.info("Save folder does not exist creating it...");
            firstLoad=true;
            try {
                Files.createDirectory(path);
                File file = path.toFile();
                file.setExecutable(true,false);
                file.setReadable(true,false);
                file.setWritable(true,false);

            } catch (IOException e) {
                logger.catching(e);
            }

        }
        return firstLoad;
    }

    public static boolean remove(int id)
    {
        File file = new File("/home/pi/Interface/config/"+id+".config");
        if(file.exists())
        {
            if(file.delete())
                return true;
            else
                return false;
        }
        else
            return false;
    }

    public static boolean create(ModuleController.DialogResult result)
    {
        File file = new File("/home/pi/Interface/config/"+result.getId()+".config");
        if(file.exists())
            return false;
        else
        {
            try {
                file.createNewFile();
                file.setExecutable(true,false);
                file.setReadable(true,false);
                file.setWritable(true,false);
                PrintWriter out = new PrintWriter(file);
                out.println("name:");
                out.println("\t"+result.getName());
                out.println("type:");
                out.println("\t"+result.getType());
                out.println("general:");
                out.println("\t"+result.getGeneral());
                out.close();
                return true;
            } catch (IOException e) {
                logger.catching(e);
            }
        }
        return false;

    }

}
