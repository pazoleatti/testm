package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateColumnUiHandlers;
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
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

public class FormTemplateColumnPresenter
		extends
		Presenter<FormTemplateColumnPresenter.MyView, FormTemplateColumnPresenter.MyProxy>
		implements FormTemplateColumnUiHandlers,
		FormTemplateSetEvent.MyHandler, FormTemplateFlushEvent.MyHandler {

	@Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateColumnPage)
	@TabInfo(container = FormTemplateMainPresenter.class, label = AdminConstants.TabLabels.formTemplateColumnLabel, priority = AdminConstants.TabPriorities.formTemplateColumnPriority)
	public interface MyProxy extends
			TabContentProxyPlace<FormTemplateColumnPresenter> {
	}

	public interface MyView extends View,
			HasUiHandlers<FormTemplateColumnUiHandlers> {
		void setColumnList(List<Column> columnList);

		void setColumn(Column column);

		void flush();
	}

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateColumnPresenter(final EventBus eventBus,
			final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		formTemplate = event.getFormTemplate();
		getView().setColumnList(formTemplate.getColumns());
	}

	@ProxyEvent
	@Override
	public void onFlush(FormTemplateFlushEvent event) {
		getView().flush();
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this,
				FormTemplateMainPresenter.TYPE_SetTabContent, this);
	}

	private boolean aliasExists(Column column) {
		for (Column col : formTemplate.getColumns()) {
			if (col.getAlias().equals(column.getAlias()) && col != column) {
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
		String oldAlias = column.getAlias();
		while (aliasExists(column)) {
			column.setAlias(oldAlias + ++i);
		}
		if (oldAlias != column.getAlias()) {
			getView().setColumn(column);
		}
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
	public void flashColumn(Column column) {
		fixAlias(column);
	}
}
