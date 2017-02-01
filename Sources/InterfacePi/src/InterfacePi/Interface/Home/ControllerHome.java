package InterfacePi.Interface.Home;

/**
 * Sample Skeleton for 'home.fxml' ControllerHome Class
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import InterfacePi.Communication.LoadHistory;
import InterfacePi.Communication.ModuleLauncher;
import InterfacePi.Communication.Type.HistoryData;
import InterfacePi.Interface.ExceptionAlert;
import InterfacePi.Main;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.action.Action;
import sun.awt.windows.ThemeReader;

import static InterfacePi.Main.launcher;

public class ControllerHome {

    Logger logger = LogManager.getLogger();

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="generalScreen"
    private Button general; // Value injected by FXMLLoader

    @FXML // fx:id="parametre"
    private Button parametre; // Value injected by FXMLLoader

    @FXML // fx:id="piece"
    private Button piece; // Value injected by FXMLLoader

    @FXML
    private StackPane stack;
    @FXML
    private Pane pane;
    @FXML
    private VBox vbox;


    public MaskerPane masker;

    public boolean onScreen = true;
    NotificationPane notificationOk;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert general != null : "fx:id=\"generalScreen\" was not injected: check your FXML file 'home.fxml'.";
        assert parametre != null : "fx:id=\"parametre\" was not injected: check your FXML file 'home.fxml'.";
        assert piece != null : "fx:id=\"piece\" was not injected: check your FXML file 'home.fxml'.";
        assert stack != null : "fx:id=\"stack\" was not injected: check your FXML file 'home.fxml'.";
        assert pane != null : "fx:id=\"pane\" was not injected: check your FXML file 'home.fxml'.";
        pane.setStyle("-fx-background-color: white");
        onScreen = true;
        masker = new MaskerPane();

        pane.getChildren().add(masker);
        masker.setPrefSize(1024,600);
        masker.setVisible(true);

        new ChangeVarListener().start();
        Image parametreImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/parametres.png"),200,200,false,true);
        ImageView paramUpView = new ImageView(parametreImg);

        Image parametredDownImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/parametresDown.png"), 200, 200, false, true);
        ImageView paramDownView = new ImageView(parametredDownImg);

        Image piecesImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/pieces.png"), 200, 200, false, true);
        ImageView piecesView = new ImageView(piecesImg);

        Image piecesDownImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/piecesDown.png"), 200, 200, false, true);
        ImageView piecesDownView = new ImageView(piecesDownImg);

        Image genralImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/general.png"), 200, 200, false, true);
        ImageView generalView = new ImageView(genralImg);

        Image generalDownImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/generalDown.png"), 200, 200, false, true);
        ImageView generalDownView = new ImageView(generalDownImg);

        ObservableList<Node> list = vbox.getChildren();

        Node n = list.iterator().next();
        Pane nn = (Pane)n;

        notificationOk = new NotificationPane(nn);
        notificationOk.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
        Image image = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/info_t.png"), 50, 50, false, true);
        ImageView testIm = new ImageView(image);
        notificationOk.setGraphic(testIm);
        notificationOk.setCloseButtonVisible(false);

        notificationOk.setText("Chargement de "+Main.clientHashMap.size()+" client(s) sans erreur.");
        notificationOk.getActions().addAll(new Action("Fermer",actionEvent -> {
            notificationOk.hide();
        }));
        notificationOk.setShowFromTop(true);

        vbox.getChildren().clear();
        vbox.getChildren().add(notificationOk);


        parametre.setGraphic(paramUpView);
        piece.setGraphic(piecesView);
        general.setGraphic(generalView);

        parametre.setOnMousePressed((event ->
        {
            parametre.setGraphic(paramDownView);
        }));
        parametre.setOnMouseReleased((event -> {
            parametre.setGraphic(paramUpView);
            try {
                onScreen=false;
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Setting/setting.fxml"));
                Stage stage = (Stage) piece.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        general.setOnMousePressed((event ->
        {
            general.setGraphic(generalDownView);
        }));

       general.setOnMouseReleased((event -> {
            general.setGraphic(generalView);
           try {
               onScreen=false;
               Main.currentRoom=0;
               Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Pieces/sample.fxml"));
               Stage stage = (Stage) piece.getScene().getWindow();
               Scene scene = new Scene(root);
               stage.setScene(scene);
               stage.show();

           } catch (IOException e) {
               e.printStackTrace();
           }

        }));

        piece.setOnMousePressed((event ->
        {
            piece.setGraphic(piecesDownView);
        }));
        piece.setOnMouseReleased((event -> {
            piece.setGraphic(piecesView);
            try {
                onScreen=false;
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/romList.fxml"));
                Stage stage = (Stage) piece.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }));



    }
    class ChangeVarListener extends Thread {
        public Logger logger = LogManager.getLogger();
        int percEtat=-1;

        @Override
        public void run() {
            while (onScreen) {
                if(percEtat!= launcher.etat)
                {

                    switch (launcher.etat)
                    {
                        case 0:
                            Platform.runLater(() -> masker.setText("Chargement des sauvegarde..."));
                            break;
                        case 1:
                            Platform.runLater(() -> masker.setText("Lancement du script Blynk..."));
                            break;
                        case 2:
                            Platform.runLater(() -> masker.setText("Lancement du script Blynk..."));
                            break;
                        case 3:
                            Platform.runLater(() -> masker.setText("Lancement du serveur de communication..."));
                            break;
                        case 4:
                            Platform.runLater(() -> {
                                launcher.etat=5;
                                pane.setStyle("-fx-background-color: black");
                                masker.setVisible(false);
                                notificationOk.setText("Chargement de "+Main.clientHashMap.size()+" client(s) sans erreur.");
                                notificationOk.show();
                                new Timer(notificationOk).start();
                            });
                            break;
                        case 5:
                            pane.setStyle("-fx-background-color: black");
                            masker.setVisible(false);
                            break;
                        default:
                            Platform.runLater(() -> masker.setText("..."));
                                break;
                    }
                    percEtat= launcher.etat;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.catching(e);
                }
            }
        }
    }

    class Timer extends Thread{
        NotificationPane toHide;
        public Timer(NotificationPane toHide) {
            this.toHide=notificationOk;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            toHide.hide();
        }
    }

}
