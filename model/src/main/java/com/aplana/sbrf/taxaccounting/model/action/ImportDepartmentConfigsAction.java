package com.aplana.sbrf.taxaccounting.model.action;

import java.io.InputStream;

public class ImportDepartmentConfigsAction {
    // выбранное подразделение в форме gui
    private int departmentId;
    // пропускать проверку выбранного подразделения с подразделением, взятым из имени файла. Иначе будет подтверждающее окно
    private boolean skipDepartmentCheck;
    private InputStream inputStream;
    private String fileName;

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isSkipDepartmentCheck() {
        return skipDepartmentCheck;
    }

    public void setSkipDepartmentCheck(boolean skipDepartmentCheck) {
        this.skipDepartmentCheck = skipDepartmentCheck;
    }
}
