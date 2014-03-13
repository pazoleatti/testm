package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookItem;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookRecordDereferenceValue;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.RefBookUiTreeItem;
import com.google.gwt.view.client.ProvidesKey;

import java.util.Date;
import java.util.List;

/**
 * Класс утилит для виджетов выбора из справоника
 *
 * @author aivanov
 */
public class RefBookPickerUtils {

    /* Провайдер для идентификации конкретноого объекта в строке
     * С помощью провайдера при листании селектшнМодел понимает что
     * за объект был выделе или развыделен */
    public static final ProvidesKey<RefBookItem> KEY_PROVIDER = new ProvidesKey<RefBookItem>() {
        @Override
        public Object getKey(RefBookItem item) {
            return item == null ? null : item.getId();
        }
    };

    /* Провайдер для идентификации конкретноого объекта в строке иерархичного справочника
     * С помощью провайдера при листании селектшнМодел понимает что
     * за объект был выделе или развыделен */
    public static final ProvidesKey<RefBookUiTreeItem> TREE_KEY_PROVIDER = new ProvidesKey<RefBookUiTreeItem>() {
        @Override
        public Object getKey(RefBookUiTreeItem item) {
            return item == null ? null : item.getRefBookTreeItem() == null ? null : item.getRefBookTreeItem().getId();
        }
    };

    /**
     * Поиск разименноованного значения для атрибута записи справочника по идентификатору атрибута
     *
     * @param recordDereferenceValues recordDereferenceValues разименновоннаые значения атрибутов у записи справоника
     * @param attrId                  идентификатор атрибута
     * @return разименног ованного значение или null
     */
    public static String getDereferenceValue(List<RefBookRecordDereferenceValue> recordDereferenceValues, Long attrId) {
        if (recordDereferenceValues != null && !recordDereferenceValues.isEmpty() && attrId != null) {
            for (RefBookRecordDereferenceValue value : recordDereferenceValues) {
                if (attrId.equals(value.getValueAttrId())) {
                    return value.getDereferenceValue();
                }
            }
        }
        return null;
    }

    /**
     * Поиск разименноованного значения для атрибута записи справочника по идентификатору атрибута
     *
     * @param recordDereferenceValues разименновоннаые значения атрибутов у записи справоника
     * @param alias                   алиас атрибута
     * @return разименног ованного значение или null
     */
    public static String getDereferenceValue(List<RefBookRecordDereferenceValue> recordDereferenceValues, String alias) {
        if (recordDereferenceValues != null && alias != null && !alias.isEmpty()) {
            for (RefBookRecordDereferenceValue value : recordDereferenceValues) {
                if (value.getValueAttrAlias() != null &&
                        alias.toLowerCase().equals(value.getValueAttrAlias().toLowerCase())) {
                    return value.getDereferenceValue();
                }
            }
        }
        return null;
    }
}
