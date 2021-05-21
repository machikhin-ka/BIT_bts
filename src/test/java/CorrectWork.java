import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CorrectWork {

	private final static int K = 1000;
	private final static AtomicLong ops = new AtomicLong(0);

	@Data
	@AllArgsConstructor
	private static class Action {
		private final int numAction;
		private final long timestamp;
		private final int key;
	}

	@AllArgsConstructor
	private static class ThreadRunnable implements Runnable {
		private final double x;
		private final TreeTimestamp tree;
		private final List<Action> actions;


		@Override
		public void run() {
			Random random = new Random();
			while (ops.get() < 200) {
				int key = random.nextInt(K);
				double p = random.nextDouble();
				if (p < x) {
					actions.add(new Action(0, tree.insert(key), key));
				} else if (x <= p && p < 2 * x) {
					actions.add(new Action(1, tree.remove(key), key));
				} else if (2 * x <= p && p <= 1) {
					tree.contains(key);
				}
				ops.getAndIncrement();
			}
		}
	}

	@Test
	void load() throws InterruptedException {
		//give
		TreeTimestamp tree = new TreeTimestamp();
		List<Integer> prepopulate = prepopulate();
		prepopulate.forEach(tree::insert);
		List<Thread> threads = new ArrayList<>();
		List<Action> actions1 = new ArrayList<>();
		List<Action> actions2 = new ArrayList<>();
		//when
		threads.add(new Thread(new ThreadRunnable(0.5, tree, actions1)));
		threads.add(new Thread(new ThreadRunnable(0.5, tree, actions2)));
		threads.forEach(Thread::start);
		for (Thread thread : threads) {
			thread.join();
		}
		//then
		actions1.addAll(actions2);
		actions1.sort((e1, e2) -> (e1.timestamp < e2.timestamp) ? 1 : 0);
		Set<Integer> integers = doSingleThread(prepopulate, actions1.stream().filter(e -> e.timestamp != -1).collect(Collectors.toList()));
		assertEquals(integers, tree.traverse());
	}

	private List<Integer> prepopulate() {
		Random random = new Random();
		return Stream.iterate(1, integer -> integer + 1).limit(K).filter(e -> {
			double p = random.nextDouble();
			return p > 0.5;
		}).collect(Collectors.toList());
	}

	private Set<Integer> doSingleThread(List<Integer> prepopulate, List<Action> actions) {
		TreeTimestamp tree = new TreeTimestamp();
		System.out.println(actions);
		prepopulate.forEach(tree::insert);
		for (Action action : actions) {
			switch (action.numAction) {
				case 0 -> tree.insert(action.key);
				case 1 -> tree.remove(action.key);
			}
		}
		return tree.traverse();
	}
}
