package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.HasEnabled;


/**
 * Интерфейс компонента выбора значений из справочника
 * 
 * @author sgoryachkin
 * 
 */
public interface RefBookPickerPopup extends RefBookPicker, HasEnabled, LeafValueEditor<Long>{
	
	public void setDereferenceValue(String value);

}
