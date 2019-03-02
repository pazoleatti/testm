package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDatesDTO;
import com.aplana.sbrf.taxaccounting.model.util.DateUtils;

import java.util.Date;


/**
 * Вспомогательный класс, редактирующий поле с датой у объекта NdflPersonIncome (строка раздела 2 формы РНУ НДФЛ).
 * Разные реализации для каждого редактируемого поля.
 */
public abstract class DateEditor {

    /**
     * Строка раздела 2 "Сведения о доходах и НДФЛ"
     */
    protected NdflPersonIncome income;
    /**
     * ДТО с данными для редактирования, пришедшее с фронтенда.
     */
    protected NdflPersonIncomeDatesDTO incomeDatesDTO;

    /**
     * ФЛ, к которому относится редактируемая строка. Нужно для вывода сообщений.
     */
    private NdflPerson person;


    /**
     * Возвращает значение редактируемого поля.
     */
    protected abstract Date getDateToEdit();

    /**
     * Возвращает значение, которое нужно установить.
     */
    protected abstract Date getDateToSet();

    /**
     * Устанавливает новое значение в редактируемое поле.
     */
    protected abstract void editDate();

    /**
     * Название редактируемого поля в тексте обоснования неудачной замены.
     */
    protected abstract String fieldTitleForWarning();

    /**
     * Название редактируемого поля в родительном падеже.
     */
    protected abstract String fieldNameInGenitiveCase();

    /**
     * Название редактируемой строки в творительном падеже.
     */
    protected abstract String rowNameInInstrumentalCase();

    /**
     * Название редактируемого поля в именительном падеже.
     */
    public abstract String fieldName();


    /**
     * Редактирует поле с нужной датой у объекта NdflPersonIncome
     *
     * @param income         редактируемый объект
     * @param incomeDatesDTO dto с новыми данными
     * @param logger         логгер для сообщений о результате
     * @return true, если поле успешно заменено
     */
    public boolean editIncomeDateField(NdflPersonIncome income, NdflPersonIncomeDatesDTO incomeDatesDTO, NdflPerson person, Logger logger) {
        this.income = income;
        this.incomeDatesDTO = incomeDatesDTO;
        this.person = person;

        if (thereIsNoDateToSet()) {
            return false;
        }
        if (thereIsNoDateToEdit()) {
            printWarning(logger);
            return false;
        }

        printSuccess(logger);
        editDate();
        return true;
    }


    private boolean thereIsNoDateToSet() {
        return getDateToSet() == null;
    }

    private boolean thereIsNoDateToEdit() {
        return getDateToEdit() == null;
    }

    private void printWarning(Logger logger) {
        logger.warnExp(
                "Раздел 2. Строка %s. %s: \" __ \" не может быть заменена значением \"%s\", т.к. строка не является %s.",
                "Установка даты не предусмотрена для этой строки",
                String.format("%s, ИНП: %s, ID операции: %s", person.getFullName(), person.getInp(), income.getOperationId()),
                income.getRowNum(),
                fieldTitleForWarning(),
                DateUtils.formatPossibleZeroDate(getDateToSet()),
                rowNameInInstrumentalCase()
        );
    }

    private void printSuccess(Logger logger) {
        logger.infoExp(
                "Раздел 2. Строка %s. Выполнена замена %s: \"%s\" -> \"%s\".",
                "Дата изменена",
                String.format("%s, ИНП: %s, ID операции: %s", person.getFullName(), person.getInp(), income.getOperationId()),
                income.getRowNum(),
                fieldNameInGenitiveCase(),
                DateUtils.formatPossibleZeroDate(getDateToEdit()),
                DateUtils.formatPossibleZeroDate(getDateToSet())
        );
    }
}
