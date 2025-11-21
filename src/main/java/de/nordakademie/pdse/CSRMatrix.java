package de.nordakademie.pdse;

/**
 * Creates CSR Matrix
 * @author miloschfuerstenberg
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CSRMatrix {

    public double[] values;   // not null values
    public int[] colIndex;    // col index
    public int[] rowPtr;      // startindex row

    public int rows;
    public int cols;

    public CSRMatrix(double[] values, int[] colIndex, int[] rowPtr, int rows, int cols) {
        this.values = values;
        this.colIndex = colIndex;
        this.rowPtr = rowPtr;
        this.rows = rows;
        this.cols = cols;
    }

    /**
     * Erzeugt eine zufällige CSR-Matrix mit gegebener Sparsity.
     * sparsity = Anteil der Nullen, z.B. 0.9 = 90% Nullen
     */
    public static CSRMatrix generateRandomCSR(int n, double sparsity) {
        Random rand = new Random();

        List<Double> valueList = new ArrayList<>();
        List<Integer> colList = new ArrayList<>();
        int[] rowPtr = new int[n + 1];

        for (int i = 0; i < n; i++) {
            rowPtr[i] = valueList.size();
            for (int j = 0; j < n; j++) {
                if (rand.nextDouble() > sparsity) {
                    // Nicht-Null-Wert
                    valueList.add(rand.nextDouble());
                    colList.add(j);
                }
            }
        }
        rowPtr[n] = valueList.size();

        double[] values = new double[valueList.size()];
        int[] colIndex = new int[colList.size()];

        for (int i = 0; i < values.length; i++) {
            values[i] = valueList.get(i);
            colIndex[i] = colList.get(i);
        }

        return new CSRMatrix(values, colIndex, rowPtr, n, n);
    }

    /**
     * Multipliziert zwei CSR-Matrizen: C = A * B
     * B wird intern in CSC-Form konvertiert für bessere Performance.
     */
    public CSRMatrix multiply(CSRMatrix B) {
        if (this.cols != B.rows) {
            throw new IllegalArgumentException("Dimension mismatch");
        }

        int n = this.rows;

        // B in CSC konvertieren für schnelle Spaltenzugriffe
        CSCMatrix Bcsc = CSCMatrix.fromCSR(B);

        List<Double> valueList = new ArrayList<>();
        List<Integer> colList = new ArrayList<>();
        int[] rowPtr = new int[n + 1];

        for (int i = 0; i < n; i++) {
            rowPtr[i] = valueList.size();

            // Temporärer Akkumulator
            double[] rowAccum = new double[n];

            // Iteriere durch die Elemente der CSR-Zeile i
            for (int idx = this.rowPtr[i]; idx < this.rowPtr[i + 1]; idx++) {
                int colA = this.colIndex[idx];
                double valA = this.values[idx];

                // Iteriere durch CSC-Spalte colA
                for (int bidx = Bcsc.colPtr[colA]; bidx < Bcsc.colPtr[colA + 1]; bidx++) {
                    int rowB = Bcsc.rowIndex[bidx];
                    rowAccum[rowB] += valA * Bcsc.values[bidx];
                }
            }

            // Nicht-Null-Werte der Zeile speichern
            for (int j = 0; j < n; j++) {
                if (rowAccum[j] != 0.0) {
                    valueList.add(rowAccum[j]);
                    colList.add(j);
                }
            }
        }

        rowPtr[n] = valueList.size();

        double[] values = new double[valueList.size()];
        int[] colIndex = new int[colList.size()];

        for (int i = 0; i < values.length; i++) {
            values[i] = valueList.get(i);
            colIndex[i] = colList.get(i);
        }

        return new CSRMatrix(values, colIndex, rowPtr, n, n);
    }
}


/**
 * Helper class: CSC Format
 */
class CSCMatrix {
    public double[] values;
    public int[] rowIndex;
    public int[] colPtr;

    public CSCMatrix(double[] values, int[] rowIndex, int[] colPtr) {
        this.values = values;
        this.rowIndex = rowIndex;
        this.colPtr = colPtr;
    }

    // CSR → CSC konvertieren
    public static CSCMatrix fromCSR(CSRMatrix csr) {
        int n = csr.rows;
        int nnz = csr.values.length;

        int[] colCount = new int[n];

        for (int i = 0; i < nnz; i++) {
            colCount[csr.colIndex[i]]++;
        }

        int[] colPtr = new int[n + 1];
        for (int i = 1; i <= n; i++) {
            colPtr[i] = colPtr[i - 1] + colCount[i - 1];
        }

        double[] values = new double[nnz];
        int[] rowIndex = new int[nnz];
        int[] nextPos = colPtr.clone();

        for (int row = 0; row < n; row++) {
            for (int idx = csr.rowPtr[row]; idx < csr.rowPtr[row + 1]; idx++) {
                int col = csr.colIndex[idx];
                int pos = nextPos[col]++;
                values[pos] = csr.values[idx];
                rowIndex[pos] = row;
            }
        }

        return new CSCMatrix(values, rowIndex, colPtr);
    }
}

