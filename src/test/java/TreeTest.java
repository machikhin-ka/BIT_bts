import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeTest {

	@Test
	void oneThreadInsertTest() {
		//given
		TreeTimestamp tree = new TreeTimestamp();
		Set<Integer> integers = Stream.iterate(1, i -> i + 1).limit(100).collect(Collectors.toSet());
		//when
		integers.forEach(tree::insert);
		integers.forEach(tree::insert);
		//then
		assertEquals(integers, tree.traverse());
	}

	@Test
	void oneThreadRemoveTest() {
		//given
		TreeTimestamp tree = new TreeTimestamp();
		Set<Integer> integers = Stream.iterate(1, i -> i + 1).limit(100).collect(Collectors.toSet());
		integers.forEach(tree::insert);
		Set<Integer> removeIntegers = Stream.iterate(30, i -> i + 1).limit(40).collect(Collectors.toSet());
		//when
		removeIntegers.forEach(tree::remove);
		//then
		integers.removeIf(removeIntegers::contains);
		assertEquals(integers, tree.traverse());
	}

	@Test
	void oneThreadContainTest() {
		//given
		TreeTimestamp tree = new TreeTimestamp();
		Set<Integer> integers = Stream.iterate(1, i -> i + 1).limit(100).collect(Collectors.toSet());
		integers.forEach(tree::insert);
		//when
		boolean b = tree.contains(99);
		//then
		assertTrue(b);
	}

	@Test
	void oneThreadTraversalWithRouting() {
		//given
		TreeTimestamp tree = new TreeTimestamp();
		//when
		tree.insert(1);
		tree.insert(2);
		tree.insert(3);
		tree.remove(2);
		//then
		List<Integer> result = tree.traverseWithRouting();
		assertEquals(List.of(0, 2, 1, 3), result);
	}
}