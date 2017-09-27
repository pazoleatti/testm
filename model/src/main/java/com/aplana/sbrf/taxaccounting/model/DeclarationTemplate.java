package com.aplana.sbrf.taxaccounting.model;

import com.aplana.sbrf.taxaccounting.model.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Шаблон декларации
 * @author dsultanbekov
 */
public class DeclarationTemplate extends IdentityObject<Integer> {
	private static final long serialVersionUID = 1L;

	private DeclarationType type;
	private Date version;
    private String xsdId;
    private String jrxmlBlobId;
    private String name;
    private String createScript;
    private VersionedObjectStatus status;
    private List<DeclarationSubreport> subreports = new ArrayList<DeclarationSubreport>();
    /**
     * Типы налоговых форм(declaration)
     */
    private DeclarationFormKind declarationFormKind;
    /**
     * Вид наловой формы(declaration)
     */
    private Long declarationFormTypeId;

    /**
     * Файлы макета
     */
    private List<DeclarationTemplateFile> declarationTemplateFiles = new ArrayList<DeclarationTemplateFile>();

    private List<DeclarationTemplateEventScript> eventScripts = new ArrayList<DeclarationTemplateEventScript>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.cleanString(name);
    }

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

    /**
     * Получить список спец отчетов
     * @return
     */
    public List<DeclarationSubreport> getSubreports() {
        return subreports;
    }

    /**
     * Установка набора спец отчетов
     * @param subreports
     */
    public void setSubreports(List<DeclarationSubreport> subreports) {
        this.subreports = subreports;
    }

    /**
     * Добавляет спец отчет
     * @param subreport добавляемый отчет
     */
    public void addSubreport(DeclarationSubreport subreport) {
        subreports.add(subreport);
    }

    /**
     * Удаляет спец отчет
     * @param subreport удаляемый отчет
     */
    public void removeSubreport(DeclarationSubreport subreport) {
        subreports.remove(subreport);
    }

    public DeclarationFormKind getDeclarationFormKind() {
        return declarationFormKind;
    }

    public void setDeclarationFormKind(DeclarationFormKind declarationFormKind) {
        this.declarationFormKind = declarationFormKind;
    }

    public Long getDeclarationFormTypeId() {
        return declarationFormTypeId;
    }

    public void setDeclarationFormTypeId(Long declarationFormTypeId) {
        this.declarationFormTypeId = declarationFormTypeId;
    }

    public List<DeclarationTemplateFile> getDeclarationTemplateFiles() {
        return declarationTemplateFiles;
    }

    public void setDeclarationTemplateFiles(List<DeclarationTemplateFile> declarationTemplateFiles) {
        this.declarationTemplateFiles = declarationTemplateFiles;
    }

    public List<DeclarationTemplateEventScript> getEventScripts() {
        return eventScripts;
    }

    public void setEventScripts(List<DeclarationTemplateEventScript> eventScripts) {
        this.eventScripts = eventScripts;
    }
}
