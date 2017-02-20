package lk.ac.mrt.projectx.buildex.complex;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.io.IOException;

/**
 * Created by krv on 2/19/17.
 */
/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ------------------------
 * FastScatterPlotDemo.java
 * ------------------------
 * (C) Copyright 2002-2004, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: FastScatterPlotDemo.java,v 1.13 2004/04/26 19:11:54 taqua Exp $
 *
 * Changes (from 29-Oct-2002)
 * --------------------------
 * 29-Oct-2002 : Added standard header and Javadocs (DG);
 * 12-Nov-2003 : Enabled zooming (DG);
 *
 */

/**
 * A demo of the fast scatter plot.
 */
public class FormalVerifier extends ApplicationFrame {

    /**
     * A constant for the number of items in the sample dataset.
     */
    private static final int width = 100;
    private static final int height = 100;
    private static final int COUNT = width * height;

    /**
     * The data.
     */
    private float[][] data = new float[ 2 ][ COUNT ];

    /**
     * Creates a new fast scatter plot demo.
     *
     * @param title the frame title.
     */
    public FormalVerifier(final String title, final String lblx, final String lbly) throws IOException {

        super( title );
        populateData();
        final NumberAxis domainAxis = new NumberAxis( lblx );
        domainAxis.setAutoRangeIncludesZero( false );
        final NumberAxis rangeAxis = new NumberAxis( lbly );
        rangeAxis.setAutoRangeIncludesZero( false );
        final FastScatterPlot plot = new FastScatterPlot( this.data, domainAxis, rangeAxis );
        final JFreeChart chart = new JFreeChart( title, plot );
//        chart.setLegend(null);

        // force aliasing of the rendered content..
        chart.getRenderingHints().put
                ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

        final ChartPanel panel = new ChartPanel( chart, true );
        panel.setPreferredSize( new java.awt.Dimension( 500, 270 ) );
        //      panel.setHorizontalZoom(true);
        //    panel.setVerticalZoom(true);
        panel.setMinimumDrawHeight( 10 );
        panel.setMaximumDrawHeight( 2000 );
        panel.setMinimumDrawWidth( 20 );
        panel.setMaximumDrawWidth( 2000 );

        setContentPane( panel );
        //TODO : Print
        //ChartUtilities.saveChartAsJPEG( new File("/home/krv/Projects/FYP/graphs/FishEye.jpg"),chart,800,600 );

    }

    // ****************************************************************************
    // * JFREECHART DEVELOPER GUIDE                                               *
    // * The JFreeChart Developer Guide, written by David Gilbert, is available   *
    // * to purchase from Object Refinery Limited:                                *
    // *                                                                          *
    // * http://www.object-refinery.com/jfreechart/guide.html                     *
    // *                                                                          *
    // * Sales are used to provide funding for the JFreeChart project - please    *
    // * support us so that we can continue developing free software.             *
    // ****************************************************************************

    /**
     * Populates the data array with random values.
     */
    private void populateData() {
        double maxR = Math.hypot( width / 2, height / 2 );

        for (int i = 0 ; i < width ; i++) {
            for (int j = 0 ; j < height ; j++) {
                CartesianCoordinate cartesianCoordinate = new CartesianCoordinate( i, j );
                PolarCoordinate polarCoordinate = CoordinateTransformer.cartesian2Polar( width, height, cartesianCoordinate );

                double thetaNew = 1.0 * polarCoordinate.getTheta() + 0.0 * (polarCoordinate.getR() / polarCoordinate.getTheta()) + -0.027 * (polarCoordinate.getTheta() / polarCoordinate.getTheta());

                double rNew = 0.472 * polarCoordinate.getR() + 0.001 * Math.pow( polarCoordinate.getR(), 2 ) + 0.002 * (polarCoordinate.getR() * polarCoordinate.getTheta());
                //thetaNew= MathUtils.normalizeAngle(thetaNew, FastMath.PI);
                PolarCoordinate newPola = new PolarCoordinate( thetaNew, rNew );

                CartesianCoordinate newCartCord = CoordinateTransformer.polar2Cartesian( width, height, newPola );
                if (clampPass( width, height, newCartCord )) {
                    //  out.setRGB(i, j, in.getRGB((int) newCartCord.getX(), (int) newCartCord.getY()));
                    //TODO : Add data 0 - X axis , 1 - Y Axis
                    this.data[ 0 ][ i + j * width ] = (float) polarCoordinate.getTheta();
                    this.data[ 1 ][ i + j * width ] = (float) newPola.getTheta();
                }
            }
        }

    }

    private boolean clampPass(int width, int height, CartesianCoordinate cartesianCoordinate) {
        int x = (int) Math.round( cartesianCoordinate.getX() );

        int y = (int) Math.round( cartesianCoordinate.getY() );

        return x >= 0 && x <= width - 1 && y >= 0 && y <= height - 1;
    }

    /**
     * Starting point for the demonstration application.
     *
     * @param args ignored.
     */
    public static void main(final String[] args) throws IOException {

        //TODO : Add labels
        final FormalVerifier demo = new FormalVerifier( "Fish Eye Filter", "Old Theta", "New Theta" );
        demo.pack();
        RefineryUtilities.centerFrameOnScreen( demo );
        demo.setVisible( true );

    }

}