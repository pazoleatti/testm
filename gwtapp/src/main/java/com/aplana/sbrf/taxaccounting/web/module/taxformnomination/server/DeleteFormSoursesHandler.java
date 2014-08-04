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

    public DeleteFormSoursesHandler() {
        super(DeleteFormsSourseAction.class);
    }

    @Override
    public DeleteFormsSourceResult execute(DeleteFormsSourseAction action, ExecutionContext executionContext) throws ActionException {
        // список id записей назначений
        Set<Long> set = new HashSet<Long>();
        Logger logger = new Logger();
        // Удаление записей, для которых не указаны источники-приёмники, из таблицы в БД Системы и из списка назначенных налоговых форм / деклараций на подразделение;
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
                    data.getFormTypeId().intValue(), data.getKind(), periodStart, periodEnd, null, false);
            // возьмем его назначений - налоговые формы
            List<DepartmentFormType> formsDestinations = departmentFormTypeService.getFormDestinations(data.getDepartment().getId(), data.getFormTypeId().intValue(), data.getKind(), null, null);
            // приемники - декларации, источников деклараций у нас не существует
            List<DepartmentDeclarationType> declarationDestinations = departmentFormTypeService.getDeclarationDestinations(data.getDepartment().getId(), data.getFormTypeId().intValue(), data.getKind(), null, null);
            // шаблонг начала сообщения
            final String headErrMsg = "Не может быть отменено назначение " +
                    data.getDepartment().getName() +
                    " - "+data.getKind().getName() +
                    " - "+data.getName() +
                    " т.к. назначение является ";
            // если есть источники или назначения выводим ошибку
            if (formsSources.size() != 0){
                StringBuffer stringBuffer = new StringBuffer();
                for (DepartmentFormType form: formsSources){
                    stringBuffer.append(getTaxFormErrorTextPart(form.getDepartmentId(), form.getKind(), form.getFormTypeId()));
                }
                // удаляем последний символ ", "
                logger.error(headErrMsg+ " приемником для "+stringBuffer.delete(stringBuffer.length()-2, stringBuffer.length()).toString());
            } else if (formsDestinations.size() != 0){
                StringBuffer stringBuffer = new StringBuffer();
                for (DepartmentFormType form: formsDestinations){
                    stringBuffer.append(getTaxFormErrorTextPart(form.getDepartmentId(), form.getKind(), form.getFormTypeId()));
                }
                // удаляем последний символ ", "
                logger.error(headErrMsg+ " источником для "+stringBuffer.delete(stringBuffer.length()-2, stringBuffer.length()).toString());
            } else if (declarationDestinations.size() != 0){
                StringBuffer stringBuffer = new StringBuffer();
                for (DepartmentDeclarationType form: declarationDestinations){
                    stringBuffer.append(departmentService.getDepartment(form.getDepartmentId()).getName());
                    stringBuffer.append(" - ");
                    stringBuffer.append(declarationTypeService.get(form.getDeclarationTypeId()).getName());
                    stringBuffer.append(", ");
                }
                // удаляем последний символ ", "
                logger.error(headErrMsg + " источником для декларации " + stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length()).toString());
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

    private StringBuffer getTaxFormErrorTextPart(int departmentId, FormDataKind formDataKind, int formTypeId){
        StringBuffer stringBuffer = new StringBuffer();

        stringBuffer.append(departmentService.getDepartment(departmentId).getName());
        stringBuffer.append(" - ");
        stringBuffer.append(formDataKind.getName());
        stringBuffer.append(" - ");
        stringBuffer.append(formTypeService.get(formTypeId).getName());
        stringBuffer.append(", ");

        return stringBuffer;
    }

    @Override
    public void undo(DeleteFormsSourseAction deleteFormsSourseAction, DeleteFormsSourceResult deleteFormsSourceResult, ExecutionContext executionContext) throws ActionException {

    }
}
