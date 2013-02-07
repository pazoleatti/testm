package com.aplana.sbrf.taxaccounting.model;

/**
 * Шаблон декларации
 * @author dsultanbekov
 */
public class DeclarationTemplate extends IdentityObject<Integer> {
	private static final long serialVersionUID = 1L;

	private DeclarationType type;
	private String version;
	private boolean active;
	private Integer edition;

	private String createScript;

	/**
	 * Получить вид декларации
	 * @return вид декларации
	 */
	public DeclarationType getDeclarationType() {
		return type;
	}

	/**
	 * Задать вид декларации
	 * @param type вид декларации
	 */	
	public void setDeclarationType(DeclarationType type) {
		this.type = type;
	}

	/**
	 * Получить название версии шаблона
	 * @return версия шаблона
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Задать версию шаблона
	 * @param version версия шаблона
	 */
	public void setVersion(String version) {
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
}
