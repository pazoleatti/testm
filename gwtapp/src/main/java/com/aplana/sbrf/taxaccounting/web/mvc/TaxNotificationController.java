package com.aplana.sbrf.taxaccounting.web.mvc;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
public class TaxNotificationController {

    private final LogEntryService logEntryService;

    public TaxNotificationController(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }

    @PostMapping("/actions/createTaxNotification")
    public String createTaxNotification() {
        LogEntry log = new LogEntry(LogLevel.INFO, "Поставлена в очередь на исполнение операция \"Формирование Уведомлений о неудержанном налоге\"");
        String logsUuid = logEntryService.save(Collections.singletonList(log));
        return logsUuid;
    }
}
