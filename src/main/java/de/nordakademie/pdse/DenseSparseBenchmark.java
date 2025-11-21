package de.nordakademie.pdse;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class DenseSparseBenchmark {

    private static final int[] SIZES = {256, 512, 1024, 2048, 4096};

    private static final double[] SPARSITIES = {
            0.25,
            0.50,
            0.90,
            0.999
    };

    public static void main(String[] args) {

        try (FileWriter csv = new FileWriter("dense_sparse_results.csv")) {

            csv.write("MatrixSize;Sparsity;Runtime_ms;NNZ_A;NNZ_B;NNZ_C\n");

            for (int n : SIZES) {
                System.out.println("\n======================================");
                System.out.println("Matrix size: " + n + " x " + n);
                System.out.println("======================================");

                for (double sparsity : SPARSITIES) {

                    System.out.println("\n--- Sparsity Level: " + (sparsity * 100) + "% zeros ---");

                    double[][] A = generateMatrix(n, sparsity);
                    double[][] B = generateMatrix(n, sparsity);

                    int nnzA = countNonZero(A);
                    int nnzB = countNonZero(B);

                    long start = System.nanoTime();
                    double[][] C = multiplyDense(A, B);
                    long end = System.nanoTime();

                    long runtimeMs = (end - start) / 1_000_000;
                    int nnzC = countNonZero(C);

                    System.out.println("Runtime (ms): " + runtimeMs);
                    System.out.println("Non-zero C: " + nnzC);

                    csv.write(n + ";" + sparsity + ";" + runtimeMs + ";" + nnzA + ";" + nnzB + ";" + nnzC + "\n");
                    csv.flush();
                }
            }

            System.out.println("\nCSV export complete: dense_sparse_results.csv");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------

    private static double[][] generateMatrix(int n, double sparsity) {
        double[][] M = new double[n][n];
        Random rand = new Random();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (rand.nextDouble() > sparsity) {
                    M[i][j] = rand.nextDouble();
                } else {
                    M[i][j] = 0.0;
                }
            }
        }
        return M;
    }

    private static double[][] multiplyDense(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                double val = A[i][k];
                for (int j = 0; j < n; j++) {
                    C[i][j] += val * B[k][j];
                }
            }
        }
        return C;
    }

    private static int countNonZero(double[][] M) {
        int count = 0;
        for (double[] row : M)
            for (double v : row)
                if (v != 0.0) count++;
        return count;
    }
}
