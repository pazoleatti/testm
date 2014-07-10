package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({DateColumn.class, StringColumn.class, NumericColumn.class, RefBookColumn.class, ReferenceColumn.class, AutoNumerationColumn.class})
public class FormTemplateContent {
	/*@XmlElement
	private FormType type;*/
	@XmlElement
	private boolean fixedRows;
	@XmlElement
	private String name;
	@XmlElement
	private String fullName;
	@XmlElement
	private String header;
	@XmlElement
	private List<Column> columns;
	@XmlElement
	private List<FormStyle> styles;

	public void fillFormTemplateContent(FormTemplate formTemplate) {
        // Для правильного назначения Parent_id при импорте, проставляем алиас.
        for (Column col : formTemplate.getColumns()){
            if(col instanceof ReferenceColumn){
                ((ReferenceColumn) col).setParentAlias(
                        formTemplate.getColumn(
                                ((ReferenceColumn) col).getParentId()).getAlias());
            }
        }
		/*this.type = formTemplate.getType();*/
		this.fixedRows = formTemplate.isFixedRows();
		this.name = formTemplate.getName();
		this.fullName = formTemplate.getFullName();
		this.header = formTemplate.getHeader();
		this.columns = formTemplate.getColumns();
		this.styles = formTemplate.getStyles();
	}

	public void fillFormTemplate(FormTemplate formTemplate) {
		/*formTemplate.setType(type);*/
		formTemplate.setFixedRows(fixedRows);
		formTemplate.setName(name);
		formTemplate.setFullName(fullName);
		formTemplate.setHeader(header);
		formTemplate.getStyles().clear();
		formTemplate.getStyles().addAll(styles != null?styles:new ArrayList<FormStyle>());
		formTemplate.getColumns().clear();
		formTemplate.getColumns().addAll(columns != null?columns:new ArrayList<Column>());
	}
}
