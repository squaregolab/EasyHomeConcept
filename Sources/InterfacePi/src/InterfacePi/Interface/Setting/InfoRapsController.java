package InterfacePi.Interface.Setting;

import com.pi4j.system.SystemInfo;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.Section;
import eu.hansolo.medusa.tools.GradientLookup;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by seb65 on 11/01/2017.
 */
public class InfoRapsController {
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox vbox;

    @FXML
    private VBox temp;

    @FXML
    private VBox memoire;

    @FXML
    private Button back;
    @FXML
    private Button detail_Raspberry;
    @FXML
    private Button wifiSetting;

    @FXML
    private Pane pane;
    static Logger logger = LogManager.getLogger();
    Gauge meme;
    Gauge tempG;
    boolean onScreen;
    @FXML
    void initialize() {
        onScreen=true;
        Image backImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/back.png"),100,100,false,true);
        ImageView backView = new ImageView(backImg);
        Image backDownImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/backDown.png"),100,100,false,true);
        ImageView backDownView = new ImageView(backDownImg);
        back.setGraphic(backView);

        back.setOnMousePressed((event ->
        {
            back.setGraphic(backDownView);
        }));
        back.setOnMouseReleased((event -> {
            onScreen=false;
            back.setGraphic(backView);
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Setting/setting.fxml"));
                Stage stage = (Stage) back.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        detail_Raspberry.setOnMouseReleased((event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Fonction en cour de dévelopement...");
            alert.setHeaderText("Fonction non disponible");
            alert.show();
//            try{
//                ProcessBuilder killApp = new ProcessBuilder("sudo", "killall", "java");
//                Process processKillApp = killApp.start();
//                Thread.sleep(2000);
//                processKillApp.destroy();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

        }));




        float maxMem=-1;
        try {
             maxMem = (SystemInfo.getMemoryUsed()+SystemInfo.getMemoryFree())/1048576;
             logger.debug(maxMem);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        meme = GaugeBuilder.create()
                .skinType(Gauge.SkinType.TILE_KPI)
                .prefSize(300,300)
                .maxValue(100)
                .minValue(0)
                .decimals(1)
                .title("Ram")
                .unit("%")
                .sectionsVisible(true)
                .barColor(Color.TRANSPARENT)
                .sections(new Section(0, 70, Color.GREEN),new Section(70,85,Color.ORANGE),new Section(85,100,Color.RED))
                .animated(true)
                .animationDuration(1000)
                .build();

        memoire.getChildren().add(meme);
        tempG = GaugeBuilder.create()
                .skinType(Gauge.SkinType.TILE_KPI)
                .prefSize(300,300)
                .maxValue(90)
                .minValue(25)
                .value(25)
                .decimals(1)
                .title("Cpu Temperature")
                .unit("°C")
                .sectionsVisible(true)
                .barColor(Color.TRANSPARENT)
                .sections(new Section(20, 70, Color.GREEN),new Section(70,80,Color.ORANGE),new Section(80,90,Color.RED))
                .animated(true)
                .animationDuration(1000)
                .build();
        temp.getChildren().add(tempG);
        new ChangeListener().start();

        wifiSetting.setOnMouseReleased((event -> {
            onScreen=false;

            try {
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Setting/wifiSetting.fxml"));
                Stage stage = (Stage) back.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }));


    }


    class ChangeListener extends Thread
    {

        float temperature=-1;
        float memUsed=-1;
        float memTotal=-1;
        float memFree=-1;
        float memPour=-1;
        @Override
        public void run() {
            while(onScreen)
            {

                try {
                    temperature = SystemInfo.getCpuTemperature();
                    memFree =SystemInfo.getMemoryFree()/ 1048576;
                    memUsed= (SystemInfo.getMemoryUsed()-SystemInfo.getMemoryCached()-SystemInfo.getMemoryBuffers())/ 1048576;
                    memTotal = SystemInfo.getMemoryTotal()/ 1048576;
                    memPour = (memUsed/memTotal)*100;

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(meme.getValue()!=memPour)
                {
                    meme.setValue(memPour);
                }
                if(tempG.getValue()!=temperature)
                {
                    tempG.setValue(temperature);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
