package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter;

import java.io.Serializable;

public class SelectItem<T> implements Serializable {
	private static final long serialVersionUID = -2957561387604546546L;

	private T id;
	private String name;

	public SelectItem() {
	};

	public SelectItem(T id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public T getId() {
		return id;
	}

	public void setId(T id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
