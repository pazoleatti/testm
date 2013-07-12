package com.aplana.sbrf.taxaccounting.dao.script.impl;

import com.aplana.sbrf.taxaccounting.dao.dataprovider.impl.DictionaryManagerImpl;
import com.aplana.sbrf.taxaccounting.dao.dataprovider.impl.StringDictionaryDataProvider;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.dictionary.DictionaryItem;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"StringDictionaryDataProviderTest.xml"})
public class StringDictionaryDataProviderTest {

	@Qualifier("stringDictionaryManager")
	@Autowired
	private DictionaryManagerImpl stringDictionaryData;

	private PagingParams page = new PagingParams(0, 5);

	private  StringDictionaryDataProvider dp;

	@Before
	public void prepareTestEnv() {
		 dp = (StringDictionaryDataProvider) stringDictionaryData.getDataProvider("transportOkato");
	}

	@Test
	public void testSimple(){
		assertNotNull(stringDictionaryData);
	}

	@Test
	public void testSize() {

		PagingResult<DictionaryItem<String>> result = dp.getValues("", page);
		Assert.assertEquals(result.getTotalRecordCount(), 10);
		Assert.assertEquals(result.getRecords().size(), 5);
	}

	@Test
	public void testValueSearch() {


		PagingResult<DictionaryItem<String>> result = dp.getValues("value5", page);
		Assert.assertEquals(result.getRecords().size(), 1);
		Assert.assertEquals(result.getTotalRecordCount(), 1);
	}

	@Test
	public void testNameSearch() {

		PagingResult<DictionaryItem<String>> result = dp.getValues("name5", page);
		Assert.assertEquals(result.getRecords().size(), 1);
		Assert.assertEquals(result.getTotalRecordCount(), 1);
	}

	@Test
	public void testEmptySearch() {
		PagingResult<DictionaryItem<String>> result = dp.getValues("Нет такого значения", page);
		Assert.assertEquals(result.getRecords().size(), 0);
		Assert.assertEquals(result.getTotalRecordCount(), 0);
	}

}