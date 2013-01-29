package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.impl.DictionaryManagerImpl;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.impl.NumericDictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchParams;
import com.aplana.sbrf.taxaccounting.model.PaginatedSearchResult;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"NumericDictionaryDataProviderTest.xml"})
public class NumericDictionaryDataProviderTest {

	@Qualifier("numericDictionaryManager")
	@Autowired
	private DictionaryManagerImpl numericDictionaryData;

	private PaginatedSearchParams page = new PaginatedSearchParams(0, 5);

	private  NumericDictionaryDataProvider dp;

	@Before
	public void prepareTestEnv() {
		dp = (NumericDictionaryDataProvider) numericDictionaryData.getDataProvider("transportEcoClass");
	}

	@Test
	public void testSimple(){
		assertNotNull(numericDictionaryData);
	}

	@Test
	public void testSize() {

		PaginatedSearchResult<DictionaryItem<BigDecimal>> result = dp.getValues("", page);
		Assert.assertEquals(result.getTotalRecordCount(), 10);
		Assert.assertEquals(result.getRecords().size(), 5);
	}

	@Test
	public void testValueSearch() {


		PaginatedSearchResult<DictionaryItem<BigDecimal>> result = dp.getValues("5", page);
		Assert.assertEquals(result.getRecords().size(), 1);
		Assert.assertEquals(result.getTotalRecordCount(), 1);
	}

	@Test
	public void testNameSearch() {

		PaginatedSearchResult<DictionaryItem<BigDecimal>> result = dp.getValues("name5", page);
		Assert.assertEquals(result.getRecords().size(), 1);
		Assert.assertEquals(result.getTotalRecordCount(), 1);
	}

	@Test
	public void testEmptySearch() {
		PaginatedSearchResult<DictionaryItem<BigDecimal>> result = dp.getValues("Нет такого значения", page);
		Assert.assertEquals(result.getRecords().size(), 0);
		Assert.assertEquals(result.getTotalRecordCount(), 0);
	}

}