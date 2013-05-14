package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.Cell;
import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormStyle;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateRowUiHandlers;
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

public class FormTemplateRowPresenter
		extends
		Presenter<FormTemplateRowPresenter.MyView, FormTemplateRowPresenter.MyProxy>
		implements FormTemplateRowUiHandlers, FormTemplateSetEvent.MyHandler {
	/**
	 * {@link FormTemplateRowPresenter}'s proxy.
	 */
	@Title("Шаблоны налоговых форм")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateRowPage)
	@TabInfo(container = FormTemplateMainPresenter.class, label = AdminConstants.TabLabels.formTemplateRowLabel, priority = AdminConstants.TabPriorities.formTemplateRowPriority)
	public interface MyProxy extends
			TabContentProxyPlace<FormTemplateRowPresenter> {
	}

	public interface MyView extends View,
			HasUiHandlers<FormTemplateRowUiHandlers> {
		void setColumnsData(List<Column> columnsData);

		void setRowsData(List<DataRow<Cell>> rows);

		void setStylesData(List<FormStyle> styles);

		void addCustomHeader(boolean addNumberedHeader);
	}

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateRowPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		formTemplate = event.getFormTemplate();
		setViewData();
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this,
				FormTemplateMainPresenter.TYPE_SetTabContent, this);

		if (formTemplate != null) {
			FormTemplateFlushEvent.fire(this);
			setViewData();
		}
	}

	@Override
	public void onRemoveButton(DataRow<Cell> row) {
		if (row != null) {
			formTemplate.getRows().remove(row);
			getView().setRowsData(formTemplate.getRows());
		}
	}

	private void setViewData() {
		getView().setStylesData(formTemplate.getStyles());
		getView().setColumnsData(formTemplate.getColumns());
		getView().setRowsData(formTemplate.getRows());
		getView().addCustomHeader(formTemplate.isNumberedColumns());
	}

}
