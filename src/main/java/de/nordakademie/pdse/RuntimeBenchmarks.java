package de.nordakademie.pdse;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark class for Matrix multiplication optimizations.
 * Uses JMH to test: Classic, Strassen, Loop Unrolling, and Tiling algorithms.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class RuntimeBenchmarks {

    @Param({"128", "256", "512", "1024", "4096"})
    private int n;

    private double[][] A;
    private double[][] B;

    @Setup(Level.Trial)
    public void setUp() {
        A = MatrixOptimized.generateMatrix(n);
        B = MatrixOptimized.generateMatrix(n);
    }

    @Benchmark
    public double[][] classicMultiplication() {
        return MatrixOptimized.multiplyClassic(A, B);
    }

    @Benchmark
    public double[][] strassenMultiplication() {
        return MatrixOptimized.multiplyStrassen(A, B);
    }

    @Benchmark
    public double[][] loopUnrollingMultiplication() {
        return MatrixOptimized.multiplyLoopUnrolling(A, B);
    }

    @Benchmark
    public double[][] tiledMultiplication() {
        return MatrixOptimized.multiplyTiled(A, B, 64);
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
