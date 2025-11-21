package de.nordakademie.pdse;

import java.io.FileWriter;
import java.io.IOException;

public class CSRBenchmark {

    private static final int N = 2048;

    private static final double[] SPARSITIES = {
            0.25,
            0.50,
            0.90,
            0.999
    };

    public static void main(String[] args) {

        try (FileWriter csv = new FileWriter("csr_results.csv")) {

            csv.write("Sparsity;Runtime_ms;NNZ_A;NNZ_B;NNZ_C\n");

            for (double s : SPARSITIES) {

                System.out.println("\n=====================================");
                System.out.println("Testing sparsity: " + (s * 100) + "% zeros");
                System.out.println("=====================================");

                CSRMatrix A = CSRMatrix.generateRandomCSR(N, s);
                CSRMatrix B = CSRMatrix.generateRandomCSR(N, s);

                long start = System.nanoTime();
                CSRMatrix C = A.multiply(B);
                long end = System.nanoTime();

                long runtimeMs = (end - start) / 1_000_000;

                int nnzA = A.values.length;
                int nnzB = B.values.length;
                int nnzC = C.values.length;

                System.out.println("CSR runtime: " + runtimeMs + " ms");

                csv.write(s + ";" + runtimeMs + ";" + nnzA + ";" + nnzB + ";" + nnzC + "\n");
                csv.flush();
            }

            System.out.println("\nCSV export complete: csr_results.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
