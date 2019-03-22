package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDatesDTO;
import com.aplana.sbrf.taxaccounting.model.util.DateUtils;
import org.joda.time.LocalDate;

import java.util.Date;


/**
 * Вспомогательный класс, редактирующий поле с датой у объекта NdflPersonIncome (строка раздела 2 формы РНУ НДФЛ).
 * Разные реализации для каждого редактируемого поля.
 */
public abstract class DateEditor {

    /**
     * Строка раздела 2 "Сведения о доходах и НДФЛ"
     */
    NdflPersonIncome income;
    /**
     * ДТО с данными для редактирования, пришедшее с фронтенда.
     */
    NdflPersonIncomeDatesDTO incomeDatesDTO;

    /**
     * ФЛ, к которому относится редактируемая строка. Нужно для вывода сообщений.
     */
    private NdflPerson person;


    /**
     * Возвращает значение редактируемого поля.
     */
    abstract Date getDateToEdit();

    /**
     * Возвращает значение, которое нужно установить.
     */
    abstract Date getDateToSet();

    /**
     * Устанавливает новое значение в редактируемое поле.
     */
    abstract void editDate();

    /**
     * Название редактируемого поля в родительном падеже.
     */
    abstract String fieldNameInGenitiveCase();

    /**
     * Название редактируемой строки в творительном падеже.
     */
    abstract String warningText();


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
        if (areDatesTheSame()) {
            printTheSameDatesMessage(logger);
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

    private boolean areDatesTheSame() {
        // Сделал сравнение через LocalDate, мало ли попадутся одинаковые даты с разным временем.
        LocalDate localDateToSet = LocalDate.fromDateFields(getDateToSet());
        LocalDate localDateToEdit = LocalDate.fromDateFields(getDateToEdit());
        return localDateToEdit.equals(localDateToSet);
    }

    private void printWarning(Logger logger) {
        logger.warnExp(
                warningText(),
                "Установка даты не предусмотрена для этой строки",
                editingObject()
        );
    }

    private void printTheSameDatesMessage(Logger logger) {
        logger.infoExp(
                "Раздел 2. Строка %s. Значение %s не было изменено. Графа уже содержит требуемое значение: \"%s\".",
                "Графа уже содержит значение",
                editingObject(),
                income.getRowNum(),
                fieldNameInGenitiveCase(),
                DateUtils.formatPossibleZeroDate(getDateToSet())
        );
    }

    private void printSuccess(Logger logger) {
        logger.infoExp(
                "Раздел 2. Строка %s. Выполнена замена %s: \"%s\" -> \"%s\".",
                "Дата изменена",
                editingObject(),
                income.getRowNum(),
                fieldNameInGenitiveCase(),
                DateUtils.formatPossibleZeroDate(getDateToEdit()),
                DateUtils.formatPossibleZeroDate(getDateToSet())
        );
    }

    // Строковое представление объекта редактирования: ФЛ, ИНП, операция.
    private String editingObject() {
        return String.format("%s, ИНП: %s, ID операции: %s", person.getFullName(), person.getInp(), income.getOperationId());
    }
}
