package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractBadValueException extends Exception implements Iterable<String> {
    protected Set<String> strings = new LinkedHashSet<String>();

    private class BVIterator implements Iterator<String> {

        private Iterator<String> iterator;

        private BVIterator(Iterator<String> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public String next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public AbstractBadValueException(Map<String, String> descriptionMap) {
        for (Map.Entry<String, String> entry : descriptionMap.entrySet()){
            if (entry.getKey().isEmpty()) {
                strings.add(entry.getValue());
            } else {
                strings.add("Атрибут \"" + entry.getKey() + "\": " + entry.getValue());
            }
        }
    }

    public AbstractBadValueException() {
    }

    @Override
    public Iterator<String> iterator() {
        return new AbstractBadValueException.BVIterator(strings.iterator());
    }
}
