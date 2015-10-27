package com.aplana.sbrf.taxaccounting.web.widget.version.client;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 27.10.2015 14:58
 */

public class ProjectVersionViewTest {

	// Тесты для IE
	private final static String[][] testSuite = new String[][] {
			{"msie 8.0", "msie 8.0", "msie\\s[\\d\\w\\.]*", "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 5.1; SLCC1; .NET CLR 1.1.4322)"},
			{"msie 7.0", "msie 7.0", "msie\\s[\\d\\w\\.]*", "Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 5.2)"},
			{"msie 7.0", "msie 7.0", "msie\\s[\\d\\w\\.]*", "Mozilla/5.0 (compatible; MSIE 7.0; Windows NT 5.0; Trident/4.0; FBSMTWB; .NET CLR 2.0.34861; .NET CLR 3.0.3746.3218; .NET CLR 3.5.33652; msn OptimizedIE8;ENUS)"},
			{"msie 9.0", "msie 9.0", "msie\\s[\\d\\w\\.]*", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0; chromeframe/13.0.782.215)"},
			{"msie 7.0", "msie 7.0", "msie\\s[\\d\\w\\.]*", "Mozilla/5.0 (MSIE 7.0; Macintosh; U; SunOS; X11; gu; SV1; InfoPath.2; .NET CLR 3.0.04506.30; .NET CLR 3.0.04506.648)"},
			{"msie 10.0","msie 10.0", "msie\\s[\\d\\w\\.]*", "Mozilla/1.22 (compatible; MSIE 10.0; Windows 3.1)"},
			{"msie 7.0", "msie 7.0", "msie\\s[\\d\\w\\.]*", "Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 5.2)"},
			{"msie 10.0","msie 10.0", "msie\\s[\\d\\w\\.]*", "Mozilla/4.0 (compatible; MSIE 10.0; Windows NT 6.1; Trident/5.0)"},
			{"msie 10.0","msie 10.0", "msie\\s[\\d\\w\\.]*", "Mozilla/5.0 (compatible; MSIE 10.0; Macintosh; Intel Mac OS X 10_7_3; Trident/6.0)"},
			{"msie 11", "msie 11", "msie\\s[\\d\\w\\.]*", "Mozilla/5.0 (compatible, MSIE 11, Windows NT 6.3; Trident/7.0; rv:11.0) like Gecko"},
			{"msie 11.0", "rv:11.0", "rv:[\\d\\w\\.]*", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; AS; rv:11.0) like Gecko"},
			{"msie 7.0b","msie 7.0b", "msie\\s[\\d\\w\\.]*", "Mozilla/4.0 (compatible; MSIE 7.0b; Windows NT 5.1; .NET CLR 1.1.4322)"}
	};

	@Test
	public void findRegExp() {
		for (String[] test : testSuite) {
			String expectedResult = test[1];
			String pattern = test[2];
			String userAgent = test[3];
			String foundResult = ProjectVersionView.findRegExp(userAgent.toLowerCase(), pattern);
			Assert.assertEquals(expectedResult, foundResult);
		}
		String userAgent = "Mozilla/5.0 (msie)".toLowerCase();
		Assert.assertNull(ProjectVersionView.findRegExp(userAgent, "msie\\s[\\d\\w\\.]*"));
		userAgent = "Mozilla/5.0 (qwe; rv:)".toLowerCase();
		Assert.assertNull(userAgent, ProjectVersionView.findRegExp(userAgent, "rv:11.0"));
		userAgent = "Mozilla/5.0 (X11; Linux i586; rv:31.0) Gecko/20100101 Firefox/31.0".toLowerCase();
		Assert.assertEquals("firefox/31.0", ProjectVersionView.findRegExp(userAgent, "firefox/[\\d\\w\\.]+"));
		userAgent = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2224.3 Safari/537.36".toLowerCase();
		Assert.assertEquals("chrome/41.0.2224.3", ProjectVersionView.findRegExp(userAgent, "chrome/[\\d\\w\\.]+"));
		userAgent = "Mozilla/5.0 (X11; Linux i586; rv:31.0) Gecko/20100101 Firefox".toLowerCase();
		Assert.assertNull(ProjectVersionView.findRegExp(userAgent, "firefox/[\\d\\w\\.]+"));
		userAgent = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/ Safari/537.36".toLowerCase();
		Assert.assertNull(ProjectVersionView.findRegExp(userAgent, "chrome/[\\d\\w\\.]+"));
	}

	@Test
	public void parseUserAgent() {
		for (String[] test : testSuite) {
			String expectedResult = test[0];
			String userAgent = test[3];
			String foundResult = ProjectVersionView.parseUserAgent(userAgent.toLowerCase());
			Assert.assertEquals(expectedResult, foundResult);
		}
		String userAgent = "Mozilla/5.0 (msie)".toLowerCase();
		Assert.assertEquals(userAgent, ProjectVersionView.parseUserAgent(userAgent));
		userAgent = "Mozilla/5.0 (qwe; rv:)".toLowerCase();
		Assert.assertEquals(userAgent, ProjectVersionView.parseUserAgent(userAgent));
		userAgent = "Mozilla/5.0 (X11; Linux i586; rv:31.0) Gecko/20100101 Firefox/31.0".toLowerCase();
		Assert.assertEquals("firefox 31.0", ProjectVersionView.parseUserAgent(userAgent));
		userAgent = "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2224.3 Safari/537.36".toLowerCase();
		Assert.assertEquals("chrome 41.0.2224.3", ProjectVersionView.parseUserAgent(userAgent));
		userAgent = "12123124234фывф ыважфыа;asfd;; as(cfsd)/ as\\f df".toLowerCase();
		Assert.assertEquals(userAgent, ProjectVersionView.parseUserAgent(userAgent));
	}

}
