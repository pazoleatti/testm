package com.aplana.sbrf.taxaccounting.web.module.formdata.server;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.datarow.DataRowRange;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.refbook.RefBookHelper;
import com.aplana.sbrf.taxaccounting.service.DataRowService;
import com.aplana.sbrf.taxaccounting.service.FormDataService;
import com.aplana.sbrf.taxaccounting.service.FormTemplateService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.web.main.api.server.SecurityService;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetRowsDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetRowsDataResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;

@Service
public class GetRowsDataHandler extends
		AbstractActionHandler<GetRowsDataAction, GetRowsDataResult> {

	@Autowired
	DataRowService dataRowService;
	@Autowired
	SecurityService securityService;
	@Autowired
	FormTemplateService formTemplateService;
    @Autowired
    FormDataService formDataService;

    @Autowired
    RefBookHelper refBookHelper;

    @Autowired
    private LogEntryService logEntryService;

	public GetRowsDataHandler() {
		super(GetRowsDataAction.class);
	}

	@Override
	public GetRowsDataResult execute(GetRowsDataAction action, ExecutionContext context) throws ActionException {
        // Режим «Корректировки» не работает с ручным вводом
        if (action.isCorrectionDiff()) {
            action.setManual(false);
        }
		GetRowsDataResult result = new GetRowsDataResult();

        // Фиксированные строки из шаблона
        FormTemplate formTemplate = formTemplateService.get(action.getFormDataTemplateId());

        if (!action.isFree()) {
            // Обновление измененных строк во временном срезе
            if (!action.getModifiedRows().isEmpty()) {
                refBookHelper.dataRowsCheck(action.getModifiedRows(), formTemplate.getColumns());
                dataRowService.update(securityService.currentUserInfo(), action.getFormDataId(), action.getModifiedRows(), action.isManual());
            }
            // Отображаемый диапазон строк
            DataRowRange dataRowRange;
            if (formTemplate.isFixedRows()) {
                dataRowRange = new DataRowRange(1, dataRowService.getRowCount(action.getFormDataId(),
                        action.isReadOnly() || action.isCorrectionDiff(), action.isManual()));
            } else {
                dataRowRange = new DataRowRange(action.getRange().getOffset(), action.getRange().getCount());
            }

            // Порция строк, режим отображения различий для корр. периода также как и режим редактирования работат со
            // временным срезом
            PagingResult<DataRow<Cell>> rows = dataRowService.getDataRows(action.getFormDataId(), dataRowRange,
                    !action.isCorrectionDiff(), action.isManual());

            Collections.sort(rows, new Comparator<DataRow<Cell>>() {
                @Override
                public int compare(DataRow<Cell> o1, DataRow<Cell> o2) {
                    return o1.getIndex().compareTo(o2.getIndex());
                }
            });
            result.setDataRows(rows);
        } else {
            result.setDataRows(new PagingResult<DataRow<Cell>>());
        }

		Logger logger = new Logger();
        refBookHelper.dataRowsDereference(logger, result.getDataRows(),
                formTemplate.getColumns());
        if (action.getInnerLogUuid() != null && !action.getInnerLogUuid().isEmpty()) {
            result.setUuid(logEntryService.update(logger.getEntries(), action.getInnerLogUuid()));
        } else {
            result.setUuid(logEntryService.save(logger.getEntries()));
        }

		return result;
	}

	@Override
	public void undo(GetRowsDataAction action, GetRowsDataResult result,
			ExecutionContext context) throws ActionException {
	}
}
