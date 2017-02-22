package lk.ac.mrt.projectx.buildex.complex.langs;

import lk.ac.mrt.projectx.buildex.complex.operations.Guess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Chathura Widanage
 */
public class JavaGenerator {
    private String halideBase = "import javax.imageio.ImageIO;\n" +
            "import java.awt.image.BufferedImage;\n" +
            "import java.io.File;\n" +
            "import java.io.IOException;\n" +
            "\n" +
            "public class JavaGen {\n" +
            "    public static void main(String[] args) throws IOException {\n" +
            "        BufferedImage read = ImageIO.read(new File(args[0]));\n" +
            "        BufferedImage out = new BufferedImage(read.getWidth(), read.getHeight(), read.getType());\n" +
            "\t\tfilter(read,out);\n" +
            "        ImageIO.write(out,\"JPG\",new File(\".\",\"out.jpg\"));    \n" +
            "\t}\n" +
            "\n" +
            "    public static void filter(BufferedImage in, BufferedImage out) {\n" +
            "        int width = in.getWidth();\n" +
            "        int height = in.getHeight();\n" +
            "        for (int i = 0; i < in.getWidth(); i++) {\n" +
            "            for (int j = 0; j < in.getHeight(); j++) {\n" +
            "                int sx = i - (width / 2);\n" +
            "                int sy = j - (height / 2);\n" +
            "\n" +
            "                double r_in = Math.hypot(sx,sy);\n" +
            "                double theta_in = normalize(Math.atan2(sy, sx));\n" +
            "\n" +
            "                double rNew = %s\n" +
            "\t\t\t\t//double rNew = %s\n" +
            "                double thetaNew = %s\n" +
            "\t\t\t\t//double thetaNew = %s\n" +
            "\n" +
            "                int tx = (int) ((rNew * Math.cos(thetaNew))+(width/2));\n" +
            "                int ty = (int) ((rNew * Math.sin(thetaNew))+(height/2));\n" +
            "\n" +
            "                if (clampPass(width, height, tx, ty)){\n" +
            "                    out.setRGB(i, j, in.getRGB((int)tx, (int)ty));\n" +
            "\t\t\t\t}\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "\t\n" +
            "\tpublic static double normalize(double theta) {\n" +
            "        return theta < 0 ? theta + (Math.PI * 2) : theta;\n" +
            "    }\n" +
            "\n" +
            "    private static boolean clampPass(int width, int height, int x, int y) {\n" +
            "        return x >= 0 && x <= width - 1 && y >= 0 && y <= height - 1;\n" +
            "    }\n" +
            "}\n";
    private Guess rGuess, tGuess;

    public JavaGenerator(Guess rGuess, Guess tGuess) {
        this.rGuess = rGuess;
        this.tGuess = tGuess;
    }

    public void generate() {
        String generated = String.format(this.halideBase,
                rGuess.getGeneratedCode(true), rGuess.getGeneratedCode(),
                tGuess.getGeneratedCode(true), tGuess.getGeneratedCode());
        System.out.println("\n\n\n\n\n\n\n");
        System.out.println(generated);
        System.out.println("\n\n\n\n\n\n\n");

        try {
            FileWriter fileWriter = new FileWriter(new File("generated", "JavaGen.java"));
            fileWriter.write(generated);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
