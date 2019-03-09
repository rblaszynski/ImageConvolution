import java.util.concurrent.CyclicBarrier;

class ImageFilter implements Runnable {
    private int start;
    private int end;
    private int width;
    private Threshold image;
    private CyclicBarrier barrier;
    private int iterations;

    ImageFilter(int start, int height, int width, Threshold image, CyclicBarrier barrier, int iterations) {
        if (start == 0) this.start = start + 1;
        else {
            this.start = start;
        }
        if (height == width) this.end = height - 1;
        else {
            this.end = height;
        }
        this.width = width - 1;
        this.image = image;
        this.barrier = barrier;
        this.iterations = iterations;
    }

    private int calcConvolution(int value, int[][] array, int x, int y) {
        return (int) (value * (0.6) + (0.1) *
                (((array)[x - 1][y]) + (array[x][y - 1]) +
                        (array[x + 1][y]) + (array[x][y + 1])));
    }

    public void run() {
        try {
            int count = barrier.await();
            if (count == 0) {
                barrier.reset();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Threshold actual = image;
        Threshold previous = image;
        for (int i = 0; i < iterations; i++) {
            for (int x = start; x < end; x++) {
                for (int y = 1; y < width; y++) {
                    actual.getImageArr()[x][y] = calcConvolution(previous.getImageArr()[x][y], previous.getImageArr(), x, y);
                }
            }
            previous = actual;
        }
        try {
            barrier.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
