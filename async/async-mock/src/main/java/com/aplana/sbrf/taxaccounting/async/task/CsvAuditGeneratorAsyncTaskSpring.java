package com.aplana.sbrf.taxaccounting.async.task;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("CsvAuditGeneratorAsyncTaskSpring")
@Transactional
public class CsvAuditGeneratorAsyncTaskSpring extends CsvAuditGeneratorAsyncTask{
}