package com.aplana.sbrf.taxaccounting.dao.impl.datarow;

import org.junit.Assert;
import org.junit.Test;


public class DataRowDaoImplUtilsTest {
	
	@Test
	public void calcOrdStep(){
		Assert.assertEquals(1l, DataRowDaoImplUtils.calcOrdStep(1l, 5l, 3));
		Assert.assertEquals(1l, DataRowDaoImplUtils.calcOrdStep(1l, 6l, 3));
		Assert.assertEquals(1l, DataRowDaoImplUtils.calcOrdStep(1l, 7l, 3));
		Assert.assertEquals(1l, DataRowDaoImplUtils.calcOrdStep(3l, 7l, 3));
		
		Assert.assertEquals(1l, DataRowDaoImplUtils.calcOrdStep(1l, 8l, 3));
		Assert.assertEquals(2l, DataRowDaoImplUtils.calcOrdStep(1l, 9l, 3));
		Assert.assertEquals(2l, DataRowDaoImplUtils.calcOrdStep(1l, 10l, 3));
	}

}
