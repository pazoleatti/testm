package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.dao.DeclarationTemplateDao;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.DeleteSelectedDeclarationRowsAction;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.script.service.NdflPersonService;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * <b>Удаление строк налоговой формы разделов 1 и 2</b>
 * Created by <i><b>s.molokovskikh</i></b> on 23.10.19.
 */
@Component("DeleteSelectedDeclarationRowsAsyncTask")
public class DeleteSelectedDeclarationRowsAsyncTask extends AbstractDeclarationAsyncTask {


    public static final String DELETE_ROW_MESSAGE = "Раздел %s. Удалена строка \"%s\". %s";
    public static final String SECTION1_INFO_MESSAGE = "ФЛ: %s, удалены все операции в форме относящиеся к данному ФЛ";
    public static final String SECTION2_INFO_MESSAGE = "ID операции: %s, удалены все строки в форме относящиеся к данной операции";
    public static final String TOTAL_MESSAGE = "Выполнено удаление строк формы ";

    @Autowired
    private NdflPersonDao ndflPersonDao;

    @Autowired
    private LogBusinessService logBusinessService;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private DeclarationTemplateDao declarationTemplateDao;

    @Autowired
    private NdflPersonService ndflPersonService;


    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger)
            throws InterruptedException {

        BusinessLogicResult result = new BusinessLogicResult(true, null);

        DeleteSelectedDeclarationRowsAction deleteRows = (DeleteSelectedDeclarationRowsAction) taskData.getParams().get("deleteRows");

        TAUserInfo userInfo = new TAUserInfo();
        userInfo.setUser(userService.getUser(taskData.getUserId()));
        userInfo.setIp((String) taskData.getParams().get("userIP"));


        Long declarationDataId = deleteRows.getDeclarationDataId();
        DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
        DeclarationTemplate declarationTemplate = declarationTemplateDao.get(declarationData.getDeclarationTemplateId());

        if (declarationTemplate.getType().getId() != (DeclarationType.NDFL_PRIMARY)) {
            logger.error("Удаление строк возможно только для первичной формы");
            return result;
        }

        //Для строк Раздела 1
        if (DeclarationDataSection.SECTION1.equals(deleteRows.getSection())) {
            List<NdflPerson> ndflPersonList = ndflPersonDao.findByIdIn(deleteRows.getSectionIds());
            for (NdflPerson ndflPerson : ndflPersonList) {
                logger.info(DELETE_ROW_MESSAGE, "1", ndflPerson.getRowNum(), String.format(SECTION1_INFO_MESSAGE, ndflPerson.getFullName()));
            }

            //Удаляем соответсвующие строки Разделов 2,3,4
            deleteRowsInNdflPerson(deleteRows);
        }

        //Для строк Раздела 2
        if (DeclarationDataSection.SECTION2.equals(deleteRows.getSection())) {
            List<NdflPersonIncome> incomes = ndflPersonDao.findAllIncomesByIdIn(deleteRows.getSectionIds());
            for (NdflPersonIncome income : incomes) {
                logger.info(DELETE_ROW_MESSAGE, "2", income.getRowNum(), String.format(SECTION2_INFO_MESSAGE, income.getOperationId()));
            }

            //Удаляем соответсвующие строки Разделов 3,4
            //а также строки раздела 1, если удаляемые строки (раздела 2) являются последними
            //в соответсвующем отношениии с строке раздела 1
            deleteRowsInPrepayments(deleteRows);
        }

        //Удалить связанные c формой отчеты
        reportService.deleteDec(
                singletonList(declarationDataId),
                asList(DeclarationReportType.SPECIFIC_REPORT_DEC, DeclarationReportType.EXCEL_DEC)
        );

        String fullDeclarationDescription = getFullDeclarationDescription(taskData.getParams());

        //Запись в журнал аудита
        auditService.add(null, userInfo, declarationData,
                "Удаление строк с данными операций в ПНФ", null);

        //запись в историю
        logBusinessService.logFormEvent(declarationDataId,
                FormDataEvent.DELETE_ROWS,
                logger.getLogId(),
                "Успешно выполнено удаление строк формы",
                userInfo);

        return result;
    }


    /**
     * Описание формы
     *
     * @param params
     * @return
     */
    private String getFullDeclarationDescription(Map<String, Object> params) {
        return (String) params.get("fullDeclarationDescription");
    }

    /**
     * Итоговое уведомление
     *
     * @param fullDeclarationDescription
     * @return
     */
    private String getTotalNotice(String fullDeclarationDescription) {
        return TOTAL_MESSAGE + fullDeclarationDescription;
    }

    /**
     * Итоговое оповещение
     *
     * @param fullDeclarationDescription
     * @return
     */
    private String getTotalNotification(String fullDeclarationDescription) {
        return TOTAL_MESSAGE + fullDeclarationDescription;
    }

    /**
     * Удалить строки разделов 2,3,4 по строкам раздела 1
     *
     * @param deleteRows Удаляемые строки
     */
    private void deleteRowsInNdflPerson(DeleteSelectedDeclarationRowsAction deleteRows) {
        List<Long> ndflPersonIds = deleteRows.getSectionIds();
        ndflPersonService.deleteRowsInNdflPerson(ndflPersonIds, deleteRows.getDeclarationDataId());
    }

    /**
     * Удалить строки разделов 1,3,4 по строкам раздела 2
     *
     * @param deleteRows Удаляемые строки
     */
    private void deleteRowsInPrepayments(DeleteSelectedDeclarationRowsAction deleteRows) {
        List<Long> ndflPersonIncomeIds = deleteRows.getSectionIds();
        ndflPersonService.deleteRowsInPrepayments(ndflPersonIncomeIds, deleteRows.getDeclarationDataId());
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        String fullDeclarationDescription = getFullDeclarationDescription(taskData.getParams());
        return getTotalNotification(fullDeclarationDescription);
    }


    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        StringBuilder message = new StringBuilder("Не выполнена операция \"Удаление строк\" для налоговой формы: ");
        Exception e = (Exception) taskData.getParams().get("exceptionThrown");
        if (e != null) {
            message.append("Причина: ").append(e.toString());
        }
        return message.toString();
    }

    @Override
    public AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params,
                                     Logger logger)
            throws AsyncTaskException {
        return AsyncQueue.SHORT;
    }

    @Override
    protected AsyncTaskType getAsyncTaskType() {
        return AsyncTaskType.DELETE_DEC_ROWS;
    }

    @Override
    public String createDescription(TAUserInfo userInfo, Map<String, Object> params) {
        return "Удаление строк формы. " + getFullDeclarationDescription(params);
    }
}
