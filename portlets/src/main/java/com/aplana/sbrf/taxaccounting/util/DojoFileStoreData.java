package com.aplana.sbrf.taxaccounting.util;

import java.util.Collection;

/**
 * Ответ для dojo.FileReadStore/FileWriteStore
 * @param <T> - тип объектов для сериализации
 */
public class DojoFileStoreData<T> {
	private String identifier;
	private Collection<T> items;
	
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public Collection<T> getItems() {
		return items;
	}
	public void setItems(Collection<T> items) {
		this.items = items;
	}
}
