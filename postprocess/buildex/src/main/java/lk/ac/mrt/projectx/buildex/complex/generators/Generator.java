package lk.ac.mrt.projectx.buildex.complex.generators;

import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.models.Pair;

import java.util.List;

/**
 * @author Chathura Widanage
 */
public abstract class Generator {
    protected boolean clampPass(int width, int height, CartesianCoordinate cartesianCoordinate) {
        int x = (int) Math.round(cartesianCoordinate.getX());

        int y = (int) Math.round(cartesianCoordinate.getY());

        return x >= 0 && x <= width - 1 && y >= 0 && y <= height - 1;
    }

    public abstract List<Pair<CartesianCoordinate, CartesianCoordinate>> generate(int width, int height);
}
