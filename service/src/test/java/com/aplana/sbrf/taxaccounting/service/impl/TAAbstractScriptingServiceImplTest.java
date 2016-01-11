package com.aplana.sbrf.taxaccounting.service.impl;

import com.aplana.sbrf.taxaccounting.model.FormDataEvent;
import com.aplana.sbrf.taxaccounting.model.exception.ServiceException;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 13.08.15 20:29
 */

public class TAAbstractScriptingServiceImplTest extends TAAbstractScriptingServiceImpl {

	// Кодировка скриптов
	private final static String SCRIPT_ENCODING = "UTF-8";
	// Префикс пути скрипта
	private final static String SCRIPT_PATH_PREFIX = "../src/main/resources";
	// Имя файла скрипта
	private final static String SCRIPT_PATH_FILE_NAME = "script.groovy";

	private static final String SCRIPT1 = "// графа 71 - col_052_3_2\n" +
			" /*   case FormDataEvent.CHECK:\n" +
			"       /* formDataService.checkUnique(formData, logger)*/\n" +
			"        break" + "switch (formDataEvent) {\n" +
			" /*   case FormDataEvent.CHECK:\n" +
			"        formDataService.checkUnique(formData, logger)*/\n" +
			"        break" + "switch (formDataEvent) {\n" +
			"//    case FormDataEvent.CALCULATE:\n" +
			"    case FormDataEvent.CREATE:\n" +
			"        formDataService.checkUnique(formData, logger)\n" +
			" /*   case FormDataEvent.CHECK:\n" +
			"        formDataService.checkUnique(formData, logger)*/\n" +
			"        break";

	//@Test
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

	@Test
	public void canExecuteScriptsTest() {
		try {
			int index = 0;
			long max = Long.MIN_VALUE;
			long min = Long.MAX_VALUE;
			List<Long> times = new ArrayList<Long>(23000);
			String badScript = "";
			System.out.println("Start scanning resource directory for groovy scripts");
			for (String script : scanDirectory(new File(SCRIPT_PATH_PREFIX))) {
				for (FormDataEvent event : FormDataEvent.values()) {
					long start = System.currentTimeMillis();
					canExecuteScript(script, event);
					long end = System.currentTimeMillis();
					times.add(end - start);
					if (min > (end - start)) {
						min = end - start;
					}
					if (max < (end - start)) {
						max = end - start;
						badScript = script;
					}
					index++;
				}
			}
			final String eol = System.getProperty("line.separator");
			System.out.println("Successfully finished scanning resource directory for groovy scripts: ");
			long totalTime = 0;
			for (long value : times) {
				totalTime += value;
			}
			double average = ((double)totalTime)/index;
			double sqrDiff = 0;
			for (long value : times) {
				sqrDiff += ((value - average) * (value - average));
			}
			System.out.println(index + " combinations \"script-event\" operated by " + totalTime + " milliseconds");
			System.out.println("Average value : " + average);
			System.out.println("Dispersion value : " + sqrDiff/index);
			System.out.println("Min time : " + min + " milliseconds");
			//System.out.println("Max time : " + max + " milliseconds. Script first line : " + badScript.substring(0, badScript.indexOf(eol)));
		} catch (IOException e) {
			e.printStackTrace();
			throw new ServiceException("Error scanning resource directory for groovy scripts.", e);
		}
	}

	private List<String> scanDirectory(File rootDirectory) throws IOException {
		File[] filesInDirectory = rootDirectory.listFiles();
		List<String> scripts = new ArrayList<String>();

		if (filesInDirectory == null) {
			return scripts;
		}

		for (File file : filesInDirectory) {
			if (file.isDirectory()) {
				scripts.addAll(scanDirectory(file));
			} else {
				if (SCRIPT_PATH_FILE_NAME.equals(file.getName())) {
					scripts.add(readFile(file.getPath(), SCRIPT_ENCODING));
				}
			}
		}
		return scripts;
	}

	/**
	 * Чтение из файла в строку
	 */
	public static String readFile(String path, String charset) throws IOException {
		FileInputStream stream = new FileInputStream(new File(path));
		try {
			Reader reader = new BufferedReader(new InputStreamReader(stream, charset));
			StringBuilder builder = new StringBuilder();
			char[] buffer = new char[10240];
			int read;
			while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
				builder.append(buffer, 0, read);
			}
			return builder.toString();
		} finally {
			stream.close();
		}
	}
}
