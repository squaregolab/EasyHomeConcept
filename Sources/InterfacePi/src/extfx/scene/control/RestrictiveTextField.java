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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;

/**
 * A text field, which restricts the user's input.
 * <p>
 * The restriction can either be a maximal number of characters which the user is allowed to input
 * or a regular expression class, which contains allowed characters.
 * </p>
 * <p/>
 * <h3>Sample Usage</h3>
 * Sample, which restricts the input to maximal 10 numeric characters:
 * <pre>
 * <code>
 * RestrictiveTextField textField = new RestrictiveTextField();
 * textField.setMaxLength(10);
 * textField.setRestrict("[0-9]");
 * </code>
 * </pre>
 *
 * @author Christian Schudt
 */
public final class RestrictiveTextField extends TextField {

    private final IntegerProperty maxLength = new SimpleIntegerProperty(this, "maxLength", -1);

    private final StringProperty restrict = new SimpleStringProperty(this, "restrict");

    public RestrictiveTextField() {

        textProperty().addListener(new ChangeListener<String>() {

            private boolean ignore;

            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s1) {
                if (ignore || s1 == null)
                    return;
                if (maxLength.get() > -1 && s1.length() > maxLength.get()) {
                    ignore = true;
                    setText(s1.substring(0, maxLength.get()));
                    ignore = false;
                }

                if (restrict.get() != null && !restrict.get().equals("") && !s1.matches(restrict.get() + "*")) {
                    ignore = true;
                    setText(s);
                    ignore = false;
                }
            }
        });
    }

    /**
     * The max length property.
     *
     * @return The max length property.
     * @see #getMaxLength()
     * @see #setMaxLength(int)
     */
    public final IntegerProperty maxLengthProperty() {
        return maxLength;
    }

    /**
     * Gets the max length of the text field.
     *
     * @return The max length.
     */
    public final int getMaxLength() {
        return maxLength.get();
    }

    /**
     * Sets the max length of the text field.
     *
     * @param maxLength The max length.
     */
    public final void setMaxLength(final int maxLength) {
        this.maxLength.set(maxLength);
    }

    /**
     * The restrict property.
     *
     * @return The restrict property.
     * @see #getRestrict()
     * @see #setRestrict(String)
     */
    public final StringProperty restrictProperty() {
        return restrict;
    }

    /**
     * Gets a regular expression character class which restricts the user input.<br/>
     *
     * @return The regular expression.
     * @see #restrictProperty()
     */
    public final String getRestrict() {
        return restrict.get();
    }

    /**
     * Sets a regular expression character class which restricts the user input.<br/>
     * E.g. [0-9] only allows numeric values.
     *
     * @param restrict The regular expression.
     */
    public final void setRestrict(final String restrict) {
        this.restrict.set(restrict);
    }
}
