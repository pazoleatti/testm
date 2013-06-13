package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({DateColumn.class, StringColumn.class, NumericColumn.class})
public class FormTemplateContent {
	@XmlElement
	private FormType type;
	@XmlElement
	private String version;
	@XmlElement
	private boolean active;
	@XmlElement
	private boolean numberedColumns;
	@XmlElement
	private boolean fixedRows;
	@XmlElement
	private String name;
	@XmlElement
	private String fullName;
	@XmlElement
	private String code;
	@XmlElement
	private List<Column> columns;
	@XmlElement
	private List<FormStyle> styles;

	public void fillFormTemplateContent(FormTemplate formTemplate) {
		this.type = formTemplate.getType();
		this.version = formTemplate.getVersion();
		this.active = formTemplate.isActive();
		this.numberedColumns = formTemplate.isNumberedColumns();
		this.fixedRows = formTemplate.isFixedRows();
		this.name = formTemplate.getName();
		this.fullName = formTemplate.getFullName();
		this.code = formTemplate.getCode();
		this.columns = formTemplate.getColumns();
		this.styles = formTemplate.getStyles();
	}

	public void fillFormTemplate(FormTemplate formTemplate) {
		formTemplate.setType(type);
		formTemplate.setVersion(version);
		formTemplate.setActive(active);
		formTemplate.setNumberedColumns(numberedColumns);
		formTemplate.setFixedRows(fixedRows);
		formTemplate.setName(name);
		formTemplate.setFullName(fullName);
		formTemplate.setCode(code);

		//TODO: тут для стилей и колонок выставляем id = null. потому что id для них хранится в xml и оно не null.
		// эти поля могут быть новыми для текущей версии формы, но id != null поэтому мы просто пытаемся их обновить
		// вместо того чтобы сделать insert.

		for (FormStyle style : styles) {
			style.setId(null);
		}
		for (Column column : columns) {
			column.setId(null);
		}

		formTemplate.getStyles().clear();
		formTemplate.getStyles().addAll(styles);
		formTemplate.getColumns().clear();
		formTemplate.getColumns().addAll(columns);
	}
}
