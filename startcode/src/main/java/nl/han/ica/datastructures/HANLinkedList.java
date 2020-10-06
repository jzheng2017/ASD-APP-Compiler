package nl.han.ica.datastructures;

import java.util.Iterator;

public class HANLinkedList<T> implements IHANLinkedList<T> {
    private Node<T> first;
    private int size;

    public HANLinkedList() {
        this.first = null;
        this.size = 0;
    }

    @Override
    public void addFirst(T value) {
        if (this.first == null) {
            this.first = new Node<>(value);
        } else {
            Node<T> tmpFirst = new Node<>(value);
            tmpFirst.setNext(first);

            first = tmpFirst;
        }

        size++;
    }

    @Override
    public void clear() {
        this.first = null;
        this.size = 0;
    }

    @Override
    public void insert(int index, T value) {
        if (index > (size - 1)) {
            throw new IndexOutOfBoundsException();
        }

        if (index == 0) {
            addFirst(value);
            return;
        }

        Node<T> last = this.first;
        Node<T> lastPrev = null;
        int i = 0;
        while (i <= index && last.getNext() != null) {
            lastPrev = last;
            last = last.getNext();
            i++;
        }

        if (lastPrev == null) {
            throw new IllegalStateException();
        }

        Node<T> newNode = new Node<>(value);
        newNode.setNext(last);
        lastPrev.setNext(newNode);

        size++;
    }

    @Override
    public void delete(int pos) {
        if (first == null) {
            return;
        }

        if (pos > (size - 1)) {
            throw new IndexOutOfBoundsException();
        }

        if (pos == 0) {
            this.first = this.first.getNext();
            size--;
            return;
        }

        Node<T> last = this.first;
        int i = 0;

        while (i <= pos) {
            if (i == pos - 1) {
                last.setNext(null);
                size--;
                return;
            }

            last = last.getNext();
            i++;
        }
    }

    @Override
    public T get(int index) {
        if (first == null) {
            throw new IndexOutOfBoundsException();
        }

        if (index > (size - 1)) {
            throw new IndexOutOfBoundsException();
        }

        Node<T> last = this.first;

        int i = 0;
        while (i <= index) {
            if (last.getNext() == null && i != index) {
                break;
            }

            if (i == index) {
                return last.getData();
            }

            last = last.getNext();
            i++;
        }

        throw new IndexOutOfBoundsException();
    }

    @Override
    public void removeFirst() {
        if (this.first == null) {
            throw new IllegalStateException("Empty list!");
        } else {
            this.first = this.first.getNext();
            size--;
        }
    }

    @Override
    public T getFirst() {
        return this.first.getData();
    }

    @Override
    public int getSize() {
        return size;
    }


    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator<>(this.first);
    }
}
