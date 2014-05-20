package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.aplana.sbrf.taxaccounting.web.main.api.client.handler.DeferredInvokeHandler;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.PickerState;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Интерфейс представления для компонента выбора значений из справочника
 * 
 * @author sgoryachkin
 */
public interface RefBookView extends HasValueChangeHandlers<Set<Long>>, IsWidget {

    void load(PickerState pickerState);

    void reload();

    void reload(List<Long> needToSelectIds);

    void find(String searchPattern);

    void reloadOnDate(Date version);

    void clearSelected(boolean fireChangeEvent);

    Set<Long> getSelectedIds();

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
     * Возвращает разименованное значение поля в выбранной строке по attrId и attrId2
     *
     * !!!!! Не предназначен для мультиселекта
     *
     * @param attrId основной атррубут для отображения
     * @param attrId2 аттрибут второго уровня
     * @return строку с единичным значением(даже если выбрано куча)
     */
    String getOtherDereferenceValue(Long attrId, Long attrId2);

    Boolean isMultiSelect();

    /**
     * Смена режима выбора значений.
     * При смене происходит очищение ранее выделенных значений
     * @param multiSelect true - множественный выбор
     */
    void setMultiSelect(Boolean multiSelect);

    int getLoadedItemsCount();

    /**
     * Выделить все записи, если мультиселект
     */
    void selectAll(DeferredInvokeHandler handler);

    /**
     * Развыделить все записи, если мультиселект
     */
    void unselectAll(DeferredInvokeHandler handler);
}
