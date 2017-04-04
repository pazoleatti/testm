package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.dao.ColumnDao;
import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.RefBookColumn;
import com.aplana.sbrf.taxaccounting.model.ReferenceColumn;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookRecord;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.refbook.RefBookCache;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
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

	private static final Log LOG = LogFactory.getLog(RefBookHelperImplTest.class);

	/*
		Два справочника. Один ссылается на другой для тестирования разыменования второго уровня

		Первый справочник "Страна" состоит из одного атрибута "Название" (id = 1). Содержит строку с id = 1,
		в которой записана строка "Россия".

		Второй справочник "Ставка" состоит из двух атрибутов "Значение" (id = 2, rate) и "Страна" (id = 3, country), который является
		ссылкой на справочник "Страна". Справочник "Ставка" содержит строку с id = 2. Значение ставки "435". Ссылается
		на страну "Россия".

		Налоговая форма также содержит только одну строку. В ней три графы: "Номер договора"(No) - строка, "Ставка"(Rate) - ссылка
		на справочник "Ставка", "Страна"(Country) - зависимая от графы "Ставка", отображает название страны (двойное разыменование)
	 */
	@Before
	public void init() throws NoSuchFieldException {
		refBookFactory = mock(RefBookFactory.class);
		RefBookDataProvider provider1 = mock(RefBookDataProvider.class);
		RefBookDataProvider provider2 = mock(RefBookDataProvider.class);

		RefBook refBook1 = new RefBook();
		refBook1.setId(1L);
		List<RefBookAttribute> attributes = new ArrayList<RefBookAttribute>();
		RefBookAttribute attribute1 = new RefBookAttribute();
		attribute1.setId(1L);
		attribute1.setAlias("name");
		attribute1.setAttributeType(RefBookAttributeType.STRING);
		attributes.add(attribute1);
		refBook1.setAttributes(attributes);
		when(refBookFactory.getByAttribute(1L)).thenReturn(refBook1);

		RefBook refBook2 = new RefBook();
        refBook2.setId(2L);
        attributes = new ArrayList<RefBookAttribute>();
        RefBookAttribute attribute2 = new RefBookAttribute();
        attribute2.setId(2L);
		attribute2.setName("Ставка");
		attribute2.setAlias("rate");
        attribute2.setAttributeType(RefBookAttributeType.NUMBER);
        attributes.add(attribute2);
		RefBookAttribute attribute3 = new RefBookAttribute();
		attribute3.setId(3L);
		attribute3.setName("Страна");
		attribute3.setAlias("country");
		attribute3.setAttributeType(RefBookAttributeType.REFERENCE);
		attribute3.setRefBookId(1L);
		attribute3.setRefBookAttribute(attribute1);
		attribute3.setRefBookAttributeId(attribute1.getId());
		attributes.add(attribute3);
		refBook2.setAttributes(attributes);

		RefBookAttribute attribute4 = new RefBookAttribute();  // дополнительный атрибут
		attribute4.setId(4L);
		attribute4.setName("Дата");
		attribute4.setAlias("date");
		attribute4.setAttributeType(RefBookAttributeType.DATE);
		attribute4.setFormat(Formats.DD_MM_YYYY);
		attributes.add(attribute4);
		refBook2.setAttributes(attributes);

		when(refBookFactory.get(2L)).thenReturn(refBook2);
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

		//Map<String, RefBookValue> result2s = new HashMap<String, RefBookValue>();
		//result2.put("", new RefBookValue(RefBookAttributeType.NUMBER, 435));
		//when(provider2.getRecordData(2L)).thenReturn(result2s);

		Map<Long, RefBookValue> result3 = new HashMap<Long, RefBookValue>();
		result3.put(2L, new RefBookValue(RefBookAttributeType.REFERENCE, 1L));
		when(provider2.dereferenceValues(3L, recordIds2)).thenReturn(result3);

        Map<Long, RefBookValue> result4 = new HashMap<Long, RefBookValue>();
        result4.put(2L, new RefBookValue(RefBookAttributeType.DATE, (new GregorianCalendar(2015, Calendar.FEBRUARY, 10)).getTime()));
        when(provider2.dereferenceValues(4L, recordIds2)).thenReturn(result4);

		when(refBookFactory.getDataProvider(1L)).thenReturn(provider1);
		when(refBookFactory.getDataProvider(2L)).thenReturn(provider2);

		RefBookCacheImpl rbCache = new RefBookCacheImpl();
		Field field = RefBookCacheImpl.class.getDeclaredField("refBookFactory");
		field.setAccessible(true);
		ReflectionUtils.setField(field, rbCache, refBookFactory);

		ApplicationContext applicationContext = mock(ApplicationContext.class);
		when(applicationContext.getBean(RefBookCache.class)).thenReturn(rbCache);

		ColumnDao columnDao = mock(ColumnDao.class);
		when(columnDao.getAttributeId2(new ArrayList<RefBookAttribute>())).thenReturn(new HashMap<Long, List<Long>>());

		refBookHelper = new RefBookHelperImpl();
		ReflectionTestUtils.setField(refBookHelper, "refBookFactory", refBookFactory);
		ReflectionTestUtils.setField(refBookHelper, "applicationContext", applicationContext);
		ReflectionTestUtils.setField(refBookHelper, "columnDao", columnDao);
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
        RefBookAttribute refBookAttribute1 = new RefBookAttribute();
        RefBookAttribute refBookAttribute2 = new RefBookAttribute();

		StringColumn sColumn = new StringColumn();
		sColumn.setAlias("No");
		sColumn.setId(1);
		RefBookColumn rbColumn = new RefBookColumn();
		rbColumn.setAlias("Rate");
		rbColumn.setId(2);
		rbColumn.setRefBookAttributeId(2L);
        rbColumn.setRefBookAttribute(refBookAttribute2);
		ReferenceColumn refColumn = new ReferenceColumn();
		refColumn.setAlias("Country");
		refColumn.setId(3);
		refColumn.setParentId(2);
		refColumn.setRefBookAttributeId(3L);
		refColumn.setRefBookAttributeId2(1L);
        refColumn.setRefBookAttribute(refBookAttribute1);
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

	@Test
	public void testRefBookRecordToString() {
		Map<String, RefBookValue> values = new HashMap<String, RefBookValue>();
		values.put("rate", new RefBookValue(RefBookAttributeType.NUMBER, 4.3));
		values.put("country", new RefBookValue(RefBookAttributeType.REFERENCE, 1L));

		RefBookRecord record = new RefBookRecord();
		record.setUniqueRecordId(9L);
		record.setRecordId(5L);
		record.setValues(values);
		String s = refBookHelper.refBookRecordToString(refBookFactory.get(2L), record);
		LOG.info(s);
		assertEquals("[id:9; recordId:5; Ставка:\"4.3\"; Страна:\"Россия\"; Дата:\"\"]", s);
	}
}
