package com.aplana.sbrf.taxaccounting.model;


import java.text.SimpleDateFormat;
import java.util.Date;

import static com.aplana.sbrf.taxaccounting.model.TaskInterruptCause.EventType.SCHEDULER;
import static com.aplana.sbrf.taxaccounting.model.TaskInterruptCause.EventType.TASK_CANCEL;

public enum TaskInterruptCause {

    LOCK_DELETE("Удалена блокировка задачи (форма \"Список блокировок\")", EventType.LOCK_DELETE),
    SCHEDULER_OLD_LOCK_DELETE("Удаление истекших блокировок", SCHEDULER),
    RESTART_TASK("Выполнен перезапуск задачи", TASK_CANCEL),
    FORM_DATA_CHANGE_CANCEL("Отмена изменений", TASK_CANCEL),
    FORM_CONSOLIDATION("Выполнена консолидация в НФ", TASK_CANCEL),
    FORM_EDIT("Выполняется редактирование НФ", TASK_CANCEL),
    FORM_MOVE("Выполнена Подготовка/Утверждение/Принятие налоговой формы", TASK_CANCEL),
    FORM_RECALCULATION("Выполнен пересчет данных НФ", TASK_CANCEL),
    FORM_REFRESH("Выполнено обновление формы", TASK_CANCEL),
    FORM_IMPORT("Выполнена загрузка XLSM-файла с формы экземпляра НФ", TASK_CANCEL),
    FORM_DELETE("Удалена налоговая форма", TASK_CANCEL),
    FORM_IMPORT_TF("Изменены данные налоговой формы путем загрузки транспортного файла", TASK_CANCEL),
    FORM_MANUAL_DELETE("Удалена версия ручного ввода для налоговой формы", TASK_CANCEL),
    FORM_AUTO_NUMERATION_UPDATE("Обновление автонумерации", TASK_CANCEL),
    FORM_DATA_UPDATE("Изменены данные налоговой формы", TASK_CANCEL),
    FORM_PERFORMER_UPDATE("Изменены параметры печатной формы", TASK_CANCEL),
    FORM_TEMPLATE_UPDATE("Изменен макет НФ", TASK_CANCEL),
    DECLARATION_TEMPLATE_JRXML_CHANGE("Выполнена замена jrxml файла макета декларации", TASK_CANCEL),
    DECLARATION_DELETE("Удалена декларация", TASK_CANCEL),
    DECLARATION_ACCEPT("Выполнено принятие экземпляра декларации", TASK_CANCEL),
    DECLARATION_RECALCULATION("Выполнен пересчет данных декларации", TASK_CANCEL),
    DECLARATION_TEMPLATE_UPDATE("Обновление макета", TASK_CANCEL),
    REFBOOK_RECORD_MODIFY("Модифицирована запись справочника %s, которая используется в данной форме", TASK_CANCEL),
    FD_DEPARTMENT_NAME_UPDATE("Обновлении имени подразделения", TASK_CANCEL),
    FD_TB_NAME_UPDATE("Обновлении имени ТБ", TASK_CANCEL);

    private String text;
    private EventType eventType;

    private Object[] args;

    enum EventType {
        // Удаление блокировки при выполнении событие "Нажатие на кнопку "Удалить блокировку"" формы "Список блокировок"
        LOCK_DELETE("С формы \"Список блокировок\" удалена блокировка, установленная %s пользователем %s. %s"),
        // Удаление блокировки при подтверждении отмены операции
        TASK_CANCEL("При отмене операции удалена блокировка, установленная %s пользователем %s. %s"),
        // Удаление блокировки в рамках задачи планировщика «Удаление истекших блокировок»
        SCHEDULER("При выполнении задачи планировщика удалена блокировка, установленная %s пользователем %s. %s");

        private String eventText;
        EventType(String eventText) {
            this.eventText = eventText;
        }

        public String getDescription() {
            return eventText;
        }
    }

    TaskInterruptCause(String text, EventType type) {
        this.text = text;
        this.eventType = type;
    }

    public TaskInterruptCause setArgs(Object ... args) {
        this.args = args;
        return this;
    }

    public String getEventDescrition(Date date, TAUser user, String lockDescription) {
        SimpleDateFormat formatter = new SimpleDateFormat(Formats.DD_MM_YYYY_HH_MM_SS.getFormat());
        String dateStr = formatter.format(date);
        return String.format(eventType.getDescription(), dateStr, user.getLogin(), lockDescription);
    }

    @Override
    public String toString() {
        return String.format(text, args);
    }
}
