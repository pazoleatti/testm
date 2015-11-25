package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.handler.DeferredInvokeHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client.event.CheckValuesCountHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.IsWidget;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Интерфейс представления для компонента выбора значений из справочника
 * 
 * @author sgoryachkin
 */
public interface RefBookView extends HasValueChangeHandlers<Set<Long>>, IsWidget {

    void load(PickerState pickerState, boolean force);

    void reload();

    void reload(List<Long> needToSelectIds);

    void find(String searchPattern);

    void reloadOnDate(Date version);

    void clearSelected(boolean fireChangeEvent);

    Set<Long> getSelectedIds();

    /**
     * Определяет единственную колонку, отображаемую в виджете
     * @param columnAlias алиас колонки
     */
    void setSingleColumn(String columnAlias);

    /**
     * Разименновванное значение
	 * @return строка из одного или нескольких значений через ";"
	 */
	String getDereferenceValue();

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
     * Возвращает разименованное значение поля в выбранной строке по attrId и attrId2
     *
     * !!!!! Не предназначен для мультиселекта
     *
     * @param attrId основной атррубут для отображения
     * @param attrId2 аттрибут второго уровня
     * @return строку с единичным значением(даже если выбрано куча)
     */
    String getOtherDereferenceValue(Long attrId, Long attrId2);

    /**
     * Смена режима выбора значений.
     * При смене происходит очищение ранее выделенных значений
     * @param multiSelect true - множественный выбор
     */
    void setMultiSelect(Boolean multiSelect);

    /**
     * Выделить все записи, если мультиселект
     */
    void selectAll(DeferredInvokeHandler handler);

    /**
     * Развыделить все записи, если мультиселект
     */
    void unselectAll(DeferredInvokeHandler handler);

    /**
     * проверка на количество попадающих под фильтр значений
     * @param text текст
     * @param checkValuesCountHandler хендлер который исполниться после получения количества попавщих под фильтр записей
     */
    void checkCount(String text, Date relevanceDate, CheckValuesCountHandler checkValuesCountHandler);

    void cleanValues();
}
