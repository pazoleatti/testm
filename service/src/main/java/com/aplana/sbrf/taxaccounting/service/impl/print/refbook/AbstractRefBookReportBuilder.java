package com.aplana.sbrf.taxaccounting.service.impl.print.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.impl.print.AbstractReportBuilder;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Абстрактный класс для формирования отчета справочников
 */
public abstract class AbstractRefBookReportBuilder extends AbstractReportBuilder {

    // Справочник
    protected RefBook refBook;
    // Атрибуты справочника
    protected List<RefBookAttribute> attributes;
    // Дата актуальности записей справочника
    protected Date version;
    // Значение из строки поиска
    protected String searchPattern;
    // Признак точного поиска
    protected boolean exactSearch;
    // Атрибут, по которому произведена сортировка записей справочника
    protected RefBookAttribute sortAttribute;

    // Дополнительный атрибут для отображения уровня записи в иерархии
    static final RefBookAttribute levelAttribute = new RefBookAttribute() {
        {
            setAlias("level");
            setName("Уровень");
            setAttributeType(RefBookAttributeType.NUMBER);
            setPrecision(0);
            setWidth(3);
        }
    };

    //Компаратор для сортировки записей
    protected final Comparator<Map<String, RefBookValue>> comparator = new Comparator<Map<String, RefBookValue>>() {
        @Override
        public int compare(Map<String, RefBookValue> o1, Map<String, RefBookValue> o2) {
            if (sortAttribute.getAttributeType().equals(RefBookAttributeType.STRING)) {
                String s1 = o1.get(sortAttribute.getAlias()).getStringValue();
                String s2 = o2.get(sortAttribute.getAlias()).getStringValue();
                return s1.compareToIgnoreCase(s2);
            } else if (sortAttribute.getAttributeType().equals(RefBookAttributeType.NUMBER)) {
                BigDecimal d1 = (BigDecimal) o1.get(sortAttribute.getAlias()).getNumberValue();
                BigDecimal d2 = (BigDecimal) o2.get(sortAttribute.getAlias()).getNumberValue();
                return d1.compareTo(d2);
            }
            return 0;
        }
    };

    AbstractRefBookReportBuilder(RefBook refBook, List<RefBookAttribute> attributes, Date version, String searchPattern,
                                 boolean exactSearch, final RefBookAttribute sortAttribute) {
        this.refBook = refBook;
        this.attributes = attributes;
        this.version = version;
        this.searchPattern = searchPattern;
        this.exactSearch = exactSearch;
        this.sortAttribute = sortAttribute;
    }
}
