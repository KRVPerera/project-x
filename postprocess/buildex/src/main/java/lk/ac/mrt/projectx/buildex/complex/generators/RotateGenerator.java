package lk.ac.mrt.projectx.buildex.complex.generators;

import lk.ac.mrt.projectx.buildex.complex.CoordinateTransformer;
import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.complex.cordinates.PolarCoordinate;
import lk.ac.mrt.projectx.buildex.models.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chathura Widanage
 */
public class RotateGenerator extends Generator {
    public List<Pair<CartesianCoordinate, CartesianCoordinate>> generate(int width, int height) {
        List<Pair<CartesianCoordinate, CartesianCoordinate>> examples = new ArrayList<>();
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
                    examples.add(new Pair<>(
                            new CartesianCoordinate(sx, sy),
                            new CartesianCoordinate(tx, ty)
                    ));
                }
            }
        }
        return examples;
    }
}
