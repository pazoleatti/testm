package com.aplana.sbrf.taxaccounting.refbook.impl.fixed;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttributeType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 26.06.14 17:07
 */
public class RefBookFormDataKindTest {

	private RefBookFormDataKind refBookFormDataKind;

	@Before
	public void init() {
		RefBook refBook = new RefBook();
		refBook.setId(RefBookFormDataKind.REF_BOOK_ID);

		RefBookAttribute attribute = new RefBookAttribute();
		attribute.setAlias(RefBookFormDataKind.ATTRIBUTE_NAME);
		attribute.setAttributeType(RefBookAttributeType.STRING);

		List<RefBookAttribute> attributes = new ArrayList<RefBookAttribute>();
		attributes.add(attribute);
		refBook.setAttributes(attributes);

		refBookFormDataKind = new RefBookFormDataKind();
		ReflectionTestUtils.setField(refBookFormDataKind, "refBook", refBook);
	}

	@Test
	public void test1() {
		PagingResult<Map<String, RefBookValue>> records = refBookFormDataKind.getRecords(new Date(), null, null, null);
		assertEquals(5, records.size());
	}

	@Test
	public void test2() {
		PagingResult<Map<String, RefBookValue>> records = refBookFormDataKind.getRecords(new Date(), null, "  1,2,3    ", null);
		assertEquals(3, records.size());
	}

	@Test
	public void test3() {
		PagingParams pagingParams = new PagingParams();
		pagingParams.setStartIndex(3);
		pagingParams.setCount(2);
		PagingResult<Map<String, RefBookValue>> records = refBookFormDataKind.getRecords(new Date(), pagingParams, null, null);
		assertEquals(2, records.size());
		assertEquals(FormDataKind.UNP.getId(), records.get(0).get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		assertEquals(FormDataKind.UNP.getName(), records.get(0).get(RefBookFormDataKind.ATTRIBUTE_NAME).getStringValue());
		assertEquals(FormDataKind.ADDITIONAL.getId(), records.get(1).get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		assertEquals(FormDataKind.ADDITIONAL.getName(), records.get(1).get(RefBookFormDataKind.ATTRIBUTE_NAME).getStringValue());
	}

	@Test
	public void test4() {
		PagingParams pagingParams = new PagingParams();
		pagingParams.setStartIndex(4);
		pagingParams.setCount(3);
		PagingResult<Map<String, RefBookValue>> records = refBookFormDataKind.getRecords(new Date(), pagingParams, null, null);
		assertEquals(1, records.size());
		assertEquals(FormDataKind.ADDITIONAL.getId(), records.get(0).get(RefBook.RECORD_ID_ALIAS).getNumberValue().intValue());
		assertEquals(FormDataKind.ADDITIONAL.getName(), records.get(0).get(RefBookFormDataKind.ATTRIBUTE_NAME).getStringValue());
	}

	@Test
	public void test5() {
		PagingParams pagingParams = new PagingParams();
		pagingParams.setStartIndex(-1);
		pagingParams.setCount(3);
	}

	@Test
	public void test6() {
		assertEquals(5, refBookFormDataKind.getRecordsCount(null, null));
	}

	@Test
	public void test7() {
		assertEquals(2, refBookFormDataKind.getRecordsCount(null, "2,4"));
	}

}
