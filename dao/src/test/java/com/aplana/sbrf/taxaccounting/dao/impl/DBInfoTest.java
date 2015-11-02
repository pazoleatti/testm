package com.aplana.sbrf.taxaccounting.dao.impl;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 02.11.2015 19:54
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ContextTest.xml"})
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DBInfoTest {

	@Autowired
	DBInfo dbInfo;

	@Test
	public void test() {
		Assert.assertFalse(dbInfo.isSupportOver());
		Assert.assertTrue(dbInfo.isWithRecursive());
		Assert.assertFalse(dbInfo.isDateDiffNumber());
	}

}
