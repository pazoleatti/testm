package com.aplana.sbrf.taxaccounting.model;

import java.util.Date;

/**
 * Шаблон декларации
 * @author dsultanbekov
 */
public class DeclarationTemplate extends IdentityObject<Integer> {
	private static final long serialVersionUID = 1L;

	private DeclarationType type;
	private Date version;
	private boolean active;
	private Integer edition;
    private String xsdId;
    private String jrxmlBlobId;

	private String createScript;

    private VersionedObjectStatus status;

    public VersionedObjectStatus getStatus() {
        return status;
    }

    public void setStatus(VersionedObjectStatus status) {
        this.status = status;
    }

    /**
	 * Получить вид декларации
	 * @return вид декларации
	 */
	public DeclarationType getType() {
		return type;
	}

	/**
	 * Задать вид декларации
	 * @param type вид декларации
	 */	
	public void setType(DeclarationType type) {
		this.type = type;
	}

	/**
	 * Получить название версии шаблона
	 * @return версия шаблона
	 */
	public Date getVersion() {
		return version;
	}

	/**
	 * Задать версию шаблона
	 * @param version версия шаблона
	 */
	public void setVersion(Date version) {
		this.version = version;
	}
	
	/**
	 * Признак активности шаблона декларации
	 * @return true - шаблон активен, false - шаблон неактивен
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Задать признак активности декларации
	 * @param active true - шаблон активен, false - шаблон неактивен
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Получить скрипт (groovy), использующийся для формирования декларации
     * Нужно пользоваться отдельным методом дао для получения тела скрипта, иначе вернет null если не был получен
	 * @return тело скрипта на groovy
	 */
	public String getCreateScript() {
		return createScript;
	}

	/**
	 * Задать скрипт, использующийся для формирования декларации
	 * @param createScript тело скрипта на groovy
	 */
	public void setCreateScript(String createScript) {
		this.createScript = createScript;
	}

	public Integer getEdition() {
		return edition;
	}

	public void setEdition(Integer edition) {
		if (this.edition != null) {
			throw new IllegalStateException(
					"Edition property already initialized");
		} else {
			this.edition = edition;
		}
	}

    /**
     * Получить идентификатор записи с содержимым XSD файла для проверки декларации
     * @return идентификатор записи
     */
    public String getXsdId() {
        return xsdId;
    }

    /**
     * Установить идентификатор записи с содержимым XSD файла для проверки декларации
     */
    public void setXsdId(String xsdId) {
        this.xsdId = xsdId;
    }

    /**
     * Получить идентификатор записи с содержимым jrxml шаблона из связанной таблицы
     * @return идентификатор записи
     */
    public String getJrxmlBlobId() {
        return jrxmlBlobId;
    }

    /**
     * Установить идентификатор записи с содержимым jrxml шаблона из связанной таблицы
     */
    public void setJrxmlBlobId(String jrxmlBlobId) {
        this.jrxmlBlobId = jrxmlBlobId;
    }
}
