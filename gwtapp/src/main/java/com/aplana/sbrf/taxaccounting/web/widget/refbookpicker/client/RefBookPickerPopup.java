package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import java.util.Date;

import com.aplana.sbrf.taxaccounting.web.widget.titlepanel.HasModalMode;
import com.aplana.sbrf.taxaccounting.web.widget.titlepanel.HasTitlePanel;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;


/**
 * Интерфейс компонента выбора значений из справочника
 * 
 * @author sgoryachkin
 * 
 */
public interface RefBookPickerPopup extends HasValue<Long>, IsWidget, HasEnabled, LeafValueEditor<Long>,
        HasTitlePanel, HasModalMode {
	
	public void setDereferenceValue(String value);
	public String getDereferenceValue();
	
    /**
     * Id отображаемого атрибута
     * @return
     */
    public Long getAttributeId();
    
    public void setAttributeId(long attributeId);

	public Date getDate1();

	public void setDate1(Date date1);

	public Date getDate2();

	public void setDate2(Date date2);

	public String getFilter();

	public void setFilter(String filter);

    public void setTitlePanelVisibility(boolean visible);

    public void setTitleText(String title);

}
