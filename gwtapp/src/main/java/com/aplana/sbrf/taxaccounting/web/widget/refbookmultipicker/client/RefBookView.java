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

    void load();


    void load(long refBookAttrId, String filter, Date startDate, Date endDate);

    /**
     * Разименновванное значение
	 * @return строка из одного или нескольких значений через ";"
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

    /**
     * Первый выделнный объект
     * обчно используется для одинарного режима выбора
     * @return ид объекта выбранной строки
     */
    Long getSingleValue();

    /**
     * Установка выделенной строки
     * @param value id объекта в строке
     */
    void setValue(Long value);

    public Long getAttributeId();

    public void setAttributeId(long attributeId);

    public Date getStartDate();

    public Date getEndDate();

    void setPeriodDates(Date startDate, Date endDate);

    public String getFilter();

    public void setFilter(String filter);
}
