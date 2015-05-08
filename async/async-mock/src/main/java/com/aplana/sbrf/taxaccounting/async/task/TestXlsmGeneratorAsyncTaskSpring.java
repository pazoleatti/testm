package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Генерация xlsm-файл" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("TestXlsmGeneratorAsyncTaskSpring")
@Transactional
public class TestXlsmGeneratorAsyncTaskSpring extends XlsmGeneratorAsyncTask {
}