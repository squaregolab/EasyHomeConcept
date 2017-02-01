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

import extfx.animation.BackInterpolator;
import extfx.scene.control.CalendarView;
import extfx.scene.control.DatePicker;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The default skin for the {@linkplain DatePicker} control.
 *
 * @author Christian Schudt
 */
public final class DatePickerSkin extends HBox implements Skin<DatePicker> {

    private final DatePicker datePicker;

    private final TextField textField;

    private boolean textSetProgrammatically;

    private final CalendarView calendarView;

    private final Popup popup;

    private final ObjectProperty<Date> normalizedMinDate = new SimpleObjectProperty<Date>();

    private final ObjectProperty<Date> normalizedMaxDate = new SimpleObjectProperty<Date>();

    private final ChangeListener<Date> changeListenerMinDate;

    private final ChangeListener<Date> changeListenerMaxDate;

    private final InvalidationListener listenerSelectedDate;

    private final ChangeListener<Locale> changeListenerLocale;

    public DatePickerSkin(final DatePicker datePicker) {
        this.datePicker = datePicker;

        Date currentDate = datePicker.getValue();
        if (currentDate != null) {
            calendarView = new CalendarView(datePicker.getLocale(), currentDate);
        } else {
            calendarView = new CalendarView(datePicker.getLocale());
        }

        textField = new TextField();

        calendarView.setEffect(new DropShadow());

        // Bind to the calendar view.
        calendarView.localeProperty().bind(datePicker.localeProperty());
        calendarView.maxDateProperty().bind(datePicker.maxDateProperty());
        calendarView.minDateProperty().bind(datePicker.minDateProperty());
        calendarView.dayCellFactoryProperty().bind(datePicker.dayCellFactoryProperty());
        calendarView.showWeeksProperty().bind(datePicker.showWeeksProperty());

        // Bind the control's properties to the text field.
        textField.minHeightProperty().bind(datePicker.minHeightProperty());
        textField.maxHeightProperty().bind(datePicker.maxHeightProperty());
        textField.editableProperty().bind(datePicker.editableProperty());


        // When the user selects a date in the calendar view, hide it.
        calendarView.selectedDateProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                datePicker.valueProperty().set(calendarView.selectedDateProperty().get());
                hidePopup();
            }
        });


        // Whenever the selected date changes, update the text field.
        listenerSelectedDate = new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                updateTextField();
                datePicker.errorProperty().set(null);

                if (datePicker.valueProperty().get() != null) {
                    calendarView.setViewedDate(datePicker.getValue());
                }
                else {
                    calendarView.setViewedDate(new Date());
                }
            }
        };
        datePicker.valueProperty().addListener(listenerSelectedDate);

        changeListenerLocale = new ChangeListener<Locale>() {
            @Override
            public void changed(ObservableValue<? extends Locale> observableValue, Locale locale, Locale locale1) {
                updateTextField();
            }
        };
        datePicker.localeProperty().addListener(changeListenerLocale);

        textField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.DOWN) {
                    showPopup();
                }
            }
        });

        // Let the prompt text property listen to locale or date format changes.
        textField.promptTextProperty().bind(new StringBinding() {
            {
                super.bind(datePicker.localeProperty(), datePicker.promptTextProperty(), datePicker.dateFormatProperty());
            }

            @Override
            protected String computeValue() {
                // First check, if there is a custom prompt text.
                if (datePicker.promptTextProperty().get() != null && datePicker.promptTextProperty().get().length() > 0) {
                    return datePicker.promptTextProperty().get();
                }

                // If not, use the the date format's pattern.
                DateFormat dateFormat = getActualDateFormat();
                if (dateFormat instanceof SimpleDateFormat) {
                    return ((SimpleDateFormat) dateFormat).toPattern();
                }

                return "";
            }
        });

        // Listen to user input.
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s1) {

                // Only evaluate the input, it it wasn't set programmatically.
                if (textSetProgrammatically) {
                    return;
                }

                // If the user clears the text field, set the date to null and the field to valid.
                if (s1.equals("")) {
                    datePicker.valueProperty().set(null);
                    datePicker.errorProperty().set(null);
                }
            }
        });

        textField.focusedProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (!textField.focusedProperty().get()) {
                    tryParse();
                }
            }
        });

        changeListenerMinDate = new ChangeListener<Date>() {
            @Override
            public void changed(ObservableValue<? extends Date> observableValue, Date date, Date date1) {
                if (date1 != null) {
                    normalizedMinDate.set(normalizeDate(date1, false));
                }
            }
        };

        this.datePicker.minDateProperty().addListener(changeListenerMinDate);

        changeListenerMaxDate = new ChangeListener<Date>() {
            @Override
            public void changed(ObservableValue<? extends Date> observableValue, Date date, Date date1) {
                if (date1 != null) {
                    normalizedMaxDate.set(normalizeDate(date1, true));
                }
            }
        };
        this.datePicker.maxDateProperty().addListener(changeListenerMaxDate);

        normalizedMinDate.set(normalizeDate(this.datePicker.minDateProperty().get(), false));
        normalizedMaxDate.set(normalizeDate(this.datePicker.maxDateProperty().get(), true));

        datePicker.addEventHandler(ComboBoxBase.ON_SHOWING, new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                showPopup();
            }
        });
        datePicker.addEventHandler(ComboBoxBase.ON_HIDDEN, new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                hidePopup();
            }
        });

        updateTextField();

        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("cal.png")));
        imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                datePicker.show();
            }
        });
        imageView.setCursor(Cursor.HAND);

        setSpacing(5);
        imageView.visibleProperty().bind(disabledProperty().not());

        HBox.setHgrow(textField, Priority.ALWAYS);

        getChildren().addAll(textField, imageView);

        popup = new AnimatedPopup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        popup.setAutoFix(true);
        popup.getContent().add(calendarView);

        popup.setOnAutoHide(new EventHandler<Event>() {
            @Override
            public void handle(Event event) {
                if (datePicker.isShowing()) {
                    datePicker.hide();
                }
            }
        });

        //popup.show(getSkinnable(), 0, 0);
        //hidePopup();

        //calendarView.setVisible(false);
    }

    /**
     * Normalizes the min or max date, either by setting the hour, minutes, seconds and milliseconds to zero or the maximal value.
     *
     * @param date The date.
     * @param up   True for the max date, false for the min date.
     * @return The normalized date.
     */
    private Date normalizeDate(Date date, boolean up) {
        if (date != null) {
            java.util.Calendar calendarMaxDate = (java.util.Calendar) getSkinnable().getCalendar().clone();
            calendarMaxDate.setTime(date);
            if (up) {
                calendarMaxDate.set(java.util.Calendar.HOUR_OF_DAY, calendarMaxDate.getActualMaximum(java.util.Calendar.HOUR_OF_DAY));
                calendarMaxDate.set(java.util.Calendar.MINUTE, calendarMaxDate.getActualMaximum(java.util.Calendar.MINUTE));
                calendarMaxDate.set(java.util.Calendar.SECOND, calendarMaxDate.getActualMaximum(java.util.Calendar.SECOND));
                calendarMaxDate.set(java.util.Calendar.MILLISECOND, calendarMaxDate.getActualMaximum(java.util.Calendar.MILLISECOND));
                return calendarMaxDate.getTime();
            } else {
                calendarMaxDate.set(java.util.Calendar.HOUR_OF_DAY, 0);
                calendarMaxDate.set(java.util.Calendar.MINUTE, 0);
                calendarMaxDate.set(java.util.Calendar.SECOND, 0);
                calendarMaxDate.set(java.util.Calendar.MILLISECOND, 0);
                return calendarMaxDate.getTime();
            }
        }
        return null;
    }

    /**
     * Gets the actual date format. If {@link DatePicker#dateFormatProperty()} is set, take it, otherwise get a default format for the current locale.
     *
     * @return The date format.
     */
    private DateFormat getActualDateFormat() {
        if (getSkinnable().dateFormatProperty().get() != null) {
            return getSkinnable().dateFormatProperty().get();
        }

        DateFormat systemFormat = DateFormat.getDateInstance(DateFormat.SHORT, getSkinnable().localeProperty().get());
        DateFormat format;
        if (systemFormat instanceof SimpleDateFormat) {
            format = new SimpleDateFormat(((SimpleDateFormat) systemFormat).toPattern().replace("yy", "yyyy"));
        } else {
            format = systemFormat;
        }
        format.setCalendar(getSkinnable().getCalendar());
        format.setLenient(false);

        return format;
    }

    /**
     * Tries to parse the text field for a valid date.
     */
    private void tryParse() {
        if (textField.getText() != null && textField.getText().length() > 0) {
            try {
                // Double parse the date here, since e.g. 01.01.1 is parsed as year 1, and then formatted as 01.01.01 and then parsed as year 2001.
                // This might lead to an undesired date.
                DateFormat dateFormat = getActualDateFormat();
                Date parsedDate = dateFormat.parse(textField.getText());

                // If the parsed exceeds the min or max date, take the min or max date instead.
                Date actualDate = parsedDate;
                if (normalizedMinDate.get() != null && parsedDate.before(normalizedMinDate.get())) {
                    actualDate = null;
                    datePicker.errorProperty().set(DatePicker.Error.DATE_LESS_THAN_MIN);
                }
                if (normalizedMaxDate.get() != null && parsedDate.after(normalizedMaxDate.get())) {
                    actualDate = null;
                    datePicker.errorProperty().set(DatePicker.Error.DATE_GREATER_THAN_MAX);
                }

                if (datePicker.errorProperty().get() != null) {
                    getSkinnable().valueProperty().set(actualDate);
                    updateTextField();
                } else if (getSkinnable().valueProperty().get() == null || getSkinnable().valueProperty().get() != null && actualDate != null && actualDate.getTime() != getSkinnable().valueProperty().get().getTime()) {
                    getSkinnable().valueProperty().set(actualDate);
                    calendarView.selectedDateProperty().set(actualDate);
                    getSkinnable().errorProperty().set(null);
                    updateTextField();
                }
            } catch (ParseException e) {
                getSkinnable().errorProperty().set(DatePicker.Error.UNPARSABLE);
                getSkinnable().valueProperty().set(null);
                updateTextField();
            }
        } else {
            getSkinnable().errorProperty().set(null);
            getSkinnable().valueProperty().set(null);
            updateTextField();
        }
    }

    /**
     * Updates the text field.
     */
    private void updateTextField() {
        // Mark the we update the text field (and not the user), so that it can be ignored, by textField.textProperty()
        textSetProgrammatically = true;
        if (getSkinnable().valueProperty().get() != null) {
            String date = getActualDateFormat().format(getSkinnable().valueProperty().get());
            if (!textField.getText().equals(date)) {
                textField.setText(date);
            }
        } else {
            textField.setText("");
        }
        textSetProgrammatically = false;
    }

    /**
     * Shows the popup.
     */
    private void showPopup() {

        if (popup.isShowing())
            return;

        calendarView.setVisible(true);
        //calendarView.setManaged(true);

        Bounds calendarBounds = calendarView.getBoundsInLocal();
        Bounds bounds = getSkinnable().localToScene(getSkinnable().getBoundsInLocal());

        double posX = calendarBounds.getMinX() + bounds.getMinX() + getSkinnable().getScene().getX() + getSkinnable().getScene().getWindow().getX();
        double posY = calendarBounds.getMinY() + bounds.getHeight() + bounds.getMinY() + getSkinnable().getScene().getY() + getSkinnable().getScene().getWindow().getY();

        popup.show(getSkinnable(), posX, posY);
    }

    /**
     * Hides the popup.
     */
    private void hidePopup() {
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }
    }

    @Override
    public DatePicker getSkinnable() {
        return datePicker;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void dispose() {
        calendarView.minDateProperty().unbind();
        calendarView.maxDateProperty().unbind();
        calendarView.localeProperty().unbind();
        calendarView.showWeeksProperty().unbind();
        calendarView.dayCellFactoryProperty().unbind();

        datePicker.valueProperty().removeListener(listenerSelectedDate);
        datePicker.localeProperty().removeListener(changeListenerLocale);
        datePicker.minDateProperty().removeListener(changeListenerMinDate);
        datePicker.maxDateProperty().removeListener(changeListenerMaxDate);

        textField.minHeightProperty().unbind();
        textField.maxHeightProperty().unbind();
        textField.alignmentProperty().unbind();
        textField.editableProperty().unbind();
        textField.prefColumnCountProperty().unbind();
        textField.promptTextProperty().unbind();
        textField.onActionProperty().unbind();

    }

    private class AnimatedPopup extends Popup {

        private final FadeTransition hideFadeTransition;

        private final ScaleTransition hideScaleTransition;
        private final FadeTransition showFadeTransition;

        private final ScaleTransition showScaleTransition;


        private AnimatedPopup() {

            showFadeTransition = new FadeTransition(Duration.seconds(0.2), getScene().getRoot());
            showFadeTransition.setFromValue(0);
            showFadeTransition.setToValue(1);
            showFadeTransition.setInterpolator(new BackInterpolator());

            showScaleTransition = new ScaleTransition(Duration.seconds(0.2), getScene().getRoot());
            showScaleTransition.setFromX(0.8);
            showScaleTransition.setFromY(0.8);
            showScaleTransition.setToY(1);
            showScaleTransition.setToX(1);

            showScaleTransition.setInterpolator(new BackInterpolator());

            hideFadeTransition = new FadeTransition(Duration.seconds(.3), getScene().getRoot());
            hideFadeTransition.setFromValue(1);
            hideFadeTransition.setToValue(0);
            hideFadeTransition.setInterpolator(new BackInterpolator());

            hideScaleTransition = new ScaleTransition(Duration.seconds(.3), getScene().getRoot());
            hideScaleTransition.setFromX(1);
            hideScaleTransition.setFromY(1);
            hideScaleTransition.setToY(0.8);
            hideScaleTransition.setToX(0.8);

            hideScaleTransition.setInterpolator(new BackInterpolator());
            hideScaleTransition.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent actionEvent) {
                    if (AnimatedPopup.super.isShowing()) {
                        AnimatedPopup.super.hide();
                    }
                }
            });
        }


        @Override
        public void show() {
            super.show();
            if (showFadeTransition.getStatus() != Animation.Status.RUNNING) {
                showFadeTransition.playFromStart();
                showScaleTransition.playFromStart();
            }
        }

        @Override
        public void hide() {
            if (isShowing()) {
                if (!getOwnerWindow().isShowing()) {
                    hideFadeTransition.stop();
                    hideScaleTransition.stop();
                } else if (hideFadeTransition.getStatus() != Animation.Status.RUNNING) {
                    hideFadeTransition.playFromStart();
                    hideScaleTransition.playFromStart();
                }
            }
        }
    }
}
