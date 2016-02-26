package com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;
import com.aplana.sbrf.taxaccounting.web.main.api.shared.dispatch.ActionName;
import com.gwtplatform.dispatch.shared.UnsecuredActionImpl;

/**
 * Действие добавления строки.
 *
 * @author lhaziev
 */
public class TimerSubreportAction extends UnsecuredActionImpl<TimerSubreportResult> implements ActionName {

    private long declarationDataId;

    public long getDeclarationDataId() {
        return declarationDataId;
    }

    public void setDeclarationDataId(long declarationDataId) {
        this.declarationDataId = declarationDataId;
    }

    @Override
	public String getName() {
		return "Проверка наличия файлов для выгрузки";
	}
}
