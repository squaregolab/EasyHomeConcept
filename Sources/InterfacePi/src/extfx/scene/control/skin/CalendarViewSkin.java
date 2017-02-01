/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013, Christian Schudt
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package extfx.scene.control.skin;

import extfx.animation.*;
import extfx.scene.control.CalendarView;
import extfx.scene.control.DateCell;
import extfx.util.ClickRepeater;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * The default skin for the {@linkplain extfx.scene.control.CalendarView} control.
 *
 * @author Christian Schudt
 */
public final class CalendarViewSkin extends StackPane implements Skin<CalendarView> {

    ///////////////////
    // CSS DEFINITIONS
    ///////////////////

    private static final String CSS_CALENDAR_YEAR_VIEW = "year-view";
    private static final String CSS_CALENDAR_MONTH_VIEW = "month-view";
    private static final String CSS_CALENDAR_DECADES_VIEW = "decades-view";
    private static final String CSS_CALENDAR_HEADER = "header";
    private static final String CSS_CALENDAR_FOOTER = "footer";
    private static final String CSS_CALENDAR_WEEKDAYS = "weekdays";
    private static final String CSS_CALENDAR_WEEK_NUMBER = "week-number";
    private static final String CSS_CALENDAR_NAVIGATION_ARROW = "navigation-arrow";
    private static final String CSS_CALENDAR_NAVIGATION_BUTTON = "navigation-button";
    private static final String CSS_CALENDAR_NAVIGATION_TITLE = "navigation-title";
    private static final String CSS_CALENDAR_YEAR_VIEW_BUTTON = "year-view-button";
    private static final String CSS_CALENDAR_DECADES_VIEW_BUTTON = "decades-view-button";

    ///////////////////
    // INTERNALS
    ///////////////////
    private final ObjectProperty<Date> normalizedMinDate = new SimpleObjectProperty<Date>();
    private final ObjectProperty<Date> normalizedMaxDate = new SimpleObjectProperty<Date>();
    private final ObjectProperty<View> currentlyViewing = new SimpleObjectProperty<View>(View.MONTH);

    /**
     * Counts the current transitions. As long as an animation is going, the panels should not move left and right.
     */
    private int ongoingTransitions;

    ///////////////////
    // NODES
    ///////////////////
    private final Button titleButton;
    private final CalendarView calendarView;
    private final StackPane contentPane;

    ///////////////////
    // LISTENERS
    ///////////////////

    private final ChangeListener<Date> minDateListener;
    private final ChangeListener<Date> maxDateListener;

    /**
     * Initializes the calendar view skin.
     *
     * @param control The control.
     */
    public CalendarViewSkin(final CalendarView control) {
        this.calendarView = control;

        // For each view, create two views, because two view are necessary for the horizontal animation.
        final AnimatedStackPane monthView = new AnimatedStackPane(new MonthView(), new MonthView());
        final AnimatedStackPane yearView = new AnimatedStackPane(new YearView(), new YearView());
        final AnimatedStackPane decadesView = new AnimatedStackPane(new DecadesView(), new DecadesView());

        contentPane = new StackPane();
        contentPane.getChildren().addAll(monthView, yearView, decadesView);

        yearView.setVisible(false);
        decadesView.setVisible(false);

        // When the view changes, update the title and show the new view and hide the old view.
        currentlyViewing.addListener(new ChangeListener<View>() {
            @Override
            public void changed(ObservableValue<? extends View> observableValue, View view, View view2) {
                switch (view) {
                    case MONTH:
                        switch (view2) {
                            // Switch from month to year.
                            case YEAR:
                                showOrHide(yearView, true);
                                break;
                        }
                        break;
                    case YEAR:
                        switch (view2) {
                            // Switch from year to month.
                            case MONTH:
                                showOrHide(yearView, false);
                                break;
                            // Switch from decades to month.
                            case DECADES:
                                showOrHide(decadesView, true);
                                break;
                        }
                        break;
                    case DECADES:
                        switch (view2) {
                            // Switch from decades to year.
                            case YEAR:
                                showOrHide(decadesView, false);
                                break;

                        }
                        break;
                }
            }
        });

        // Make the calendar grow vertically
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        BorderPane mainNavigationPane = new BorderPane();
        titleButton = new Button();
        BorderPane.setMargin(titleButton, new Insets(0, 5, 0, 5));
        titleButton.getStyleClass().add(CSS_CALENDAR_NAVIGATION_TITLE);
        titleButton.setMaxWidth(Double.MAX_VALUE);
        titleButton.textProperty().bind(new StringBinding() {
            {
                super.bind(currentlyViewing, monthView.secondPane.title, yearView.secondPane.title, decadesView.secondPane.title);
            }

            @Override
            protected String computeValue() {
                switch (currentlyViewing.get()) {
                    case DECADES:
                        return decadesView.secondPane.title.get();
                    case YEAR:
                        return yearView.secondPane.title.get();
                    default:
                        return monthView.secondPane.title.get();
                }
            }
        });

        titleButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                switch (currentlyViewing.get()) {
                    case MONTH:
                        currentlyViewing.set(View.YEAR);
                        break;
                    case YEAR:
                        currentlyViewing.set(View.DECADES);
                }
            }
        });
        // Disable the button if the decades view is shown.
        titleButton.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(currentlyViewing);
            }

            @Override
            protected boolean computeValue() {
                return currentlyViewing.get() == View.DECADES;
            }
        });

        mainNavigationPane.setLeft(getNavigationButton(-1));
        mainNavigationPane.setCenter(titleButton);
        mainNavigationPane.setRight(getNavigationButton(1));

        mainNavigationPane.getStyleClass().add(CSS_CALENDAR_HEADER);
        HBox footer = new HBox();
        footer.getStyleClass().add(CSS_CALENDAR_FOOTER);
        footer.setAlignment(Pos.CENTER);


        // The VBox which holds the main navigation pane.
        VBox mainVBox = new VBox();
        mainVBox.getChildren().addAll(mainNavigationPane, contentPane);
        getChildren().add(mainVBox);

        mainVBox.getChildren().add(footer);

        minDateListener = new ChangeListener<Date>() {
            @Override
            public void changed(ObservableValue<? extends Date> observableValue, Date date, Date date1) {
                normalizedMinDate.set(normalizeDate(date1, false));
            }
        };
        control.minDateProperty().addListener(new WeakChangeListener<Date>(minDateListener));

        maxDateListener = new ChangeListener<Date>() {
            @Override
            public void changed(ObservableValue<? extends Date> observableValue, Date date, Date date1) {
                normalizedMaxDate.set(normalizeDate(date1, true));
            }
        };
        control.maxDateProperty().addListener(new WeakChangeListener<Date>(maxDateListener));

        normalizedMinDate.set(normalizeDate(calendarView.minDateProperty().get(), false));
        normalizedMaxDate.set(normalizeDate(calendarView.maxDateProperty().get(), true));
    }

    /**
     * Shows or hides the pane with an animation.
     *
     * @param stackPane The StackPane, which is shown or hidden.
     * @param show      True, when shown, false when hidden.
     */
    private void showOrHide(final AnimatedStackPane stackPane, final boolean show) {
        stackPane.setVisible(true);

        ongoingTransitions++;
        TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(.5), stackPane);
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(.5), stackPane);
        stackPane.setCache(true);
        stackPane.setCacheHint(CacheHint.SPEED);
        contentPane.setClip(new Rectangle(stackPane.getBoundsInLocal().getWidth(), stackPane.getBoundsInLocal().getHeight()));

        if (show) {
            translateTransition.setFromY(-stackPane.getBoundsInLocal().getHeight());
            translateTransition.setToY(0);
            fadeTransition.setToValue(1);
            fadeTransition.setFromValue(0);
            translateTransition.setInterpolator(new CircularInterpolator(EasingMode.EASE_OUT));
            fadeTransition.setInterpolator(new CircularInterpolator(EasingMode.EASE_OUT));

        } else {
            translateTransition.setToY(-stackPane.getBoundsInLocal().getHeight());
            translateTransition.setFromY(0);
            fadeTransition.setToValue(0);
            fadeTransition.setFromValue(1);
            translateTransition.setInterpolator(new CircularInterpolator(EasingMode.EASE_OUT));
            fadeTransition.setInterpolator(new CircularInterpolator(EasingMode.EASE_OUT));
        }

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().add(translateTransition);
        parallelTransition.getChildren().add(fadeTransition);

        parallelTransition.playFromStart();
        parallelTransition.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                if (!show) {
                    titleButton.requestFocus();
                    stackPane.setVisible(false);
                }
                stackPane.setCache(false);
                ongoingTransitions--;
            }
        });
    }

    /**
     * Gets a navigation button.
     *
     * @param direction Either -1 (for left) or 1 (for right).
     * @return The button.
     */
    private Button getNavigationButton(final int direction) {

        Button button = new Button();
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                final Calendar calendar = calendarView.getCalendar();
                switch (currentlyViewing.get()) {
                    case MONTH:
                        calendar.add(Calendar.MONTH, direction);
                        break;
                    case YEAR:
                        calendar.add(Calendar.YEAR, direction);
                        break;
                    case DECADES:
                        calendar.add(Calendar.YEAR, 20 * direction);
                        break;
                }
                calendarView.setViewedDate(calendar.getTime());
            }
        });

        button.disableProperty().bind(new BooleanBinding() {
            {
                super.bind(currentlyViewing, calendarView.viewedDateProperty(), normalizedMinDate, normalizedMaxDate);
            }

            @Override
            protected boolean computeValue() {
                final Calendar calendar = (Calendar) calendarView.getCalendar().clone();

                switch (currentlyViewing.get()) {
                    case MONTH:
                        calendar.add(Calendar.MONTH, direction);
                        if (direction < 0) {
                            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
                        } else {
                            calendar.set(Calendar.DATE, 0);
                        }
                        break;
                    case YEAR:
                        calendar.add(Calendar.YEAR, direction);
                        if (direction < 0) {
                            calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
                        } else {
                            calendar.set(Calendar.MONTH, 0);
                            calendar.set(Calendar.DATE, 0);
                        }
                        break;
                    case DECADES:
                        calendar.add(Calendar.YEAR, 20 * direction);
                        if (direction < 0) {
                            calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                            calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
                        } else {
                            calendar.set(Calendar.MONTH, 0);
                            calendar.set(Calendar.DATE, 0);
                        }
                        break;
                }

                return direction < 0 && normalizedMinDate.get() != null && normalizedMinDate.get().after(normalizeDate(calendar.getTime(), false))
                        || direction > 0 && normalizedMaxDate.get() != null && normalizedMaxDate.get().before(normalizeDate(calendar.getTime(), true));

            }
        });

        ClickRepeater.install(button);

        // Make a region, so that -fx-shape can be applied from CSS.
        Region rectangle = new Region();
        rectangle.setMaxWidth(Control.USE_PREF_SIZE);
        rectangle.setMaxHeight(Control.USE_PREF_SIZE);
        rectangle.setRotate(direction < 0 ? 90 : 270);
        rectangle.getStyleClass().add(CSS_CALENDAR_NAVIGATION_ARROW);
        // Set that region as the button graphic.
        button.setGraphic(rectangle);
        button.getStyleClass().add(CSS_CALENDAR_NAVIGATION_BUTTON);
        return button;
    }

    /**
     * Normalizes the min or max date, either by setting the hour, minutes, seconds and milliseconds to zero or the maximal value.
     *
     * @param date The date.
     * @param up   True for the max date, false for the min date
     * @return The normalized date.
     */
    private Date normalizeDate(Date date, boolean up) {
        if (date != null) {
            Calendar calendar = (Calendar) getSkinnable().getCalendar().clone();
            calendar.setTime(date);
            if (up) {
                calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMaximum(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, calendar.getActualMaximum(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, calendar.getActualMaximum(Calendar.SECOND));
                calendar.set(Calendar.MILLISECOND, calendar.getActualMaximum(Calendar.MILLISECOND));
                return calendar.getTime();
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar.getTime();
            }
        }
        return null;
    }

    @Override
    public CalendarView getSkinnable() {
        return calendarView;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void dispose() {
    }

    private enum View {
        MONTH,
        YEAR,
        DECADES
    }

    /**
     * The class manages and animates two {@linkplain DatePane CalendarDatePanes}, which are necessary for the animation.
     */
    private final class AnimatedStackPane extends StackPane {

        private static final double SLIDE_ANIMATION_DURATION = 0.7;

        private final DatePane firstPane;

        private final DatePane secondPane;

        private final ParallelTransition slideTransition;
        private final ChangeListener<Date> viewedDateChangeListener;

        private AnimatedStackPane(final DatePane firstPane, final DatePane secondPane) {

            viewedDateChangeListener = new ChangeListener<Date>() {
                @Override
                public void changed(ObservableValue<? extends Date> observableValue, Date oldDate, Date newDate) {

                    Calendar calendar = calendarView.getCalendar();

                    calendar.setTime(oldDate);
                    int oldYear = calendar.get(Calendar.YEAR);
                    int oldMonth = calendar.get(Calendar.MONTH);

                    calendar.setTime(newDate);
                    int newYear = calendar.get(Calendar.YEAR);
                    int newMonth = calendar.get(Calendar.MONTH);

                    // move the panes, if necessary.
                    if (getWidth() > 0 && ongoingTransitions == 0) {
                        int direction = oldDate.after(newDate) ? 1 : -1;
                        if (newYear > oldYear || newYear == oldYear && newMonth > oldMonth) {
                            slide(direction, oldDate, newDate);
                        } else if (newYear < oldYear || newYear == oldYear && newMonth < oldMonth) {
                            slide(direction, oldDate, newDate);
                        }
                    }
                }
            };

            // The first pane, which displays the old date.
            this.firstPane = firstPane;

            // The second pane, which displays the new date.
            this.secondPane = secondPane;

            // Set the first invisible as long as it is not needed.
            firstPane.setVisible(false);

            slideTransition = new ParallelTransition();

            getChildren().add(firstPane);
            getChildren().add(secondPane);

            layoutBoundsProperty().addListener(new ChangeListener<Bounds>() {
                @Override
                public void changed(ObservableValue<? extends Bounds> observableValue, Bounds bounds, Bounds bounds1) {
                    setClip(new Rectangle(getLayoutBounds().getWidth(), getLayoutBounds().getHeight()));
                }
            });

            // Listen to changes of the calendar date, if it changes, check if the new date has another month and move the panes accordingly.
            calendarView.viewedDateProperty().addListener(new WeakChangeListener<Date>(viewedDateChangeListener));
        }

        /**
         * Slides the panes from left to right or vice versa.
         *
         * @param direction The direction, either 1 (moves to right) or -1 (moves to left).
         * @param oldDate   The old date, which the {@link #firstPane} gets set to.
         */
        private void slide(int direction, Date oldDate, Date newDate) {

            // Stop any previous animation.
            slideTransition.stop();

            firstPane.setCache(true);
            secondPane.setCache(true);

            TranslateTransition transition1 = new TranslateTransition(Duration.seconds(SLIDE_ANIMATION_DURATION), firstPane);
            TranslateTransition transition2 = new TranslateTransition(Duration.seconds(SLIDE_ANIMATION_DURATION), secondPane);

            Interpolator interpolator = new QuadraticInterpolator(EasingMode.EASE_OUT);
            transition1.setInterpolator(interpolator);
            transition2.setInterpolator(interpolator);

            // Make the first pane visible.
            firstPane.setVisible(true);

            // Set the old date to the first pane.
            firstPane.setDate(oldDate);

             secondPane.setDate(newDate);

            // Set the clip, so that the translate transition stays within the clip.
            //setClip(new Rectangle(getLayoutBounds().getWidth(), getLayoutBounds().getHeight()));

            // Move the first pane away from 0. (I added 1px, so that both panes overlap, which makes it look a little smoother).
            transition1.setFromX(-direction);
            // and either to right or to left.
            transition1.setToX(getLayoutBounds().getWidth() * direction - direction);

            // Move the second pane from left or right
            transition2.setFromX(-getLayoutBounds().getWidth() * direction - direction);

            // Move the second pane to 0
            transition2.setToX(direction);

            slideTransition.getChildren().addAll(transition1, transition2);

            slideTransition.playFromStart();
            slideTransition.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    // When we are finished, set the first pane to invisible and remove the clip.
                    firstPane.setVisible(false);
                    firstPane.setCache(false);
                    secondPane.setCache(false);
                }
            });
        }
    }

    /**
     * Responsible for displaying the days of a month.
     */
    private final class MonthView extends DatePane {

        /**
         * The number of days per week.
         * I don't know if there is a culture with more or less than seven days per week, but theoretically {@link Calendar} allows it.
         * This variable will correspond to the number of columns.
         */
        private int numberOfDaysPerWeek;

        private final InvalidationListener localeInvalidationListener;

        private final InvalidationListener showWeeksInvalidationListener;

        /**
         * Constructs the month view.
         */
        private MonthView() {

            getStyleClass().add(CSS_CALENDAR_MONTH_VIEW);
            localeInvalidationListener = new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    updateCells();
                }
            };
            showWeeksInvalidationListener = new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    getChildren().clear();
                    createCells();
                    updateCells();
                }
            };
            // When the locale changed, update the weeks to the new locale.
            calendarView.localeProperty().addListener(new WeakInvalidationListener(localeInvalidationListener));

            // When the disabled dates change, update the days.
            calendarView.showWeeksProperty().addListener(new WeakInvalidationListener(showWeeksInvalidationListener));
        }

        @Override
        public void createCells() {
            Calendar calendar = calendarView.calendarProperty().get();

            // Get the maximum number of days in a week.
            numberOfDaysPerWeek = calendar.getMaximum(Calendar.DAY_OF_WEEK);

            // Get the maximum number of days a month could have.
            int maxNumberOfDaysInMonth = calendar.getMaximum(Calendar.DAY_OF_MONTH);

            // Assume the first row has only 1 day, then distribute the rest among the remaining weeks and add the first week.
            int numberOfRows = (int) Math.ceil((maxNumberOfDaysInMonth - 1) / (double) numberOfDaysPerWeek) + 1;

            // Remove all controls
            getChildren().clear();

            int colOffset = calendarView.getShowWeeks() ? 1 : 0;

            // If we show weeks, add an extra column to the grid pane.
            if (calendarView.getShowWeeks()) {
                Label empty = new Label();
                empty.setMaxWidth(Double.MAX_VALUE);
                empty.getStyleClass().add(CSS_CALENDAR_WEEKDAYS);
                add(empty, 0, 0);
            }

            // For each week day add a label of the week.
            for (int i = 0; i < numberOfDaysPerWeek; i++) {
                Label label = new Label();
                label.getStyleClass().add(CSS_CALENDAR_WEEKDAYS);
                label.setMaxWidth(Double.MAX_VALUE);
                label.setAlignment(Pos.CENTER);
                add(label, i + colOffset, 0);
            }

            // Iterate through the rows
            for (int rowIndex = 0; rowIndex < numberOfRows; rowIndex++) {

                // If we show weeks, show them left
                if (calendarView.getShowWeeks()) {
                    Label label = new Label();
                    label.setMaxWidth(Double.MAX_VALUE);
                    label.setMaxHeight(Double.MAX_VALUE);
                    label.getStyleClass().add(CSS_CALENDAR_WEEK_NUMBER);
                    add(label, 0, rowIndex + 1);
                }

                // For each week day
                for (int colIndex = 0; colIndex < numberOfDaysPerWeek; colIndex++) {
                    final DateCell cell;
                    if (calendarView.getDayCellFactory() != null) {
                        cell = calendarView.getDayCellFactory().call(calendarView);
                    } else {
                        cell = new DateCell();
                    }

                    cell.updateCalendarView(calendarView);

                    GridPane.setVgrow(cell, Priority.ALWAYS);
                    GridPane.setHgrow(cell, Priority.ALWAYS);

                    // add the button, starting at second row.
                    add(cell, colIndex + colOffset, rowIndex + 1);
                }
            }
        }

        /**
         * Updates the days.
         */
        private void updateDays() {
            Calendar calendar = (Calendar) calendarView.getCalendar().clone();

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Set the calendar to the first day of the current month.
            calendar.set(Calendar.DAY_OF_MONTH, 1);

            int month = calendar.get(Calendar.MONTH);

            // Set the calendar to the end of the previous month.
            while (calendar.getFirstDayOfWeek() != calendar.get(Calendar.DAY_OF_WEEK)) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            }

            boolean firstLoop = true;

            // Ignore the week day row and the week number column
            for (int i = numberOfDaysPerWeek + (calendarView.getShowWeeks() ? 1 : 0); i < getChildren().size(); i++) {

                // If we are in the first column and if we want to show the weeks, set them.
                if (i % (numberOfDaysPerWeek + 1) == 0 && calendarView.getShowWeeks()) {
                    Label label = (Label) getChildren().get(i);
                    label.setText(Integer.toString(calendar.get(Calendar.WEEK_OF_YEAR)));
                } else {

                    DateCell dateCell = (DateCell) getChildren().get(i);

                    boolean disabled = normalizedMaxDate.get() != null && calendar.getTime().after(normalizedMaxDate.get())
                            || normalizedMinDate.get() != null && calendar.getTime().before(normalizedMinDate.get());

                    dateCell.setDisable(disabled);

                    dateCell.updateInRange(calendar.get(Calendar.MONTH) == month);
                    dateCell.setItem(calendar.getTime());
                    dateCell.requestLayout();
                    if (firstLoop) {
                        firstLoop = false;
                    }

                    calendar.add(Calendar.DATE, 1);
                }
            }
        }

        /**
         * Updates the week names, when the locale changed.
         */
        private void updateWeekNames() {
            DateFormatSymbols dateFormatSymbols;
            if (calendarView.localeProperty().get() != null) {
                dateFormatSymbols = new DateFormatSymbols(calendarView.localeProperty().get());
            }
            else {
                dateFormatSymbols = new DateFormatSymbols();
            }

            String[] weekDays = dateFormatSymbols.getShortWeekdays();

            // Start with 1 instead of 0, since the first element in the array is empty.
            for (int i = 1; i < weekDays.length; i++) {
                // Get the first two characters only.
                String shortWeekDay = weekDays[i].substring(0, Math.min(weekDays[i].length(), 2));

                // Shift the index according to the first day of week.
                int j = i - calendarView.getCalendar().getFirstDayOfWeek();
                if (j < 0) {
                    j += weekDays.length - 1;
                }

                Label label = (Label) getChildren().get(j + (calendarView.getShowWeeks() ? 1 : 0));

                label.setText(shortWeekDay);
            }
            title.set(getDateFormat("MMMM yyyy").format(calendarView.getCalendar().getTime()));
        }

        @Override
        protected void updateCells() {
            updateDays();
            updateWeekNames();
        }
    }

    /**
     * The year view shows the months of one year.
     */
    private final class YearView extends DatePane {

        private final InvalidationListener localeInvalidationListener;

        private YearView() {

            localeInvalidationListener = new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    updateCells();
                }
            };
            getStyleClass().add(CSS_CALENDAR_YEAR_VIEW);
            // When the locale changes, update the contents (month names).
            calendarView.localeProperty().addListener(new WeakInvalidationListener(localeInvalidationListener));
        }

        @Override
        protected void createCells() {

            // Get the number of months. I read, there are some lunar calendars, with more than 12 months.
            Calendar calendar = getSkinnable().getCalendar();
            int numberOfMonths = calendar.getMaximum(Calendar.MONTH) + 1;

            // Set a fix columns of 3.
            int numberOfColumns = 3;

            for (int i = 0; i < numberOfMonths; i++) {
                final int j = i;
                final Button button;

                button = new Button();
                button.getStyleClass().add(CSS_CALENDAR_YEAR_VIEW_BUTTON);
                button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                // Make the button stretch.
                GridPane.setVgrow(button, Priority.ALWAYS);
                GridPane.setHgrow(button, Priority.ALWAYS);

                button.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        if (currentlyViewing.get() == View.YEAR) {
                            calendarView.getCalendar().set(Calendar.MONTH, j);
                            currentlyViewing.set(View.MONTH);
                            calendarView.viewedDateProperty().set(calendarView.getCalendar().getTime());
                        }
                        actionEvent.consume();
                    }
                });
                int rowIndex = i % numberOfColumns;
                int colIndex = (i - rowIndex) / numberOfColumns;
                add(button, rowIndex, colIndex);
            }
        }

        @Override
        protected void updateCells() {
            Calendar calendar = (Calendar) calendarView.getCalendar().clone();

            // For each cell, update it with the corresponding date for each month.
            for (int i = 0; i < getChildren().size(); i++) {
                Button button = (Button) getChildren().get(i);
                // Check if there are more cells, than months. (which should never happen in practice).
                if (i <= calendar.getActualMaximum(Calendar.MONTH)) {
                    calendar.set(Calendar.MONTH, i);
                    calendar.set(Calendar.DATE, 1);
                    boolean disabled = normalizedMaxDate.get() != null && calendar.getTime().after(normalizedMaxDate.get());
                    calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
                    disabled = disabled || normalizedMinDate.get() != null && calendar.getTime().before(normalizedMinDate.get());

                    button.setDisable(disabled);

                    DateFormatSymbols symbols;
                    if (calendarView.localeProperty().get() != null) {
                       symbols = new DateFormatSymbols(calendarView.localeProperty().get());
                    }
                    else {
                        symbols = new DateFormatSymbols();
                    }

                    String[] monthNames = symbols.getShortMonths();
                    calendar.setTime(calendar.getTime());
                    int month = calendar.get(Calendar.MONTH);
                    button.setText(monthNames[month]);
                }
            }
            title.set(getDateFormat("yyyy").format(calendarView.getCalendar().getTime()));
        }
    }

    /**
     * Shows the years of several decades.
     */
    private final class DecadesView extends DatePane {

        /**
         * Define a fix number of 2 decades.
         */
        private static final int NUMBER_OF_DECADES = 2;

        private DecadesView() {
            getStyleClass().add(CSS_CALENDAR_DECADES_VIEW);
        }

        @Override
        protected void createCells() {

            final Calendar calendar = calendarView.getCalendar();

            // For each year in the decade, add a button to the view.
            for (int i = 0; i < NUMBER_OF_DECADES * 10; i++) {

                final Button button = new Button();
                button.getStyleClass().add(CSS_CALENDAR_DECADES_VIEW_BUTTON);
                button.setMaxWidth(Double.MAX_VALUE);
                button.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(button, Priority.ALWAYS);
                GridPane.setHgrow(button, Priority.ALWAYS);

                button.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        if (currentlyViewing.get() == View.DECADES) {
                            calendar.set(Calendar.YEAR, (Integer) button.getUserData());
                            currentlyViewing.set(View.YEAR);
                            calendarView.viewedDateProperty().set(calendar.getTime());
                        }
                    }
                });
                int rowIndex = i % 5;
                int colIndex = (i - rowIndex) / 5;

                add(button, rowIndex, colIndex);
            }
        }

        @Override
        protected void updateCells() {
            final Calendar calendar = (Calendar) calendarView.getCalendar().clone();

            int year = calendar.get(Calendar.YEAR);
            // Get the beginning of the decade.
            int a = year % 10;
            if (a < 5) {
                a += 10;
            }
            int startYear = year - a;
            for (int i = 0; i < 10 * NUMBER_OF_DECADES; i++) {
                final int y = i + startYear;
                calendar.set(Calendar.YEAR, y);
                calendar.set(Calendar.MONTH, 0);
                calendar.set(Calendar.DATE, 1);
                Button button = (Button) getChildren().get(i);

                boolean disabled = normalizedMaxDate.get() != null && calendar.getTime().after(normalizedMaxDate.get());
                calendar.set(Calendar.MONTH, calendar.getActualMaximum(Calendar.MONTH));
                calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));
                disabled = disabled || normalizedMinDate.get() != null && calendar.getTime().before(normalizedMinDate.get());

                button.setDisable(disabled);
                button.setText(Integer.toString(y));
                button.setUserData(y);
            }
            title.set(String.format("%s - %s", startYear, startYear + 10 * NUMBER_OF_DECADES - 1));
        }
    }

    /**
     * Abstract base class for the {@link MonthView}, {@link YearView} and {@link DecadesView}.
     */
    private abstract class DatePane extends GridPane {

        protected StringProperty title = new SimpleStringProperty();

        private final InvalidationListener viewedDateInvalidationListener;
        private final InvalidationListener calendarInvalidationListener;

        /**
         * Sets basic stuff
         */
        private DatePane() {
            viewedDateInvalidationListener = new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    updateCells();
                }
            };
            // When the date changed, update the days.
            calendarView.viewedDateProperty().addListener(new WeakInvalidationListener(viewedDateInvalidationListener));

            calendarInvalidationListener = new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    getChildren().clear();
                    createCells();
                    updateCells();
                }
            };
            // Every time the calendar changed, rebuild the pane and update the content.
            calendarView.calendarProperty().addListener(new WeakInvalidationListener(calendarInvalidationListener));

            // When the min date changes, update the cells.
            normalizedMinDate.addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    updateCells();
                }
            });


            // When the max date changes, update the cells.
            normalizedMaxDate.addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    updateCells();
                }
            });

            createCells();
            updateCells();
        }

        /**
         * This is the date, this pane operates on.
         *
         * @param date The date.
         */
        private void setDate(Date date) {
            calendarView.getCalendar().setTime(date);
            updateCells();
            // Restore
            calendarView.getCalendar().setTime(calendarView.viewedDateProperty().get());
        }

        /**
         * Creates the cells.
         */
        protected abstract void createCells();

        /**
         * Updates the cells.
         */
        protected abstract void updateCells();

        /**
         * Gets the date format, associated with the current calendar.
         *
         * @param format The date format as String.
         * @return The date format.
         */
        protected DateFormat getDateFormat(String format) {
            DateFormat dateFormat;
            if (calendarView.localeProperty().get() != null) {
                dateFormat = new SimpleDateFormat(format, calendarView.localeProperty().get());
            }
            else {
                dateFormat = new SimpleDateFormat(format);
            }
            dateFormat.setCalendar(calendarView.getCalendar());
            return dateFormat;
        }
    }
}

