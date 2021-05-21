import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Load {

	private final static int K = 100_000;
	private final static AtomicLong ops = new AtomicLong(0);

	@AllArgsConstructor
	private static class ThreadRunnable implements Runnable {
		private final double x;
		private final Tree tree;


		@Override
		public void run() {
			Random random = new Random();
			long start = System.currentTimeMillis();
			while (System.currentTimeMillis() - start < 5000) {
				int key = random.nextInt(K);
				double p = random.nextDouble();
				if (p < x) {
					tree.insert(key);
				} else if (x <= p && p < 2 * x) {
					tree.remove(key);
				} else if (2 * x <= p && p <= 1) {
					tree.contains(key);
				}
				ops.getAndIncrement();
			}
		}
	}

	@Test
	void load() throws InterruptedException {
		Tree tree = new Tree();
		prepopulate().forEach(tree::insert);
		List<Thread> threads = new ArrayList<>();
		threads.add(new Thread(new ThreadRunnable(0, tree)));
//		threads.add(new Thread(new ThreadRunnable(0, tree)));
//		threads.add(new Thread(new ThreadRunnable(0, tree)));
//		threads.add(new Thread(new ThreadRunnable(0, tree)));
		threads.forEach(Thread::start);
		for (Thread thread : threads) {
			thread.join();
		}
		System.out.println(ops);
	}

	private List<Integer> prepopulate() {
		Random random = new Random();
		return Stream.iterate(1, integer -> integer + 1).limit(K).filter(e -> {
			double p = random.nextDouble();
			return p > 0.5;
		}).collect(Collectors.toList());
	}
}
