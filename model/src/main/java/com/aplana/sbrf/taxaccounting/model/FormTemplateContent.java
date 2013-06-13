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

	public FormType getType() {
		return type;
	}

	public void setType(FormType type) {
		this.type = type;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isNumberedColumns() {
		return numberedColumns;
	}

	public void setNumberedColumns(boolean numberedColumns) {
		this.numberedColumns = numberedColumns;
	}

	public boolean isFixedRows() {
		return fixedRows;
	}

	public void setFixedRows(boolean fixedRows) {
		this.fixedRows = fixedRows;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public List<FormStyle> getStyles() {
		return styles;
	}

	public void setStyles(List<FormStyle> styles) {
		this.styles = styles;
	}

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
		formTemplate.getColumns().clear();
		formTemplate.getColumns().addAll(columns);
		formTemplate.getStyles().clear();
		formTemplate.getStyles().addAll(styles);
	}
}
