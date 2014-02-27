package com.aplana.sbrf.taxaccounting.model;

import java.io.Serializable;

/**
 * Информация о подписанте налогвой формы.
 * У налоговой формы может быть несколько подписантов
 *
 * @author dsultanbekov
 */
public class FormDataSigner implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private long id;
	private String name;
	private String position;
    private int ord;

    /**
	 * Получить идентификатор записи
	 * @return идентификатор записи
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Задать идентификатор записи
	 * @param id идентификатор записи
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	/**
	 * Получить ФИО подписанта
	 * @return ФИО подписанта
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Задать ФИО подписанта
	 * @param name ФИО подписанта
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Получить должность подписанта
	 * @return должность подписанта
	 */
	public String getPosition() {
		return position;
	}
	
	/**
	 * Задать должность подписанта
	 * @param position должность подписанта
	 */
	public void setPosition(String position) {
		this.position = position;
	}

    /**
     * Получить порядковый номер подписанта
     * @return номер подписанта
     */
    public int getOrd() {
        return ord;
    }

    /**
     * Задать порядковый номер подписанта
     * @param ord порядковый номер подписанта
     */
    public void setOrd(int ord) {
        this.ord = ord;
    }
}
