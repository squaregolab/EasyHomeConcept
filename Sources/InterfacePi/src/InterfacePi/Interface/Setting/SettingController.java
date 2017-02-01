package InterfacePi.Interface.Setting;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by seb65 on 11/01/2017.
 */
public class SettingController {
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="vbox"
    private VBox vbox; // Value injected by FXMLLoader

    @FXML // fx:id="gestionModule"
    private Button gestionModule; // Value injected by FXMLLoader

    @FXML // fx:id="back"
    private Button back; // Value injected by FXMLLoader

    @FXML // fx:id="infoRaps"
    private Button infoRaps; // Value injected by FXMLLoader

    @FXML // fx:id="gestionApp"
    private Button gestionApp; // Value injected by FXMLLoader

    @FXML // fx:id="pane"
    private Pane pane; // Value injected by FXMLLoader

    Logger logger = LogManager.getLogger();

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {





        infoRaps.setOnMouseReleased(event -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Setting/infoRasp.fxml"));
                Stage stage = (Stage) back.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                logger.catching(e);
            }
        });

        gestionApp.setOnMouseReleased(event -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Fonction en cour de dévelopement.\n\nVoulez-vous quiter l'application?");
            alert.setHeaderText("Fonction non disponible");
            alert.getButtonTypes().setAll(ButtonType.YES,ButtonType.CANCEL);
            Optional<ButtonType> result = alert.showAndWait();

            if(result.isPresent())
            {
                if(result.get().equals(ButtonType.YES))
                {
                    try{
                        ProcessBuilder killApp = new ProcessBuilder("sudo", "killall", "java");
                        Process processKillApp = killApp.start();
                        Thread.sleep(2000);
                        processKillApp.destroy();
                        killApp = new ProcessBuilder("sudo", "killall", "node");
                        processKillApp = killApp.start();
                        Thread.sleep(2000);
                        processKillApp.destroy();

                    } catch (InterruptedException e) {
                        logger.catching(e);
                    } catch (IOException e) {
                        logger.catching(e);
                    }
                }
            }

        });

        gestionModule.setOnMouseReleased(event -> {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Setting/moduleManagement.fxml"));
                Stage stage = (Stage) back.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                logger.catching(e);
            }
        });



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
            back.setGraphic(backView);
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Home/home.fxml"));
                Stage stage = (Stage) back.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

    }
}
