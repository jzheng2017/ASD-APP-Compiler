package nl.han.ica.datastructures;

import java.util.Iterator;

public class LinkedListIterator<T> implements Iterator<T> {
    private Node<T> currentNode;

    public LinkedListIterator(Node<T> currentNode) {
        this.currentNode = currentNode;
    }

    @Override
    public boolean hasNext() {
        return currentNode != null;
    }

    @Override
    public T next() {
        T data = currentNode.getData();
        currentNode = currentNode.getNext();
        return data;
    }
}
