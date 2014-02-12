package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.shared;

import java.io.Serializable;
import java.util.List;

/**
 * GUI модель для строки справочника
 * 
 * @author sgoryachkin
 * @deprecated
 * @see com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookItem
 */
@Deprecated
public class RefBookItem implements Serializable{
	private static final long serialVersionUID = 6686089751137927944L;
	
	private Long id;
	
	private String dereferenceValue;

    // Порядок соответсвия для всех листов должен гарантироваться
	private List<String> values;
    private List<Long> valuesAttrId;
    private List<String> valuesAttrAlias;

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

    public List<Long> getValuesAttrId() {
        return valuesAttrId;
    }

    public void setValuesAttrId(List<Long> valuesAttrId) {
        this.valuesAttrId = valuesAttrId;
    }

    public List<String> getValuesAttrAlias() {
        return valuesAttrAlias;
    }

    public void setValuesAttrAlias(List<String> valuesAttrAlias) {
        this.valuesAttrAlias = valuesAttrAlias;
    }
}
