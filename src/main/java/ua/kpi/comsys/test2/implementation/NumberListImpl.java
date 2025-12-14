/*
 * Copyright (c) 2014, NTUU KPI, Computer systems department and/or its affiliates. All rights reserved.
 * NTUU KPI PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package ua.kpi.comsys.test2.implementation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

import ua.kpi.comsys.test2.NumberList;

/**
 * Custom implementation of INumberList interface.
 * Variant: 3.
 * List Type: Лінійний двонаправлений.
 * Base System: Десяткова.
 * Operation: Ціла частина від ділення.
 * Secondary System: Шістнадцяткова.
 *
 * @author Варчук Денис, Група ІА-31
 */
public class NumberListImpl implements NumberList {

    private static class Node {
        Byte data;
        Node next;
        Node prev;

        Node(Byte data) {
            this.data = data;
        }
    }

    private Node head;
    private Node tail;
    private int size;
    private int base;

    public NumberListImpl() {
        this.head = null;
        this.tail = null;
        this.size = 0;
        this.base = 10;
    }

    private NumberListImpl(int base) {
        this();
        this.base = base;
    }

    public NumberListImpl(File file) {
        this();
        try (Scanner scanner = new Scanner(file)) {
            scanner.useDelimiter("\\Z");
            if (scanner.hasNext()) {
                String val = scanner.next().trim();
                parseAndFill(val);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public NumberListImpl(String value) {
        this();
        parseAndFill(value);
    }

    private void parseAndFill(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        char[] chars = value.toCharArray();
        for (char c : chars) {
            if (Character.isDigit(c)) {
                this.add((byte) Character.getNumericValue(c));
            } else {
                this.clear();
                return;
            }
        }
    }

    public void saveList(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print(this.toDecimalString());
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file", e);
        }
    }

    public static int getRecordBookNumber() {
        return 3;
    }

    public NumberListImpl changeScale() {
        if (isEmpty()) return new NumberListImpl(16);

        String decimalStr = this.toDecimalString();
        if (decimalStr == null || decimalStr.isEmpty()) {
            NumberListImpl emptyHex = new NumberListImpl(16);
            emptyHex.add((byte)0);
            return emptyHex;
        }

        BigInteger val = new BigInteger(decimalStr);
        String hexString = val.toString(16).toUpperCase();

        NumberListImpl newList = new NumberListImpl(16);
        for (char c : hexString.toCharArray()) {
            byte digit = (byte) Character.digit(c, 16);
            newList.add(digit);
        }
        return newList;
    }

    public NumberListImpl additionalOperation(NumberList arg) {
        String num1Str = this.toDecimalString();
        String num2Str;

        if (arg instanceof NumberListImpl) {
            num2Str = ((NumberListImpl) arg).toDecimalString();
        } else {
            StringBuilder sb = new StringBuilder();
            for (Byte b : arg) {
                sb.append(b);
            }
            num2Str = sb.toString();
        }

        if (num1Str.isEmpty()) num1Str = "0";
        if (num2Str == null || num2Str.isEmpty()) num2Str = "0";

        BigInteger bigNum1 = new BigInteger(num1Str);
        BigInteger bigNum2 = new BigInteger(num2Str);

        if (bigNum2.equals(BigInteger.ZERO)) {
            throw new ArithmeticException("Division by zero");
        }

        BigInteger result = bigNum1.divide(bigNum2);

        return new NumberListImpl(result.toString());
    }

    public String toDecimalString() {
        if (isEmpty()) return "";

        if (this.base == 10) {
            StringBuilder sb = new StringBuilder();
            for (Byte b : this) {
                sb.append(b);
            }
            return sb.toString();
        }

        BigInteger result = BigInteger.ZERO;
        BigInteger bigBase = BigInteger.valueOf(this.base);

        for (Byte b : this) {
            result = result.multiply(bigBase).add(BigInteger.valueOf(b));
        }

        return result.toString();
    }

    @Override
    public String toString() {
        if (isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (Byte b : this) {
            if (b >= 0 && b <= 9) {
                sb.append(b);
            } else {
                sb.append((char) ('A' + (b - 10)));
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof List)) return false;

        List<?> that = (List<?>) o;
        if (this.size() != that.size()) return false;

        Iterator<Byte> i1 = this.iterator();
        Iterator<?> i2 = that.iterator();

        while (i1.hasNext()) {
            Object o1 = i1.next();
            Object o2 = i2.next();
            if (!Objects.equals(o1, o2)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (Byte e : this)
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        return hashCode;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Iterator<Byte> iterator() {
        return new Iterator<Byte>() {
            private Node current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public Byte next() {
                if (current == null) throw new NoSuchElementException();
                Byte data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] arr = new Object[size];
        int i = 0;
        for (Node x = head; x != null; x = x.next)
            arr[i++] = x.data;
        return arr;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Byte e) {
        Node newNode = new Node(e);
        if (head == null) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (o == null) return false;
        for (Node x = head; x != null; x = x.next) {
            if (o.equals(x.data)) {
                unlink(x);
                return true;
            }
        }
        return false;
    }

    private void unlink(Node x) {
        final Node next = x.next;
        final Node prev = x.prev;

        if (prev == null) {
            head = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            tail = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        x.data = null;
        size--;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object e : c)
            if (!contains(e))
                return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Byte> c) {
        boolean modified = false;
        for (Byte e : c)
            if (add(e)) modified = true;
        return modified;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Byte> c) {
        checkPositionIndex(index);
        boolean modified = false;
        for (Byte e : c) {
            add(index++, e);
            modified = true;
        }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;
        for (Object e : c) {
            while (contains(e)) {
                remove(e);
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Node current = head;
        while (current != null) {
            Node next = current.next;
            if (!c.contains(current.data)) {
                unlink(current);
                modified = true;
            }
            current = next;
        }
        return modified;
    }

    @Override
    public void clear() {
        Node x = head;
        while (x != null) {
            Node next = x.next;
            x.data = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        head = tail = null;
        size = 0;
    }

    @Override
    public Byte get(int index) {
        checkElementIndex(index);
        return node(index).data;
    }

    @Override
    public Byte set(int index, Byte element) {
        checkElementIndex(index);
        Node x = node(index);
        Byte oldVal = x.data;
        x.data = element;
        return oldVal;
    }

    @Override
    public void add(int index, Byte element) {
        checkPositionIndex(index);
        if (index == size)
            add(element);
        else {
            Node succ = node(index);
            final Node pred = succ.prev;
            final Node newNode = new Node(element);
            newNode.next = succ;
            succ.prev = newNode;
            newNode.prev = pred;
            if (pred == null)
                head = newNode;
            else
                pred.next = newNode;
            size++;
        }
    }

    @Override
    public Byte remove(int index) {
        checkElementIndex(index);
        Node x = node(index);
        Byte element = x.data;
        unlink(x);
        return element;
    }

    @Override
    public int indexOf(Object o) {
        int index = 0;
        for (Node x = head; x != null; x = x.next) {
            if (o.equals(x.data))
                return index;
            index++;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        int index = size;
        for (Node x = tail; x != null; x = x.prev) {
            index--;
            if (o.equals(x.data))
                return index;
        }
        return -1;
    }

    @Override
    public ListIterator<Byte> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<Byte> listIterator(int index) {
        checkPositionIndex(index);
        return new ListIterator<Byte>() {
            private Node lastReturned;
            private Node next = (index == size) ? null : node(index);
            private int nextIndex = index;

            public boolean hasNext() { return nextIndex < size; }
            public Byte next() {
                if (!hasNext()) throw new NoSuchElementException();
                lastReturned = next;
                next = next.next;
                nextIndex++;
                return lastReturned.data;
            }
            public boolean hasPrevious() { return nextIndex > 0; }
            public Byte previous() {
                if (!hasPrevious()) throw new NoSuchElementException();
                lastReturned = next = (next == null) ? tail : next.prev;
                nextIndex--;
                return lastReturned.data;
            }
            public int nextIndex() { return nextIndex; }
            public int previousIndex() { return nextIndex - 1; }
            public void remove() { throw new UnsupportedOperationException(); }
            public void set(Byte e) {
                if (lastReturned == null) throw new IllegalStateException();
                lastReturned.data = e;
            }
            public void add(Byte e) { throw new UnsupportedOperationException(); }
        };
    }

    @Override
    public List<Byte> subList(int fromIndex, int toIndex) {
        NumberListImpl sub = new NumberListImpl();
        Node x = node(fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            sub.add(x.data);
            x = x.next;
        }
        return sub;
    }

    @Override
    public boolean swap(int index1, int index2) {
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) return false;
        if (index1 == index2) return true;

        Node node1 = node(index1);
        Node node2 = node(index2);

        Byte temp = node1.data;
        node1.data = node2.data;
        node2.data = temp;
        return true;
    }

    @Override
    public void sortAscending() {
        if (size <= 1) return;
        boolean swapped;
        do {
            swapped = false;
            Node current = head;
            while (current != null && current.next != null) {
                if (current.data > current.next.data) {
                    Byte temp = current.data;
                    current.data = current.next.data;
                    current.next.data = temp;
                    swapped = true;
                }
                current = current.next;
            }
        } while (swapped);
    }

    @Override
    public void sortDescending() {
        if (size <= 1) return;
        boolean swapped;
        do {
            swapped = false;
            Node current = head;
            while (current != null && current.next != null) {
                if (current.data < current.next.data) {
                    Byte temp = current.data;
                    current.data = current.next.data;
                    current.next.data = temp;
                    swapped = true;
                }
                current = current.next;
            }
        } while (swapped);
    }

    @Override
    public void shiftLeft() {
        if (size <= 1) return;

        Node oldHead = head;
        head = head.next;
        head.prev = null;

        tail.next = oldHead;
        oldHead.prev = tail;
        oldHead.next = null;
        tail = oldHead;
    }

    @Override
    public void shiftRight() {
        if (size <= 1) return;

        Node oldTail = tail;
        tail = tail.prev;
        tail.next = null;

        oldTail.next = head;
        head.prev = oldTail;
        oldTail.prev = null;
        head = oldTail;
    }

    private Node node(int index) {
        if (index < (size >> 1)) {
            Node x = head;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node x = tail;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
    }

    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }
}
