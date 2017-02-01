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

package extfx.scene.chart;

import com.sun.javafx.charts.ChartLayoutAnimator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.chart.ValueAxis;
import javafx.util.Duration;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * An axis that plots numbers on a logarithmic scale.
 * <p/>
 * <h3>Screenshots</h3>
 * <img src="doc-files/LogarithmicAxis.png" alt="LogarithmicAxis" />
 *
 * @author Christian Schudt
 */
public final class LogarithmicAxis extends ValueAxis<Number> {

    private final DoubleProperty currentUpperBound = new SimpleDoubleProperty();

    private final ChartLayoutAnimator animator = new ChartLayoutAnimator(this);

    private Object currentAnimationID;

    /**
     * Creates an auto-ranging logarithmic axis.
     */
    public LogarithmicAxis() {
        super();
    }

    /**
     * Creates a non-auto-ranging logarithmic axis with the given lower and upper bound.
     *
     * @param lowerBound The lower bound.
     * @param upperBound The upper bound.
     */
    public LogarithmicAxis(double lowerBound, double upperBound) {
        super(lowerBound, upperBound);
    }

    @Override
    protected Object autoRange(double minValue, double maxValue, double length, double labelSize) {
        if (isAutoRanging()) {
            return new double[]{minValue, maxValue};
        } else {
            return getRange();
        }
    }

    @Override
    protected void layoutChildren() {
        if (!isAutoRanging()) {
            currentLowerBound.set(getLowerBound());
            currentUpperBound.set(getUpperBound());
        }
        super.layoutChildren();
    }

    @Override
    public double getDisplayPosition(Number value) {

        // Consider this axis, with lower bound 1 and upper bound 1000:
        // |1----------10---------100--------1000|
        //
        // Lets assume our value is 10. First, we want to get the relative position of the value between lower and upper bound.
        // Therefore we get the logarithmic value of 10, which is 1 and subtract the logarithmic value of 1 (lower bound), which is 0.
        // |1----------10---------100--------1000|
        //  ^^^^^^^^^^^^
        //
        // Then we divide this value by total by the total length of the axis, which is log(1000) (==3) minus log(1) (==0), which is 3.
        // |1----------10---------100--------1000|
        //  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
        //
        // We now know, that the value 10 lies on 33% of the axis.
        // To get the actual value, we only need to multiply the percent value with the absolute length of the axis.
        //
        // log(a) - log(b) == log(a/b)
        // log(a/b) is faster in Java, so we use that.

        // Get the logarithmic difference between the value and the lower bound.
        double diffValue = Math.log10(value.doubleValue() / currentLowerBound.get());

        // Get the logarithmic difference between lower and upper bound.
        double diffTotal = Math.log10(currentUpperBound.get() / currentLowerBound.get());

        double percent = diffValue / diffTotal;

        if (getSide().isHorizontal()) {
            return percent * getWidth();
        } else {
            // Invert for the vertical axis.
            return (1 - percent) * getHeight();
        }
    }

    @Override
    public Number getValueForDisplay(double displayPosition) {
        // This is basically only the equivalence transformation of the getDisplayPosition method.
        if (getSide().isHorizontal()) {
            return Math.pow(10, displayPosition / getWidth() * Math.log10(currentUpperBound.get() / currentLowerBound.get())) * currentLowerBound.get();
        } else {
            return Math.pow(10, ((displayPosition / getHeight()) - 1) * -Math.log10(currentUpperBound.get() / currentLowerBound.get())) * currentLowerBound.get();
        }
    }

    @Override
    protected void setRange(Object range, boolean animate) {

        double lowerBound = ((double[]) range)[0];
        double upperBound = ((double[]) range)[1];
        double[] r = (double[]) range;
        double oldLowerBound = getLowerBound();
        double oldUpperBound = getUpperBound();
        double lower = r[0];
        double upper = r[1];

        setLowerBound(lower);
        setUpperBound(upper);

        if (animate) {
            animator.stop(currentAnimationID);
            currentAnimationID = animator.animate(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(currentLowerBound, oldLowerBound),
                            new KeyValue(currentUpperBound, oldUpperBound)
                    ),
                    new KeyFrame(Duration.millis(700),
                            new KeyValue(currentLowerBound, lower),
                            new KeyValue(currentUpperBound, upper)
                    )
            );
        } else {
            currentLowerBound.set(lowerBound);
            currentUpperBound.set(upperBound);
        }
    }

    @Override
    protected double[] getRange() {
        return new double[]{getLowerBound(), getUpperBound()};
    }

    @Override
    protected List<Number> calculateTickValues(double length, Object range) {
        List<Number> tickValues = new ArrayList<Number>();

        final double[] rangeProps = (double[]) range;
        final double lowerBound = rangeProps[0];
        final double upperBound = rangeProps[1];
        double logLowerBound = Math.log10(lowerBound);
        double logUpperBound = Math.log10(upperBound);

        // Always start with a "even" integer. That's why we floor the start value.
        // Otherwise the scale would contain odd values, rather then normal 1, 2, 3, 4, ... values.
        for (double major = Math.floor(logLowerBound); major < logUpperBound; major++) {
            double p = Math.pow(10, major);
            for (double j = 1; j < 10; j++) {
                tickValues.add(j * p);
            }
        }
        return tickValues;
    }

    @Override
    protected List<Number> calculateMinorTickMarks() {

        final List<Number> minorTickMarks = new ArrayList<Number>();
        double step = 1.0 / getMinorTickCount();
        double logLowerBound = Math.log10(getLowerBound());
        double logUpperBound = Math.log10(getUpperBound());

        for (double major = Math.floor(logLowerBound); major < logUpperBound; major++) {
            for (double j = 0; j < 10; j += step) {
                minorTickMarks.add(j * Math.pow(10, major));
            }
        }

        return minorTickMarks;
    }

    @Override
    protected String getTickMarkLabel(Number value) {
        NumberFormat formatter = NumberFormat.getInstance();
        return formatter.format(value);
    }
}
