package com.aplana.sbrf.taxaccounting;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.NamingException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Vitalii Samolovskikh
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/testServiceContext.xml", "service.xml"})
public class ContextTest implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	
	@BeforeClass
	public static void setUp() throws MalformedURLException, NamingException  {
		URL urlSign = new URL("http://ignore/");
		URL urlRefBook = new URL("http://refBookImportPath/");
		SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		builder.bind("url/Sign", urlSign);
		builder.bind("url/RefBookDirectory", urlRefBook);
	}

	@Test
	public void testContext(){
		Assert.assertNotNull(applicationContext);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
