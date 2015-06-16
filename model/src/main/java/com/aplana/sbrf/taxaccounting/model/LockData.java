package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * Модельный класс с информацией о блокировке
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 13:51
 */

public final class LockData {
	/* Идентификатор блокировки */
	private String key;
	/* Код пользователя, установившего блокировку*/
	private int userId;
	/* Дата истечения блокировки */
	private Date dateBefore;
    /* Дата установки блокировки */
    private Date dateLock;
    /* Cтатус выполнения асинхронной задачи, связанной с блокировкой */
    private String state;
    /* Дата последнего изменения статуса */
    private Date stateDate;
    /* Описание блокировки */
    private String description;
    /* Очередь, в которой находится связанная асинхронная задача */
    private LockQueues queue;

    public enum LockObjects {
        REF_BOOK,
        DECLARATION_DATA,
        DECLARATION_CREATE,
        FORM_DATA,
        FORM_DATA_IMPORT,      //Блокировка при импорте xls
        FORM_DATA_CREATE,      //Блокировка при создании вручную, через интерфейс
        DECLARATION_TEMPLATE,
        FORM_TEMPLATE,
        LOG_SYSTEM_BACKUP,
        LOG_SYSTEM_CSV,
        IFRS,
        FILE,
        CONFIGURATION_PARAMS,
        SCHEDULER_TASK,
        XSD_VALIDATION,
        LOAD_TRANSPORT_DATA     //Для задач асинх загрузки ТФ
    }

    public enum DescriptionTemplate {
        REF_BOOK("Справочник \"%s\""),
        FORM_DATA("Налоговая форма \"%s\", \"%s\", подразделение \"%s\", период \"%s%s%s\""),
        FORM_DATA_REPORT("Формирование отчета в формате %s для налоговой формы \"%s\", \"%s\", подразделение \"%s\", период \"%s%s%s\""),
        FORM_DATA_TASK("%s \"%s\", \"%s\", подразделение \"%s\", период \"%s%s%s\""),
        FORM_DATA_IMPORT("Импорт файла %s в налоговую форму из каталога загрузки \"%s\", \"%s\", подразделение \"%s\", период \"%s%s%s\""),
        FORM_DATA_CREATE("Создание налоговой формы \"%s\", \"%s\", подразделение \"%s\", период \"%s%s%s\""),
        DECLARATION("Декларация \"%s\", подразделение \"%s\", период \"%s%s\"%s%s"),
        DECLARATION_REPORT("Формирование отчета в формате %s для %s \"%s\", подразделение \"%s\", период \"%s%s\"%s%s"),
        DECLARATION_CREATE("Создание %s \"%s\", подразделение \"%s\", период \"%s%s\"%s%s"),
        DECLARATION_CALCULATE("Расчет %s \"%s\", подразделение \"%s\", период \"%s%s\"%s%s"),
        DECLARATION_CHECK("Проверка %s \"%s\", подразделение \"%s\", период \"%s%s\"%s%s"),
        DECLARATION_ACCEPT("Принятии %s \"%s\", подразделение \"%s\", период \"%s%s\"%s%s"),
        FILE("Файл \"%s\""),
        DECLARATION_TEMPLATE("Шаблон декларации \"%s\" на дату %s"),
        FORM_TEMPLATE("Шаблон налоговой формы \"%s\" на дату %s"),
        LOG_SYSTEM_BACKUP("Архивация журнала аудита (до даты: %s)"),
        LOG_SYSTEM_CSV("Печать журнала аудита по параметрам пользователя"),
        IFRS("Формирование архива с отчетностью для МСФО за %s %s"),
        CONFIGURATION_PARAMS("Конфигурационные параметры"),
        SCHEDULER_TASK("Выполнение задачи планировщика \"%s\""),
        LOAD_TRANSPORT_DATA("Импорт ТФ из каталога загрузки");

        private String text;

        private DescriptionTemplate(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public enum State {
        IN_QUEUE("В очереди на выполнение"),
        LOCKED("Ожидает отмены выполнения предыдущей задачи"),
        STARTED("Началось выполнение"),
        SAVING_MSGS("Выполняется сохранение уведомлений"),
        SENDING_MSGS("Выполняется рассылка уведомлений"),
        SENDING_ERROR_MSGS("Произошла ошибка. Выполняется рассылка уведомлений"),
        POST_LOGIC("Выполняется пост-обработка");

        private String text;

        private State(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    public enum LockQueues {
        ALL("Все блокировки", -1),
        LONG("Очередь длительных задач", BalancingVariants.LONG.getId()),
        SHORT("Очередь кратковременных задач", BalancingVariants.SHORT.getId()),
        NONE("Без очереди", 0);

        private String text;
        private Integer id;

        LockQueues(String text, int id) {
            this.text = text;
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public int getId() {
            return id;
        }

        public static LockQueues getById(int id) {
            for (LockQueues queue : LockQueues.values()) {
                if (queue.getId() == id) {
                    return queue;
                }
            }
            return null;
        }
    }

    public static final String CANCEL_MSG = "Запрашиваемая операция \"%s\" уже поставлена в очередь. Отменить задачу и создать новую?";
    public static final String RESTART_MSG = "Запрашиваемая операция \"%s\" уже выполняется Системой. При ее отмене задача выполнится до конца, но результат выполнения не будет сохранен. Отменить задачу и создать новую?";
    public static final String RESTART_LINKED_TASKS_MSG = "Запуск операции приведет к отмене некоторых ранее запущенных операций (операции, уже выполняемые Системой выполнятся до конца, но результат их выполнения не будет сохранен). Продолжить?";
    public static final String LOCK_INFO_MSG = "Запрашиваемая операция \"%s\" уже запущена %s пользователем %s. Вы добавлены в список получателей оповещения о выполнении данной операции.";
    public static final String LOCK_CURRENT = "\"%s\" пользователем \"%s\" запущена операция \"%s\"";
    public static final String CANCEL_TASK_IN_PROGRESS = "\"%s\" пользователем \"%s\" запущена операция \"%s\". Данная операция уже выполняется Системой.";
    public static final String CANCEL_TASK_NOT_PROGRESS = "\"%s\" пользователем \"%s\" запущена операция \"%s\". Данная операция находится в очереди на выполнение.";

	public LockData(){
	}

	public LockData(String key, int userId, Date dateBefore) {
		this.key = key;
		this.userId = userId;
		this.dateBefore = dateBefore;
	}

	public Date getDateBefore() {
		return dateBefore;
	}

	public void setDateBefore(Date dateBefore) {
		this.dateBefore = dateBefore;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

    public Date getDateLock() {
        return dateLock;
    }

    public void setDateLock(Date dateLock) {
        this.dateLock = dateLock;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getStateDate() {
        return stateDate;
    }

    public void setStateDate(Date stateDate) {
        this.stateDate = stateDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LockQueues getQueue() {
        return queue;
    }

    public void setQueue(LockQueues queue) {
        this.queue = queue;
    }

    @Override
    public String toString() {
        return "LockData{" +
                "key='" + key + '\'' +
                ", userId=" + userId +
                ", dateBefore=" + dateBefore +
                ", dateLock=" + dateLock +
                ", state='" + state + '\'' +
                ", stateDate=" + stateDate +
                ", description='" + description + '\'' +
                ", queue=" + queue +
                '}';
    }
}
