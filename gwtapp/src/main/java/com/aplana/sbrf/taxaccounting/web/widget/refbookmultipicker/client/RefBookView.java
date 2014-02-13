package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Интерфейс представления для компонента выбора значений из справочника
 * 
 * @author sgoryachkin
 */
public interface RefBookView extends HasValue<List<Long>>, LeafValueEditor<List<Long>>, IsWidget {

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
     *
     * !!!!! Не предназначен для мультиселекта
     *
     * @param alias наименование атрибута
     * @return строку с единичным значением(даже если выбрано куча)
     */
    String getOtherDereferenceValue(String alias);

    /**
     * Возвращает разименованное значение поля в выбранной строке по attrId
     *
     * !!!!! Не предназначен для мультиселекта
     *
     * @param attrId индентификатор атрибута
     * @return строку с единичным значением(даже если выбрано куча)
     */
    String getOtherDereferenceValue(Long attrId);

    Long getSingleValue();

    @Deprecated
    void setValue(Long value);
}
