package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;


import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateRowUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;


public class FormTemplateRowPresenter  extends Presenter<FormTemplateRowPresenter.MyView, FormTemplateRowPresenter.MyProxy>
		implements FormTemplateRowUiHandlers, FormTemplateSetEvent.MyHandler{
	/**
	 * {@link FormTemplateRowPresenter}'s proxy.
	 */
	@Title("Администрирование")
	@ProxyCodeSplit
	@NameToken(AdminNameTokens.formTemplateRowPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = "Начальные данные",
			priority = 3) // The 4 tab
	public interface MyProxy extends TabContentProxyPlace<FormTemplateRowPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateRowUiHandlers> {
		void setColumnsData(List<Column> columnsData);
		void setRowsData(List<DataRow> rows);
	}

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateRowPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		formTemplate = event.getFormTemplate();
		getView().setColumnsData(formTemplate.getColumns());
		getView().setRowsData(formTemplate.getRows());
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, FormTemplateMainPresenter.TYPE_SetTabContent, this);

		if (formTemplate != null) {
			getView().setColumnsData(formTemplate.getColumns());
			getView().setRowsData(formTemplate.getRows());
		}
	}

	@Override
	public void onAddButton() {
		formTemplate.getRows().add(new DataRow("Новый код", formTemplate.getColumns()));
		getView().setRowsData(formTemplate.getRows());
	}

	@Override
	public void onRemoveButton(DataRow row) {
		if (row != null && !row.isManagedByScripts()) {
			formTemplate.getRows().remove(row);
			getView().setRowsData(formTemplate.getRows());
		}
	}

}
