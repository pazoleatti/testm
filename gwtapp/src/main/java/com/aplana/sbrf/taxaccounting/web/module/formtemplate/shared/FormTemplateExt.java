package com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared;

import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * User: avanteev
 */
public class FormTemplateExt implements Serializable {
    private FormTemplate formTemplate;
    private Date actualEndVersionDate;
    private List<FormStyle> styles;

    public FormTemplate getFormTemplate() {
        return formTemplate;
    }

    public void setFormTemplate(FormTemplate formTemplate) {
        this.formTemplate = formTemplate;
    }

    public Date getActualEndVersionDate() {
        return actualEndVersionDate;
    }

    public void setActualEndVersionDate(Date actualEndVersionDate) {
        this.actualEndVersionDate = actualEndVersionDate;
    }

    public List<FormStyle> getStyles() {
        return styles;
    }

    public void setStyles(List<FormStyle> styles) {
        this.styles = styles;
    }
}
