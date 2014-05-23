package com.aplana.sbrf.taxaccounting.dao.impl.util;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 07.02.14 11:50
 */

public class SqlUtilsTest {

	@Test(expected = IllegalArgumentException.class)
	public void checkListSize1() {
		SqlUtils.checkListSize(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkListSize2() {
		SqlUtils.checkListSize(Arrays.asList(new Integer[] {}));
	}

    @Test
    public void transformToSqlInStatementTest1() {
        Assert.assertEquals("(abc in (1,2,3) or abc in (4,5,6))", SqlUtils.transformToSqlInStatement("abc", Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6}), 3));
    }

    @Test
    public void splitCollectionTest(){
        List<List<Integer>> lists = new ArrayList<List<Integer>>(SqlUtils.splitCollection(Arrays.asList(new Integer[]{1, 2, 3, 4, 5, 6}), 2));
        Assert.assertTrue(lists.get(0).get(0).equals(1));
        Assert.assertTrue(lists.get(0).get(1).equals(2));

        Assert.assertTrue(lists.get(1).get(0).equals(3));
        Assert.assertTrue(lists.get(1).get(1).equals(4));

        Assert.assertTrue(lists.get(2).get(0).equals(5));
        Assert.assertTrue(lists.get(2).get(1).equals(6));
    }

    @Test
    public void splitCollectionTest2(){
        List<List<Integer>> lists = new ArrayList<List<Integer>>(SqlUtils.splitCollection(Arrays.asList(new Integer[]{1, 2, 3, 4, 5}), 3));
        Assert.assertTrue(lists.get(0).get(0).equals(1));
        Assert.assertTrue(lists.get(0).get(1).equals(2));
        Assert.assertTrue(lists.get(0).get(2).equals(3));

        Assert.assertTrue(lists.get(1).get(0).equals(4));
        Assert.assertTrue(lists.get(1).get(1).equals(5));
    }

	@Test
	public void transformFormStatesToSqlInStatementTest() {
		Assert.assertEquals(String.format("(%s,%s)", WorkflowState.ACCEPTED.getId(), WorkflowState.CREATED.getId()),
				SqlUtils.transformFormStatesToSqlInStatement(Arrays.asList(new WorkflowState[] {WorkflowState.ACCEPTED, WorkflowState.CREATED})));
	}

	@Test
	public void transformTaxTypeToSqlInStatementTest() {
		Assert.assertEquals(String.format("('%s','%s')", TaxType.INCOME.getCode(), TaxType.DEAL.getCode()),
				SqlUtils.transformTaxTypeToSqlInStatement(Arrays.asList(new TaxType[] {TaxType.INCOME, TaxType.DEAL})));
	}

	@Test
	public void transformFormKindsToSqlInStatementTest() {
		Assert.assertEquals(String.format("(%s,%s)", FormDataKind.ADDITIONAL.getId(), FormDataKind.UNP.getId()),
				SqlUtils.transformFormKindsToSqlInStatement(Arrays.asList(new FormDataKind[] {FormDataKind.ADDITIONAL, FormDataKind.UNP})));
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

}
