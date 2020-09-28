package uk.gov.gchq.palisade.client.java;

import uk.gov.gchq.palisade.client.java.resource.*;

import java.util.Iterator;

/*
 * TODO:
 *
 * This could be a fifo blocking queue.
 * The state would the queue. The Kafka reader would then write to the queue.
 */
public class ResourceGenerator implements Iterator<Resource> {

    private final int size;
    private int count = 0;

    public ResourceGenerator(int size) {
        this.size = size;
    }

    @Override
    public Resource next() {
        return IResource.create("Resource " + ++count);
    }

    @Override
    public boolean hasNext() {
        return size == 0 || count < size;
    }

}