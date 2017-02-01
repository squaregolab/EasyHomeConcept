package InterfacePi.Interface.General;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import InterfacePi.Communication.LoadHistory;
import InterfacePi.Communication.Type.HistoryData;
import InterfacePi.Main;
import extfx.scene.chart.DateAxis;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.controlsfx.control.MaskerPane;

public class GeneralController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private VBox vbox;

    @FXML
    private HBox HBox;

    @FXML
    private ToggleButton heure;

    @FXML
    private ToggleButton jour;

    @FXML
    private Button back;

    @FXML
    private ToggleButton semaine;

    @FXML
    private ToggleButton annee;

    @FXML
    private Pane pane;

    @FXML
    private ListView<?> listePieces;

    @FXML
    private ToggleButton mois;

    @FXML
    private ChoiceBox<Integer> multi;

    @FXML
    private ToggleGroup toggleGroup;

    private LineChart chart;

    @FXML
    private VBox vBox;

    @FXML
    AnchorPane anchor;
    @FXML
    StackPane stack;
    @FXML
    ToggleButton temp;

    @FXML
    ToggleButton humidity;

    @FXML
    ToggleButton elec;

    @FXML
    ToggleButton water;

    Logger logger = LogManager.getLogger();
    private ArrayList<HistoryData> V10H;
    private ArrayList<HistoryData> V12H;
    private ArrayList<HistoryData> V15H;

    private boolean onScreen;




    long HOUR = new Long(3600000L);
    long DAY = new Long(86400000L);
    long WEEK = new Long(604800000L);
    long MONTH = new Long(2678400000L);
    long YEAR = new Long(31557600000L);
    long curentRange = WEEK;
    public MaskerPane maskerPane;
    boolean softChange=false;


    @FXML
    void initialize() {
        maskerPane=new MaskerPane();
        maskerPane.setText("Actualisation...");
        stack.getChildren().add(maskerPane);
        maskerPane.setVisible(true);
        onScreen=true;
        annee.setUserData(1);
        mois.setUserData(2);
        semaine.setUserData(3);
        jour.setUserData(4);
        heure.setUserData(5);
        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
            public void changed(ObservableValue<? extends Toggle> ov, Toggle toggle, Toggle new_toggle) {
                if(new_toggle==null)
                {
                    toggle.setSelected(true);
                }
                else
                {
                    softChange=true;
                    switch ((int)new_toggle.getUserData())
                    {
                        case 1:
                            new Updater(YEAR,  multi.getValue(),chart).start();
                            multi.setItems(FXCollections.observableArrayList(1,2,3,4,5,6));
                            logger.debug("1");
                            break;
                        case 2:
                            new Updater(MONTH,  multi.getValue(),chart).start();
                            multi.setItems(FXCollections.observableArrayList(1,2,3,4,5,6));
                            logger.debug("2");
                            break;
                        case 3:
                            new Updater(WEEK,  multi.getValue(),chart).start();
                            multi.setItems(FXCollections.observableArrayList(1,2,3,4));
                            logger.debug("3");
                            break;
                        case 4:
                            new Updater(DAY,  multi.getValue(),chart).start();
                            multi.setItems(FXCollections.observableArrayList(1,2,3));
                            logger.debug("4");
                            break;
                        case 5:
                            new Updater(HOUR, multi.getValue(),chart).start();
                            multi.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10,12));
                            logger.debug("5");
                            break;
                    }
                    multi.setValue(1);

                }

            }
        });
        multi.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7));
        multi.setValue(1);
        multi.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if(!softChange)
                new Updater(curentRange,newValue,chart).start();
        });

        temp.selectedProperty().addListener(new ChangeListener<Boolean>()
        {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                new Updater(curentRange,  multi.getValue(),chart).start();
            }
        });
        humidity.selectedProperty().addListener(new ChangeListener<Boolean>()
        {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                new Updater(curentRange,  multi.getValue(),chart).start();
            }
        });
        elec.selectedProperty().addListener(new ChangeListener<Boolean>()
        {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                new Updater(curentRange,  multi.getValue(),chart).start();
            }
        });
        water.selectedProperty().addListener(new ChangeListener<Boolean>()
        {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                new Updater(curentRange,  multi.getValue(),chart).start();
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
            onScreen=false;
            try {
                onScreen=false;
                Main.currentRoom=0;
                Parent root = FXMLLoader.load(getClass().getResource("/InterfacePi/Interface/Pieces/sample.fxml"));
                Stage stage = (Stage) back.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                logger.catching(e);
            }
        }));



        new Draw(anchor,DAY,1).start();





    }


    class Draw extends Thread{
        AnchorPane anchorPane;
        long range;
        int multi;

        public Draw(AnchorPane anchorPane, long range, int multi) {
            this.anchorPane = anchorPane;
            this.range = range;
            this.multi = multi;
            maskerPane.setVisible(true);
            curentRange=range;
        }

        @Override
        public void run() {
            V10H=new LoadHistory().getHistory("V10");
            V12H=new LoadHistory().getHistory("V12");
            V15H=new LoadHistory().getHistory("V15");

            ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();

            ObservableList<XYChart.Data<Date, Number>> series1Data = FXCollections.observableArrayList();

            for(HistoryData aData: V10H)
            {

                if(aData.getDate().getTimeInMillis() >=  V10H.get( V10H.size()-1).getDate().getTimeInMillis()-(range*multi))
                {
                    series1Data.add(new XYChart.Data<Date, Number>(aData.getDate().getTime(),aData.getData()));
                }

            }

            ObservableList<XYChart.Data<Date, Number>> series2Data = FXCollections.observableArrayList();

            for(HistoryData aData: V12H)
            {

                if(aData.getDate().getTimeInMillis() >=  V12H.get( V12H.size()-1).getDate().getTimeInMillis()-(range*multi))
                {
                    series2Data.add(new XYChart.Data<Date, Number>(aData.getDate().getTime(),aData.getData()));
                }

            }
            V15H=new LoadHistory().getHistory("V15");
            ObservableList<XYChart.Data<Date, Number>> seriesData = FXCollections.observableArrayList();

            for(HistoryData aData: V15H)
            {

                if(aData.getDate().getTimeInMillis() >=  V15H.get( V15H.size()-1).getDate().getTimeInMillis()-(range*multi))
                {
                    if(aData.getData()<=20)
                    {
                        seriesData.add(new XYChart.Data<Date, Number>(aData.getDate().getTime(),aData.getData()));
                    }

                }

            }




            series.add(new XYChart.Series<>("Temperature", series1Data));
            series.add(new XYChart.Series<>("Humidité",series2Data));
            series.add(new XYChart.Series<>("Consommation eau",seriesData));

            //StringAxis
            NumberAxis numberAxis = new NumberAxis();
            numberAxis.setLowerBound(20.0);
            numberAxis.setForceZeroInRange(false);
            DateAxis dateAxis = new DateAxis();
            chart = new LineChart<>(dateAxis, numberAxis, series);

            chart.setCreateSymbols(false);
            //lineChart.setPrefWidth(1024);
            chart.setPrefSize(850,550);
            chart.setMinSize(850,550);
            chart.setMaxSize(850,550);
            Platform.runLater(()->{
                anchorPane.getChildren().add(chart);
                maskerPane.setVisible(false);

            });
        }
    }
    class Updater extends Thread{
        long range;
        int multi;
        LineChart chart;

        public Updater(long range, int multi, LineChart chartToUpdate) {
            this.range = range;
            this.multi = multi;
            this.chart = chartToUpdate;
            maskerPane.setVisible(true);
            curentRange=range;
        }

        @Override
        public void run() {
            ObservableList<XYChart.Series<Date, Number>> series = FXCollections.observableArrayList();
            if(temp.isSelected())
            {
                V10H=new LoadHistory().getHistory("V10");
                ObservableList<XYChart.Data<Date, Number>> seriesData = FXCollections.observableArrayList();

                for(HistoryData aData: V10H)
                {

                    if(aData.getDate().getTimeInMillis() >=  V10H.get( V10H.size()-1).getDate().getTimeInMillis()-(range*multi))
                    {
                        seriesData.add(new XYChart.Data<Date, Number>(aData.getDate().getTime(),aData.getData()));
                    }

                }
                series.add(new XYChart.Series<>("Temperature", seriesData));
            }
            if(humidity.isSelected())
            {
                V12H=new LoadHistory().getHistory("V12");
                ObservableList<XYChart.Data<Date, Number>> seriesData = FXCollections.observableArrayList();

                for(HistoryData aData: V12H)
                {

                    if(aData.getDate().getTimeInMillis() >=  V12H.get( V12H.size()-1).getDate().getTimeInMillis()-(range*multi))
                    {
                        seriesData.add(new XYChart.Data<Date, Number>(aData.getDate().getTime(),aData.getData()));
                    }

                }
                series.add(new XYChart.Series<>("Humidité",seriesData));
            }

            if(water.isSelected())
            {
                V15H=new LoadHistory().getHistory("V15");
                ObservableList<XYChart.Data<Date, Number>> seriesData = FXCollections.observableArrayList();

                for(HistoryData aData: V15H)
                {

                    if(aData.getDate().getTimeInMillis() >=  V15H.get( V15H.size()-1).getDate().getTimeInMillis()-(range*multi))
                    {
                        if(aData.getData()<=20)
                        {
                            seriesData.add(new XYChart.Data<Date, Number>(aData.getDate().getTime(),aData.getData()));
                        }

                    }

                }
                series.add(new XYChart.Series<>("Consommation eau",seriesData));
            }










            logger.debug("test");
            Platform.runLater(()-> {
                chart.getData().clear();
                chart.getData().addAll(series);
                maskerPane.setVisible(false);
            });
        }
    }


}






