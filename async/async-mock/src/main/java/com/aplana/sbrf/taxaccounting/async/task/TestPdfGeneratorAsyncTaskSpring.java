package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Спринговая реализация таска "Генерация pdf-файла" для вызова из дев-мода
 * @author Lhaziev
 */
@Component("TestPdfGeneratorAsyncTaskSpring")
@Transactional
public class TestPdfGeneratorAsyncTaskSpring extends PdfGeneratorAsyncTask {
}
