package com.aplana.sbrf.taxaccounting.service.script.util;

/**
 * Тесты для ScriptUtils
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 28.01.13 14:31
 */

import com.aplana.sbrf.taxaccounting.dao.FormDataDao;
import com.aplana.sbrf.taxaccounting.dao.FormTemplateDao;
import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataKind;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.WorkflowState;
import com.aplana.sbrf.taxaccounting.model.range.ColumnRange;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"ScriptUtilsTest.xml"})
public class ScriptUtilsTest {

	private static final Log logger = LogFactory.getLog(ScriptUtilsTest.class);

	@Autowired
	FormDataDao formDataDao;

	private FormData getFormData() {
		return formDataDao.get(1);
	}

	@Test
	public void summTest() {
		FormData fd = getFormData();
		logger.info(fd);
		double r = ScriptUtils.summ(fd, new ColumnRange(2, 1, 2));
		logger.info("summTest: " + r);
		Assert.assertTrue(Math.abs(r) > Constants.EPS);
	}

}
