import java.io.File;
import java.io.IOException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.nanoTime;

class Main {
    public static void main(String[] args) throws IOException {
        String filename = "man.pgm";
        int[][] tab = PGMIO.read(new File(filename));
        Threshold image = new Threshold(tab);

        int width = tab.length;
        int height = tab.length;
        int iterations = 20;

        int n = Runtime.getRuntime().availableProcessors();

        ExecutorService threadPool = Executors.newFixedThreadPool(n);

        System.out.println("Number of threads : " + n);

        CyclicBarrier cyclicBarrier = new CyclicBarrier(n, new Runnable() {
            private int count = 1;

            long start = nanoTime();

            public void run() {
                if (count != 1) {
                    try {
                        File file = new File(filename.split("\\.")[0] + "_threads_0" + n + ".pgm");
                        PGMIO.write(image.getImageArr(), file);
                        System.out.println("time: " + (double) (nanoTime() - start) / 1_000_000_000 + "s");
                    } catch (IOException e) {
                        System.err.println("Error while writing : " + e);
                    }
                    threadPool.shutdownNow();
                }
                count++;
            }
        });

        for (int i = 0; i < n; i++) {
            if (n == 1) {
                threadPool.submit(new ImageFilter(0, height, width, image, cyclicBarrier, iterations));
                break;
            }
            if (i == 0) {
                threadPool.submit(new ImageFilter(0, height / n, width, image, cyclicBarrier, iterations));
            } else {
                threadPool.submit(new ImageFilter(((height / n) * i), ((height / n) * (i + 1)), width, image, cyclicBarrier, iterations));
            }
        }
    }
}

