package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server;

import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.DepartmentFormType;
import com.aplana.sbrf.taxaccounting.model.FormTypeKind;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.service.DepartmentService;
import com.aplana.sbrf.taxaccounting.service.FormTypeService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.SourceService;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.DeleteFormsSourceResult;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.shared.DeleteFormsSourseAction;
import com.gwtplatform.dispatch.server.ExecutionContext;
import com.gwtplatform.dispatch.server.actionhandler.AbstractActionHandler;
import com.gwtplatform.dispatch.shared.ActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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
        for (FormTypeKind data: action.getKind()){
            // возьмем его источников - налоговые формы
            List<DepartmentFormType> formsSources = departmentFormTypeService.getDFTSourcesByDFT(data.getDepartment().getId(), data.getFormTypeId().intValue(), data.getKind());
            // возьмем его назначений - налоговые формы
            List<DepartmentFormType> formsDestinations = departmentFormTypeService.getFormDestinations(data.getDepartment().getId(), data.getFormTypeId().intValue(), data.getKind());
            // приемники - декларации, источников деклараций у нас не существует
            List<DepartmentDeclarationType> declarationDestinitions = departmentFormTypeService.getDeclarationDestinations(data.getDepartment().getId(), data.getFormTypeId().intValue(), data.getKind());
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
                    stringBuffer.append(departmentService.getDepartment(form.getDepartmentId()).getName());
                    stringBuffer.append(form.getKind().getName());
                    stringBuffer.append(formTypeService.get(form.getFormTypeId()).getName());
                    stringBuffer.append(", ");
                }
                // удаляем последний символ ", "
                logger.error(headErrMsg+ " приемником для "+stringBuffer.delete(stringBuffer.length()-2, stringBuffer.length()).toString());
            } else if (formsDestinations.size() != 0){
                StringBuffer stringBuffer = new StringBuffer();
                for (DepartmentFormType form: formsDestinations){
                    stringBuffer.append(departmentService.getDepartment(form.getDepartmentId()).getName());
                    stringBuffer.append(form.getKind().getName());
                    stringBuffer.append(formTypeService.get(form.getFormTypeId()).getName());
                    stringBuffer.append(", ");
                }
                // удаляем последний символ ", "
                logger.error(headErrMsg+ " источником для "+stringBuffer.delete(stringBuffer.length()-2, stringBuffer.length()).toString());
            } else if (declarationDestinitions.size() != 0){
                StringBuffer stringBuffer = new StringBuffer();
                for (DepartmentDeclarationType form: declarationDestinitions){
                    stringBuffer.append(departmentService.getDepartment(form.getDepartmentId()).getName());
                    stringBuffer.append(formTypeService.get(form.getDeclarationTypeId()).getName());
                    stringBuffer.append(", ");
                }
                // удаляем последний символ ", "
                logger.error(headErrMsg + " источником для декларации " + stringBuffer.delete(stringBuffer.length() - 2, stringBuffer.length()).toString());
            } else{
                set.add(data.getId());
            }
        }
        // удаляем назначение
        departmentFormTypeService.deleteDFT(set);

        DeleteFormsSourceResult result = new DeleteFormsSourceResult();
        result.setUuid(logEntryService.save(logger.getEntries()));

        return result;
    }

    @Override
    public void undo(DeleteFormsSourseAction deleteFormsSourseAction, DeleteFormsSourceResult deleteFormsSourceResult, ExecutionContext executionContext) throws ActionException {

    }
}
