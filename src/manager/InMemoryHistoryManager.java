package manager;

import manager.HistoryManager;
import model.Task;
import java.util.*;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    private Node head;
    private Node tail;
    private final Map<Integer, Node> taskIdToNode = new HashMap<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        remove(task.getId());

        Node newNode = new Node(task);
        if (head == null) {
            head = tail = newNode;
        } else {
            newNode.prev = tail;
            tail.next = newNode;
            tail = newNode;
        }

        taskIdToNode.put(task.getId(), newNode);
    }

    @Override
    public void remove(int id) {
        Node node = taskIdToNode.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }
}