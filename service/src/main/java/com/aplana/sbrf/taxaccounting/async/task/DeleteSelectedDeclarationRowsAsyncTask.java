package com.aplana.sbrf.taxaccounting.async.task;

import com.aplana.sbrf.taxaccounting.async.exception.AsyncTaskException;
import com.aplana.sbrf.taxaccounting.dao.ndfl.NdflPersonDao;
import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.action.DeleteSelectedDeclarationRowsAction;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.script.service.DeclarationService;
import com.aplana.sbrf.taxaccounting.service.AuditService;
import com.aplana.sbrf.taxaccounting.service.DeclarationDataService;
import com.aplana.sbrf.taxaccounting.service.LogBusinessService;
import com.aplana.sbrf.taxaccounting.service.ReportService;
import com.aplana.sbrf.taxaccounting.service.component.operation.DeclarationDataAsyncTaskDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * <b>Удаление строк налоговой формы разделов 1 и 2</b>
 * Created by <i><b>s.molokovskikh</i></b> on 23.10.19.
 */
@Component("DeleteSelectedDeclarationRowsAsyncTask")
public class DeleteSelectedDeclarationRowsAsyncTask extends AbstractAsyncTask {


    public static final String DELETE_ROW_MESSAGE = "Раздел%s. Удалена строка \"%s\". %s";
    public static final String SECTION1_INFO_MESSAGE = "ФЛ: %s";
    public static final String SECTION2_INFO_MESSAGE = "ID операции: %s";
    public static final String TOTAL_MESSAGE = "Выполнено удаление строк формы";

    @Autowired
    private DeclarationService declarationService;

    @Autowired
    private NdflPersonDao ndflPersonDao;

    @Autowired
    private LogBusinessService logBusinessService;

    @Autowired
    private DeclarationDataAsyncTaskDescriptor declarationDataAsyncTaskDescriptor;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DeclarationDataService declarationDataService;

    @Autowired
    private ReportService reportService;

    private String totalNotification;


    @Override
    protected BusinessLogicResult executeBusinessLogic(AsyncTaskData taskData, Logger logger)
            throws InterruptedException {
        Collection<DeleteSelectedDeclarationRowsAction> toDeleteRows =
                (Collection<DeleteSelectedDeclarationRowsAction>) taskData.getParams().get("toDeleteRows");

        //для каждого набора строк удаления из коллекции
        for (DeleteSelectedDeclarationRowsAction deleteRows : toDeleteRows) {

            //Каждый набор deleteRows относится к своей налоговой форме,
            // (этот комент тут только для, того чтобы помнить об этом)
            Long declarationDataId = deleteRows.getDeclarationDataId();

            //Для строк Раздела 1
            if (DeclarationDataSection.SECTION1.equals(deleteRows.getSection())) {
                //Удаляем соответсвующие строки Разделов 2,3,4
                deleteRowsBySection1(deleteRows);

                //TODO получение строк раздела 1 перетащить в сервис из dao или подумать
                List<NdflPerson> ndflPersonList = ndflPersonDao.findByIdIn(deleteRows.getSectionIds());
                for (NdflPerson ndflPerson : ndflPersonList) {
                    logger.info(DELETE_ROW_MESSAGE, String.format(SECTION1_INFO_MESSAGE, ndflPerson.getFullName()));
                }
            }

            //Для строк Раздела 2
            if (DeclarationDataSection.SECTION2.equals(deleteRows.getSection())) {
                //Удаляем соответсвующие строки Разделов 3,4
                //а также строки раздела 1, если удаляемые строки (раздела 2) являются последними
                //в соответсвующем отношениии с строке раздела 1
                deleteRowsBySection2(deleteRows);
                //TODO получение строк раздела 2 перетащить в сервис из dao или подумать
                List<NdflPersonIncome> incomes = ndflPersonDao.findAllIncomesByIdIn(deleteRows.getSectionIds());
                for (NdflPersonIncome income : incomes) {
                    logger.info(DELETE_ROW_MESSAGE, String.format(SECTION2_INFO_MESSAGE, income.getOperationId()));
                }
            }

            //Удалить связанные c формой отчеты
            reportService.deleteDec(
                    singletonList(declarationDataId),
                    asList(DeclarationReportType.SPECIFIC_REPORT_DEC, DeclarationReportType.EXCEL_DEC)
            );

            //итоговый текст уведомления
            String totalNotice = declarationDataAsyncTaskDescriptor.createDescription(declarationDataId, TOTAL_MESSAGE);

            //итоговый текст оповещения
            totalNotification = totalNotice;

            logger.info(totalNotice);

            TAUserInfo userInfo = new TAUserInfo();
            userInfo.setUser(userService.getUser(taskData.getUserId()));

            //Запись в журнал аудита
            DeclarationData declarationData = declarationDataService.get(declarationDataId, userInfo);
            auditService.add(FormDataEvent.DELETE_ROWS, userInfo, declarationData, "Удаление строк с данными операций в ПНФ", null);

            //запись в историю
            logBusinessService.logFormEvent(declarationDataId,
                    FormDataEvent.DELETE_ROWS,
                    logger.getLogId(),
                    "Успешно выполнено удаление строк формы",
                    userInfo);


        }


        return new BusinessLogicResult(true, null);
    }

    /**
     * Удалить строки разделов 2,3,4 по строкам раздела 1
     *
     * @param deleteRows Удаляемые строки
     */
    private void deleteRowsBySection1(DeleteSelectedDeclarationRowsAction deleteRows) {
        List<Long> ndflPersonIds = deleteRows.getSectionIds();
        declarationService.deleteRowsBySection1(ndflPersonIds);
    }

    /**
     * Удалить строки разделов 1,3,4 по строкам раздела 2
     *
     * @param deleteRows Удаляемые строки
     */
    private void deleteRowsBySection2(DeleteSelectedDeclarationRowsAction deleteRows) {
        List<Long> ndflPersonIncomeIds = deleteRows.getSectionIds();
        declarationService.deleteRowsBySection2(ndflPersonIncomeIds);
    }

    @Override
    protected String getNotificationMsg(AsyncTaskData taskData) {
        return totalNotification;
    }

    @Override
    protected String getErrorMsg(AsyncTaskData taskData, boolean unexpected) {
        return "Текст ошибки при удалении строк налоговой формы";
    }

    @Override
    protected AsyncQueue checkTaskLimit(String taskDescription, TAUserInfo user, Map<String, Object> params,
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
        Long declarationDataId = (Long) params.get("declarationDataId");
        return declarationDataAsyncTaskDescriptor.createDescription(declarationDataId, "Удаление строк формы");
    }
}
