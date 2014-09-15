package com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookItem;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookRecordDereferenceValue;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.shared.model.RefBookUiTreeItem;
import com.google.gwt.view.client.ProvidesKey;

import java.util.List;

/**
 * Класс утилит для виджетов выбора из справоника
 *
 * @author aivanov
 */
public final class RefBookPickerUtils {

    private RefBookPickerUtils() {}

    public static final String NO_REGION_MATCHES_FLAG = "NO_REGION_MATCHES";

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
     * @return разименованного значение или null
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
     * Поиск разименноованного значения для атрибута записи справочника по идентификатору атрибута и атрибуту второго уровня
     *
     * @param recordDereferenceValues recordDereferenceValues разименновоннаые значения атрибутов у записи справоника
     * @param attrId                  идентификатор атрибута
     * @param attrId2                 идентификатор атрибута второго уровня
     * @return разименованного значение или null
     */
    public static String getDereferenceValue(List<RefBookRecordDereferenceValue> recordDereferenceValues, Long attrId, Long attrId2) {
        if (recordDereferenceValues != null && !recordDereferenceValues.isEmpty() && attrId != null) {
            for (RefBookRecordDereferenceValue value : recordDereferenceValues) {
                if (attrId.equals(value.getValueAttrId())) {
                    if (value.getAttrId2DerefValueMap().containsKey(attrId2)){
                        return value.getAttrId2DerefValueMap().get(attrId2);
                    }
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
                        alias.equalsIgnoreCase(value.getValueAttrAlias())) {
                    return value.getDereferenceValue();
                }
            }
        }
        return null;
    }

    public static String buildRegionFilterForUser(List<Department> departments, RefBook refBook) {
        if (departments == null || departments.isEmpty()) return null;
        if (refBook == null) return null;
        if (refBook.getRegionAttribute() == null) return null;

        String attrAlias = refBook.getRegionAttribute().getAlias();
        StringBuilder regions = new StringBuilder("(");
        boolean haveRegion = false;
        for (Department dep : departments) {
            if (dep.getRegionId() != null) {
                regions.append(attrAlias).append(" = ").append(dep.getRegionId()).append(" or ");
                haveRegion = true;
            }
        }
        if (!haveRegion) {
            return NO_REGION_MATCHES_FLAG;
        }
        regions.delete(regions.length() - 4, regions.length() - 1);
        regions.append(")");
        return regions.toString();
    }
}
