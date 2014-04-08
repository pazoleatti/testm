package com.aplana.sbrf.taxaccounting.web.module.bookerstatements.shared;

import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.Date;

/**
 * Получение бухгалтерской отчетности
 * @author dloshkarev
 */
public class GetBookerStatementsAction extends UnsecuredActionImpl<GetBookerStatementsResult> implements ActionName {
    private Date version;
    /** Идентификатор подразделения */
    private int departmentId;
    /** Тип бухотчетности (101, 102) */
    private int statementsKind;
    private PagingParams pagingParams;

    /** Флаг для получения всех идентификаторов записей. Используется только при удалении */
    private boolean needOnlyIds;

    public void setPagingParams(PagingParams pagingParams) {
        this.pagingParams = pagingParams;
    }

    public PagingParams getPagingParams() {
        return pagingParams;
    }

    public int getStatementsKind() {
        return statementsKind;
    }

    public void setStatementsKind(int statementsKind) {
        this.statementsKind = statementsKind;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public Date getVersion() {
        return version;
    }

    public void setVersion(Date version) {
        this.version = version;
    }

    public boolean isNeedOnlyIds() {
        return needOnlyIds;
    }

    public void setNeedOnlyIds(boolean needOnlyIds) {
        this.needOnlyIds = needOnlyIds;
    }

    @Override
    public String getName() {
        return "Получение бухгалтерской отчетности";
    }
}
