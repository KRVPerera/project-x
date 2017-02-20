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
public class PolarGenerator extends Generator {
    public List<Pair<CartesianCoordinate, CartesianCoordinate>> generate(int width, int height) {
        List<Pair<CartesianCoordinate, CartesianCoordinate>> examples = new ArrayList<>();
        double maxR = Math.hypot(width / 2, height / 2);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                CartesianCoordinate cartesianCoordinate = new CartesianCoordinate(i, j);
                PolarCoordinate polarCoordinate = CoordinateTransformer.cartesian2Polar(width, height, cartesianCoordinate);

                CartesianCoordinate newCartCord = new CartesianCoordinate((int)(polarCoordinate.getTheta() * width / (Math.PI * 4)), (int)(polarCoordinate.getR() * height / maxR));
                CoordinateTransformer.cartesianToCorner(width, height, newCartCord);
                if (clampPass(width, height, newCartCord)) {
                    examples.add(new Pair<>(
                            cartesianCoordinate,
                            newCartCord
                    ));
                }
            }
        }
        return examples;
    }
}
