package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import java.util.Date;
import java.util.List;

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
public interface RefBookPicker extends HasValue<List<Long>>, LeafValueEditor<List<Long>>, HasEnabled, IsWidget{

    @Deprecated
	public void setDereferenceValue(String value);

	public String getDereferenceValue();
	
    /**
     * Id отображаемого атрибута
     * @return
     */
    public Long getAttributeId();
    
    public void setAttributeId(long attributeId);

	public Date getStartDate();

	public Date getEndDate();

	public String getFilter();

	public void setFilter(String filter);

    void setPeriodDates(Date startDate, Date endDate);

    public Long getSingleValue();

    public void setValue(Long value);


}
