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

package extfx.scene.control;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.Cell;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * The default date cell, which is used by the {@link CalendarView}.
 *
 * @author Christian Schudt
 * @see CalendarView
 */
public class DateCell extends Cell<Date> {

    private static final String CSS_CURRENT_MONTH = "current-month";

    private static final String CSS_OTHER_MONTH = "other-month";

    private static final String CSS_SELECTED_DATE = "selected";

    private final ReadOnlyBooleanWrapper inRange = new ReadOnlyBooleanWrapper(this, "inRange");

    private final ReadOnlyObjectWrapper<CalendarView> calendarView = new ReadOnlyObjectWrapper<>(this, "calendarView");

    private final ChangeListener<Date> selectedDateChangeListener;

    public Calendar calendar;

    private boolean itemDirty = false;

    public DateCell() {

        getStyleClass().add("date-cell");
        itemProperty().addListener(new ChangeListener<Date>() {
            @Override
            public void changed(ObservableValue<? extends Date> observableValue, Date date, Date date2) {
                itemDirty = true;
                requestLayout();
            }
        });

        selectedDateChangeListener = new ChangeListener<Date>() {
            @Override
            public void changed(ObservableValue<? extends Date> observableValue, Date date, Date date1) {
                itemDirty = true;
                requestLayout();
            }
        };
    }

    /**
     * Indicates whether the {@linkplain #itemProperty() date} belongs to the current month which is displayed.
     * Some dates can belong to the previous or next month. If this is the case the property is false, otherwise it's true.
     *
     * @return The property.
     * @see #isInRange()
     * @see #updateInRange(boolean)
     */
    public ReadOnlyBooleanProperty inRangeProperty() {
        return inRange.getReadOnlyProperty();
    }

    /**
     * @return True, if the {@linkplain #itemProperty() date} belongs to the current month, otherwise false.
     * @see #inRangeProperty()
     */
    public boolean isInRange() {
        return inRange.get();
    }

    /**
     * Updates the {@link #inRangeProperty()}. This method should only be called by skin implentations.
     *
     * @param inRange True, if the date belongs to the current month.
     */
    public void updateInRange(boolean inRange) {
        this.inRange.set(inRange);
    }

    /**
     * Updates the calendar view. This method should usually only be called by skin implementations.
     *
     * @param calendarView The calendar view.
     */
    public void updateCalendarView(final CalendarView calendarView) {
        this.calendarView.set(calendarView);
        this.calendar = (Calendar) calendarView.getCalendar().clone();
        calendarView.selectedDateProperty().addListener(new WeakChangeListener<Date>(selectedDateChangeListener));
    }

    /**
     * Gets the calendar view.
     *
     * @return The calendar view.
     * @see #getCalendarView()
     */
    public ReadOnlyObjectProperty<CalendarView> calendarViewProperty() {
        return calendarView.getReadOnlyProperty();
    }

    /**
     * Gets the calendar view.
     *
     * @return The calendar view.
     * @see #calendarViewProperty()
     */
    public CalendarView getCalendarView() {
        return calendarView.get();
    }

    @Override
    protected void updateItem(Date item, boolean empty) {
        super.updateItem(item, empty);
        setText(null);
        getStyleClass().remove(CSS_CURRENT_MONTH);
        getStyleClass().remove(CSS_OTHER_MONTH);
        getStyleClass().remove(CSS_SELECTED_DATE);

        if (calendarView.get().getSelectedDate() != null) {
            calendar.setTime(calendarView.get().getSelectedDate());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            boolean isSelected = calendarView.get().selectedDateProperty().get() != null && calendar.getTime().equals(getItem());
            updateSelected(isSelected);
        }

        if (!empty) {
            DateFormat dateFormat = new SimpleDateFormat("d");
            setText(dateFormat.format(item));

            if (isInRange()) {
                getStyleClass().add(CSS_CURRENT_MONTH);
            } else {
                getStyleClass().add(CSS_OTHER_MONTH);
            }
            if (isSelected()) {
                getStyleClass().add(CSS_SELECTED_DATE);
            }
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        if (itemDirty) {
            updateItem(getItem(), getItem() == null);
            itemDirty = false;
        }
    }
}
