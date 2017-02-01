/**
 * Sample Skeleton for 'InterfacePi.fxml' ControllerHome Class
 */

package InterfacePi.Interface.Pieces;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import InterfacePi.Main;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.ToggleSwitch;
import InterfacePi.Communication.ClientS;

public class Controller {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="rightBottom1"
    private VBox rightBottom; // Value injected by FXMLLoader

    @FXML // fx:id="rightAnchor"
    private HBox bottomAnchor; // Value injected by FXMLLoader

    @FXML // fx:id="leftUp"
    private VBox leftUp; // Value injected by FXMLLoader

    @FXML // fx:id="leftBottom"
    private VBox leftBottom; // Value injected by FXMLLoader

    @FXML // fx:id="rigthUp"
    private VBox rigthUp; // Value injected by FXMLLoader

    @FXML // fx:id="validerTempCible"
    private Button validerTempCible; // Value injected by FXMLLoader

    @FXML // fx:id="centerBottom"
    private VBox LightBox; // Value injected by FXMLLoader

    @FXML // fx:id="labelTempCible"
    public Label labelTempCible; // Value injected by FXMLLoader

    @FXML // fx:id="leftAnchor"
    private HBox leftAnchor; // Value injected by FXMLLoader

    @FXML // fx:id="annulerTempCible"
    private Button annulerTempCible; // Value injected by FXMLLoader

    @FXML // fx:id="labelUnitTempCible"
    private Label labelUnitTempCible; // Value injected by FXMLLoader

    @FXML // fx:id="switchLumiere"
    private ToggleSwitch switchLumiere; // Value injected by FXMLLoader

    @FXML // fx:id="sliderTemp"
    private Slider sliderTemp; // Value injected by FXMLLoader

    @FXML // fx:id="switchChauffage"
    private ToggleSwitch switchChauffage; // Value injected by FXMLLoader

    @FXML // fx:id="offChauffage"
    private Label offChauffage; // Value injected by FXMLLoader

    @FXML // fx:id="onChauffage"
    private Label onChauffage; // Value injected by FXMLLoader

    @FXML
    private Button back;

    @FXML
    private Button home;

    @FXML
    private Button ouvrirVolet;

    @FXML
    private Button stopVolet;

    @FXML
    private Button fermerVolet;

    @FXML
    private Label etatVolet;

    @FXML
    private StackPane stack;

    @FXML
    private BorderPane historique;

    @FXML
    private Button historiqueButton;

    @FXML
    private Label romName;

    Gauge tempGauge;
    Gauge humiGauge;
    final private Logger logger = LogManager.getLogger();
    public boolean clickedSlider = false;
    public boolean sliderEnCourDeModif = false;
    public boolean sliderValider =false;
    public ClientS client ;
    public boolean onScreen = true;
    private boolean softChangeLumiere;
    MaskerPane maskerPane;
    private boolean softChangeChauffage;

    @FXML // This method is called by the FXMLLoader when initialization is complete
    void initialize() {

        maskerPane = new MaskerPane();
        maskerPane.setText("Veuillez patienter...");
        stack.getChildren().add(maskerPane);
        maskerPane.setVisible(true);

        //check which room it loading
        if(Main.currentRoom==0)
        {
            //For generalScreen screen
            LightBox.setVisible(false);
            historique.setVisible(true);
            client=Main.clientHashMap.get(Main.generalScreen);
            romName.setText("Général");

        }
        else
        {

            LightBox.setVisible(true);
            historique.setVisible(false);
            client=Main.clientHashMap.get(Main.currentRoom);
            romName.setText(client.name);
        }
        if(client.type==1)
        {
            leftBottom.setDisable(true);
        }


        onScreen=true;

        Image backImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/back.png"),100,100,false,true);
        ImageView backView = new ImageView(backImg);
        Image backDownImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/backDown.png"),100,100,false,true);
        ImageView backDownView = new ImageView(backDownImg);

        Image homeImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/home.png"),100,100,false,true);
        ImageView homeView = new ImageView(homeImg);
        Image homeDownImg = new Image(getClass().getResourceAsStream("/InterfacePi/Interface/Icon/homeDown.png"),100,100,false,true);
        ImageView homeDownView = new ImageView(homeDownImg);

        back.setGraphic(backView);
        home.setGraphic(homeView);

        historiqueButton.setOnMouseClicked((event -> {
            onScreen=false;
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/General/general.fxml"));
                Stage stage = (Stage) home.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }));


        switchLumiere.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                logger.debug("Light switch changed....");
                if(!softChangeLumiere)
                {
                    logger.debug("Manual switch for light");
                    if(!switchLumiere.isSelected())
                    {
                        switchLumiere.setDisable(true);

                        client.send("<c,1,0>");
                        logger.debug("Light switched to off.");
                        logger.debug("Switch State: "+switchLumiere.selectedProperty());
                        maskerPane.setVisible(true);
                    }
                    else
                    {switchLumiere.setDisable(true);

                        client.send("<c,1,1>");
                        logger.debug("Light switched to on.");
                        maskerPane.setVisible(true);
                    }
                }
                else
                {
                    logger.debug("Software switch the light");
                    softChangeLumiere=false;
                }





            }
        });

        softChangeChauffage=false;
        switchChauffage.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!softChangeChauffage)
                {
                    logger.debug("Manual Chauffage switch changed....");
                    if(!switchChauffage.isSelected())
                    {
                        if (Main.currentRoom == 0) {
                            maskerPane.setVisible(true);
                            Main.clientHashMap.forEach((k,v)->{
                                if(v.socket!=null)
                                {
                                    v.send("<c,4,0>");
                                }
                            });
                        }
                        else
                            client.send("<c,4,0>");
                        maskerPane.setVisible(true);
                        sliderEnCourDeModif=false;


                        logger.debug("Chauggage switched to off.");
                        logger.debug("Switch State: "+switchChauffage.selectedProperty());
                    }
                    else
                    {
                        if (Main.currentRoom == 0) {
                            maskerPane.setVisible(true);
                            Main.clientHashMap.forEach((k,v)->{
                                if(v.socket!=null)
                                {
                                    v.send("<c,4,1>");
                                }
                            });
                        }
                        else
                        client.send("<c,4,1>");
                        offChauffage.setTextFill(Color.DARKSLATEGRAY);
                        onChauffage.setTextFill(Color.WHITE);
                        maskerPane.setVisible(true);
                        logger.debug("Chauffage switched to on.");
                    }
                }
                else
                {
                    logger.debug("Software switch the chauffage");
                    softChangeChauffage=false;

                }



            }
        });
        validerTempCible.setDisable(true);
        annulerTempCible.setDisable(true);
        sliderTemp.setOnMousePressed((event) -> {
            clickedSlider=true;
            sliderEnCourDeModif=true;
            validerTempCible.setDisable(false);
            annulerTempCible.setDisable(false);

        });


        sliderTemp.setOnMouseReleased((event ->
        {
            clickedSlider=false;
        }));
        validerTempCible.setOnMouseClicked((event ->
        {
            logger.debug("Validation temp");
            sliderEnCourDeModif = false;
            validerTempCible.setDisable(true);
            annulerTempCible.setDisable(true);
            sliderTemp.setDisable(true);
            sliderValider = true;
            if (Main.currentRoom == 0) {
                maskerPane.setVisible(true);
                Main.clientHashMap.forEach((k,v)->{
                    if(v.socket!=null)
                    {
                        v.send("<c,3,"+Float.parseFloat(labelTempCible.getText())+">");
                    }
                });
            }
            else
                client.send("<c,3,"+Float.parseFloat(labelTempCible.getText())+">");
            maskerPane.setVisible(true);
        }));

        annulerTempCible.setOnMouseClicked((event -> {
            logger.debug("Anulation temp");
            sliderEnCourDeModif = false;

            validerTempCible.setDisable(true);
            annulerTempCible.setDisable(true);
            sliderTemp.setDisable(true);
            maskerPane.setVisible(true);
        }));

        fermerVolet.setOnMouseClicked((event -> {
            if (Main.currentRoom == 0) {
                maskerPane.setVisible(true);
                Main.clientHashMap.forEach((k,v)->{
                    if(v.socket!=null)
                    {
                        v.send("<c,2,1>");
                    }
                });
            }
            else{
                logger.debug("Fermer volet");
                client.send("<c,2,1>");
                maskerPane.setVisible(true);
            }

        }));

        ouvrirVolet.setOnMouseClicked(event -> {
            if (Main.currentRoom == 0) {
                maskerPane.setVisible(true);
                Main.clientHashMap.forEach((k,v)->{
                    if(v.socket!=null)
                    {
                        v.send("<c,2,2>");
                    }
                });
            }
            else
            {
                logger.debug("Ouvrir volet");
                client.send("<c,2,2>");
                maskerPane.setVisible(true);
            }

        });

        stopVolet.setOnMouseClicked(event -> {
            if (Main.currentRoom == 0) {
                maskerPane.setVisible(true);
                Main.clientHashMap.forEach((k,v)->{
                    if(v.socket!=null)
                    {
                        v.send("<c,2,2>");
                    }
                });
            }else {
                logger.debug("Stop volet");
                client.send("<c,2,0>");
                maskerPane.setVisible(true);
            }

        });

        home.setOnMousePressed((event ->
        {
            System.out.println("presed");
            home.setGraphic(homeDownView);
        }));
        home.setOnMouseReleased((event -> {
            onScreen=false;
            home.setGraphic(homeView);
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Home/home.fxml"));
                Stage stage = (Stage) home.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        back.setOnMousePressed((event ->
        {
            back.setGraphic(backDownView);
        }));
        back.setOnMouseReleased((event -> {
            back.setGraphic(backView);
            onScreen=false;

            try {
                Parent root;
                if(Main.currentRoom==0)
                    root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Home/home.fxml"));
                else
                    root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/romList.fxml"));
                Main.currentRoom=-1;
                Stage stage = (Stage) back.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));







        sliderTemp.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(clickedSlider)
                {
                    labelTempCible.setTextFill(Color.RED);
                    labelUnitTempCible.setTextFill(Color.RED);
                    labelTempCible.setText(String.valueOf(newValue.intValue()));
                    validerTempCible.setDisable(false);
                    annulerTempCible.setDisable(false);
                }
            }
        });

        tempGauge = this.formatVBox(leftUp,"Température", Double.valueOf(220),Color.RED);
        humiGauge = this.formatVBox(rigthUp,"Humidité",Double.valueOf(220),Color.AQUA);

        ToggleSwitch toggleSwitch = new ToggleSwitch("test");
        toggleSwitch.resize(100,200);



        ChangeVarListener changeVarListener =new ChangeVarListener();
        changeVarListener.start();






    }



    public Gauge getGauge(String type,double size,Color color)
    {

        Gauge gauge;
        switch (type)
        {
            case "Température":
                gauge = GaugeBuilder.create()
                        .skinType(Gauge.SkinType.SLIM)
                        .prefSize(size, size)
                        .minValue(0)
                        .maxValue(35)
                        .animated(true)
                        .unit("°C")
                        .build();
                gauge.setBarColor(color);
                gauge.setBarBackgroundColor(Color.rgb(39,44,50));
                return gauge;
            case "Humidité":
                 gauge = GaugeBuilder.create()
                         .skinType(Gauge.SkinType.SLIM)
                         .prefSize(size, size)
                         .minValue(0)
                         .maxValue(100)
                         .animated(true)
                         //.animationDuration(500)
                         .title(type)
                         .unit("%")
                         .layoutX(256-size/2)
                         .layoutY(170-size/2)
                         .build();
                 return gauge;
            case "eau":
                gauge = GaugeBuilder.create()
                        .skinType(Gauge.SkinType.SLIM)
                        .prefSize(size, size)
                        .minValue(0)
                        .maxValue(100)
                        .animated(true)
                        //.animationDuration(500)
                        .title(type)
                        .unit("L/h")
                        .layoutX(256-size/2)
                        .layoutY(170-size/2)
                        .build();
                return gauge;
            case "elec":
                break;
        }
        return null;
    }
    public Gauge formatVBox(VBox vBox, String type, Double sizeOfGauge, Color color) {
        Rectangle bar = new Rectangle(sizeOfGauge, 3, color);
        bar.setArcHeight(6);
        bar.setArcWidth(6);

        Label label = new Label(type);
        label.setTextFill(color);
        label.setFont(new Font(20));
        label.setAlignment(Pos.CENTER);
        label.setPadding(new Insets(0,0,10,0));


        vBox.setAlignment(Pos.CENTER);
        vBox.setSpacing(3);

        Gauge gauge = this.getGauge(type,sizeOfGauge,color);
        vBox.getChildren().add(bar);
        vBox.getChildren().add(label);
        vBox.getChildren().add(gauge);
        return gauge;

    }

    class ChangeVarListener extends Thread
    {
        public Logger logger = LogManager.getLogger();
        private int etatPrecedentVolet=10;


        @Override
        public void run() {
            while(onScreen)
            {
                if(client.socket!=null)
                {
                    List<InterfacePi.Communication.Data> data = client.data;
                    if(data.get(0).getData()!=null) {
                        if(!data.get(0).getData().equals("nan"))
                        {
                            if (Float.parseFloat(data.get(0).getData()) != tempGauge.getCurrentValue()) {

                                tempGauge.setValue(Float.parseFloat(data.get(0).getData()));
                            }
                            if (Float.parseFloat(data.get(2).getData()) != humiGauge.getCurrentValue()) {
                                humiGauge.setValue(Float.parseFloat(data.get(2).getData()));
                            }
                        }


                        if (!client.sendCommande && !client.waitAfterCommande) {
                            if (switchLumiere.isDisable()) {
                                switchLumiere.setDisable(false);
                            }
                            if ( maskerPane.isVisible()) {
                                maskerPane.setVisible(false);
                            }
                            if(Main.currentRoom==0)
                            {
                                Platform.runLater(()->{
                                    etatVolet.setText("Controle de tout les volets");
                                    ouvrirVolet.setDisable(false);
                                    fermerVolet.setDisable(false);
                                    stopVolet.setDisable(false);
                                });

                            }
                            else
                            {
                                if (etatPrecedentVolet != Integer.parseInt(data.get(4).getData())) {
                                    Platform.runLater(() ->
                                    {
                                        switch (Integer.parseInt(data.get(4).getData())) {

                                            case 0:
                                                etatVolet.setText("Entre ouvert");
                                                ouvrirVolet.setDisable(false);
                                                fermerVolet.setDisable(false);
                                                stopVolet.setDisable(true);
                                                break;
                                            case 1:
                                                etatVolet.setText("Fermer");
                                                ouvrirVolet.setDisable(false);
                                                fermerVolet.setDisable(true);
                                                stopVolet.setDisable(true);
                                                break;
                                            case 2:
                                                etatVolet.setText("Ouvert");
                                                ouvrirVolet.setDisable(true);
                                                fermerVolet.setDisable(false);
                                                stopVolet.setDisable(true);
                                                break;
                                            case 3:
                                                etatVolet.setText("En montée");
                                                ouvrirVolet.setDisable(true);
                                                fermerVolet.setDisable(false);
                                                stopVolet.setDisable(false);
                                                break;
                                            case 4:
                                                etatVolet.setText("En descente");
                                                ouvrirVolet.setDisable(false);
                                                fermerVolet.setDisable(true);
                                                stopVolet.setDisable(false);
                                                break;
                                        }

                                    });

                                }


                                if (Integer.parseInt(data.get(3).getData()) == 1 && !switchLumiere.isSelected()) {
                                    softChangeLumiere=true;
                                    switchLumiere.setSelected(true);
                                }
                                else if(Integer.parseInt(data.get(3).getData()) == 0 && switchLumiere.isSelected())
                                {
                                    softChangeLumiere=true;
                                    switchLumiere.setSelected(false);
                                }
                            }



                            if (switchChauffage.isDisable()) {
                                switchChauffage.setDisable(false);
                            }
                            if (Integer.parseInt(data.get(6).getData()) == 1&&!switchChauffage.isSelected()) {
                                softChangeChauffage = true;
                                switchChauffage.setSelected(true);


                            }else if(Integer.parseInt(data.get(6).getData()) == 0 && switchChauffage.isSelected()) {
                                softChangeChauffage = true;
                                switchChauffage.setSelected(false);
                                Platform.runLater(() ->
                                {
                                    labelTempCible.setTextFill(Color.DARKSLATEGRAY);
                                    labelUnitTempCible.setTextFill(Color.DARKSLATEGRAY);
                                    validerTempCible.setDisable(true);
                                    annulerTempCible.setDisable(true);
                                    sliderTemp.setDisable(true);
                                    labelTempCible.setText("--");
                                });
                            }
                            if (!sliderEnCourDeModif&&!labelTempCible.getText().equals(data.get(1).getData().replaceAll("(?<=^\\d+)\\.0*$", ""))&&switchChauffage.isSelected()) {
                                Platform.runLater(() ->
                                {
                                    labelTempCible.setTextFill(Color.WHITE);
                                    labelUnitTempCible.setTextFill(Color.WHITE);
                                    validerTempCible.setDisable(true);
                                    annulerTempCible.setDisable(true);
                                    sliderTemp.setDisable(false);
                                    //Math.round(Double.parseDouble(data.get(1).getData())* 1d) / 1d)
                                    labelTempCible.setText(data.get(1).getData().replaceAll("(?<=^\\d+)\\.0*$", ""));
                                    sliderTemp.setValue(Double.parseDouble(data.get(1).getData()));
                                });

                            }
                            if(!switchChauffage.isSelected())
                            {
                                Platform.runLater(() ->
                                {
                                    onChauffage.setTextFill(Color.DARKSLATEGRAY);
                                    offChauffage.setTextFill(Color.WHITE);
                                    labelTempCible.setTextFill(Color.DARKSLATEGRAY);
                                    labelUnitTempCible.setTextFill(Color.DARKSLATEGRAY);
                                    validerTempCible.setDisable(true);
                                    annulerTempCible.setDisable(true);
                                    sliderTemp.setDisable(true);
                                    labelTempCible.setText("--");
                                });
                            }
                            else if(!sliderEnCourDeModif){
                                Platform.runLater(() ->
                                {
                                    labelTempCible.setTextFill(Color.WHITE);
                                    labelUnitTempCible.setTextFill(Color.WHITE);
                                    validerTempCible.setDisable(true);
                                    annulerTempCible.setDisable(true);
                                    sliderTemp.setDisable(false);
                                    //Math.round(Double.parseDouble(data.get(1).getData())* 1d) / 1d)
                                });
                            }
                        }
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        logger.catching(e);
                    }

                }
                else
                {
                    //logger.debug("test");
                    Platform.runLater(() ->
                    {
                        Alert dlg = new Alert(Alert.AlertType.WARNING, "Perte de la connection au module \"" + client.name + "\"...");
                        dlg.setHeaderText("Erreur de communication...");
                        dlg.showAndWait();

                        try {
                            Parent root;
                            if(Main.currentRoom==0)
                                root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Home/home.fxml"));
                            else
                                root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/romList.fxml"));
                            Main.currentRoom=-1;
                            Stage stage = (Stage) back.getScene().getWindow();
                            Scene scene = new Scene(root);
                            stage.setScene(scene);
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    onScreen = false;
                    //Main.currentRoom = -1;




                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    logger.catching(e);
                }


            }
            logger.debug("Thread of screen finish.");
        }
    }

}