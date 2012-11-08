package com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.filter;

import java.io.Serializable;

public class SelectItem implements Serializable {
	private static final long serialVersionUID = -2957561387604546546L;

	private Long id;
	private String name;

	public SelectItem() {
	};

	public SelectItem(Long id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
