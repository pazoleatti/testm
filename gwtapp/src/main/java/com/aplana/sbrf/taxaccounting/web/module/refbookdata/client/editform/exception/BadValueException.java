package com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.editform.exception;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class BadValueException extends Exception implements Iterable<String> {
    private Set<String> strings = new HashSet<String>();

	public BadValueException(Map<String, String> descriptionMap) {
        for (Map.Entry<String, String> entry : descriptionMap.entrySet()){
            strings.add("Атрибут \"" + entry.getKey() + "\": " + entry.getValue());
        }
	}

    public BadValueException() {
    }

    @Override
    public Iterator<String> iterator() {
        return strings.iterator();
    }
}
