package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.UpdateFTIdEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateColumnUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormTemplateColumnPresenter
		extends
		Presenter<FormTemplateColumnPresenter.MyView, FormTemplateColumnPresenter.MyProxy>
		implements FormTemplateColumnUiHandlers,
		FormTemplateSetEvent.MyHandler, FormTemplateFlushEvent.MyHandler, UpdateFTIdEvent.MyHandler {

    private Map<Long, RefBook> refBookMap = new HashMap<Long, RefBook>();
    private Map<Long, RefBookAttribute> refBookAttributeMap = new HashMap<Long, RefBookAttribute>();
    private Map<Long, Long> refBookAttributeToRefBookMap = new HashMap<Long, Long>();
    private List<RefBook> refBookList = new ArrayList<RefBook>();
    private int generatedColumnId = -1;

    @ProxyEvent
    @Override
    public void onUpdateId(UpdateFTIdEvent event) {
        getView().setEnableModify(event.getFtId()==0);
    }

    @Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateColumnPage)
	@TabInfo(container = FormTemplateMainPresenter.class, label = AdminConstants.TabLabels.formTemplateColumnLabel, priority = AdminConstants.TabPriorities.formTemplateColumnPriority)
	public interface MyProxy extends
			TabContentProxyPlace<FormTemplateColumnPresenter> {
	}

	public interface MyView extends View,
			HasUiHandlers<FormTemplateColumnUiHandlers> {
		void setColumnList(List<Column> columnList, boolean isFormChanged);

		void setColumn(Column column);

        void setRefBookList(List<RefBook> refBookList);

		void flush();

        /**
         * Устанавливает доступность колонок на редактирование.
         * Если только создали версию макета, то доступны, в остальных случаях нет.
         * Задача http://jira.aplana.com/browse/SBRFACCTAX-11384.
         */
        void setEnableModify(boolean isEnable);
	}

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateColumnPresenter(final EventBus eventBus,
			final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy, FormTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
	}
	
	@Override
	protected void onBind() {
		super.onBind();
		addRegisteredHandler(FormTemplateFlushEvent.getType(), this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		boolean isFormChanged = true;
        if (formTemplate != null) {
            isFormChanged = formTemplate.getId() != null && !formTemplate.getId().equals(event.getFormTemplateExt().getFormTemplate().getId());
        }
        formTemplate = event.getFormTemplateExt().getFormTemplate();

        refBookAttributeToRefBookMap.clear();
        refBookAttributeMap.clear();
        refBookMap.clear();

        refBookList = event.getRefBookList();
        if (refBookList != null) {
            for (RefBook refBook : refBookList) {
                refBookMap.put(refBook.getId(), refBook);
                for (RefBookAttribute refBookAttribute : refBook.getAttributes()) {
                    refBookAttributeMap.put(refBookAttribute.getId(), refBookAttribute);
                    refBookAttributeToRefBookMap.put(refBookAttribute.getId(), refBook.getId());
                }
            }
        }

        getView().setRefBookList(refBookList);
		getView().setColumnList(formTemplate.getColumns(), isFormChanged);
        getView().setEnableModify(formTemplate.getId()==null||formTemplate.getId()==0);
	}

	@Override
	public void onFlush(FormTemplateFlushEvent event) {
		getView().flush();
	}

	/**
	 * Исправляем значение alias чтобы небыло дубликатов
	 * 
	 * @param column столбец
	 */
	private void fixAlias(Column column) {
		/*int i = 0;
		String oldAlias = column.getAlias() == null ? "псевдоним" : column.getAlias();
		while (aliasExists(column)) {
			column.setAlias(oldAlias + ++i);
		}
		if (!oldAlias.equals(column.getAlias())) {
			getView().setColumn(column);
		} */
	}

	@Override
	public void addColumn(Column column) {
		fixAlias(column);
		formTemplate.addColumn(column);
	}

	@Override
	public void removeColumn(Column column) {
		formTemplate.removeColumn(column);
	}

	@Override
	public void flushColumn(Column column) {
		fixAlias(column);
	}

    @Override
    public RefBook getRefBook(Long refBookId) {
        return refBookMap.get(refBookId);
    }

    @Override
    public RefBookAttribute getRefBookAttribute(Long refBookAttributeId) {
        return refBookAttributeMap.get(refBookAttributeId);
    }

    @Override
    public Long getRefBookByAttributeId(Long refBookAttributeId, boolean selectFirstWhenNull) {
        return (selectFirstWhenNull && refBookAttributeId == null) ?
                refBookList.get(0).getId() :
                refBookAttributeToRefBookMap.get(refBookAttributeId);
    }

    @Override
    public int getNextGeneratedColumnId() {
        return generatedColumnId--;
    }

    @Override
    public void changeColumnType(int position, Column oldColumn, Column newColumn) {
        // собрать значения ячеек данных
        List<Integer> colSpansRows = new ArrayList<Integer>(formTemplate.getRows().size());
        List<Integer> rowSpansRows = new ArrayList<Integer>(formTemplate.getRows().size());
        List<String> styleAliasRows = new ArrayList<String>(formTemplate.getRows().size());
        List<Boolean> editableRows = new ArrayList<Boolean>(formTemplate.getRows().size());
        for (DataRow<Cell> row : formTemplate.getRows()) {
            Cell cell = row.getCell(oldColumn.getAlias());
            colSpansRows.add(cell.getColSpan());
            rowSpansRows.add(cell.getRowSpan());
            styleAliasRows.add(cell.getStyleAlias());
            editableRows.add(cell.isEditable());
        }
        // собрать значения ячеек шапки
        List<Integer> colSpansHeaders = new ArrayList<Integer>(formTemplate.getHeaders().size());
        List<Integer> rowSpansHeaders = new ArrayList<Integer>(formTemplate.getHeaders().size());
        List<Object> valuesHeaders = new ArrayList<Object>(formTemplate.getHeaders().size());
        for (DataRow<HeaderCell> headerRow : formTemplate.getHeaders()) {
            HeaderCell cell = headerRow.getCell(oldColumn.getAlias());
            colSpansHeaders.add(cell.getColSpan());
            rowSpansHeaders.add(cell.getRowSpan());
            valuesHeaders.add(cell.getValue());
        }

        // удалить колонку со старым типов и добавить новую
        formTemplate.removeColumn(oldColumn);
        fixAlias(newColumn);
        formTemplate.addColumn(position, newColumn);

        // задать старые значения ячейкам данных
        int i = 0;
        for (DataRow<Cell> row : formTemplate.getRows()) {
            Cell cell = row.getCell(oldColumn.getAlias());
            cell.setColSpan(colSpansRows.get(i));
            cell.setRowSpan(rowSpansRows.get(i));
            cell.setStyle(formTemplate.getStyle(styleAliasRows.get(i)));
            cell.setEditable(editableRows.get(i));
            i++;
        }
        // задать старые значения ячейкам шапки
        i = 0;
        for (DataRow<HeaderCell> headerRow : formTemplate.getHeaders()) {
            HeaderCell cell = headerRow.getCell(oldColumn.getAlias());
            cell.setColSpan(colSpansHeaders.get(i));
            cell.setRowSpan(rowSpansHeaders.get(i));
            cell.setValue(valuesHeaders.get(i), headerRow.getIndex());
            i++;
        }
    }
}
