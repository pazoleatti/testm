package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Модельный класс для файлов версии макета
 *
 * @author lhaziev
 */
public class DeclarationTemplateFile implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Шаблон РНУ
     */
    public static final String TF_TEMPLATE = "excel_template_dec.xlsx";
    /**
     * Приложение 11 задолженности по налогу
     */
    public static final String APPLICATION11_NDFL_DEBT_TEMPLATE = "app11_ndfl_debt.docx";
    /**
     * Приложение 12 задолженности по налогу
     */
    public static final String APPLICATION12_NDFL_DEBT_TEMPLATE = "app12_ndfl_debt.docx";
    /**
     * Приложение 13 задолженности по налогу для АСНУ != 6000
     */
    public static final String APPLICATION13_1_NDFL_DEBT_TEMPLATE = "app13_1_ndfl_debt.docx";
    /**
     * Приложение 13 задолженности по налогу для АСНУ == 6000
     */
    public static final String APPLICATION13_2_NDFL_DEBT_TEMPLATE = "app13_2_ndfl_debt.docx";
    /**
     * Приложение 14 задолженности по налогу
     */
    public static final String APPLICATION14_NDFL_DEBT_TEMPLATE = "app14_ndfl_debt.docx";

    /**
     * Отчет 2НДФЛ по ФЛ. Основная страница
     */
    public static final String NDFL_2_REPORT_BY_PERSON_PAGE_BASE = "ndfl_2_report_by_person_page_base.pdf";

    /**
     * Отчет 2НДФЛ по ФЛ. Страница приложения.
     */
    public static final String NDFL_2_REPORT_BY_PERSON_PAGE_APPLICATION = "ndfl_2_report_by_person_page_application.pdf";

    /**
     * 2НДФЛ для выдачи сотруднику - основная страница
     */
    public static final String NDFL_2_REFERENCE_FOR_PERSON_BASE_PAGE = "2ndfl_by_person_base_page.pdf";
    /**
     * 2НДФЛ для выдачи сотруднику - дополнительная страница
     */
    public static final String NDFL_2_REFERENCE_FOR_PERSON_ADDITIONAL_PAGE = "2ndfl_by_person_additional_page.pdf";

    private String fileName;
    private String blobDataId;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getBlobDataId() {
        return blobDataId;
    }

    public void setBlobDataId(String blobDataId) {
        this.blobDataId = blobDataId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeclarationTemplateFile that = (DeclarationTemplateFile) o;

        if (!fileName.equals(that.fileName)) return false;
        return blobDataId.equals(that.blobDataId);
    }

    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 31 * result + blobDataId.hashCode();
        return result;
    }
}
