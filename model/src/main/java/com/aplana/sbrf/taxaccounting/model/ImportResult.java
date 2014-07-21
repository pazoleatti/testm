package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Результат импорта справочников. Списки сущностей (например файлов).
 *
 * @author Dmitriy Levykin
 */
public class ImportResult<T> {
    private final List<T> successFileList = new LinkedList<T>();
    private final List<T> skipFileList = new LinkedList<T>();
    private final List<T> failFileList = new LinkedList<T>();
    private final Map<T, List<LogEntry>> failLogMap = new HashMap<T, List<LogEntry>>();

    public List<T> getSuccessFileList() {
        return successFileList;
    }

    public List<T> getSkipFileList() {
        return skipFileList;
    }

    public List<T> getFailFileList() {
        return failFileList;
    }

    public Map<T, List<LogEntry>> getFailLogMap() {
        return failLogMap;
    }

    public void add(ImportResult importResult) {
        if (importResult.getSuccessFileList() != null && successFileList != null) {
            successFileList.addAll(importResult.getSuccessFileList());
        }
        if (importResult.getSkipFileList() != null) {
            skipFileList.addAll(importResult.getSkipFileList());
        }
        if (importResult.getFailFileList() != null) {
            failFileList.addAll(importResult.getFailFileList());
        }
        if (importResult.getFailLogMap() != null) {
            failLogMap.putAll(importResult.getFailLogMap());
        }
    }
}
