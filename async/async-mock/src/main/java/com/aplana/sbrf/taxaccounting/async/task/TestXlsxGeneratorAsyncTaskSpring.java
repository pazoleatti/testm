package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Генерация xlsx-файла" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("TestXlsxGeneratorAsyncTaskSpring")
@Transactional
public class TestXlsxGeneratorAsyncTaskSpring extends XlsxGeneratorAsyncTask {
}