package com.aplana.sbrf.taxaccounting.web.widget.refbookpicker.client;

import java.util.Date;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Интерфейс компонента выбора значений из справочника
 * 
 * @author sgoryachkin
 * @deprecated
 * @see com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker
 */
@Deprecated
public interface RefBookPicker extends HasValue<Long>, LeafValueEditor<Long>, IsWidget {

	/**
	 * Устанавливает параметры и инициализирует компонент.
	 * В компоненте доступны версии справочника из диапазона дат.
	 * 
	 * @param refBookAttrId
	 * @param startDate начало ограничивающего периода
	 * @param endDate начало ограничивающего периода
	 */
	void setAcceptableValues(long refBookAttrId, Date startDate, Date endDate);
	
	
	/**
	 * Устанавливает параметры и инициализирует компонент.
	 * В компоненте доступны версии справочника c учетом фильтра.
	 * 
	 * 
	 * @param refBookAttrId
	 * @param filter
     * @param startDate начало ограничивающего периода
     * @param endDate начало ограничивающего периода
	 */
	void setAcceptableValues(long refBookAttrId, String filter, Date startDate, Date endDate);
	
	
	/**
	 * @return
	 */
	String getDereferenceValue();

    /**
     * Возвращает разименованное значение поля в выбранной строке по alias
     * @param alias
     * @return
     */
    String getOtherDereferenceValue(String alias);

    /**
     * Возвращает разименованное значение поля в выбранной строке по attrId
     * @param attrId
     * @return
     */
    String getOtherDereferenceValue(Long attrId);
}
