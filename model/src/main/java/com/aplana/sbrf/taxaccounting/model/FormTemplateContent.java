package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({DateColumn.class, StringColumn.class, NumericColumn.class, RefBookColumn.class})
public class FormTemplateContent {
	@XmlElement
	private FormType type;
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
		formTemplate.setNumberedColumns(numberedColumns);
		formTemplate.setFixedRows(fixedRows);
		formTemplate.setName(name);
		formTemplate.setFullName(fullName);
		formTemplate.setCode(code);
		formTemplate.getStyles().clear();
		formTemplate.getStyles().addAll(styles != null?styles:new ArrayList<FormStyle>());
		formTemplate.getColumns().clear();
		formTemplate.getColumns().addAll(columns != null?columns:new ArrayList<Column>());
	}
}
