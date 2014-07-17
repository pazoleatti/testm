package com.aplana.sbrf.taxaccounting.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Результат импорта справочников. Списки сущностей (например файлов).
 *
 * @author Dmitriy Levykin
 */
public class ImportResult<T> {
    List<T> successFileList = new LinkedList<T>();
    List<T> skipFileList = new LinkedList<T>();
    List<T> failFileList = new LinkedList<T>();

    public List<T> getSuccessFileList() {
        return successFileList;
    }

    public List<T> getSkipFileList() {
        return skipFileList;
    }

    public List<T> getFailFileList() {
        return failFileList;
    }

    public void add(ImportResult importResult) {
        if (importResult.getSuccessFileList() != null && successFileList != null) {
            successFileList.addAll(importResult.getSuccessFileList());
        }
        if (importResult.getSkipFileList()!= null && skipFileList != null) {
            skipFileList.addAll(importResult.getSkipFileList());
        }
        if (importResult.getFailFileList() != null && failFileList != null) {
            failFileList.addAll(importResult.getFailFileList());
        }
    }
}
