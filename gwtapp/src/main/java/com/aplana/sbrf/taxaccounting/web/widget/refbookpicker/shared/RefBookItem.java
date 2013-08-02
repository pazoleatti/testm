package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared;

import java.io.Serializable;
import java.util.List;


/**
 * GUI модель для строки справочника
 * 
 * @author sgoryachkin
 *
 */
public class RefBookItem implements Serializable{
	private static final long serialVersionUID = 6686089751137927944L;
	
	private Long id;
	
	private String dereferenceValue;
	
	private List<String> values;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public String getDereferenceValue() {
		return dereferenceValue;
	}

	public void setDereferenceValue(String dereferenceValue) {
		this.dereferenceValue = dereferenceValue;
	}
	
}
