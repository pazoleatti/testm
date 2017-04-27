package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Описание налоговой формы (шаблон налоговой формы)
 * 
 * @author dsultanbekov
 */
public class FormTemplate extends IdentityObject<Integer> implements Cloneable {
	private static final long serialVersionUID = -8304772615983231523L;

	private FormType type;
	private Date version;

    private String name;
    private String fullName;
    private String header;

    private VersionedObjectStatus status;

    /**
     * Признак статуса шаблона
     * @return статус шаблона
     */
    public VersionedObjectStatus getStatus() {
        return status;
    }

    public void setStatus(VersionedObjectStatus status) {
        this.status = status;
    }

    /**
     * Тело скрипта
     * Оставлен для сохранения шаблона налоговой формы.
     * Заполненяться поле должно отдельным вызовом функции dao.
     */
    private String script;

    /**
     * @return имя отображаемое на форме
     */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = StringUtils.cleanString(name);
	}

	/**
	 *
	 * @return полное наименование необходимое для печатной формы.
	 */
	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	/**
	 * @return идентификатор формы и номер формы
	 */
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		if (script == null || script.trim().isEmpty()) {
			this.script = null;
		} else {
			this.script = script;
		}
	}

	/**
	 * Задаёт {@link FormType вид налоговой формы}
	 *
	 * @param type
	 *            вид налоговой формы
	 */
	public void setType(FormType type) {
		this.type = type;
	}

	/**
	 * Возвращает {@link FormType вид налоговой формы}
	 *
	 * @return вид налоговой формы
	 */
	public FormType getType() {
		return type;
	}


	/**
	 * Получить версию формы: для каждого типа формы может существовать
	 * несколько версий
	 *
	 * @return версия формы
	 */
	public Date getVersion() {
		return version;
	}

	/**
	 * Установить дату актуальности шаблона
	 *
	 * @param version
	 *            дата актуальности
	 */
	public void setVersion(Date version) {
		this.version = version;
	}
}
