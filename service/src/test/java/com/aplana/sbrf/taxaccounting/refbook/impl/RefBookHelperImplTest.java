package com.aplana.sbrf.taxaccounting.refbook.impl;

import com.aplana.sbrf.taxaccounting.model.Formats;
import com.aplana.sbrf.taxaccounting.model.refbook.*;
import com.aplana.sbrf.taxaccounting.refbook.RefBookCache;
import com.aplana.sbrf.taxaccounting.refbook.RefBookDataProvider;
import com.aplana.sbrf.taxaccounting.refbook.RefBookFactory;
import com.aplana.sbrf.taxaccounting.service.refbook.CommonRefBookService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 24.10.14 14:32
 */

public class RefBookHelperImplTest {

	private RefBookFactory refBookFactory;

	private CommonRefBookService commonRefBookService;

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
		commonRefBookService = mock(CommonRefBookService.class);
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
		when(commonRefBookService.getByAttribute(1L)).thenReturn(refBook1);

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

		when(commonRefBookService.get(2L)).thenReturn(refBook2);
		when(commonRefBookService.getByAttribute(2L)).thenReturn(refBook2);
        when(commonRefBookService.getByAttribute(3L)).thenReturn(refBook2);
		when(commonRefBookService.getByAttribute(4L)).thenReturn(refBook2);

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
		Field field2 = RefBookCacheImpl.class.getDeclaredField("commonRefBookService");
		field2.setAccessible(true);
		ReflectionUtils.setField(field, rbCache, refBookFactory);
		ReflectionUtils.setField(field2, rbCache, commonRefBookService);

		ApplicationContext applicationContext = mock(ApplicationContext.class);
		when(applicationContext.getBean(RefBookCache.class)).thenReturn(rbCache);

		refBookHelper = new RefBookHelperImpl();
		ReflectionTestUtils.setField(refBookHelper, "refBookFactory", refBookFactory);
		ReflectionTestUtils.setField(refBookHelper, "commonRefBookService", commonRefBookService);
		ReflectionTestUtils.setField(refBookHelper, "applicationContext", applicationContext);
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
		String s = refBookHelper.refBookRecordToString(commonRefBookService.get(2L), record);
		LOG.info(s);
		assertEquals("[id:9; recordId:5; Ставка:\"4.3\"; Страна:\"Россия\"; Дата:\"\"]", s);

		Calendar calendar = Calendar.getInstance();
		calendar.set(2013, Calendar.JANUARY, 1);
		values.put("date", new RefBookValue(RefBookAttributeType.DATE, calendar.getTime()));
		s = refBookHelper.refBookRecordToString(commonRefBookService.get(2L), record);
		LOG.info(s);
		assertEquals("[id:9; recordId:5; Ставка:\"4.3\"; Страна:\"Россия\"; Дата:\"01.01.2013\"]", s);
	}
}
