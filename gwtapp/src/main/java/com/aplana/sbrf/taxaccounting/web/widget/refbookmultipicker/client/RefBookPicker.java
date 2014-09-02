package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import java.util.Date;
import java.util.List;

import com.aplana.gwt.client.ModalWindow;
import com.aplana.gwt.client.modal.CanHide;
import com.aplana.gwt.client.modal.OnHideHandler;
import com.google.gwt.editor.client.LeafValueEditor;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Интерфейс компонента выбора значений из справочника
 *
 * @author sgoryachkin
 */
public interface RefBookPicker extends HasValue<List<Long>>, LeafValueEditor<List<Long>>, HasEnabled, IsWidget {

    void setDereferenceValue(String value);

    String getDereferenceValue();

    /* id отображаемого атрибута */
    Long getAttributeId();

    void setAttributeId(long attributeId);

    String getFilter();

    void setFilter(String filter);

    /**
     * Установка ограничивающего периода
     * @param startDate
     * @param endDate
     */
    void setPeriodDates(Date startDate, Date endDate);

    void setMultiSelect(boolean multiSelect);

    Boolean getMultiSelect();

    Long getSingleValue();

    void setSingleValue(Long value, boolean fireEvents);

    void setSingleValue(Long value);

    void open();

    void load(long attributeId, String filter, Date startDate, Date endDate);

    void load();

    void reload();

    // @deprecated Использовать getOtherDereferenceValue(Long attrId)
    @Deprecated
    String getOtherDereferenceValue(String alias);

    String getOtherDereferenceValue(Long attrId);

    /**
     * Получить разименнованное значение выбранной записи по атрибуту и атрибуту второго уровня
     * @param attrId атрибут справочника колонки
     * @param attrId2 атрибут второго уровня для альтернативного отображения
     * @return строка с разименованных значением
     */
    String getOtherDereferenceValue(Long attrId, Long attrId2);

    boolean getSearchEnabled();

    void setSearchEnabled(boolean isSearchEnabled);

    boolean getVersionEnabled();

    void setVersionEnabled(boolean isVersionEnabled);

    boolean isManualUpdate();

    /**
     * Установка режима ручного редактирования значения виджета
     * Если true то dereferenceValue нужно вручную проставлять,
     * иначе выставится автоматически относительно засеченого значения
     * @param isManualUpdate
     */
    void setManualUpdate(boolean isManualUpdate);

    boolean isHierarchical();

    /**
     * регистрация события после закрытия модального окна
     * @param handler
     * @return
     */
    HandlerRegistration addCloseHandler(CloseHandler<ModalWindow> handler);

    /**
     * Установка хендлера который срабатывает перед закрытием модального окна
     * @param hideHandler
     */
    void setOnHideHandler(OnHideHandler<CanHide> hideHandler);

    boolean isVisible();

    void setVisible(boolean visible);
}
