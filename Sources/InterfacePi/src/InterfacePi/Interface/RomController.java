package InterfacePi.Interface;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import InterfacePi.Main;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.NotificationPane;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;

public class RomController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private HBox HBox;

    @FXML
    private Button back;

    @FXML
    private ListView<Button> listePieces;

    @FXML
    public VBox vbox;

    final private Logger logger = LogManager.getLogger();

    ArrayList<Integer> tableId = new ArrayList<Integer>();
    ArrayList<String> tableName = new ArrayList<String>();
    ArrayList<Socket> tableSocket = new ArrayList<>();
    public int disconectedClient=0;


    NotificationPane diconectInfo;
    NotificationPane reconnectInfo;
    public boolean onScreen;

    @FXML
    void initialize() {
        assert HBox != null : "fx:id=\"leftAnchor\" was not injected: check your FXML file 'romList.fxml'.";
        assert back != null : "fx:id=\"back\" was not injected: check your FXML file 'romList.fxml'.";
        assert listePieces != null : "fx:id=\"listePieces\" was not injected: check your FXML file 'romList.fxml'.";
        onScreen=true;












        HBox.setDisable(false);
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
            onScreen=false;
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Home/home.fxml"));
                Stage stage = (Stage) back.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                logger.catching(e);
            }
        }));

        ObservableList<Button> items= FXCollections.observableArrayList();
        Main.clientHashMap.forEach((k, v) ->
        {
            Button temp=null;
            temp=new Button(v.name);
            temp.setPrefWidth(750);
            tableId.add(v.ID);
            tableName.add(v.name);
            tableSocket.add(v.socket);
            temp.getStylesheets().add("/InterfacePi/Interface/button.css");
            if(v.socket==null)
            {
                temp.setId("red");
                disconectedClient++;
            }
            else
            {
                temp.setId("white");
            }


            temp.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println();
                    onScreen=false;
                    int id = tableId.get(tableName.indexOf(((Button) event.getSource()).getText()));
                    if(Main.clientHashMap.get(id).socket!=null)
                    {
                        Main.currentRoom=id;
                        try {

                            Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Pieces/sample.fxml"));
                            Stage stage = (Stage) listePieces.getScene().getWindow();
                            Scene scene = new Scene(root);
                            stage.setScene(scene);
                            stage.show();
                        } catch (IOException e) {
                            logger.catching(e);
                        }
                    }
                    else
                    {
                        Alert dlg = new Alert(Alert.AlertType.ERROR, "Erreur, le module \""+Main.clientHashMap.get(id).name+"\" n'est pas connecté!");
                        dlg.setHeaderText("Erreur de communication...");
                        dlg.showAndWait();
                    }


                }
            });
            items.add(temp);




        });
        ObservableList<Node> list = vbox.getChildren();

        Node n = list.iterator().next();
        Pane nn = (Pane)n;

        diconectInfo = new NotificationPane(nn);
        Image image = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/warning.png"),50,50,false,true);
        ImageView testIm = new ImageView(image);
        diconectInfo.setGraphic(testIm);
        diconectInfo.setCloseButtonVisible(false);

        diconectInfo.setText("Chargement de "+Main.clientHashMap.size()+" client(s) sans erreur.");
        diconectInfo.getActions().addAll(new Action("Fermer", actionEvent -> {
            diconectInfo.hide();
        }));
        diconectInfo.setShowFromTop(false);




        // and the Tab inside a TabPane. We just have one tab here, but of course
        // you can have more!


        vbox.getChildren().clear();
        vbox.getChildren().addAll(diconectInfo);


        listePieces.setItems(items);
        new Updateur().start();



    }


    class Updateur extends Thread
    {
        boolean first =true;
        @Override
        public void run() {
            while (onScreen)
            {
                ObservableList<Button> listeBouton = listePieces.getItems();
                listeBouton.forEach((abuton) -> {
                    int index = tableName.indexOf(abuton.getText());
                    int id = tableId.get(index);
                    if(Main.clientHashMap.get(id).socket!=tableSocket.get(index))
                    {
                        if(Main.clientHashMap.get(id).socket==null)
                        {
                            abuton.setStyle("-fx-background-color: linear-gradient(#ff5400, #be1d00);"
                                    +"-fx-background-radius: 6, 5;"
                                    +"-fx-background-insets: 0, 1;"
                                    +"-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.4) , 5, 0.0 , 0 , 1 );"
                                    +"-fx-text-fill: white;");
                            disconectedClient++;
                            Platform.runLater(() -> {
                                Notifications.create()
                                        .text("Connection au module " + abuton.getText() + " perdu...")
                                        .title("Perte de connection")
                                        .showWarning();

                                diconectInfo.hide();
                                diconectInfo.setText("Impossible d'établir la connection avec " + disconectedClient + " module(s).");
                                diconectInfo.show();



                            });
                        }
                        else
                        {
                            abuton.setStyle("-fx-background-color: linear-gradient(#f2f2f2, #d6d6d6),"
                                    +"linear-gradient(#fcfcfc 0%, #d9d9d9 20%, #d6d6d6 100%),"
                                    +"linear-gradient(#dddddd 0%, #f6f6f6 50%);"
                                    +"-fx-background-radius: 8,7,6;"
                                    +"fx-background-insets: 0,1,2;"
                                    +"-fx-effect: dropshadow( three-pass-box , rgba(0,0,0,0.6) , 5, 0.0 , 0 , 1 );"
                                    +"-fx-text-fill: black;");
                            disconectedClient--;
                            Platform.runLater(() -> {
                                Notifications.create()
                                        .text("Module " + abuton.getText() + " reconnecté...")
                                        .title("Connection")
                                        .showInformation();
                            });
                            if(disconectedClient!=0)
                            {
                                Platform.runLater(() -> {
                                    diconectInfo.hide();
                                    diconectInfo.setText("Impossible d'établir la connection avec " + disconectedClient + " module(s).");
                                    diconectInfo.show();
                                });
                            }
                            else
                                Platform.runLater(() -> diconectInfo.hide());


                        }
                        tableSocket.set(index,Main.clientHashMap.get(id).socket);
                    }


                });
                if(disconectedClient!=0 && first)
                {
                    Platform.runLater(() -> {
                        diconectInfo.setText("Impossible d'établir la connection avec " + disconectedClient + " module(s).");
                        diconectInfo.show();
                    });
                    first=false;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.catching(e);
                }
            }

        }
    }
}
