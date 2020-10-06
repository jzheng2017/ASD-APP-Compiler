package nl.han.ica.datastructures;

public class Node<T> {
    private T data;
    private Node<T> next;

    public Node(T key) {
        this.data = key;
        this.next = null;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Node<T> getNext() {
        return next;
    }

    public void setNext(Node<T> next) {
        this.next = next;
    }
}
