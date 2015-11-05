package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.*;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.DeleteFormsSourceResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.DeleteFormsSourseAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author auldanov
 */
@Service
@PreAuthorize("hasAnyRole('ROLE_CONTROL', 'ROLE_CONTROL_UNP', 'ROLE_CONTROL_NS')")
public class DeleteFormSoursesHandler extends AbstractActionHandler<DeleteFormsSourseAction, DeleteFormsSourceResult> {

    private static final String SOURCE_CANCEL_ERR =
            "Не может быть отменено назначение \"%s\"-\"%s\"-\"%s\", т.к. назначение является %s ";

    @Autowired
    SourceService departmentFormTypeService;

    @Autowired
    DepartmentService departmentService;

    @Autowired
    FormTypeService formTypeService;

    @Autowired
    FormDataService formDataService;

    @Autowired
    DeclarationTypeService declarationTypeService;

    @Autowired
    private LogEntryService logEntryService;

    @Autowired
    private PeriodService periodService;

    public DeleteFormSoursesHandler() {
        super(DeleteFormsSourseAction.class);
    }

    @Override
    public DeleteFormsSourceResult execute(DeleteFormsSourseAction action, ExecutionContext executionContext) throws ActionException {
        // список id записей назначений
        Set<Long> set = new HashSet<Long>();
        Logger logger = new Logger();
        // Удаление записей, для которых не указаны источники-приёмники, из таблицы в БД Системы и из списка назначенных налоговых форм / деклараций на подразделение
        boolean existFormData = false;
        for (FormTypeKind data: action.getKind()) {
            // проверим наличие форм
            existFormData |= formDataService.existFormData(data.getFormTypeId().intValue(), data.getKind(), data.getDepartment().getId(), logger);
            // если есть формы, то проверки на связи не делаем
            if (existFormData) {
                continue;
            }
            // возьмем его источников - налоговые формы
            //TODO передавать данные с клиента
            Date periodStart = new Date();
            Date periodEnd = new Date();
            List<DepartmentFormType> formsSources = departmentFormTypeService.getDFTSourcesByDFT(data.getDepartment().getId(),
                    data.getFormTypeId().intValue(), data.getKind(), periodStart, periodEnd);
            // возьмем его назначений - налоговые формы
            List<DepartmentFormType> formsDestinations = departmentFormTypeService.getFormDestinations(data.getDepartment().getId(), data.getFormTypeId().intValue(), data.getKind(), null, null);
            // приемники - декларации, источников деклараций у нас не существует
            List<DepartmentDeclarationType> declarationDestinations = departmentFormTypeService.getDeclarationDestinations(data.getDepartment().getId(), data.getFormTypeId().intValue(), data.getKind(), null, null);
            // если есть источники или назначения выводим ошибку
            if (!formsSources.isEmpty()){
                StringBuilder stringBuffer = new StringBuilder();
                for (DepartmentFormType form: formsSources){
                    stringBuffer.append(getTaxFormErrorTextPart(form));
                }
                // удаляем последний символ ", "
                logger.error(
                        SOURCE_CANCEL_ERR + stringBuffer.delete(stringBuffer.length()-2, stringBuffer.length()).toString(),
                        data.getDepartment().getName(), data.getKind().getTitle(), data.getName(), "приемником для"
                );
            } else if (!formsDestinations.isEmpty()){
                StringBuilder stringBuffer = new StringBuilder();
                for (DepartmentFormType form: formsDestinations){
                    stringBuffer.append(getTaxFormErrorTextPart(form));
                }
                // удаляем последний символ ", "
                logger.error(
                        SOURCE_CANCEL_ERR + stringBuffer.delete(stringBuffer.length()-2, stringBuffer.length()).toString(),
                        data.getDepartment().getName(), data.getKind().getTitle(), data.getName(), "источником для"
                );
            } else if (!declarationDestinations.isEmpty()){
                StringBuilder stringBuffer = new StringBuilder();
                for (DepartmentDeclarationType form: declarationDestinations){
                    stringBuffer.append(getTaxDeclarationErrorTextPart(form));
                }
                // удаляем последний символ ", "
                logger.error(
                        SOURCE_CANCEL_ERR + stringBuffer.delete(stringBuffer.length()-2, stringBuffer.length()).toString(),
                        data.getDepartment().getName(), data.getKind().getTitle(), data.getName(), "источником для декларации"
                );
            } else{
                set.add(data.getId());
            }
        }
        if (!logger.containsLevel(LogLevel.ERROR)) {
            // удаляем назначение
            departmentFormTypeService.deleteDFT(set);
        }

        DeleteFormsSourceResult result = new DeleteFormsSourceResult();
        result.setUuid(logEntryService.save(logger.getEntries()));
        result.setExistFormData(existFormData);

        return result;
    }

    //http://conf.aplana.com/pages/viewpage.action?pageId=9583288
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
                        "\"%s\"-\"%s\"-\"%s\" %s, ",
                        departmentService.getDepartment(dft.getDepartmentId()).getName(),
                        dft.getKind().getTitle(),
                        type.getName(),
                        periodCombo)
        );

        return stringBuffer;
    }

    private StringBuffer getTaxDeclarationErrorTextPart(DepartmentDeclarationType dft){
        StringBuffer stringBuffer = new StringBuffer();
        DeclarationType type = declarationTypeService.get(dft.getDeclarationTypeId());
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

        stringBuffer.append(departmentService.getDepartment(dft.getDepartmentId()).getName());
        stringBuffer.append(" - ");
        stringBuffer.append(type.getName());
        stringBuffer.append(periodCombo);
        stringBuffer.append(", ");

        return stringBuffer;
    }

    @Override
    public void undo(DeleteFormsSourseAction deleteFormsSourseAction, DeleteFormsSourceResult deleteFormsSourceResult, ExecutionContext executionContext) throws ActionException {

    }
}
