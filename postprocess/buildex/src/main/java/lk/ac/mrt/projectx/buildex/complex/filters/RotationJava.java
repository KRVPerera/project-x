package lk.ac.mrt.projectx.buildex.complex.filters;

import lk.ac.mrt.projectx.buildex.complex.CoordinateTransformer;
import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;
import org.apache.commons.math3.util.MathUtils;

import java.awt.image.BufferedImage;

/**
 * @author Chathura Widanage
 */
public class RotationJava {
    public void filter(BufferedImage in, BufferedImage out) {
        int width = in.getWidth();
        int height = in.getHeight();
        for (int i = 0; i < in.getWidth(); i++) {
            for (int j = 0; j < in.getHeight(); j++) {
                CartesianCoordinate cartesianCoordinate = new CartesianCoordinate(i, j);
                PolarCoordinate polarCoordinate = CoordinateTransformer.cartesian2Polar(width, height, cartesianCoordinate);


                double thetaNew = 0.012 * polarCoordinate.getR() + 1.014 * polarCoordinate.getTheta();
                double rNew = 1.007 * polarCoordinate.getR() + 0.0 * Math.pow(polarCoordinate.getR(), 2) + -0.003 * (polarCoordinate.getR() * polarCoordinate.getTheta());
                //thetaNew= MathUtils.normalizeAngle(thetaNew, FastMath.PI);
                PolarCoordinate newPola = new PolarCoordinate(thetaNew, rNew);

                CartesianCoordinate newCartCord = CoordinateTransformer.polar2Cartesian(width, height, newPola);
                if (clampPass(width, height, newCartCord))
                    out.setRGB(i, j, in.getRGB((int) newCartCord.getX(), (int) newCartCord.getY()));
            }
        }
    }

    public void filterCartesian(BufferedImage in, BufferedImage out) {
        int width = in.getWidth();
        int height = in.getHeight();
        for (int sx = 0; sx < width; sx++) {
            for (int sy = 0; sy < height; sy++) {
                CartesianCoordinate cartesianCoordinate = new CartesianCoordinate(sx, sy);
                CoordinateTransformer.cartesianToCenter(width, height, cartesianCoordinate);
                PolarCoordinate polarCoordinate = CoordinateTransformer.cartesian2Polar(cartesianCoordinate);
                double theta = polarCoordinate.getTheta();
                //System.out.print(Math.toDegrees(theta)+":");
                theta += Math.PI;
                //theta = MathUtils.normalizeAngle(theta, Math.PI);
                //System.out.println(Math.toDegrees(theta));
                polarCoordinate.setTheta(theta);
                CartesianCoordinate cartesianCoordinate2 = CoordinateTransformer.polar2Cartesian(polarCoordinate);
                CoordinateTransformer.cartesianToCorner(width, height, cartesianCoordinate2);
                int tx = (int) cartesianCoordinate2.getX();
                int ty = (int) (cartesianCoordinate2.getY());
                // plot pixel (sx, sy) the same color as (tx, ty) if it's in bounds
                if (tx >= 0 && tx < width && ty >= 0 && ty < height) {
                    out.setRGB(sx, sy, in.getRGB(tx, ty));
                }
            }
        }
    }

    private boolean clampPass(int width, int height, CartesianCoordinate cartesianCoordinate) {
        int x = (int) Math.round(cartesianCoordinate.getX());

        int y = (int) Math.round(cartesianCoordinate.getY());

        return x >= 0 && x <= width - 1 && y >= 0 && y <= height - 1;
    }
}
