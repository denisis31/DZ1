import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        int[] array = new Random()
                .ints(10, 0, 1_000)
                .toArray();
        System.out.println(Arrays.toString(array));
        Map<Integer, BigInteger> factCache = new ConcurrentSkipListMap<>();
        ExecutorService executorService = Executors.newFixedThreadPool(2 * Runtime.getRuntime().availableProcessors());
        List<Callable<Fact>> callables = IntStream.of(array)
                .mapToObj(number -> (Callable<Fact>) () -> new Fact(number, fact(number, factCache)))
                .collect(Collectors.toList());
        List<Future<Fact>> results = executorService.invokeAll(callables);
        while (!results.isEmpty()) {
            int i;
            for (i = 0; i < results.size(); i++) {
                Future<Fact> result = results.get(i);
                if (result.isDone()) { // ищем готовую
                    System.out.println(result.get());
                    break;
                }
            }
            if (i != results.size()) {
                results.remove(i);
        }
        executorService.shutdown();
    }


    private static BigInteger fact(int a, Map<Integer, BigInteger> factCache) {
        if (factCache.containsKey(a)) {
            return factCache.get(a);
        }
        if (!factCache.isEmpty()) {
            int max = Collections.max(factCache.keySet());
            BigInteger result = factCache.get(max);
            for (int i = max + 1; i <= a; i++) {
                result = result.multiply(BigInteger.valueOf(i));
                factCache.putIfAbsent(i, result);
            }
            return result;
        } else {
            BigInteger result = BigInteger.ONE;
            for (int i = 2; i <= a; i++) {
                result = result.multiply(BigInteger.valueOf(i));
                factCache.putIfAbsent(i, result);
            }
            return result;
        }
    }


    private static class Fact {

        int number; 
        BigInteger fact;

        public Fact(int number, BigInteger fact) {
            this.number = number;
            this.fact = fact;
        }

        @Override
        public String toString() {
            return "fact(" + number + ") = " + fact;
        }

    }

}
