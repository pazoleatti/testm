package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormType;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.DeleteDeclarationSourcesAction;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.DeleteDeclarationSourcesResult;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@PreAuthorize("hasAnyRole('N_ROLE_CONTROL_UNP', 'N_ROLE_CONTROL_NS', 'F_ROLE_CONTROL_UNP', 'F_ROLE_CONTROL_NS')")
public class DeleteDeclarationSourcesHandler extends AbstractActionHandler<DeleteDeclarationSourcesAction, DeleteDeclarationSourcesResult> {

    private static final String SOURCE_CANCEL_ERR =
            "Не может быть отменено назначение \"%s\"-\"%s\", т.к. назначение является приемником для ";

	public DeleteDeclarationSourcesHandler() {
		super(DeleteDeclarationSourcesAction.class);
	}

	@Autowired
	SourceService departmentFormTypeService;
	@Autowired
	LogEntryService logEntryService;
	@Autowired
	DepartmentService departmentService;
	@Autowired
	FormTypeService formTypeService;
	@Autowired
	DeclarationTypeService declarationTypeService;
	@Autowired
	DeclarationDataService declarationDataService;
    @Autowired
    private PeriodService periodService;

	@Override
	public DeleteDeclarationSourcesResult execute(DeleteDeclarationSourcesAction action, ExecutionContext executionContext) throws ActionException {
		DeleteDeclarationSourcesResult result = new DeleteDeclarationSourcesResult();
        Logger logger = new Logger();
        boolean existDeclaration = false;
        //TODO передавать данные с клиента
        Date periodStart = new Date();
        Date periodEnd = new Date();
		for (FormTypeKind ddt : action.getKind()) {
            // проверим наличие деклараций
            existDeclaration |= declarationDataService.existDeclaration(ddt.getFormTypeId().intValue(), ddt.getDepartment().getId(), logger.getEntries());
            // если есть, то проверки на связи не делаем
            if (existDeclaration) {
                continue;
            }
			List<DepartmentFormType> departmentFormTypes = departmentFormTypeService
					.getDFTSourceByDDT(ddt.getDepartment().getId(), ddt.getFormTypeId().intValue(), periodStart, periodEnd);
			if (departmentFormTypes.isEmpty()) { // Нет назначений
				departmentFormTypeService.deleteDDT(Arrays.asList(ddt.getId()));
			} else {
				StringBuilder sb = new StringBuilder();

				for (DepartmentFormType dft : departmentFormTypes) {
                    sb.append(getTaxFormErrorTextPart(dft));
				}
                logger.error(
                        SOURCE_CANCEL_ERR + sb.delete(sb.length() - 2, sb.length()).toString(),
                        ddt.getDepartment().getName(), ddt.getName()
                );
			}
		}

		result.setUuid(logEntryService.save(logger.getEntries()));
        result.setExistDeclaration(existDeclaration);
		return result;
	}

	@Override
	public void undo(DeleteDeclarationSourcesAction deleteDeclarationSourcesAction, DeleteDeclarationSourcesResult deleteDeclarationSourcesResult, ExecutionContext executionContext) throws ActionException {

	}

    private StringBuffer getTaxFormErrorTextPart(DepartmentFormType dft){
        StringBuffer stringBuffer = new StringBuffer();
        FormType type = formTypeService.get(dft.getFormTypeId());
        List<ReportPeriod> periods =
                periodService.getReportPeriodsByDateAndDepartment(type.getTaxType(), dft.getDepartmentId(), dft.getPeriodStart(), dft.getPeriodEnd());
        String periodCombo = "";
        if (!periods.isEmpty()){
            if (periods.size() == 1){
                ReportPeriod first = periods.get(0);
                periodCombo = String.format(" в периоде %s %d", first.getName(), first.getTaxPeriod().getYear());
            } else {
                ReportPeriod first = periods.get(0);
                ReportPeriod last = periods.get(periods.size()-1);
                periodCombo = dft.getPeriodEnd() == null ?
                        String.format(" в периоде %s %d", first.getName(), first.getTaxPeriod().getYear())
                        :
                        String.format(" в периоде %s %d-%s %d", first.getName(), first.getTaxPeriod().getYear(), last.getName(), last.getTaxPeriod().getYear());
            }
        }

        stringBuffer.append(
                String.format(
                        "\"%s\"-\"%s\"-\"%s\" %s; ",
                        departmentService.getDepartment(dft.getDepartmentId()).getName(),
                        dft.getKind().getTitle(),
                        type.getName(),
                        periodCombo)
        );

        return stringBuffer;
    }
}
