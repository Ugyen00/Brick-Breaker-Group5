package com.example.brickbreaker;

public class BrickMap {
    private static final int INITIAL_CAPACITY = 50;
    private static final float LOAD_FACTOR = 0.75f;

    private Entry[] entries;
    private int size;

    public BrickMap() {
        entries = new Entry[INITIAL_CAPACITY];
        size = 0;
    }

    public void put(int key, Brick value) {
        if (size >= entries.length * LOAD_FACTOR) {
            resize();
        }

        int index = getIndex(key);
        if (entries[index] == null) {
            entries[index] = new Entry(key, value);
            size++;
        } else {
            Entry current = entries[index];
            while (current.next != null) {
                if (current.key == key) {
                    current.value = value;
                    return;
                }
                current = current.next;
            }
            current.next = new Entry(key, value);
            size++;
        }
    }

    public Brick get(int key) {
        int index = getIndex(key);
        Entry current = entries[index];
        while (current != null) {
            if (current.key == key) {
                return current.value;
            }
            current = current.next;
        }
        return null;
    }

    public int size() {
        return size;
    }

    private int getIndex(int key) {
        return key % entries.length;
    }

    private void resize() {
        Entry[] oldEntries = entries;
        entries = new Entry[oldEntries.length * 2];

        for (Entry entry : oldEntries) {
            Entry current = entry;
            while (current != null) {
                put(current.key, current.value);
                current = current.next;
            }
        }
    }

    private static class Entry {
        int key;
        Brick value;
        Entry next;

        Entry(int key, Brick value) {
            this.key = key;
            this.value = value;
            this.next = null;
        }
    }
}

