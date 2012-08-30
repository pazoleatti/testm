package com.aplana.sbrf.taxaccounting.dao.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;

import com.aplana.sbrf.taxaccounting.dao.RowCheck;
import com.aplana.sbrf.taxaccounting.dao.RowCheckDao;

/**
 * Реализация хранилища для получения скриптов, задающих проверки строк в системе
 * TODO: черновая реализация, основанная на использовании папки с файлами на сервере приложений
 * В будущем нужно переписать на использование другого хранилища, например БД
 */
@Repository
public class RowCheckDaoImpl implements RowCheckDao {
	Log logger = LogFactory.getLog(getClass());
	private final static String ROOT = "C:/Work/TaxAccounting";
	@Override
	public List<RowCheck> getFormRowChecks(int formId) {
		File dir = new File(ROOT + "/" + formId + "/rows");
		if (!dir.exists() || !dir.isDirectory()) {
			return Collections.emptyList();
		}
		String[] scriptNames = dir.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".groovy");
			}
		});
		List<RowCheck> result =new ArrayList<RowCheck>(scriptNames.length);
		for (String fileName: scriptNames) {
			RowCheck rc = new RowCheck();
			rc.setName(fileName.substring(0, fileName.length() - 7));
			InputStream is = null;
			try {
				is = new FileInputStream(dir.getAbsoluteFile() + "/" + fileName);
				rc.setScript(IOUtils.toString(is, "WINDOWS-1251"));
			} catch (IOException e) {
				logger.error(e);
			} finally {
				IOUtils.closeQuietly(is);
			}
			result.add(rc);
		}
		return result;
	}
}
