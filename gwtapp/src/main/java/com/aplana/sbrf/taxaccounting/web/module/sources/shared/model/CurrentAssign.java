package com.aplana.sbrf.taxaccounting.web.module.sources.shared.model;

import java.io.Serializable;
import java.util.Date;

import com.aplana.sbrf.taxaccounting.model.FormDataKind;

/**
 * Общая модель для нижней таблицы
 * @author sgoryachkin
 * @author aivanov
 */
public class CurrentAssign implements Serializable{
	private static final long serialVersionUID = -5564995354908631670L;

    /* Идентификатор */
    private Long id;
    /* Название подразделения */
    private String departmentName;

    /* Название вида налоговой формы или декларации */
    private String name;

    /* Название типа НФ */
    private FormDataKind formKind;
    /* Количество назначеных форм/деклараций */
    private Integer count;
    /* Дата начала назначения */
    private Date startDateAssign;
    /* Дата окончания */
    private Date endDateAssign;

    private Boolean isForm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartDateAssign() {
        return startDateAssign;
    }

    public void setStartDateAssign(Date startDateAssign) {
        this.startDateAssign = startDateAssign;
    }

    public Date getEndDateAssign() {
        return endDateAssign;
    }

    public void setEndDateAssign(Date endDateAssign) {
        this.endDateAssign = endDateAssign;
    }

    public FormDataKind getFormKind() {
        return formKind;
    }

    public void setFormKind(FormDataKind formKind) {
        this.formKind = formKind;
    }

    public Boolean getIsForm() {
        return isForm;
    }

    public void setIsForm(Boolean isForm) {
        this.isForm = isForm;
    }
}
