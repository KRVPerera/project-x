package lk.ac.mrt.projectx.buildex.complex.langs;

import lk.ac.mrt.projectx.buildex.complex.operations.Guess;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Chathura Widanage
 */
public class HalideGenerator {
    private String halideBase = "#include \"Halide.h\"\n" +
            "\n" +
            "using namespace std;\n" +
            "using namespace Halide;\n" +
            "/**\n" +
            "* Auto generated by Project X\n" +
            "* Department of Computer Science & Engineering, Univeristy of Moratuwa, Sri Lanka\n" +
            "* %s\n" +
            "*/\n" +
            "int main(int argc, char **argv) {\n" +
            "\n" +
            "\tImageParam input(UInt(8), 3);\n" +
            "\tFunc output(\"output\");\n" +
            "\tVar x(\"x\"), y(\"y\"), c(\"c\");\n" +
            "\tExpr M_PI = 3.14159265f;\n" +
            "\tExpr width=input.width();\n" +
            "\tExpr height=input.height();\n" +
            "\tExpr x_in=x-(width/2);\n" +
            "\tExpr y_in=y-(height/2);\n" +
            "\tExpr r_in = sqrt(pow(x_in, 2) + pow(y_in, 2));\n" +
            "\tExpr theta_in = atan2(y_in, x_in);\n" +
            "\ttheta_in = select(theta_in<0,6.28318530718f+theta_in,theta_in);\n" +
            "\n" +
            "\t\n" +
            "\tExpr r_out=%s;\n" +
            "\t//Expr r_out=%s;\n" +
            "\tExpr theta_out=%s;\n" +
            "\t//Expr theta_out=%s;\n" +
            "\t\n" +
            "\t\n" +
            "\tExpr newX = (r_out*cos(theta_out))+(width/2);\n" +
            "\tExpr newY = (r_out*sin(theta_out))+(height/2);\n" +
            "\tnewX = cast<uint16_t>(newX);\n" +
            "\tnewY = cast<uint16_t>(newY);\n" +
            "\tnewX = select(newX>width-1||newX<0,0,newX);\n" +
            "\tnewY = select(newY>height-1||newY<0,0,newY);\n" +
            "\tnewX = clamp(cast<uint16_t>(newX), 0, width - 1);\n" +
            "\tnewY = clamp(cast<uint16_t>(newY), 0, height - 1);\n" +
            "\tExpr color = select(newX==0||newY==0,input(0,0,c)*0,input(newX,newY,c));\n" +
            "\t\n" +
            "\toutput(x, y, c) = color;\n" +
            "\n" +
            "\tstd::vector<Argument> args = {input};\n" +
            "    output.compile_to_file(\"halide_gen\", input);\n" +
            "\t\n" +
            "\treturn 0;\n" +
            "}\n";
    private Guess rGuess, tGuess;

    public HalideGenerator(Guess rGuess, Guess tGuess) {
        this.rGuess = rGuess;
        this.tGuess = tGuess;
    }

    public void generate() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh/mm/dd HH:mm:ss");
        String generated = String.format(this.halideBase,
                sdf.format(Calendar.getInstance().getTime()).toString(),
                rGuess.getGeneratedCode(true), rGuess.getGeneratedCode(),
                tGuess.getGeneratedCode(true), tGuess.getGeneratedCode()
        );
        System.out.println("\n\n\n\n\n\n\n");
        System.out.println(generated);
        System.out.println("\n\n\n\n\n\n\n");

        try {
            FileWriter fileWriter = new FileWriter(new File("generated", "halide.cpp"));
            fileWriter.write(generated);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
