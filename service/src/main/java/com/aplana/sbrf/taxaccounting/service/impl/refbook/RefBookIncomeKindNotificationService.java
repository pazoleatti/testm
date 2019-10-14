package com.aplana.sbrf.taxaccounting.service.impl.refbook;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookValue;
import com.aplana.sbrf.taxaccounting.script.service.RefBookService;
import com.aplana.sbrf.taxaccounting.service.LogEntryService;
import com.aplana.sbrf.taxaccounting.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * <b>Сервис отправки уведомлений для создания и редактирования элементов справочника Виды доходов</b>
 * Created by <i><b>s.molokovskikh</i></b> on 10.10.19.
 */
@Service
public class RefBookIncomeKindNotificationService {

    public static final String SUCCESS_MESSAGE = "Успешно %s Вид Дохода с параметрами:  " +
            "\"Действует с: %s по : %s, " +
            "Код вида дохода: %s, Признак вида дохода : %s, " +
            "Наименование: %s";

    public static final String FAILED_MESSAGE = "Ошибка при создании Вида Дохода с параметрами:  " +
            "\"Действует с: %s по : %s, " +
            "Код вида дохода: %s, Признак вида дохода : %s, " +
            "Наименование: %s. %s. " +
            "Обратитесь к Администратору Системы или повторите операцию позднее.";

    private static final String EMPTY_STRING = "";
    public static final String NAME = "NAME";
    public static final String INCOME_TYPE_ID = "INCOME_TYPE_ID";
    public static final String MARK = "MARK";
    public static final String RECORD_VERSION_FROM = "record_version_from";
    public static final String RECORD_VERSION_TO = "record_version_to";
    private static final String CODE = "CODE";
    private static final String CREATE_ACTION = "создан";
    private static final String EDIT_ACTION = "изменен";

    @Autowired
    private RefBookService refBookService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LogEntryService logEntryService;

    /**
     *
     * @param record
     * @param isEdit
     * @return uuid сохраненного лога
     */
    public String notify(Map<String, RefBookValue> record, boolean isEdit) {
        String description = getSuccessDescription(record, isEdit);
        return sendNotify(description, false);
    }

    /**
     *
     * @param record
     * @param exc
     * @return uuid сохраненного лога
     */
    public String notifyException(Map<String, RefBookValue> record, Exception exc) {
        String description = getFailedDescription(record, exc.getMessage());
        return sendNotify(description, true);
    }

    private String sendNotify(String description, boolean fatal) {
        Logger logger = new Logger();
        if (fatal) {
            logger.error(description);
        } else {
            logger.info(description);
        }
        String logUuid = logEntryService.save(logger);
        return logUuid;
    }

    private String formatDate(Date date) {
        if (ObjectUtils.isEmpty(date))
            return EMPTY_STRING;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        return dateFormat.format(date);
    }

    private String getSuccessDescription(Map<String, RefBookValue> record, boolean isEdit) {
        String name = record.get(NAME).getStringValue();
        Long incomeTypeId = record.get(INCOME_TYPE_ID).getReferenceValue();
        String code = refBookService.getStringValue(RefBook.Id.INCOME_CODE.getId(), incomeTypeId, CODE);
        String mark = record.get(MARK).getStringValue();
        Date versionFrom = record.get(RECORD_VERSION_FROM).getDateValue();
        Date versionTo = record.get(RECORD_VERSION_TO).getDateValue();

        String result = String.format(SUCCESS_MESSAGE,
                isEdit ? EDIT_ACTION : CREATE_ACTION,
                formatDate(versionFrom),
                formatDate(versionTo),
                code,
                mark,
                name
        );
        return result;
    }

    private String getFailedDescription(Map<String, RefBookValue> record, String message) {
        String name = record.get(NAME).getStringValue();
        Long incomeTypeId = record.get(INCOME_TYPE_ID).getReferenceValue();
        String code = refBookService.getStringValue(RefBook.Id.INCOME_CODE.getId(), incomeTypeId, CODE);
        String mark = record.get(MARK).getStringValue();
        Date versionFrom = record.get(RECORD_VERSION_FROM).getDateValue();
        Date versionTo = record.get(RECORD_VERSION_TO).getDateValue();

        String result = String.format(FAILED_MESSAGE,
                formatDate(versionFrom),
                formatDate(versionTo),
                code,
                mark,
                name,
                message
        );
        return result;
    }


}
