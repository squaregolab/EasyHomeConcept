package InterfacePi.Interface.Setting;

/**
 * Created by Seb on 23/01/2017.
 */

import InterfacePi.Communication.SaveManager;
import InterfacePi.Main;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.controlsfx.control.MaskerPane;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;


public class ModuleController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button valider;

    @FXML
    private VBox vbox;

    @FXML
    private HBox HBox;

    @FXML
    private Button addModulle;

    @FXML
    private Button back;

    @FXML
    private Pane pane;

    @FXML
    StackPane stack;

    @FXML
    private ListView<Button> listePieces;
    Logger logger = LogManager.getLogger();
    private boolean edited;
    private  MaskerPane masker;

    @FXML
    void initialize() {

        masker = new MaskerPane();
        masker.setText("Veuillez patienter...");
        masker.setVisible(false);
        stack.getChildren().add(masker);
        edited=false;
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
            if(edited)
            {

                Alert dlg = new Alert(Alert.AlertType.CONFIRMATION, "Un redemarage est nécessaire pour appliquer les modifications.\nVoulez-vous redemarrer maintenant?");
                dlg.setHeaderText("Redemarage");
                dlg.getButtonTypes().setAll(ButtonType.YES,ButtonType.NO);
                Optional<ButtonType> result = dlg.showAndWait();
                if(result.get().equals(ButtonType.YES))
                {
                    try{
                        ProcessBuilder killApp = new ProcessBuilder("sudo", "reboot");
                        Process processKillApp = killApp.start();
                        Thread.sleep(2000);
                        processKillApp.destroy();
                    } catch (InterruptedException e) {
                       logger.catching(e);
                    } catch (IOException e) {
                        logger.catching(e);
                    }
                }
                else {
                    try {
                        Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Setting/setting.fxml"));
                        Stage stage = (Stage) back.getScene().getWindow();
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.show();
                    } catch (IOException e) {
                        logger.catching(e);
                    }
                }
            }
            else
            {
                try {
                    Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Setting/setting.fxml"));
                    Stage stage = (Stage) back.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                } catch (IOException e) {
                    logger.catching(e);
                }
            }

        }));


        ObservableList<Button> items= FXCollections.observableArrayList();
        Main.clientHashMap.forEach((k, v) ->
        {
            Button temp=null;
            temp=new Button(v.name);
            temp.setPrefWidth(725);
            temp.setUserData(k);

            temp.getStylesheets().add("/InterfacePi/Interface/button.css");
            if(v.socket==null)
            {
                temp.setId("red");
            }
            else
            {
                temp.setId("white");
            }


            temp.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    Alert dlg = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment suprimer le module "+Main.clientHashMap.get(((Button)event.getSource()).getUserData()).name);
                    dlg.setHeaderText("Confirmation");
                    dlg.getButtonTypes().setAll(ButtonType.YES,ButtonType.NO);
                    Optional<ButtonType> result = dlg.showAndWait();

                    if(result.get().equals(ButtonType.YES))
                    {
                        edited=true;
                        boolean succes = SaveManager.remove((Integer) ((Button) event.getSource()).getUserData());
                        if(succes)
                        {
                            Alert confirmation = new Alert(Alert.AlertType.INFORMATION,"Module suprimer avec succés!");
                            confirmation.setHeaderText("Succés");
                            masker.setVisible(true);
                            new LoadSave(confirmation).start();
                        }
                        else{
                            Alert error = new Alert(Alert.AlertType.WARNING,"Echec de supresion du module!");
                            error.setHeaderText("Echec");
                            error.show();
                        }
                    }



                }
            });
            items.add(temp);
        });
        listePieces.setItems(items);

        addModulle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                Dialog<DialogResult> dlg = getSettingDialog();
                Optional<DialogResult> result = dlg.showAndWait();
                masker.setVisible(true);
                if(result.isPresent())
                {
                    DialogResult resultC = result.get();
                    new CreateSave(resultC).start();

                }
            }
        });




    }

    private Dialog<DialogResult> getSettingDialog()
    {
        // Create the custom dialog.
        Dialog<DialogResult> dialog = new Dialog<>();
        dialog.setTitle("Info module");
        dialog.setHeaderText("Parametre Module:");

        ButtonType OkButtonType = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(OkButtonType, ButtonType.CANCEL);

        // Create labels and fields.
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));



        TextField id = new TextField();
        id.setPromptText("ID");
        TextField name = new TextField();
        name.setPromptText("Nom");
        CheckBox checkBox = new CheckBox("Controle des volets");
        checkBox.setSelected(true);
        CheckBox general = new CheckBox("Pieces Principal de la Maison");

        grid.add(new Label("ID du module (chiffre):"), 0, 1);
        grid.add(id, 1, 1);
        grid.add(new Label("Nom du module:"), 0, 2);
        grid.add(name, 1, 2);
        grid.add(checkBox,1,3);
        grid.add(general,1,4);

        // Enable/Disable button depending on whether all data was entered.
        Node OkButton = dialog.getDialogPane().lookupButton(OkButtonType);
        OkButton.setDisable(true);

        id.textProperty().addListener((observable, oldValue, newValue) -> {
            //check if it's a number
            if (!newValue.matches("\\d*")) {
                id.setText(newValue.replaceAll("[^\\d]", ""));
            }
            OkButton.setDisable((newValue.trim().isEmpty()) || name.textProperty().isEmpty().get());


        });
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            OkButton.setDisable((newValue.trim().isEmpty()) || id.textProperty().isEmpty().get());

        });


        dialog.getDialogPane().setContent(grid);

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == OkButtonType) {
                int type;
                if(checkBox.isSelected())
                    type=0;
                else type = 1;
                return new DialogResult(name.getText(),Integer.parseInt(id.getText()),type,general.isSelected());
            }
            else
                return null;

        });
        return dialog;
    }


    public class DialogResult{
        String name;
        int id;
        int type;
        int general;

        public DialogResult(String name, int id, int type, boolean general) {
            this.name = name;
            this.id = id;
            this.type = type;
            if(general)
                this.general=1;
            else
                this.general=0;
        }

        public int getId() {
            return id;
        }

        public int getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public int getGeneral() {
            return general;
        }
    }

    class CreateSave extends Thread
    {
        DialogResult result;

        public CreateSave(DialogResult result) {
            this.result = result;
        }

        @Override
        public void run() {
            boolean succes = SaveManager.create(result);
            masker.setVisible(false);
            if(succes)
            {
                SaveManager.load();
                reload();
                Platform.runLater(()->{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,"Creation du module reussi!");
                    alert.setHeaderText("Succès");
                    alert.show();
                });


            }
            else
            {
                Platform.runLater(()->
                {
                    Alert alert = new Alert(Alert.AlertType.WARNING,"Echec de creation du module!");
                    alert.setHeaderText("Erreur");
                    alert.show();
                });

            }
        }
    }

    class LoadSave extends Thread
    {
        Alert toShow;

        public LoadSave(Alert toShow) {
            this.toShow = toShow;
        }

        @Override
        public void run() {
            SaveManager.load();
            masker.setVisible(false);
            if(toShow!=null)
                reload();
                Platform.runLater(()->{
                    toShow.show();
                });

        }
    }



    private void reload()
    {
        ObservableList<Button> items= FXCollections.observableArrayList();
        Main.clientHashMap.forEach((k, v) ->
        {
            Button temp=null;
            temp=new Button(v.name);
            temp.setPrefWidth(725);
            temp.setUserData(k);

            temp.getStylesheets().add("/InterfacePi/Interface/button.css");
            if(v.socket==null)
            {
                temp.setId("red");
            }
            else
            {
                temp.setId("white");
            }


            temp.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {

                    Alert dlg = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment suprimer le module "+Main.clientHashMap.get(((Button)event.getSource()).getUserData()).name);
                    dlg.setHeaderText("Confirmation");
                    dlg.getButtonTypes().setAll(ButtonType.YES,ButtonType.NO);
                    Optional<ButtonType> result = dlg.showAndWait();

                    if(result.get().equals(ButtonType.YES))
                    {
                        edited=true;
                        boolean succes = SaveManager.remove((Integer) ((Button) event.getSource()).getUserData());
                        if(succes)
                        {
                            Alert confirmation = new Alert(Alert.AlertType.INFORMATION,"Module suprimer avec succés!");
                            confirmation.setHeaderText("Succés");
                            masker.setVisible(true);
                            new LoadSave(confirmation).start();
                        }
                        else{
                            Alert error = new Alert(Alert.AlertType.WARNING,"Echec de supresion du module!");
                            error.setHeaderText("Echec");
                            error.show();
                        }
                    }



                }
            });
            items.add(temp);
        });
        Platform.runLater(()->
        {
            listePieces.setSelectionModel(null);
            listePieces.setItems(items);
            listePieces.refresh();
        });
    }






}

