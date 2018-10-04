package com.aplana.sbrf.taxaccounting.service.print.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.service.print.AbstractReportBuilderTest;
import org.junit.Before;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.RECORD_VERSION_FROM_ALIAS;
import static com.aplana.sbrf.taxaccounting.model.refbook.RefBook.RECORD_VERSION_TO_ALIAS;
import static com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType.*;

public class AbstractRefBookReportBuilderTest extends AbstractReportBuilderTest {

    static RefBook linearRefBook = new RefBook();
    static RefBook hierRefBook = new RefBook();

    static {
        linearRefBook.setName("Линейный справочник");
        linearRefBook.setVersioned(true);
        hierRefBook.setName("Иерархический справочник");
        hierRefBook.setVersioned(true);
    }

    // Алиасы атрибутов справочника
    private static String NAME_ALIAS = "NAME";
    private static String NUMBER_ALIAS = "NUMBER";
    static RefBookAttribute sortAttribute;
    // Атрибуты для линейного справочника
    static List<RefBookAttribute> linearAttributes;
    // Атрибуты для иерархического справочника
    static List<RefBookAttribute> hierAttributes;
    // Дата актуальности записей справочника
    static Date version = new GregorianCalendar(2018, Calendar.JANUARY, 1).getTime();
    // Записи для линейного справочника для формирования отчета
    static List<Map<String, RefBookValue>> linearRecords;
    // Записи для иерархического справочника для формирования отчета
    static List<Map<String, RefBookValue>> hierRecords;

    @Before
    public void before() {
        initLinearAttributes();
        initHierAttributes();
        initLinearRecords();
        initHierRecords();
    }

    private void initLinearAttributes() {
        linearAttributes = new ArrayList<>();

        RefBookAttribute attribute;
        attribute = newAttribute("Наименование", NAME_ALIAS, STRING);
        linearAttributes.add(attribute);
        sortAttribute = attribute;

        attribute = newAttribute("Число", NUMBER_ALIAS, NUMBER);
        attribute.setPrecision(2);
        linearAttributes.add(attribute);

        attribute = newAttribute(RefBook.REF_BOOK_VERSION_FROM_TITLE, RECORD_VERSION_FROM_ALIAS, DATE);
        linearAttributes.add(attribute);

        attribute = newAttribute(RefBook.REF_BOOK_VERSION_TO_TITLE, RECORD_VERSION_TO_ALIAS, DATE);
        linearAttributes.add(attribute);

        linearRefBook.setAttributes(linearAttributes);
    }

    private void initHierAttributes() {
        hierAttributes = new ArrayList<>();
        hierAttributes.addAll(linearAttributes);

        RefBookAttribute parentAttribute = newAttribute("Код родительского подразделения", RefBook.RECORD_PARENT_ID_ALIAS, REFERENCE);
        parentAttribute.setRefBookAttribute(linearAttributes.get(0));
        hierAttributes.add(parentAttribute);

        hierRefBook.setAttributes(hierAttributes);
    }

    private void initLinearRecords() {
        linearRecords = new ArrayList<>();
        Map<String, RefBookValue> record;
        record = newRecord(1, "Вася", 111);
        linearRecords.add(record);

        record = newRecord(2, "Дася", 222);
        linearRecords.add(record);

        record = newRecord(3, "Мася", 333);
        linearRecords.add(record);
    }

    private void initHierRecords() {
        hierRecords = new ArrayList<>();
        for (Map<String, RefBookValue> linearRecord : linearRecords) {
            Map<String, RefBookValue> hierRecord = new HashMap<>(linearRecord);
            hierRecords.add(hierRecord);
        }
        // Добавляем ссылки на родительские записи
        RefBookValue parentValue = new RefBookValue(REFERENCE, null);
        hierRecords.get(0).put(RefBook.RECORD_PARENT_ID_ALIAS, parentValue);

        parentValue = new RefBookValue(REFERENCE, hierRecords.get(0));
        for (int i = 1; i < hierRecords.size(); i++) {
            hierRecords.get(i).put(RefBook.RECORD_PARENT_ID_ALIAS, parentValue);
        }
    }

    private static RefBookAttribute newAttribute(String name, String alias, RefBookAttributeType type) {
        RefBookAttribute attribute = new RefBookAttribute(alias, type);
        attribute.setName(name);
        attribute.setWidth(10);
        attribute.setVisible(true);
        return attribute;
    }

    private static Map<String, RefBookValue> newRecord(Integer id, String name, Integer number) {
        Map<String, RefBookValue> record = new HashMap<>();
        record.put(RefBook.RECORD_ID_ALIAS, new RefBookValue(NUMBER, id));
        record.put(NAME_ALIAS, new RefBookValue(STRING, name));
        record.put(NUMBER_ALIAS, new RefBookValue(NUMBER, new BigDecimal(number)));
        record.put(RECORD_VERSION_FROM_ALIAS, new RefBookValue(DATE, new GregorianCalendar(2017, Calendar.JANUARY, 1).getTime()));
        record.put(RECORD_VERSION_TO_ALIAS, new RefBookValue(DATE, new GregorianCalendar(2019, Calendar.JANUARY, 1).getTime()));
        return record;
    }
}
