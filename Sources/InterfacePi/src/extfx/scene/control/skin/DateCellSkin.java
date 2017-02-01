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

import extfx.scene.control.DateCell;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;

/**
 * The default skin for the {@link DateCell}.
 *
 * @author Christian Schudt
 */
public final class DateCellSkin extends StackPane implements Skin<DateCell> {

    private final DateCell dateCell;

    private final Button button;

    public DateCellSkin(final DateCell control) {
        this.dateCell = control;
        button = new Button();
        button.textProperty().bind(control.textProperty());

        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER);
        button.setMinWidth(30);
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                control.getCalendarView().setSelectedDate(control.getItem());
            }
        });

        button.tooltipProperty().bind(control.tooltipProperty());

        getChildren().add(button);
    }

    @Override
    public DateCell getSkinnable() {
        return dateCell;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void dispose() {
        button.textProperty().unbind();
        button.tooltipProperty().unbind();
    }
}
