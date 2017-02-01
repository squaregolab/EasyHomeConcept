package InterfacePi.Interface.Setting;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import InterfacePi.Communication.Type.Wifi;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.MaskerPane;

public class WifiSettingController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox vbox;

    @FXML
    private ListView<Button> chanelList;

    @FXML
    private Button back;

    @FXML
    private Button refresh;

    @FXML
    private Pane pane;
    @FXML
    private StackPane listBody;

    private Wifi wifi;
    private Wifi.WifiState wifiState;

    static Logger logger = LogManager.getLogger();
    MaskerPane maskerPane;




    @FXML
    void initialize() {
        maskerPane = new MaskerPane();

        listBody.getChildren().add(maskerPane);
        maskerPane.setText("Verification de la connection...");
        maskerPane.setVisible(true);

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
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Setting/infoRasp.fxml"));
                Stage stage = (Stage) back.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        ObservableList<Button> items = FXCollections.observableArrayList();
        for(int i=0; i<4 ; i++)
        {
            Button temp = new Button();
            temp.getStylesheets().add("/InterfacePi/Interface/button.css");
            temp.setPrefWidth(chanelList.getPrefWidth()-40);
            items.add(temp);
        }
        chanelList.setItems(items);

        new Updateur(false,true).start();





        refresh.setOnMouseReleased((event -> {
            new Updateur(false,false).start();
            maskerPane.setText("Actualisation...");
            maskerPane.setVisible(true);
        }));










    }

    class Updateur extends Thread
    {
        private boolean showPopUp;
        private boolean skiptLoadConfig;

        public Updateur(boolean skiptLoadConfig,boolean showPopUp) {
            this.showPopUp = showPopUp;
            this.skiptLoadConfig = skiptLoadConfig;
        }

        @Override
        public void run() {
            wifi=new Wifi();
            wifi.scan();
            while(!wifi.scan.isFinish())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logger.debug("Scan finish");
            ObservableList<Button> items = FXCollections.observableArrayList();
            int i = 0;
            for(Wifi.Chanel aChanel:wifi.chanelsList)
            {
                logger.debug(aChanel.getSSID());
                Button temp = null;
                temp = new Button(aChanel.getSSID());
                temp.setPrefWidth(chanelList.getPrefWidth()-40);
                temp.setAlignment(Pos.BOTTOM_LEFT);
                temp.getStylesheets().add("/InterfacePi/Interface/button.css");
                Gauge gauge = GaugeBuilder.create()
                        .title(String.valueOf(i))
                        .skinType(Gauge.SkinType.CHARGE)

                        .prefWidth(155)
                        .maxWidth(155)

                        .build();
                logger.debug(aChanel.getQuality());
                float level = (float)aChanel.getQuality()/70;
                logger.debug(level);
                gauge.setValue(level);
                temp.setGraphic(gauge);
                temp.setGraphicTextGap(20);
                temp.setId("white");


                temp.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        wifi.setSelected(Integer.parseInt(gauge.getTitle()));
                        Dialog<String> dialog = getConectionDialog(wifi.getSelectedSSID());

                        Optional<String> result = dialog.showAndWait();

                        result.ifPresent(usernamePassword -> {
                           logger.debug("Password=" + usernamePassword);
                           maskerPane.setVisible(true);
                           new ChangeConfig(usernamePassword).start();



                        });
                        if(!result.isPresent())
                        {
                            wifi.setSelected(-1);
                        }


                    }
                });
                items.add(temp);
                i++;
            }
            Platform.runLater(()-> {
                chanelList.setItems(items);
                //maskerPane.setVisible(false);
                maskerPane.setText("Lecture des  paramètres Wifi...");

                checkConection(skiptLoadConfig,showPopUp);



            });

        }
    }



    public void checkConection(boolean skiptLoadConfig,boolean showPopUp)
    {
        wifiState= wifi.checkConnection(skiptLoadConfig,wifiState);
        maskerPane.setVisible(false);
        if(wifiState.isConnected())
        {
            if(showPopUp)
            {
                Platform.runLater(()->{

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setHeaderText("Connection Wifi établie!");
                    alert.setContentText("Réseau: "+wifiState.getSsid()+"\nIp: "+wifiState.getIp()+"\n\nVoulez-vous modifier les paramètres?\n");
                    ButtonType noButton = ButtonType.NO;
                    alert.getButtonTypes().setAll(ButtonType.YES,ButtonType.NO);

                    Optional<ButtonType> result = alert.showAndWait();
                    if(result.get()==ButtonType.NO)
                    {
                        try {
                            Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Setting/infoRasp.fxml"));
                            Stage stage = (Stage) back.getScene().getWindow();
                            Scene scene = new Scene(root);
                            stage.setScene(scene);
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });





            }


        }
        else
        {
            if(showPopUp) {
                Platform.runLater(()->{
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setHeaderText("Aucune connection Wifi!");
                alert.setContentText("Aucune connection Wifi n'a pu être établie.\n\nVoulez-vous modifier les paramètres?\n");


                Label label = new Label("Detail:");
                String temp = "Fichier de configuration: ";
                if(wifiState.isEmpty())
                {
                    temp=temp+"ERREUR";
                }
                else
                {
                    temp= temp+"OK\n" +
                            "\tSSID: "+wifiState.getSsid()+
                            "\n\tPSK: "+wifiState.getPsk();
                }
                temp = temp+"\n\nConnection: ";
                if(wifiState.isConnected())
                    temp = temp+"OK";
                else
                    temp = temp+"ERREUR";

                TextArea textArea = new TextArea(temp);
                textArea.setEditable(false);
                textArea.setWrapText(true);

                textArea.setMaxWidth(350);
                textArea.setMaxHeight(150);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);

                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(label, 0, 0);
                expContent.add(textArea, 0, 1);

                // Set expandable Exception into the dialog pane.
                alert.getDialogPane().setExpandableContent(expContent);
                alert.getButtonTypes().setAll(ButtonType.NO,ButtonType.YES);

                alert.showAndWait();
                });
            }
        }
    }


    private Dialog<String> getConectionDialog(String ssdi)
    {
        // Create the custom dialog.
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Entrez la clé de sécurité du réseau \""+ssdi+"\".");

        ButtonType loginButtonType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));


        PasswordField passwordMasked = new PasswordField();
        passwordMasked.setPromptText("Clé de sécurité");
        TextField password = new TextField();
        password.setPromptText("Clé de sécurité");
        CheckBox checkBox = new CheckBox("Afficher les caractères");

        grid.add(new Label("Clé de sécurité:"), 0, 1);
        grid.add(passwordMasked, 1, 1);
        grid.add(checkBox,1,2);

        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        // Do some validation (using the Java 8 lambda syntax).
        password.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());

        });
        passwordMasked.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());

        });
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue)
                {
                    password.setText(passwordMasked.getText());
                    grid.getChildren().remove(passwordMasked);
                    grid.add(password, 1, 1);
                }
                else
                {
                    passwordMasked.setText(password.getText());
                    grid.getChildren().remove(password);
                    grid.add(passwordMasked, 1, 1);
                }
            }
        });

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                if(checkBox.isSelected())
                    return password.getText();
                else
                    return passwordMasked.getText();
            }
            return null;
        });
        return dialog;
    }

    class ChangeConfig extends Thread
    {
        private String psk;
        public ChangeConfig(String psk) {
            this.psk=psk;
        }

        @Override
        public void run() {

            wifi.changeConfig(psk, maskerPane);
            maskerPane.setVisible(false);
            checkConection(false,true);

        }
    }
}

