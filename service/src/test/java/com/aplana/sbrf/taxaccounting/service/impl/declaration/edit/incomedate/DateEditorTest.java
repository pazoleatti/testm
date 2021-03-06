package com.aplana.sbrf.taxaccounting.service.impl.declaration.edit.incomedate;

import com.aplana.sbrf.taxaccounting.model.log.LogEntry;
import com.aplana.sbrf.taxaccounting.model.log.LogLevel;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPerson;
import com.aplana.sbrf.taxaccounting.model.ndfl.NdflPersonIncome;
import com.aplana.sbrf.taxaccounting.model.result.NdflPersonIncomeDatesDTO;
import com.aplana.sbrf.taxaccounting.model.util.DateUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;


public class DateEditorTest {

    // Редактируемая строка раздела 2
    private NdflPersonIncome income;
    // Объект с датами, которые нужно заменить
    private NdflPersonIncomeDatesDTO datesDTO;
    // ФЛ, в данных которого производится операция
    private NdflPerson person;
    // Логгер, куда печатаются сообщения о результатах
    private Logger logger = new Logger();


    @Before
    public void init() {
        // Строка раздела 2, номер строки 1, начальные значения всех дат "01.01.1970"
        income = new NdflPersonIncome();
        income.setRowNum(BigDecimal.ONE);
        income.setNdflPersonId(10L);
        income.setOperationId("100");
        income.setIncomeAccruedDate(new Date(0));
        income.setIncomePayoutDate(new Date(0));
        income.setTaxDate(new Date(0));
        income.setTaxTransferDate(new Date(0));
        income.setModifiedDate(new Date(0));

        // Заменяем все даты на указанную
        Date date = LocalDate.parse("2000-01-01").toDate();
        datesDTO = new NdflPersonIncomeDatesDTO();
        datesDTO.setAccruedDate(date);
        datesDTO.setPayoutDate(date);
        datesDTO.setTaxDate(date);
        datesDTO.setTransferDate(date);

        person = new NdflPerson();
        person.setInp("1000");
        person.setLastName("Иванов");
        person.setFirstName("Иван");
        person.setMiddleName("Иванович");
    }


    @Test
    public void test_DateEditorFactory() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.ACCRUED);
        assertThat(editor).isExactlyInstanceOf(AccruedDateEditor.class);

        editor = DateEditorFactory.getEditor(EditableDateField.PAYOUT);
        assertThat(editor).isExactlyInstanceOf(PayoutDateEditor.class);

        editor = DateEditorFactory.getEditor(EditableDateField.TAX);
        assertThat(editor).isExactlyInstanceOf(TaxDateEditor.class);

        editor = DateEditorFactory.getEditor(EditableDateField.TRANSFER);
        assertThat(editor).isExactlyInstanceOf(TransferDateEditor.class);
    }

    @Test
    public void test_editIncomeDateField_onNoDateToSet() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.ACCRUED);
        datesDTO.setAccruedDate(null);

        Date oldValue = income.getIncomeAccruedDate();
        boolean edited = editor.editIncomeDateField(income, datesDTO, person, logger, null);

        assertThat(edited).isFalse();
        assertThat(income.getIncomeAccruedDate()).isEqualTo(oldValue);
        assertThat(logger.getEntries()).isEmpty();
    }

    @Test
    public void test_editIncomeDateField_onNoAccruedDateToEdit() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.ACCRUED);
        income.setIncomeAccruedDate(null);

        boolean edited = editor.editIncomeDateField(income, datesDTO, person, logger, null);
        LogEntry lastEntry = logger.getLastEntry();

        assertThat(edited).isFalse();
        assertThat(income.getIncomeAccruedDate()).isNull();
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Дата начисления дохода: \" __ \" не может быть заменена значением \"01.01.2000\", т.к. строка не является строкой начисления дохода.");
    }

    @Test
    public void test_editIncomeDateField_onNoPayoutDateToEdit() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.PAYOUT);
        income.setIncomePayoutDate(null);

        boolean edited = editor.editIncomeDateField(income, datesDTO, person, logger, null);
        LogEntry lastEntry = logger.getLastEntry();

        assertThat(edited).isFalse();
        assertThat(income.getIncomePayoutDate()).isNull();
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Дата выплаты дохода: \" __ \" не может быть заменена значением \"01.01.2000\", т.к. строка не является строкой выплаты дохода.");
    }

    @Test
    public void test_editIncomeDateField_onNoTaxDateToEdit() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.TAX);
        income.setTaxDate(null);

        boolean edited = editor.editIncomeDateField(income, datesDTO, person, logger, null);
        LogEntry lastEntry = logger.getLastEntry();

        assertThat(edited).isFalse();
        assertThat(income.getTaxDate()).isNull();
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Дата НДФЛ: \" __ \" не может быть заменена значением \"01.01.2000\", т.к. строка не является строкой начисления либо выплаты дохода.");
    }

    @Test
    public void test_editIncomeDateField_onNoTransferDateToEdit() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.TRANSFER);
        income.setTaxTransferDate(null);

        boolean edited = editor.editIncomeDateField(income, datesDTO, person, logger, null);
        LogEntry lastEntry = logger.getLastEntry();

        assertThat(edited).isFalse();
        assertThat(income.getTaxTransferDate()).isNull();
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Срок перечисления: \" __ \" не может быть заменен значением \"01.01.2000\", т.к. строка не является строкой выплаты дохода либо перечисления в бюджет.");
    }

    @Test
    public void test_editIncomeDateField_onAccruedDate() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.ACCRUED);

        boolean edited = editor.editIncomeDateField(income, datesDTO, person, logger, null);

        LogEntry lastEntry = logger.getLastEntry();

        assertThat(edited).isTrue();
        assertThat(income.getIncomeAccruedDate()).isEqualTo(datesDTO.getAccruedDate());
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Выполнена замена Даты начисления дохода: \"01.01.1970\" -> \"01.01.2000\".");
    }

    @Test
    public void test_editIncomeDateField_onPayoutDate() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.PAYOUT);

        boolean edited = editor.editIncomeDateField(income, datesDTO, person, logger, null);

        LogEntry lastEntry = logger.getLastEntry();

        assertThat(edited).isTrue();
        assertThat(income.getIncomePayoutDate()).isEqualTo(datesDTO.getPayoutDate());
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Выполнена замена Даты выплаты дохода: \"01.01.1970\" -> \"01.01.2000\".");
    }

    @Test
    public void test_editIncomeDateField_onTaxDate() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.TAX);

        boolean edited = editor.editIncomeDateField(income, datesDTO, person, logger, null);

        LogEntry lastEntry = logger.getLastEntry();

        assertThat(edited).isTrue();
        assertThat(income.getTaxDate()).isEqualTo(datesDTO.getTaxDate());
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Выполнена замена Даты НДФЛ: \"01.01.1970\" -> \"01.01.2000\".");
    }

    @Test
    public void test_editIncomeDateField_onTransferDate() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.TRANSFER);

        boolean edited = editor.editIncomeDateField(income, datesDTO, person, logger, null);

        LogEntry lastEntry = logger.getLastEntry();

        assertThat(edited).isTrue();
        assertThat(income.getTaxTransferDate()).isEqualTo(datesDTO.getTransferDate());
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Выполнена замена Срока перечисления: \"01.01.1970\" -> \"01.01.2000\".");
    }

    @Test
    public void test_editTransferDate_onZeroDateToEdit_logMessage() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.TRANSFER);
        income.setTaxTransferDate(DateUtils.DATE_ZERO);

        editor.editIncomeDateField(income, datesDTO, person, logger, null);

        LogEntry lastEntry = logger.getLastEntry();
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Выполнена замена Срока перечисления: \"00.00.0000\" -> \"01.01.2000\".");
    }

    @Test
    public void test_editTransferDate_onZeroDateToSet_logMessage() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.TRANSFER);
        datesDTO.setTransferDate(DateUtils.DATE_ZERO);

        editor.editIncomeDateField(income, datesDTO, person, logger, null);

        LogEntry lastEntry = logger.getLastEntry();
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Выполнена замена Срока перечисления: \"01.01.1970\" -> \"00.00.0000\".");
    }

    @Test
    public void test_editIncomeDateField_onSuccess_logEntry() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.ACCRUED);

        editor.editIncomeDateField(income, datesDTO, person, logger, null);

        LogEntry lastEntry = logger.getLastEntry();
        assertThat(lastEntry)
                .extracting("level", "message", "type", "object")
                .containsExactly(
                        LogLevel.INFO,
                        "Раздел 2. Строка 1. Выполнена замена Даты начисления дохода: \"01.01.1970\" -> \"01.01.2000\".",
                        "Дата изменена",
                        "Иванов Иван Иванович, ИНП: 1000, ID операции: 100"
                );
    }

    @Test
    public void test_editIncomeDateField_onReplacingTheSame() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.ACCRUED);
        datesDTO.setAccruedDate(new Date(0));

        boolean edited = editor.editIncomeDateField(income, datesDTO, person, logger, null);

        LogEntry lastEntry = logger.getLastEntry();

        assertThat(edited).isFalse();
        assertThat(lastEntry)
                .extracting("level", "message", "type", "object")
                .containsExactly(
                        LogLevel.INFO,
                        "Раздел 2. Строка 1. Значение Даты начисления дохода не было изменено. Графа уже содержит требуемое значение: \"01.01.1970\".",
                        "Графа уже содержит значение",
                        "Иванов Иван Иванович, ИНП: 1000, ID операции: 100"
                );
    }

    @Test
    public void test_editIncomeDateField_onNotEdited_logEntry() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.ACCRUED);
        income.setIncomeAccruedDate(null);

        editor.editIncomeDateField(income, datesDTO, person, logger, null);

        LogEntry lastEntry = logger.getLastEntry();
        assertThat(lastEntry)
                .extracting("level", "message", "type", "object")
                .containsExactly(
                        LogLevel.WARNING,
                        "Раздел 2. Строка 1. Дата начисления дохода: \" __ \" не может быть заменена значением \"01.01.2000\", т.к. строка не является строкой начисления дохода.",
                        "Установка даты не предусмотрена для этой строки",
                        "Иванов Иван Иванович, ИНП: 1000, ID операции: 100"
                );
    }

    @Test
    public void test_editTransferDate_onBothZeroDates_logMessage() {
        DateEditor editor = DateEditorFactory.getEditor(EditableDateField.TRANSFER);
        income.setTaxTransferDate(DateUtils.DATE_ZERO);
        datesDTO.setTransferDate(DateUtils.DATE_ZERO);

        editor.editIncomeDateField(income, datesDTO, person, logger, null);

        LogEntry lastEntry = logger.getLastEntry();
        assertThat(lastEntry.getMessage()).isEqualTo("Раздел 2. Строка 1. Значение Срока перечисления не было изменено. Графа уже содержит требуемое значение: \"00.00.0000\".");
    }
}
