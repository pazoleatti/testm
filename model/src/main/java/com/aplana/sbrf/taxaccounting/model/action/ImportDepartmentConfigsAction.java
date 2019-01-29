package com.aplana.sbrf.taxaccounting.model.action;

import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;

/**
 * Параметры загрузки настроек подразделений
 */
@Getter
@Setter
public class ImportDepartmentConfigsAction {
    // выбранное подразделение в форме gui
    private int departmentId;
    // пропускать проверку выбранного подразделения с подразделением, взятым из имени файла. Иначе будет подтверждающее окно
    private boolean skipDepartmentCheck;
    private InputStream inputStream;
    private String fileName;
    private long fileSize;
}
