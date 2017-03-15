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
    /* Положение задачи в очереди */
    private int queuePosition;
    /* Наименование узла кластера, на котором выполняется связанная асинхронная задача */
    private String serverNode;

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
        LOAD_TRANSPORT_DATA,     //Для задач асинх загрузки ТФ
        DEPARTMENT_HISTORY
    }

    public enum DescriptionTemplate {
        REF_BOOK("Редактирование справочника \"%s\""),
        FORM_DATA_TASK("%s: Период: \"%s%s%s\", Подразделение: \"%s\", Тип: \"%s\", Вид: \"%s\", Версия: \"%s\""),
        DECLARATION_TASK("%s: Период: \"%s%s\", Подразделение: \"%s\", Вид: \"%s\"%s%s%s"),
        FILE("Загрузка ТФ \"%s\""),
        DECLARATION_TEMPLATE("Редактирование версии макета налоговой формы \"%s\" (%s) за период с %s по %s"),
        FORM_TEMPLATE("Редактирование версии макета \"%s\" (%s) за период с %s по %s"),
        LOG_SYSTEM_BACKUP("Архивация журнала аудита с %s по %s"),
        LOG_SYSTEM_CSV("Формирование файла с данными журнала аудита по параметрам поиска \"%s\""),
        IFRS("Формирование отчетности МСФО за период \"%s %s\""),
        CONFIGURATION_PARAMS("Блокировка конфигурационных параметров при загрузке ТФ"),
        SCHEDULER_TASK("Выполнение задачи планировщика \"%s\""),
        LOAD_TRANSPORT_DATA("Импорт ТФ из каталога загрузки"),
        IMPORT_TRANSPORT_DATA("Загрузка файла \"%s\"");

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
        LOCKED("Ожидание выполнения"),
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
		/** -1 - Все блокировки */
        ALL("Все блокировки", -1),
		/** 2 - Длительные задачи */
        LONG("Очередь длительных задач", BalancingVariants.LONG.getId()),
		/** 1 - Кратковременные задачи */
        SHORT("Очередь кратковременных задач", BalancingVariants.SHORT.getId()),
		/** 0 - Без очереди */
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

    public static final String CHECK_TASK = "Выполнение операции \"%s\" невозможно, т.к. %s";
    public static final String CANCEL_TASK = "Пользователем \"%s\" отменена операция \"%s\". Причина отмены: %s";
    public static final String RESTART_LINKED_TASKS_MSG = "Запуск операции приведет к отмене некоторых ранее запущенных операций. Операции, уже выполняемые Системой, выполнятся до конца, но результат их выполнения не будет сохранен. Продолжить?";
    public static final String CANCEL_TASKS_MSG = "Выполнение операции \"%s\" приведет к отмене некоторых ранее запущенных операций. Операции, уже выполняемые Системой, выполнятся до конца, но результат их выполнения не будет сохранен. Продолжить?";
    public static final String LOCK_CURRENT = "\"%s\" пользователем \"%s\" запущена операция \"%s\"";
    public static final String CANCEL_TASK_IN_PROGRESS = "\"%s\" пользователем \"%s\" запущена операция \"%s\". Данная операция уже выполняется Системой.";
    public static final String CANCEL_TASK_NOT_PROGRESS = "\"%s\" пользователем \"%s\" запущена операция \"%s\". Данная операция находится в очереди на выполнение.";

    public static final String STANDARD_LOCK_MSG = "Объект заблокирован другой операцией. Попробуйте выполнить операцию позже";

	public LockData(){
	}

	public LockData(String key, int userId) {
		this.key = key;
		this.userId = userId;
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

    public int getQueuePosition() {
        return queuePosition;
    }

    public void setQueuePosition(int queuePosition) {
        this.queuePosition = queuePosition;
    }

    public String getServerNode() {
        return serverNode;
    }

    public void setServerNode(String serverNode) {
        this.serverNode = serverNode;
    }

    public boolean isAsync() {
        return (getQueue() != null && !LockQueues.NONE.equals(getQueue()));
    }

    @Override
    public String toString() {
        return "LockData{" +
                "key='" + key + '\'' +
                ", userId=" + userId +
                ", dateLock=" + dateLock +
                ", state='" + state + '\'' +
                ", stateDate=" + stateDate +
                ", description='" + description + '\'' +
                ", queue=" + queue +
                ", queuePosition=" + queuePosition +
                ", serverNode='" + serverNode + '\'' +
                '}';
    }
}