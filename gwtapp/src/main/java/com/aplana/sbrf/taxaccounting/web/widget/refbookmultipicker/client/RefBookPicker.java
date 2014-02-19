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

    /**
     *
     * @param value
     * @deprecated Это значение будет автоматически меняться при смене значения через setValue или через UI
     */
    @Deprecated
    void setDereferenceValue(String value);

    String getDereferenceValue();

    /* id отображаемого атрибута */
    Long getAttributeId();

    void setAttributeId(long attributeId);

    String getFilter();

    void setFilter(String filter);

    void setPeriodDates(Date startDate, Date endDate);

    void setMultiSelect(boolean multiSelect);

    Boolean isMultiSelect();

    Long getSingleValue();

    void setSingleValue(Long value, boolean fireEvents);

    void setSingleValue(Long value);

    void open();

    void load(long attributeId, String filter, Date startDate, Date endDate);

    void load();

    String getOtherDereferenceValue(String alias);

    String getOtherDereferenceValue(Long attrId);

    HandlerRegistration addCloseHandler(CloseHandler<ModalWindow> handler);

    void setOnHideHandler(OnHideHandler<CanHide> hideHandler);
}
