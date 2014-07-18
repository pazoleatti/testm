package com.aplana.sbrf.taxaccounting.web.module.sources.shared;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.model.source.SourceMode;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.DepartmentAssign;
import com.aplana.sbrf.taxaccounting.web.module.sources.shared.model.PeriodsInterval;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Set;

/**
 * Создание нового назначения
 * @author dloshkarev
 */
public class CreateAssignAction  extends UnsecuredActionImpl<CreateAssignResult> implements ActionName {
    /** Признак того, что идет обработка назначений источников для деклараций */
    private boolean isDeclaration;
    /** Назначение источников или приемников? */
    private SourceMode mode;
    /** Период дествия назначения */
    private PeriodsInterval periodsInterval;
    /** Объект, выбранный в левой таблице */
    private DepartmentAssign leftObject;
    /** Объекты, выбранные в правой таблице */
    private Set<DepartmentAssign> rightSelectedObjects;
    /** Подразделение-слева */
    private Integer leftDepartmentId;
    /** Подразделение-справа */
    private Integer rightDepartmentId;
    private TaxType taxType;


    public boolean isDeclaration() {
        return isDeclaration;
    }

    public void setDeclaration(boolean declaration) {
        isDeclaration = declaration;
    }

    public SourceMode getMode() {
        return mode;
    }

    public void setMode(SourceMode mode) {
        this.mode = mode;
    }

    public PeriodsInterval getPeriodsInterval() {
        return periodsInterval;
    }

    public void setPeriodsInterval(PeriodsInterval periodsInterval) {
        this.periodsInterval = periodsInterval;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public Integer getLeftDepartmentId() {
        return leftDepartmentId;
    }

    public void setLeftDepartmentId(Integer leftDepartmentId) {
        this.leftDepartmentId = leftDepartmentId;
    }

    public Integer getRightDepartmentId() {
        return rightDepartmentId;
    }

    public void setRightDepartmentId(Integer rightDepartmentId) {
        this.rightDepartmentId = rightDepartmentId;
    }

    public DepartmentAssign getLeftObject() {
        return leftObject;
    }

    public void setLeftObject(DepartmentAssign leftObject) {
        this.leftObject = leftObject;
    }

    public Set<DepartmentAssign> getRightSelectedObjects() {
        return rightSelectedObjects;
    }

    public void setRightSelectedObjects(Set<DepartmentAssign> rightSelectedObjects) {
        this.rightSelectedObjects = rightSelectedObjects;
    }

    @Override
    public String getName() {
        return "Создание нового назначения";
    }
}
