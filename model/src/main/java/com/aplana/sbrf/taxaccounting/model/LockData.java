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
        SCHEDULER_TASK
    }

	/* Идентификатор блокировки */
	private String key;
	/* Код пользователя, установившего блокировку*/
	private int userId;
	/* Дата истечения блокировки */
	private Date dateBefore;
    /* Дата установки блокировки */
    private Date dateLock;

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

    @Override
    public String toString() {
        return "LockData{" +
                "key='" + key + '\'' +
                ", userId=" + userId +
                ", dateBefore=" + dateBefore +
                ", dateLock=" + dateLock +
                '}';
    }
}
