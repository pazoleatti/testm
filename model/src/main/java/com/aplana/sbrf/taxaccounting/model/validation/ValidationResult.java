package com.aplana.sbrf.taxaccounting.model.validation;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import lombok.Data;

import java.util.List;

@Data
public class ValidationResult {
    private boolean success;
    private String message;
    private List<LogEntry> logEntries;

    public ValidationResult(boolean success) {
        this.success = success;
    }
}
