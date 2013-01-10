package com.aplana.sbrf.taxaccounting;

import com.aplana.sbrf.taxaccounting.util.ScriptExposed;
import junit.framework.Assert;
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

	@Test
	public void testScriptExposed(){
		Assert.assertTrue(applicationContext.getBeansWithAnnotation(ScriptExposed.class).containsKey("formDataCompositionService"));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
