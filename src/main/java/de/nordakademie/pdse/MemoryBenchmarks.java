package de.nordakademie.pdse;

import com.sun.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.io.FileWriter;

public class MemoryBenchmarks {

    private static final int[] SIZES = {128, 256, 512, 1024, 2048, 4096};

    public static void main(String[] args) throws Exception {

        FileWriter csv = new FileWriter("memory_alloc_results.csv");
        csv.write("Algorithm;MatrixSize;AllocatedBytes\n");

        long threadId = Thread.currentThread().getId();
        ThreadMXBean threadBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();

        if (!threadBean.isThreadAllocatedMemorySupported()) {
            throw new RuntimeException("Thread allocated memory measurement not supported on this JVM!");
        }
        threadBean.setThreadAllocatedMemoryEnabled(true);

        for (int n : SIZES) {
            double[][] A = MatrixOptimized.generateMatrix(n);
            double[][] B = MatrixOptimized.generateMatrix(n);

            benchmarkAlloc(csv, threadBean, threadId, "Classic", n, () -> MatrixOptimized.multiplyClassic(A, B));
            benchmarkAlloc(csv, threadBean, threadId, "Strassen", n, () -> MatrixOptimized.multiplyStrassen(A, B));
            benchmarkAlloc(csv, threadBean, threadId, "LoopUnrolling", n, () -> MatrixOptimized.multiplyLoopUnrolling(A, B));
            benchmarkAlloc(csv, threadBean, threadId, "Tiled", n, () -> MatrixOptimized.multiplyTiled(A, B, 64));

            System.out.println("Finished n=" + n);
        }

        csv.close();
        System.out.println("Done! Output → memory_alloc_results.csv");
    }

    private static void benchmarkAlloc(FileWriter csv, ThreadMXBean bean, long threadId,
                                       String name, int n, Runnable task) throws Exception {

        long before = bean.getThreadAllocatedBytes(threadId);
        task.run();
        long after = bean.getThreadAllocatedBytes(threadId);

        long allocated = after - before;

        System.out.printf("%s | n=%d | Allocated = %d bytes%n", name, n, allocated);
        csv.write(name + ";" + n + ";" + allocated + "\n");
    }
}
