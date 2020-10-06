package nl.han.ica.datastructures;

public class HANStack<T> implements IHANStack<T> {
    private IHANLinkedList<T> list;

    public HANStack() {
        list = new HANLinkedList<>();
    }

    @Override
    public void push(T element) {
        list.addFirst(element);
    }

    @Override
    public T pop() {
        T temp = list.getFirst();
        list.removeFirst();

        return temp;
    }

    @Override
    public T peek() {
        return list.getFirst();
    }

    public boolean empty() {
        return list.getSize() == 0;
    }
}
