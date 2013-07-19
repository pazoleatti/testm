package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared;

import java.io.Serializable;


/**
 * GUI модель для строки справочника
 * 
 * @author sgoryachkin
 *
 */
public class RefBookItem implements Serializable{
	private static final long serialVersionUID = 6686089751137927944L;
	
	private Long id;
	
	private String name;

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

	@Override
	public String toString() {
		return "RefBookItem [id=" + id + ", name=" + name + "]";
	}
	
}
