package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 24.10.14 14:32
 */

public class RefBookHelperImplTest {

	private RefBookFactory refBookFactory;

	private RefBookHelperImpl refBookHelper;

	/*
		Два справочника. Один ссылается на другой для тестирования разыменования второго уровня

		Первый справочник "Страна" состоит из одного атрибута "Название" (id = 1). Содержит строку с id = 1,
		в которой записана строка "Россия".

		Второй справочник "Ставка" состоит из двух атрибутов "Значение" (id = 2) и "Страна" (id = 3), который является
		ссылкой на справочник "Страна". Справочник "Ставка" содержит строку с id = 2. Значение ставки "435". Ссылается
		на страну "Россия".

		Налоговая форма также содержит только одну строку. В ней три графы: "Номер договора"(No) - строка, "Ставка"(Rate) - ссылка
		на справочник "Ставка", "Страна"(Country) - зависимая от графы "Ставка", отображает название страны (двойное разыменование)
	 */
	@Before
	public void init() {
		refBookFactory = mock(RefBookFactory.class);
		RefBookDataProvider provider1 = mock(RefBookDataProvider.class);
		RefBookDataProvider provider2 = mock(RefBookDataProvider.class);

		RefBook refBook1 = new RefBook();
		refBook1.setId(1L);
		when(refBookFactory.getByAttribute(1L)).thenReturn(refBook1);
		RefBook refBook2 = new RefBook();
        refBook2.setId(2L);
        List<RefBookAttribute> attributes = new ArrayList<RefBookAttribute>();
        RefBookAttribute attribute1 = new RefBookAttribute();
        attribute1.setId(4L);
        attribute1.setAttributeType(RefBookAttributeType.DATE);
        attribute1.setFormat(Formats.DD_MM_YYYY);
        attributes.add(attribute1);
        refBook2.setAttributes(attributes);
		when(refBookFactory.getByAttribute(2L)).thenReturn(refBook2);
        when(refBookFactory.getByAttribute(3L)).thenReturn(refBook2);
        when(refBookFactory.getByAttribute(4L)).thenReturn(refBook2);

		Map<Long, RefBookValue> result1 = new HashMap<Long, RefBookValue>();
		result1.put(1L, new RefBookValue(RefBookAttributeType.STRING, "Россия"));
		Collection<Long> recordIds1 = new HashSet<Long>();
		recordIds1.add(1L);
		when(provider1.dereferenceValues(1L, recordIds1)).thenReturn(result1);

		Map<Long, RefBookValue> result2 = new HashMap<Long, RefBookValue>();
		result2.put(2L, new RefBookValue(RefBookAttributeType.NUMBER, 435));
		Collection<Long> recordIds2 = new HashSet<Long>();
		recordIds2.add(2L);
		when(provider2.dereferenceValues(2L, recordIds2)).thenReturn(result2);

		Map<Long, RefBookValue> result3 = new HashMap<Long, RefBookValue>();
		result3.put(2L, new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
		when(provider2.dereferenceValues(3L, recordIds2)).thenReturn(result3);

        Map<Long, RefBookValue> result4 = new HashMap<Long, RefBookValue>();
        result4.put(2L, new RefBookValue(RefBookAttributeType.DATE, (new GregorianCalendar(2015, Calendar.FEBRUARY, 10)).getTime()));
        when(provider2.dereferenceValues(4L, recordIds2)).thenReturn(result4);

		when(refBookFactory.getDataProvider(1L)).thenReturn(provider1);
		when(refBookFactory.getDataProvider(2L)).thenReturn(provider2);

		refBookHelper = new RefBookHelperImpl();
		ReflectionTestUtils.setField(refBookHelper, "refBookFactory", refBookFactory);
	}

	/**
	 * Создает строку в НФ
	 * @param columns
	 * @return
	 */
	private DataRow<Cell> createDataRow(List<Column> columns) {
		FormTemplate formTemplate = new FormTemplate();
		formTemplate.getColumns().addAll(columns);
		List<Cell> cells = FormDataUtils.createCells(formTemplate);
		return new DataRow<Cell>(cells);
	}

	@Test
	public void testDataRowsDereference() {
		StringColumn sColumn = new StringColumn();
		sColumn.setAlias("No");
		sColumn.setId(1);
		RefBookColumn rbColumn = new RefBookColumn();
		rbColumn.setAlias("Rate");
		rbColumn.setId(2);
		rbColumn.setRefBookAttributeId(2L);
		ReferenceColumn refColumn = new ReferenceColumn();
		refColumn.setAlias("Country");
		refColumn.setId(3);
		refColumn.setParentId(2);
		refColumn.setRefBookAttributeId(3L);
		refColumn.setRefBookAttributeId2(1L);
        ReferenceColumn dateColumn = new ReferenceColumn();
        dateColumn.setAlias("date");
        dateColumn.setId(4);
        dateColumn.setParentId(2);
        dateColumn.setRefBookAttributeId(4L);
        dateColumn.setRefBookAttributeId2(null);

		List<Column> columns = Arrays.asList(new Column[] {sColumn, rbColumn, refColumn, dateColumn});

		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		DataRow<Cell> dataRow = createDataRow(columns);
		dataRow.put(sColumn.getAlias(), "Договор №1");
		dataRow.put(rbColumn.getAlias(), 2L);
		dataRows.add(dataRow);

		DataRow<Cell> dataRow2 = createDataRow(columns);
		dataRow2.put(sColumn.getAlias(), "Договор №1");
		dataRows.add(dataRow2);

		refBookHelper.dataRowsDereference(null, dataRows, columns);

		assertEquals("Договор №1", dataRow.get(sColumn.getAlias()));
		assertEquals(2L, dataRow.get(rbColumn.getAlias()));
		assertNull(dataRow.get(refColumn.getAlias()));

		assertEquals("Договор №1", dataRow.getCell(sColumn.getAlias()).getStringValue());
		assertEquals("435", dataRow.getCell(rbColumn.getAlias()).getRefBookDereference());
		assertEquals("Россия", dataRow.getCell(refColumn.getAlias()).getRefBookDereference());
        assertEquals("10.02.2015", dataRow.getCell(dateColumn.getAlias()).getRefBookDereference());

		assertEquals("", dataRow2.getCell(rbColumn.getAlias()).getRefBookDereference());
        assertEquals("", dataRow2.getCell(dateColumn.getAlias()).getRefBookDereference());
    }
}
