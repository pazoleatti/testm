package com.aplana.sbrf.taxaccounting.dao.impl;

import com.aplana.sbrf.taxaccounting.dao.FormDataSearchDao;
import com.aplana.sbrf.taxaccounting.model.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@Ignore("Налоговые формы не используются!")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"FormDataSearchDaoTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class FormDataSearchDaoTest {
	@Autowired
	private FormDataSearchDao formDataSearchDao;
	
	@Test
	public void testFindByFilter() {
		FormDataDaoFilter filter = new FormDataDaoFilter();
		
		final long TOTAL_RECORDS_COUNT = formDataSearchDao.findByFilter(filter).size();
		List<FormDataSearchResultItem> res;

		List<TaxType> taxTypes 				= new ArrayList<TaxType>();
		List<FormDataKind> formDataKinds 	= new ArrayList<FormDataKind>();
		List<WorkflowState> workflowStates 	= new ArrayList<WorkflowState>();
		List<Long> formTypes 			= new ArrayList<Long>();
		List<Integer> departments 			= new ArrayList<Integer>();
		List<Integer> reportPeriods 		= new ArrayList<Integer>();

		//Если условия фильтрации не заданы, проверяем что findByFilter вернет все записи.
		res = formDataSearchDao.findByFilter(filter);
		assertEquals(TOTAL_RECORDS_COUNT, res.size());

		//Добавляем в фильтр Тип Налога
		taxTypes.add(TaxType.TRANSPORT);
		filter.setTaxTypes(taxTypes);
		res = formDataSearchDao.findByFilter(filter);
		assertEquals(14, res.size());

		//Добавляем в фильтр Тип Налоговой Формы
		formDataKinds.add(FormDataKind.SUMMARY);
		filter.setFormDataKind(formDataKinds);
		res = formDataSearchDao.findByFilter(filter);
		assertEquals(9, res.size());

		//Добавляем в фильтр Статус Формы
		workflowStates.add(WorkflowState.ACCEPTED);
		filter.setStates(workflowStates);
		res = formDataSearchDao.findByFilter(filter);
		assertEquals(4, res.size());

		//Добавляем в фильтр Вид Налоговой Формы
		formTypes.add(1l);
		filter.setFormTypeIds(formTypes);
		res = formDataSearchDao.findByFilter(filter);
		assertEquals(4, res.size());

		//Добавляем в фильтр Департамент
		departments.add(1);
		filter.setDepartmentIds(departments);
		res = formDataSearchDao.findByFilter(filter);
		assertEquals(2, res.size());

		//Добавляем в фильтр Отчетный период
		reportPeriods.add(1);
		filter.setReportPeriodIds(reportPeriods);
		res = formDataSearchDao.findByFilter(filter);
		assertEquals(2, res.size());

        // Признак корректирующего периода
        reportPeriods.add(1);
        filter.setCorrectionTag(true);
        res = formDataSearchDao.findByFilter(filter);
        assertEquals(0, res.size());
	}
	
	@Test
	public void findForCurrentDepartmentTest() {
        FormDataDaoFilter filter = new FormDataDaoFilter();
        filter.setDepartmentIds(asList(1, 2, 3));
        List<FormDataSearchResultItem> res = formDataSearchDao.findByFilter(filter);
        assertIdsEquals(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18}, res);

        filter.setDepartmentIds(asList(3));
        res = formDataSearchDao.findByFilter(filter);
        assertIdsEquals(new long[]{3, 6, 9, 12, 15, 18}, res);
    }

    @Test
    public void findForCurrentDepartmentWithKindTest() {
        FormDataDaoFilter filter = new FormDataDaoFilter();
        filter.setDepartmentIds(asList(1));
        filter.setFormDataKind(asList(FormDataKind.CONSOLIDATED));
        List<FormDataSearchResultItem> res = formDataSearchDao.findByFilter(filter);
        assertIdsEquals(new long[]{1, 7, 13}, res);
    }

	//@Test
	public void testFindPage() {
		FormDataDaoFilter filter = new FormDataDaoFilter();
		
		PagingParams pageParams = new PagingParams(0, 0);
		PagingResult<FormDataSearchResultItem> res;
		final long TOTAL_RECORDS_COUNT = formDataSearchDao.getCount(filter);

		for(int requestedCount = 0; requestedCount < TOTAL_RECORDS_COUNT; requestedCount += 5){
			pageParams.setStartIndex(0);
			pageParams.setCount(requestedCount);
			res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.ID, true, pageParams);
			assertEquals(requestedCount, res.size());
			assertEquals(TOTAL_RECORDS_COUNT, res.getTotalCount());
		}
	}

    @Test
    public void testFindIdsByFilter() {
        FormDataDaoFilter filter = new FormDataDaoFilter();
        List<Long> res = formDataSearchDao.findIdsByFilter(filter);

        assertArrayEquals(new Long[]{18l, 17l, 16l, 15l, 14l, 13l, 12l, 11l, 10l, 9l, 8l, 7l, 6l, 5l, 4l, 3l, 2l, 1l}, res.toArray());

        assertEquals(formDataSearchDao.findByFilter(filter).size(), res.size());
    }
	
	/**
	 * Проверяем разные способы сортировки
	 */
	//@Test
	public void testFindPageSorting() {
		FormDataDaoFilter filter = new FormDataDaoFilter();
		
		PagingParams pageParams = new PagingParams(0, 5);
		
		PagingResult<FormDataSearchResultItem> res;
		
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.ID, true, pageParams);
		assertIdsEquals(new long[] {1, 2, 3, 4, 5}, res);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.ID, false, pageParams);
		assertIdsEquals(new long[] {18, 17, 16, 15, 14}, res);

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.DEPARTMENT_NAME, true, pageParams);
		assertIdsEquals(new long[] {1, 4, 7, 10, 13}, res);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.DEPARTMENT_NAME, false, pageParams);
		assertIdsEquals(new long[] {18, 15, 12, 9, 6}, res);

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.FORM_TYPE_NAME, true, pageParams);
		assertIdsEquals(new long[] {4, 8, 12, 16, 3}, res);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.FORM_TYPE_NAME, false, pageParams);
		assertIdsEquals(new long[] {17, 13, 9, 5, 1}, res);

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.KIND, true, pageParams);
		assertIdsEquals(new long[] {1, 3, 5, 7, 9}, res);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.KIND, false, pageParams);
		assertIdsEquals(new long[] {18, 16, 14, 12, 10}, res);

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.REPORT_PERIOD_NAME, true, pageParams);
		assertIdsEquals(new long[] {1, 4, 7, 10, 13}, res);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.REPORT_PERIOD_NAME, false, pageParams);
		assertIdsEquals(new long[] {18, 15, 12, 9, 6}, res);

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.STATE, true, pageParams);
		assertIdsEquals(new long[] {1, 5, 9, 13, 17}, res);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.STATE, false, pageParams);
		assertIdsEquals(new long[] {16, 12, 8, 4, 15}, res);

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.YEAR, true, pageParams);
		assertIdsEquals(new long[] {1, 2, 3, 4, 5}, res);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.YEAR, false, pageParams);
		assertIdsEquals(new long[] {18, 17, 16, 15, 14}, res);

		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.RETURN, true, pageParams);
		assertIdsEquals(new long[] {1, 3, 4, 6, 7}, res);
		res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.RETURN, false, pageParams);
		assertIdsEquals(new long[] {12, 8, 5, 2, 18}, res);

        res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.DATE, true, pageParams);
        assertIdsEquals(new long[] {18, 17, 16, 15, 14}, res);
        res = formDataSearchDao.findPage(filter, FormDataSearchOrdering.DATE, false, pageParams);
        assertIdsEquals(new long[] {6, 4, 3, 2, 5}, res);
	}
	
	@Test
	public void testGetCount() {
		FormDataDaoFilter filter = new FormDataDaoFilter();
		
		assertEquals(18, formDataSearchDao.getCount(filter));
		
		filter.setDepartmentIds(Collections.singletonList(1));
		assertEquals(6, formDataSearchDao.getCount(filter));
		
		filter.setDepartmentIds(null);
		filter.setFormTypeIds(Collections.singletonList(1l));
		assertEquals(4, formDataSearchDao.getCount(filter));
	}
	
	private void assertIdsEquals(long[] expected, List<FormDataSearchResultItem> items) {
		if (expected.length != items.size()) {
			fail("List size mismatch: " + expected.length + " expected but " + items.size() + " received");
			return;
		}
		
		long[] received = new long[expected.length];

        int cnt = 0;
		for (FormDataSearchResultItem item : items) {
			received[cnt++] = item.getFormDataId();
		}
        Arrays.sort(expected);
        Arrays.sort(received);
        Assert.assertArrayEquals(expected, received);
	}
}
