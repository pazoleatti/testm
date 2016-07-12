package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateRowUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;

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

		void addCustomHeader(List<DataRow<HeaderCell>> newHeaders);
	}

	private FormTemplate formTemplate;
	private FormTemplateMainPresenter formTemplateMainPresenter;

	@Inject
	public FormTemplateRowPresenter(final EventBus eventBus, final MyView view,
			final MyProxy proxy, FormTemplateMainPresenter formTemplateMainPresenter) {
		super(eventBus, view, proxy, FormTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
		this.formTemplateMainPresenter = formTemplateMainPresenter;
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		formTemplate = event.getFormTemplateExt().getFormTemplate();
		setViewData();
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this,
				FormTemplateMainPresenter.TYPE_SetTabContent, this);
		// TODO: [sgoryachkin] 
		// 1) В перегрузке этого метода нет необходимости
		// 2) В этом методе не должно быть логики (для этого есть события - onReveal)
		
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
		FormDataUtils.setValueOwners(formTemplate.getHeaders());
		getView().setStylesData(formTemplate.getStyles());
		getView().setColumnsData(formTemplate.getColumns());
		getView().setRowsData(formTemplate.getRows());
		getView().addCustomHeader(formTemplate.getHeaders());
	}

	public void onDataViewChanged(){
		formTemplateMainPresenter.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
	}

}
