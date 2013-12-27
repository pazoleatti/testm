package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBook;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAttribute;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateColumnUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.gwtplatform.mvp.client.annotations.TabInfo;
import com.gwtplatform.mvp.client.annotations.Title;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class FormTemplateColumnPresenter
		extends
		Presenter<FormTemplateColumnPresenter.MyView, FormTemplateColumnPresenter.MyProxy>
		implements FormTemplateColumnUiHandlers,
		FormTemplateSetEvent.MyHandler, FormTemplateFlushEvent.MyHandler {

    private Map<Long, RefBook> refBookMap = new HashMap<Long, RefBook>();
    private Map<Long, RefBookAttribute> refBookAttributeMap = new HashMap<Long, RefBookAttribute>();
    private Map<Long, Long> refBookAttributeToRefBookMap = new HashMap<Long, Long>();
    private int generatedColumnId = -1;

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
            isFormChanged = !formTemplate.getId().equals(event.getFormTemplate().getId());
        }
        formTemplate = event.getFormTemplate();

        refBookAttributeToRefBookMap.clear();
        refBookAttributeMap.clear();
        refBookMap.clear();

        if (event.getRefBookList() != null) {
            for (RefBook refBook : event.getRefBookList()) {
                refBookMap.put(refBook.getId(), refBook);
                for (RefBookAttribute refBookAttribute : refBook.getAttributes()) {
                    refBookAttributeMap.put(refBookAttribute.getId(), refBookAttribute);
                    refBookAttributeToRefBookMap.put(refBookAttribute.getId(), refBook.getId());
                }
            }
        }

        getView().setRefBookList(event.getRefBookList());
		getView().setColumnList(formTemplate.getColumns(), isFormChanged);
	}

	@Override
	public void onFlush(FormTemplateFlushEvent event) {
		getView().flush();
	}

	private boolean aliasExists(Column column) {
		for (Column col : formTemplate.getColumns()) {
			if ((col.getAlias() == null)  || col.getAlias().equals(column.getAlias()) && col != column) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Исправляем значение alias чтобы небыло дубликатов
	 * 
	 * @param column
	 */
	private void fixAlias(Column column) {
		int i = 0;
		String oldAlias = column.getAlias() == null ? "псевдоним" : column.getAlias();
		while (aliasExists(column)) {
			column.setAlias(oldAlias + ++i);
		}
		if (oldAlias != column.getAlias()) {
			getView().setColumn(column);
		}
	}

	@Override
	public void addColumn(int position, Column column) {
		fixAlias(column);
		formTemplate.addColumn(position, column);
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
    public Long getRefBookByAttributeId(Long refBookAttributeId) {
        return refBookAttributeToRefBookMap.get(refBookAttributeId);
    }

    @Override
    public int getNextGeneratedColumnId() {
        return generatedColumnId--;
    }
}
