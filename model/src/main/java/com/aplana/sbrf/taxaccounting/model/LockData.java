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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("LockData{");
		sb.append("key='").append(key).append('\'');
		sb.append(", userId=").append(userId);
		sb.append(", dateBefore=").append(dateBefore);
		sb.append('}');
		return sb.toString();
	}
}
