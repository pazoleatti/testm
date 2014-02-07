package com.aplana.sbrf.taxaccounting.model.refbook;

import org.junit.Assert;
import org.junit.Test;

import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 06.02.14 18:05
 */

public class RefBookTypeTest {

	@Test
	public void getTest1() {
		Assert.assertEquals(RefBookType.LINEAR, RefBookType.get(0));
		Assert.assertEquals(RefBookType.HIERARCHICAL, RefBookType.get(1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void getTest2() {
		RefBookType.get(87);
	}

	@Test
	public void getIdTest(){
		Assert.assertEquals(0, RefBookType.LINEAR.getId());
		Assert.assertEquals(1, RefBookType.HIERARCHICAL.getId());
	}

}
