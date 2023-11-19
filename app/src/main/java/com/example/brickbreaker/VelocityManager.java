package com.example.brickbreaker;

public class VelocityManager {
    private Node head;
    private int size;

    private static class Node {
        Velocity velocity;
        Node next;

        Node(Velocity velocity) {
            this.velocity = velocity;
            this.next = null;
        }
    }

    public VelocityManager() {
        this.head = null;
        this.size = 0;
    }

    public void addVelocityChange(Velocity velocityChange) {
        Node newNode = new Node(velocityChange);
        if (head == null) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }

    public void increaseVelocity(int incrementX, int incrementY) {
        Node current = head;
        while (current != null) {
            current.velocity.setX(current.velocity.getX() + incrementX);
            current.velocity.setY(current.velocity.getY() + incrementY);
            current = current.next;
        }
    }
}
