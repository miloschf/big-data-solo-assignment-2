package de.nordakademie.pdse;

import java.util.Random;

public class MatrixOptimized {

    // Matrix Generator
    public static double[][] generateMatrix(int n) {
        Random rand = new Random();
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = rand.nextDouble();
            }
        }
        return matrix;
    }

    // Classic Matrix multi
    public static double[][] multiplyClassic(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double sum = 0.0;
                for (int k = 0; k < n; k++) {
                    sum += A[i][k] * B[k][j];
                }
                C[i][j] = sum;
            }
        }
        return C;
    }

    // Strassen Algo
    public static double[][] multiplyStrassen(double[][] A, double[][] B) {
        int n = A.length;

        // Onl
        if (n <= 64) {
            return multiplyClassic(A, B);
        }

        int newSize = n / 2;
        double[][] A11 = new double[newSize][newSize];
        double[][] A12 = new double[newSize][newSize];
        double[][] A21 = new double[newSize][newSize];
        double[][] A22 = new double[newSize][newSize];
        double[][] B11 = new double[newSize][newSize];
        double[][] B12 = new double[newSize][newSize];
        double[][] B21 = new double[newSize][newSize];
        double[][] B22 = new double[newSize][newSize];

        // Extract submatrices
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                A11[i][j] = A[i][j];
                A12[i][j] = A[i][j + newSize];
                A21[i][j] = A[i + newSize][j];
                A22[i][j] = A[i + newSize][j + newSize];

                B11[i][j] = B[i][j];
                B12[i][j] = B[i][j + newSize];
                B21[i][j] = B[i + newSize][j];
                B22[i][j] = B[i + newSize][j + newSize];
            }
        }

        // Strassen-Multiplication
        double[][] M1 = multiplyStrassen(add(A11, A22), add(B11, B22));
        double[][] M2 = multiplyStrassen(add(A21, A22), B11);
        double[][] M3 = multiplyStrassen(A11, subtract(B12, B22));
        double[][] M4 = multiplyStrassen(A22, subtract(B21, B11));
        double[][] M5 = multiplyStrassen(add(A11, A12), B22);
        double[][] M6 = multiplyStrassen(subtract(A21, A11), add(B11, B12));
        double[][] M7 = multiplyStrassen(subtract(A12, A22), add(B21, B22));

        // C-Submatrices extraction
        double[][] C11 = add(subtract(add(M1, M4), M5), M7);
        double[][] C12 = add(M3, M5);
        double[][] C21 = add(M2, M4);
        double[][] C22 = add(subtract(add(M1, M3), M2), M6);

        // Final value
        double[][] C = new double[n][n];
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                C[i][j] = C11[i][j];
                C[i][j + newSize] = C12[i][j];
                C[i + newSize][j] = C21[i][j];
                C[i + newSize][j + newSize] = C22[i][j];
            }
        }

        return C;
    }

    // 2. Loop Unrolling
    public static double[][] multiplyLoopUnrolling(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < n; k++) {
                double a = A[i][k];
                int j = 0;
                for (; j <= n - 4; j += 4) {
                    C[i][j] += a * B[k][j];
                    C[i][j + 1] += a * B[k][j + 1];
                    C[i][j + 2] += a * B[k][j + 2];
                    C[i][j + 3] += a * B[k][j + 3];
                }
                for (; j < n; j++) {
                    C[i][j] += a * B[k][j];
                }
            }
        }
        return C;
    }

    // 3. Tiling / Blocking
    public static double[][] multiplyTiled(double[][] A, double[][] B, int blockSize) {
        int n = A.length;
        double[][] C = new double[n][n];

        for (int ii = 0; ii < n; ii += blockSize) {
            for (int jj = 0; jj < n; jj += blockSize) {
                for (int kk = 0; kk < n; kk += blockSize) {

                    int iMax = Math.min(ii + blockSize, n);
                    int jMax = Math.min(jj + blockSize, n);
                    int kMax = Math.min(kk + blockSize, n);

                    for (int i = ii; i < iMax; i++) {
                        for (int k = kk; k < kMax; k++) {
                            double a = A[i][k];
                            for (int j = jj; j < jMax; j++) {
                                C[i][j] += a * B[k][j];
                            }
                        }
                    }
                }
            }
        }
        return C;
    }

    // Matrix Add helper
    private static double[][] add(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] + B[i][j];
        return C;
    }

    // Matrix Subtract helper
    private static double[][] subtract(double[][] A, double[][] B) {
        int n = A.length;
        double[][] C = new double[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                C[i][j] = A[i][j] - B[i][j];
        return C;
    }

    // Test Benchmark (Actual Benchmark is done using JMH)
    public static void main(String[] args) {
        int n = 1024;
        double[][] A = generateMatrix(n);
        double[][] B = generateMatrix(n);

        System.out.println("Matrix Multiplication Benchmark (n = " + n + ")");

        long start, end;

        // Classic
        start = System.nanoTime();
        multiplyClassic(A, B);
        end = System.nanoTime();
        System.out.printf("Classic:        %.3f ms%n", (end - start) / 1_000_000.0);

        // Strassen
        start = System.nanoTime();
        multiplyStrassen(A, B);
        end = System.nanoTime();
        System.out.printf("Strassen:       %.3f ms%n", (end - start) / 1_000_000.0);

        // Loop Unrolling
        start = System.nanoTime();
        multiplyLoopUnrolling(A, B);
        end = System.nanoTime();
        System.out.printf("Loop Unrolling: %.3f ms%n", (end - start) / 1_000_000.0);

        // Tiling
        start = System.nanoTime();
        multiplyTiled(A, B, 64);
        end = System.nanoTime();
        System.out.printf("Tiling:         %.3f ms%n", (end - start) / 1_000_000.0);
    }
}

