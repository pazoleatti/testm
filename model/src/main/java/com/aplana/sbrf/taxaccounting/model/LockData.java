package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * Модельный класс с информацией о блокировке
 *
 * @author <a href="mailto:Marat.Fayzullin@aplana.com">Файзуллин Марат</a>
 * @since 17.07.14 13:51
 */

public final class LockData {

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
        FORM_DATA("Налоговая форма \"%s\", \"%s\", подразделение \"%s\", период \"%s%s\""),
        FORM_DATA_REPORT("Отчет в формате %s для налоговой формы \"%s\", \"%s\", подразделение \"%s\", период \"%s%s\""),
        FORM_DATA_IMPORT("Импорт файла %s в налоговую форму \"%s\", \"%s\", подразделение \"%s\", период \"%s%s\""),
        FORM_DATA_CREATE("Создание налоговой формы \"%s\", \"%s\", подразделение \"%s\", период \"%s%s\""),
        DECLARATION("Декларация \"%s\", подразделение \"%s\", период \"%s%s\""),
        DECLARATION_REPORT("Отчет в формате %s для декларации \"%s\", подразделение \"%s\", период \"%s%s\""),
        DECLARATION_CREATE("Создание декларации \"%s\", подразделение \"%s\", период \"%s%s\""),
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
        STARTED("Началось выполнение"),
        SAVING_MSGS("Выполняется сохранение уведомлений"),
        SENDING_MSGS("Выполняется рассылка уведомлений"),
        SENDING_ERROR_MSGS("Произошла ошибка. Выполняется рассылка уведомлений"),
        POST_LOGIC("Выполненяется пост-обработка");

        private String text;

        private State(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

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
    private String queue;

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

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
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
                ", queue='" + queue + '\'' +
                '}';
    }
}
