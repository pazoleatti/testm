package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 13.08.15 20:29
 */

public class TAAbstractScriptingServiceImplTest extends TAAbstractScriptingServiceImpl {

	private static final String SCRIPT1 = "// графа 71 - col_052_3_2\n" +
			"switch (formDataEvent) {\n" +
			"//    case FormDataEvent.CALCULATE:\n" +
			"    case FormDataEvent.CREATE:\n" +
			"        formDataService.checkUnique(formData, logger)\n" +
			" /*   case FormDataEvent.CHECK:\n" +
			"        formDataService.checkUnique(formData, logger)*/\n" +
			"        break";

	@Test
	public void canExecuteScriptTest() {
		assertTrue(canExecuteScript(SCRIPT1, FormDataEvent.CREATE));
		assertFalse(canExecuteScript(SCRIPT1, FormDataEvent.IMPORT));
		assertFalse(canExecuteScript(SCRIPT1, FormDataEvent.CALCULATE));
		assertFalse(canExecuteScript(SCRIPT1, FormDataEvent.CHECK));
		assertFalse(canExecuteScript("  ", FormDataEvent.IMPORT));
		assertFalse(canExecuteScript(null, FormDataEvent.IMPORT));
		assertFalse(canExecuteScript(null, null));
		assertTrue(canExecuteScript("test", null));
	}
}
