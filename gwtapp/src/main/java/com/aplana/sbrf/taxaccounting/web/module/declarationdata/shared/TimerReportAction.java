package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.ReportType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Действие добавления строки.
 *
 * @author lhaziev
 */
public class TimerReportAction extends UnsecuredActionImpl<TimerReportResult> implements ActionName {

    private long declarationDataId;
    private ReportType type;

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    public ReportType getType() {
        return type;
    }

    public void setType(ReportType type) {
        this.type = type;
    }

    @Override
	public String getName() {
		return "Проверка наличия файлов для выгрузки";
	}
}
