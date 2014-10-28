package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.RefBookColumn;
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
		when(refBookFactory.getByAttribute(2L)).thenReturn(refBook2);
		when(refBookFactory.getByAttribute(3L)).thenReturn(refBook2);

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
		List<Cell> cells = FormDataUtils.createCells(columns, null);
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

		List<Column> columns = Arrays.asList(new Column[] {sColumn, rbColumn, refColumn});

		List<DataRow<Cell>> dataRows = new ArrayList<DataRow<Cell>>();
		DataRow<Cell> dataRow = createDataRow(columns);
		dataRow.put(sColumn.getAlias(), "Договор №1");
		dataRow.put(rbColumn.getAlias(), 2L);
		dataRows.add(dataRow);

		refBookHelper.dataRowsDereference(null, dataRows, columns);

		assertEquals("Договор №1", dataRow.get(sColumn.getAlias()));
		assertEquals(2L, dataRow.get(rbColumn.getAlias()));
		assertNull(dataRow.get(refColumn.getAlias()));

		assertEquals("Договор №1", dataRow.getCell(sColumn.getAlias()).getStringValue());
		assertEquals("435", dataRow.getCell(rbColumn.getAlias()).getRefBookDereference());
		assertEquals("Россия", dataRow.getCell(refColumn.getAlias()).getRefBookDereference());
	}
}
