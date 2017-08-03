package com.aplana.sbrf.taxaccounting.web.module.declarationlist.shared;

import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

import java.util.List;

public class OperationInfoAction extends UnsecuredActionImpl<OperationInfoResult> implements ActionName {

    private String declarationDataReportType;

    private List<Long> declarationDataIdList;

    public String getDeclarationDataReportType() {
        return declarationDataReportType;
    }

    public void setDeclarationDataReportType(String declarationDataReportType) {
        this.declarationDataReportType = declarationDataReportType;
    }

    public List<Long> getDeclarationDataIdList() {
        return declarationDataIdList;
    }

    public void setDeclarationDataIdList(List<Long> declarationDataIdList) {
        this.declarationDataIdList = declarationDataIdList;
    }

    @Override
    public String getName() {
        return "Вернуть в Создана";
    }
}
