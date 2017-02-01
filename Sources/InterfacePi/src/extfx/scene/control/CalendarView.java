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

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Control;
import javafx.util.Callback;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * The calendar view is a visual representation of the {@link Calendar} class.
 * <p/>
 * Usually it shows the days of one month.<br />
 * You can navigate through months to {@linkplain #selectedDateProperty() select a date}.
 * <p/>
 * <h3>Screenshots</h3>
 * <img src="doc-files/CalendarView.png" alt="CalendarView" />
 * <p/>
 * <h3>Sample Usage</h3>
 * The following sample shows the usage of a cell factory.
 * <pre>
 * <code>
 * CalendarView calendarView = new CalendarView(Locale.ENGLISH);
 * calendarView.setMinDate(new Date());
 * calendarView.setDayCellFactory(new Callback&lt;CalendarView, DateCell&gt;() {
 *    {@literal @}Override
 *    public DateCell call(final CalendarView calendarView) {
 *       DateCell dateCell = new DateCell() {
 *          {@literal @}Override
 *          protected void updateItem(Date item, boolean empty) {
 *             super.updateItem(item, empty);
 *             getStyleClass().remove("sample");
 *             calendar.setTime(item);
 *             if (calendar.get(Calendar.MONTH) == 4 && calendar.get(Calendar.DATE) == 3) {
 *                getStyleClass().addAll("sample");
 *             }
 *          }
 *       };
 *       return dateCell;
 *    }
 * });
 * </code>
 * </pre>
 *
 * @author Christian Schudt
 */
public final class CalendarView extends Control {

    /**
     * This represents the date, which is currently viewed.
     */
    private final ObjectProperty<Date> viewedDate = new SimpleObjectProperty<Date>(this, "viewedDate");

    /**
     * This is the selected date. When this date changes, it also changes the viewed date.
     */
    private final ObjectProperty<Date> selectedDate = new SimpleObjectProperty<>(this, "selectedDate");

    /**
     * This is the locale which is used for getting day names, month names and so on.
     */
    private final ObjectProperty<Locale> locale = new SimpleObjectProperty<Locale>(this, "locale");

    /**
     * The calendar, which is used.
     */
    private final ObjectProperty<Calendar> calendar = new SimpleObjectProperty<Calendar>(this, "calendar");

    /**
     * Indicates whether the weeks numbers are shown.
     */
    private final BooleanProperty showWeeks = new SimpleBooleanProperty(this, "showWeeks", false);

    /**
     * The min date.
     */
    private final ObjectProperty<Date> minDate = new SimpleObjectProperty<Date>(this, "minDate");

    /**
     * The max date.
     */
    private final ObjectProperty<Date> maxDate = new SimpleObjectProperty<Date>(this, "maxDate");

    /**
     * The cell factory for the dates.
     */
    private final ObjectProperty<Callback<CalendarView, DateCell>> dayCellFactory = new SimpleObjectProperty<>(this, "dayCellFactory");


    /**
     * Helper constructor assigning current date as initial date. See called constructor for further details.
     */
    public CalendarView() {
        this(Locale.getDefault(), new Date());
    }

    /**
     * Initializes a calendar with the default locale and the current date as viewedDate.
     * <p/>
     * See called constructor for details.
     */
    public CalendarView(Date viewedDate) {
            this(Locale.getDefault(), viewedDate);
    }

    /**
     * Helper constructor assigning current date as initial date. See called constructor for further details.
     *
     * @param locale The locale.
     */
    public CalendarView(final Locale locale) {
        this(locale, new Date());
    }

    /**
     * Initializes a calendar with the given locale and the given date.
     * E.g. if the locale is en-US, the calendar starts the days on Sunday.
     * If it is de-DE the calendar starts the days on Monday.
     * <p/>
     * Note that the Java implementation only knows {@link java.util.GregorianCalendar} and <code>sun.util.BuddhistCalendar</code>.
     *
     * @param locale The locale.
     */
    public CalendarView(final Locale locale, Date viewedDate) {
        this(locale, Calendar.getInstance(locale), viewedDate);

        // When the locale changes, also change the calendar.
        this.locale.addListener(new ChangeListener<Locale>() {
            @Override
            public void changed(ObservableValue<? extends Locale> observableValue, Locale locale, Locale locale1) {
                if (locale1 != null) {
                    calendar.set(Calendar.getInstance(locale1));
                } else {
                    calendar.set(Calendar.getInstance());
                }
            }
        });
    }

    /**
     * Helper constructor assigning current date as initial date. See called constructor for further details.
     * @param locale The locale.
     * @param calendar The calendar.
     */
    public CalendarView(final Locale locale, final Calendar calendar) {
        this(locale, calendar, new Date());
    }

    /**
     * Initializes the control with the given locale and the given calendar. Main constructor of the class.
     * <p/>
     * This way, you can pass a custom calendar (e.g. you could implement the Hijri CalendarView for the arabic world).
     * Or you can use an American style calendar (starting with Sunday as first day of the week)
     * together with another language.
     * <p/>
     * The locale determines the date format.
     * <p/>
     * The given initial date influences the month that is displayed upon first display of the created view.
     *
     * @param locale   The locale.
     * @param calendar The calendar
     * @param initialViewedDate Initial date for the view.
     */
    public CalendarView(final Locale locale, final Calendar calendar, Date initialViewedDate) {

        this.locale.set(locale);
        this.calendar.set(calendar);

        getStyleClass().add("calendar-view");

        viewedDate.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if (viewedDate.get() != null) {
                    calendar.setTime(viewedDate.get());
                }
            }
        });

        selectedDate.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                viewedDate.set(selectedDate.get());
            }
        });

        viewedDate.set(initialViewedDate);
    }

    /**
     * The locale is used to determine the month and day names and to initialize the default calendar.
     * Note that every time you change the locale the {@link #calendarProperty()} is overwritten with the calendar associated with that locale.
     *
     * @return The locale property.
     * @see #getLocale()
     * @see #setLocale(Locale)
     */
    public ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    /**
     * Gets the locale.
     *
     * @return The locale.
     * @see #localeProperty()
     */
    public Locale getLocale() {
        return locale.get();
    }

    /**
     * Sets the locale.
     *
     * @param locale The locale.
     * @see #localeProperty()
     */
    public void setLocale(Locale locale) {
        this.locale.set(locale);
    }

    /**
     * This property defines the calendar which is used. By default the calendar is chosen by the locale.
     * <p/>
     * However, you might want to use the german calendar, where the first day of the week starts on Monday, but display the
     * month names in english.
     * <p/>
     * In this case you would have to initialize the {@link CalendarView} with {@link Locale#ENGLISH} and then set this property
     * with <code>Calendar.getInstance(Locale.GERMAN)</code>
     *
     * @return The calendar property.
     * @see #getCalendar()
     * @see #setCalendar(Calendar)
     */
    public ObjectProperty<Calendar> calendarProperty() {
        return calendar;
    }

    /**
     * Gets the calendar.
     *
     * @return The calendar.
     * @see #calendarProperty()
     */
    public Calendar getCalendar() {
        return calendar.get();
    }

    /**
     * Sets the calendar.
     *
     * @param calendar The calendar.
     * @see #calendarProperty()
     */
    public void setCalendar(Calendar calendar) {
        this.calendar.set(calendar);
    }

    /**
     * Indicates, whether the week numbers are shown.
     *
     * @return The property.
     * @see #getShowWeeks()
     * @see #setShowWeeks(boolean)
     */
    public BooleanProperty showWeeksProperty() {
        return showWeeks;
    }

    /**
     * Gets the value, whether weeks are shown.
     *
     * @return True, if weeks are shown, otherwise false.
     * @see #showWeeksProperty()
     */
    public boolean getShowWeeks() {
        return showWeeks.get();
    }

    /**
     * Sets the value, whether weeks are shown.
     *
     * @param showWeeks True, if weeks are shown, otherwise false.
     * @see #showWeeksProperty()
     */
    public void setShowWeeks(boolean showWeeks) {
        this.showWeeks.set(showWeeks);
    }

    /**
     * Defines the min date. No dates before this date can be selected.
     *
     * @return The min date.
     * @see #getMinDate()
     * @see #setMinDate(Date)
     */
    public ObjectProperty<Date> minDateProperty() {
        return minDate;
    }

    /**
     * Gets the min date.
     *
     * @return The max date.
     * @see #minDateProperty()
     */
    public Date getMinDate() {
        return minDate.get();
    }

    /**
     * Sets the max date.
     *
     * @param minDate The min date.
     * @see #minDateProperty()
     */
    public void setMinDate(Date minDate) {
        this.minDate.set(minDate);
    }

    /**
     * Defines the max date. No dates after this date can be selected.
     *
     * @return The max date.
     * @see #getMaxDate()
     * @see #setMaxDate(Date)
     */
    public ObjectProperty<Date> maxDateProperty() {
        return maxDate;
    }

    /**
     * Gets the max date.
     *
     * @return The max date.
     * @see #maxDateProperty()
     */
    public Date getMaxDate() {
        return maxDate.get();
    }

    /**
     * Sets the max date.
     *
     * @param maxDate The max date.
     * @see #maxDateProperty()
     */
    public void setMaxDate(Date maxDate) {
        this.maxDate.set(maxDate);
    }

    /**
     * The cell factory for the days.
     *
     * @return The cell factory.
     * @see #getDayCellFactory()
     * @see #setDayCellFactory(Callback)
     */
    public ObjectProperty<Callback<CalendarView, DateCell>> dayCellFactoryProperty() {
        return dayCellFactory;
    }

    /**
     * Gets the cell factory.
     *
     * @return The cell factory.
     * @see #dayCellFactoryProperty()
     */
    public Callback<CalendarView, DateCell> getDayCellFactory() {
        return dayCellFactory.get();
    }

    /**
     * Sets the cell factory.
     *
     * @param dayCellFactory The cell factory.
     * @see #dayCellFactoryProperty()
     */
    public void setDayCellFactory(final Callback<CalendarView, DateCell> dayCellFactory) {
        this.dayCellFactory.set(dayCellFactory);
    }

    /**
     * Gets the selected date.
     *
     * @return The selected date.
     * @see #selectedDateProperty()
     */
    public Date getSelectedDate() {
        return selectedDate.get();
    }

    /**
     * Sets the selected date.
     *
     * @param selectedDate The selected date.
     * @see #selectedDateProperty()
     */
    public void setSelectedDate(Date selectedDate) {
        this.selectedDate.set(selectedDate);
    }

    /**
     * The selected date. When the selected date changes, it also changes the {@linkplain #viewedDateProperty() viewed date}.
     *
     * @return The property.
     * @see #getSelectedDate()
     * @see #setSelectedDate(Date)
     */
    public ObjectProperty<Date> selectedDateProperty() {
        return selectedDate;
    }

    /**
     * Gets the viewed date.
     *
     * @return The viewed date.
     * @see #viewedDateProperty()
     */
    public Date getViewedDate() {
        return viewedDate.get();
    }

    /**
     * Sets the viewed date.
     *
     * @param viewedDate The viewed date.
     * @see #viewedDateProperty()
     */
    public void setViewedDate(Date viewedDate) {
        this.viewedDate.set(viewedDate);
    }

    /**
     * Represents the date, which is currently viewed.
     *
     * @return The property.
     * @see #getViewedDate()
     * @see #setViewedDate(Date)
     */
    public ObjectProperty<Date> viewedDateProperty() {
        return viewedDate;
    }

    @Override
    public String getUserAgentStylesheet() {
        return getClass().getResource("CalendarView.css").toExternalForm();
    }
}
