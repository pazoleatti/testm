package com.aplana.sbrf.taxaccounting.model.util;

import junit.framework.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 15.08.14 17:17
 */

public class StringUtilsTest {

	private static final String s1 = "год";
	private static final String s2 = "года";
	private static final String s3 = "лет";

	@Test
	public void getNumberStringTest() {
		assertEquals(s1, StringUtils.getNumberString(1, s1, s2, s3));
		assertEquals(s2, StringUtils.getNumberString(2, s1, s2, s3));
		assertEquals(s2, StringUtils.getNumberString(3, s1, s2, s3));
		assertEquals(s2, StringUtils.getNumberString(4, s1, s2, s3));
		assertEquals(s3, StringUtils.getNumberString(8, s1, s2, s3));
		assertEquals(s3, StringUtils.getNumberString(11, s1, s2, s3));
		assertEquals(s3, StringUtils.getNumberString(13, s1, s2, s3));
		assertEquals(s2, StringUtils.getNumberString(124235324, s1, s2, s3));
	}


}
