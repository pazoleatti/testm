package com.aplana.sbrf.taxaccounting.dao.impl;

import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.model.FormDataDaoFilter;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchOrdering;
import com.aplana.sbrf.taxaccounting.model.FormDataSearchResultItem;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchParams;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataSearchDaoTest.xml"})
public class FormDataSearchDaoTest {
	@Autowired
	private FormDataSearchDao formDataSearchDao;
	
	@Test
	public void testFindByFilter() {
		// TODO: сделать тесты
	}
	
	@Test
	public void testFindPage() {
		// TODO: сделать тесты
	}
	
	/**
	 * Проверяем разные способы сортировки
	 */
	@Test
	public void testFindPageSorting() {
		FormDataDaoFilter filter = new FormDataDaoFilter();
		PaginatedSearchParams pageParams = new PaginatedSearchParams(0, 5);
		
		PaginatedSearchResult<FormDataSearchResultItem> res;
		
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.ID, true, pageParams);
		assertIdsEquals(new long[] {1, 2, 3, 4, 5}, res.getRecords());
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.ID, false, pageParams);
		assertIdsEquals(new long[] {18, 17, 16, 15, 14}, res.getRecords());

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.DEPARTMENT_NAME, true, pageParams);
		assertIdsEquals(new long[] {1, 4, 7, 10, 13}, res.getRecords());
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.DEPARTMENT_NAME, false, pageParams);
		assertIdsEquals(new long[] {18, 15, 12, 9, 6}, res.getRecords());

		// TODO: добавить проверку, что порядок записей верный, для каждого из вариантов сортировки		
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.FORM_TYPE_NAME, true, pageParams);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.FORM_TYPE_NAME, false, pageParams);

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.KIND, true, pageParams);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.KIND, false, pageParams);

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.REPORT_PERIOD_NAME, true, pageParams);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.REPORT_PERIOD_NAME, false, pageParams);

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.STATE, true, pageParams);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.STATE, false, pageParams);		
	}
	
	@Test
	public void testGetCount() {
		FormDataDaoFilter filter = new FormDataDaoFilter();
		assertEquals(18, formDataSearchDao.getCount(filter));
		
		filter.setDepartmentIds(Collections.singletonList(1));
		assertEquals(6, formDataSearchDao.getCount(filter));
		
		filter.setDepartmentIds(null);
		filter.setFormTypeIds(Collections.singletonList(1));
		assertEquals(4, formDataSearchDao.getCount(filter));
	}
	
	private void assertIdsEquals(long[] expected, List<FormDataSearchResultItem> items) {
		if (expected.length != items.size()) {
			fail("List size mismatch: " + expected.length + " expected but " + items.size() + " received");
			return;
		}
		
		long[] received = new long[expected.length];
		
		boolean failed = false;
		for (int i = 0; i < expected.length; ++i) {
			FormDataSearchResultItem item = items.get(i);
			received[i] = item.getFormDataId();
			if (received[i] != expected[i]) {
				failed = true;
			}
		}
		
		if (failed) {
			fail("Wrong list of ids: " + expected + " expected but " + received + " received");
		}
	}
}
