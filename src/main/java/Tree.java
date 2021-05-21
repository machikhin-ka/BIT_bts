import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.*;

public class Tree {
	@Data
	@NoArgsConstructor
	static class Node {
		private int key;
		private Node right;
		private Node left;
		private boolean isDeleted = false;

		public Node(int key, Node right, Node left) {
			this.key = key;
			this.right = right;
			this.left = left;
		}

		public boolean isLeaf() {
			return right == null && left == null;
		}

		public Node copy() {
			return new Node(key, right, left);
		}
	}

	@Data
	@RequiredArgsConstructor
	static class Window {
		private final Node gprev;
		private final Node prev;
		private final Node curr;
	}

	private final Node left = new Node(-1, null, null);
	private final Node right = new Node(Integer.MAX_VALUE, null, null);
	private final Node root = new Node(0, right, left);

	public Window search(int key) {
		Node gprev = null, prev = null, curr = root;
		while (curr != null && (curr.key != key || !curr.isLeaf())) {
			if (curr.key <= key) {
				gprev = prev;
				prev = curr;
				curr = curr.right;
			} else {
				gprev = prev;
				prev = curr;
				curr = curr.left;
			}
		}
		return new Window(gprev, prev, curr);
	}

	public boolean contains(int key) {
		return search(key).curr != null;
	}

	public boolean insert(int key) {
		while (true) {
			Window window = search(key);
			if (window.curr != null) return false;

			synchronized (window.gprev) {
				Node gprev = window.gprev;
				boolean isRight = key >= gprev.key;
				if (window.prev.key != (isRight ? gprev.right.key : gprev.left.key) || gprev.isDeleted()) continue;
				synchronized (window.prev) {
					Node prev = window.prev;
					Node newNode = new Node(key, null, null);
					if (!prev.isLeaf() || prev.isDeleted()) continue;
					if (key > prev.key) {
						if (isRight) {
							gprev.right = newNode;
						} else {
							gprev.left = newNode;
						}
						newNode.right = newNode.copy();
						newNode.left = prev;
					} else {
						prev.right = prev.copy();
						prev.left = newNode;
					}
					return true;
				}
			}
		}
	}

	public boolean remove(int key) {
		while (true) {
			Window window = search(key);
			if (window.curr == null) return false;

			synchronized (window.gprev) {
				Node gprev = window.gprev;
				boolean isRightPrev = key >= gprev.key;
				if (window.prev.key != (isRightPrev ? gprev.right.key : gprev.left.key) || gprev.isDeleted()) continue;
				synchronized (window.prev) {
					Node prev = window.prev;
					boolean isRightCurr = key >= prev.key;
					if (window.curr.key != (isRightCurr ? prev.right.key : prev.left.key) || prev.isDeleted()) continue;
					synchronized (window.curr) {
						Node curr = window.curr;
						if (!curr.isLeaf() || curr.isDeleted()) continue;
						if (isRightPrev) {
							if (isRightCurr) {
								gprev.right = prev.left;
							} else {
								gprev.right = prev.right;
							}
						} else {
							if (isRightCurr) {
								gprev.left = prev.left;
							} else {
								gprev.left = prev.right;
							}
							prev.setDeleted(true);
						}
						return true;
					}
				}
			}
		}
	}

	public Set<Integer> traverse() {
		Set<Integer> result = new HashSet<>();
		Stack<Node> s = new Stack<>();
		s.push(root);
		while (!s.isEmpty()) {
			Node node = s.pop();
			if (node.isLeaf()) {
				result.add(node.key);
			}
			if (node.right != null) {
				s.push(node.right);
			}
			if (node.left != null) {
				s.push(node.left);
			}
		}
		result.removeIf(x -> x == -1);
		result.removeIf(x -> x == Integer.MAX_VALUE);
		return result;
	}

	public List<Integer> traverseWithRouting() {
		List<Integer> result = new ArrayList<>();
		Stack<Node> s = new Stack<>();
		s.push(root);
		while (!s.isEmpty()) {
			Node node = s.pop();
			result.add(node.key);
			if (node.right != null) {
				s.push(node.right);
			}
			if (node.left != null) {
				s.push(node.left);
			}
		}
		result.removeIf(x -> x == -1);
		result.removeIf(x -> x == Integer.MAX_VALUE);
		return result;
	}
}
