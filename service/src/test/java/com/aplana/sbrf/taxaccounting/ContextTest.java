package com.aplana.sbrf.taxaccounting;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Vitalii Samolovskikh
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/testServiceContext.xml", "service.xml"})
public class ContextTest implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	@Test
	public void testContext(){
		Assert.assertNotNull(applicationContext);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
