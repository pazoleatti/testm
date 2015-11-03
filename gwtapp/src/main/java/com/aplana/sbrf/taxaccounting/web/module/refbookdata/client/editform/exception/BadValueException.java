package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BadValueException extends Exception implements Iterable<String> {
    private Set<String> strings = new HashSet<String>();

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

	public BadValueException(Map<String, String> descriptionMap) {
        for (Map.Entry<String, String> entry : descriptionMap.entrySet()){
            strings.add("Атрибут \"" + entry.getKey() + "\": " + entry.getValue());
        }
	}

    public BadValueException() {
    }

    @Override
    public Iterator<String> iterator() {
        return new BVIterator(strings.iterator());
    }
}
