package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат запуска задачи на загрузку настроек подразделений из excel-файла
 */
public class ImportDepartmentConfigsResult extends ActionResult {
    //Сообщение об подтверждении что выбранное подразделение не совпадает с подразделением из имени файла
    private String confirmDepartmentCheck;

    public String getConfirmDepartmentCheck() {
        return confirmDepartmentCheck;
    }

    public void setConfirmDepartmentCheck(String confirmDepartmentCheck) {
        this.confirmDepartmentCheck = confirmDepartmentCheck;
    }
}
