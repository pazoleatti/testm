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
		formTemplate.getStyles().clear();
		formTemplate.getStyles().addAll(styles);
		formTemplate.getColumns().clear();
		formTemplate.getColumns().addAll(columns);
	}
}
