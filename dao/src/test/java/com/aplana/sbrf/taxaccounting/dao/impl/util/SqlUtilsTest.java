package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class SqlUtilsTest {

	@Test(expected = IllegalArgumentException.class)
	public void checkListSize1() {
		SqlUtils.checkListSize(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkListSize2() {
		SqlUtils.checkListSize(Collections.emptyList());
	}

    @Test
    public void transformToSqlInStatementTest1() {
        Assert.assertEquals("(abc IN (1,2,3) OR abc IN (4,5,6))", SqlUtils.transformToSqlInStatement("abc", Arrays.asList(1, 2, 3, 4, 5, 6), 3));
    }

    @Test
    public void splitCollectionTest(){
        List<List<Integer>> lists = new ArrayList<>(SqlUtils.splitCollection(Arrays.asList(1, 2, 3, 4, 5, 6), 2));
        Assert.assertTrue(lists.get(0).get(0).equals(1));
        Assert.assertTrue(lists.get(0).get(1).equals(2));

        Assert.assertTrue(lists.get(1).get(0).equals(3));
        Assert.assertTrue(lists.get(1).get(1).equals(4));

        Assert.assertTrue(lists.get(2).get(0).equals(5));
        Assert.assertTrue(lists.get(2).get(1).equals(6));
    }

    @Test
    public void splitCollectionTest2(){
        List<List<Integer>> lists = new ArrayList<>(SqlUtils.splitCollection(Arrays.asList(1, 2, 3, 4, 5), 3));
        Assert.assertTrue(lists.get(0).get(0).equals(1));
        Assert.assertTrue(lists.get(0).get(1).equals(2));
        Assert.assertTrue(lists.get(0).get(2).equals(3));

        Assert.assertTrue(lists.get(1).get(0).equals(4));
        Assert.assertTrue(lists.get(1).get(1).equals(5));
    }

	@Test
	public void transformTaxTypeToSqlInStatementTest() {
		Assert.assertEquals(String.format("('%s','%s')", TaxType.INCOME.getCode(), TaxType.DEAL.getCode()),
				SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(TaxType.INCOME, TaxType.DEAL)));
	}

	@Test
	public void transformFormKindsToSqlInStatementTest() {
		Assert.assertEquals(String.format("(%s,%s)", FormDataKind.ADDITIONAL.getId(), FormDataKind.UNP.getId()),
				SqlUtils.transformFormKindsToSqlInStatement(Arrays.asList(FormDataKind.ADDITIONAL, FormDataKind.UNP)));
	}

	@Test
	public void preparePlaceHoldersTest1() {
		Assert.assertEquals("?", SqlUtils.preparePlaceHolders(1));
		Assert.assertEquals("?,?", SqlUtils.preparePlaceHolders(2));
	}

	@Test(expected = IllegalArgumentException.class)
	public void preparePlaceHoldersTest2() {
		Assert.assertEquals("?", SqlUtils.preparePlaceHolders(0));
	}

	@Test(expected = IllegalArgumentException.class)
	public void preparePlaceHoldersTest3() {
		Assert.assertEquals("?", SqlUtils.preparePlaceHolders(-47));
	}

	@Test
	public void test_transformToStringPrefixDisjunctionStatement() {
    	List<String> tokens = new ArrayList<>();
		tokens.add("token1");
		tokens.add("token2");
		tokens.add("token3");
    	String result = SqlUtils.transformToStringPrefixDisjunctionStatement("field", tokens);
    	Assert.assertEquals("(field LIKE 'token1%' OR field LIKE 'token2%' OR field LIKE 'token3%')", result);
	}

}
