package lk.ac.mrt.projectx.buildex.complex;


import lk.ac.mrt.projectx.buildex.complex.cordinates.CartesianCoordinate;
import lk.ac.mrt.projectx.buildex.models.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * Created by Lasantha on 21-Feb-17.
 */
public class ExamplesFile {

    public ExamplesFile() {
    }

    public List<Pair<CartesianCoordinate, CartesianCoordinate>> read(String fileName) throws FileNotFoundException {

        List<Pair<CartesianCoordinate, CartesianCoordinate>> examples = new ArrayList<>();

        Scanner input = new Scanner(new File(fileName));

        input.nextLine();

        while (input.hasNextLine()){
            String[] data = input.nextLine().split(",");

            CartesianCoordinate first = new CartesianCoordinate(Double.parseDouble(data[0]),Double.parseDouble(data[1]));
            CartesianCoordinate second = new CartesianCoordinate(Double.parseDouble(data[2]),Double.parseDouble(data[3]));

            examples.add(new Pair<CartesianCoordinate, CartesianCoordinate>(first,second));
        }

        return examples;
    }

    public void write(List<Pair<CartesianCoordinate, CartesianCoordinate>> examples ) throws IOException {

        String fileName = "examples_"+System.currentTimeMillis()+".csv";

        OutputFile out = new OutputFile(fileName);
        out.append("Size = "+examples.size());
        out.appendLine();

        for (int i = 0; i < examples.size(); i++) {
            Pair<CartesianCoordinate, CartesianCoordinate> pair = examples.get(i);

            out.append(pair.first.getX()+","+pair.first.getY()+","+pair.second.getX()+","+pair.second.getY());
            out.appendLine();
        }

        out.close();
    }




    static class OutputFile {

        private final File file;
        private BufferedWriter bufferedWriter;

        public OutputFile(String pathName) throws IOException {
            this.file = new File("examples",pathName);
            if (file.exists()) {
                file.delete();
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file, true);
            bufferedWriter = new BufferedWriter(fileWriter);

        }

        /**
         * Append the text with an new line at the end of the file
         *
         * @throws java.io.IOException
         */
        public void appendLine() throws IOException {
            bufferedWriter.newLine();
            System.out.println("");
        }

        /**
         * Append the text at the end of the file
         *
         * @param text
         * @throws java.io.IOException
         */
        public void append(String text) throws IOException {
            bufferedWriter.append(text);
            System.out.print(text);
        }

        public void close() throws IOException {
            bufferedWriter.close();
        }

    }
}
