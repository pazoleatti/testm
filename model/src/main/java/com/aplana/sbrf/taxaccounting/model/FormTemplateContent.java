package com.aplana.sbrf.taxaccounting.model;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({DateColumn.class, StringColumn.class, NumericColumn.class, RefBookColumn.class, ReferenceColumn.class, AutoNumerationColumn.class})
public class FormTemplateContent {
	@XmlElement
	private boolean fixedRows;
    @XmlElement
    private boolean monthly;
    @XmlElement
    private boolean comparative;
    @XmlElement
    private boolean accruing;
    @XmlElement
    private boolean updating;
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
            if(ColumnType.REFERENCE.equals(col.getColumnType())){
                ((ReferenceColumn) col).setParentAlias(formTemplate.getColumn(((ReferenceColumn) col).getParentId()).getAlias());
            }
        }
		fixedRows = formTemplate.isFixedRows();
		name = formTemplate.getName();
		fullName = formTemplate.getFullName();
		header = formTemplate.getHeader();
		columns = formTemplate.getColumns();
		styles = formTemplate.getStyles();
        monthly = formTemplate.isMonthly();
        comparative = formTemplate.isComparative();
        accruing = formTemplate.isAccruing();
        updating = formTemplate.isUpdating();
	}

	public void fillFormTemplate(FormTemplate formTemplate) {
		formTemplate.setFixedRows(fixedRows);
        formTemplate.setMonthly(monthly);
        formTemplate.setComparative(comparative);
        formTemplate.setAccruing(accruing);
        formTemplate.setUpdating(updating);
		formTemplate.setName(name);
		formTemplate.setFullName(fullName);
		formTemplate.setHeader(header);
		formTemplate.getStyles().clear();
		formTemplate.getStyles().addAll(styles != null?styles:new ArrayList<FormStyle>());
		formTemplate.getColumns().clear();
		formTemplate.getColumns().addAll(columns != null?columns:new ArrayList<Column>());
	}

    public void fillFormTemplateWithoutRows(FormTemplate formTemplate) {
        formTemplate.setFixedRows(fixedRows);
        formTemplate.setMonthly(monthly);
        formTemplate.setComparative(comparative);
        formTemplate.setAccruing(accruing);
        formTemplate.setUpdating(updating);
        formTemplate.setName(name);
        formTemplate.setFullName(fullName);
        formTemplate.setHeader(header);
        formTemplate.getStyles().clear();
        formTemplate.getStyles().addAll(styles != null?styles:new ArrayList<FormStyle>());
    }
}
