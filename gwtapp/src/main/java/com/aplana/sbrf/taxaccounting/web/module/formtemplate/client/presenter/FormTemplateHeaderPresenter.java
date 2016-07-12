package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.presenter;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.DataRow;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.formdata.HeaderCell;
import com.aplana.sbrf.taxaccounting.model.util.FormDataUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.AdminConstants;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.view.FormTemplateHeaderUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;

public class FormTemplateHeaderPresenter
		extends
		Presenter<FormTemplateHeaderPresenter.MyView, FormTemplateHeaderPresenter.MyProxy>
		implements FormTemplateHeaderUiHandlers, FormTemplateSetEvent.MyHandler {


	@Title("Заголовки")
	@ProxyCodeSplit
	@NameToken(AdminConstants.NameTokens.formTemplateHeaderPage)
	@TabInfo(container = FormTemplateMainPresenter.class, label = AdminConstants.TabLabels.formTemplateHeaderLabel, priority = AdminConstants.TabPriorities.formTemplateHeaderPriority)
	public interface MyProxy extends
			TabContentProxyPlace<FormTemplateHeaderPresenter> {
	}

	public interface MyView extends View,
			HasUiHandlers<FormTemplateHeaderUiHandlers> {
		void setHeaderData( List<DataRow<HeaderCell>> headers);

		void setColumnsData( List<Column> columns);
	}

	private FormTemplate formTemplate;
	private FormTemplateMainPresenter formTemplateMainPresenter;

	@Inject
	public FormTemplateHeaderPresenter(final EventBus eventBus, final MyView view,
	                                   final MyProxy proxy, FormTemplateMainPresenter formTemplateMainPresenter) {
		super(eventBus, view, proxy);
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

		if (formTemplate != null) {
			FormTemplateFlushEvent.fire(this);
			setViewData();

		}
	}


	private void setViewData() {
		FormDataUtils.cleanValueOwners(formTemplate.getHeaders());
		getView().setColumnsData(formTemplate.getColumns());
		getView().setHeaderData(formTemplate.getHeaders());

	}

	@Override
	public void onRemoveButton(DataRow<HeaderCell> row) {
		if (row != null) {
			formTemplate.getHeaders().remove(row);
			getView().setHeaderData(formTemplate.getHeaders());
		}
	}

	@Override
	public void onAddButton(DataRow<HeaderCell> row) {
		if (row != null) {
			for (Column column: formTemplate.getColumns()) {
				if (column.getWidth() > 0) {
				row.getCell(column.getAlias()).setValue(formTemplate.getColumn(column.getAlias()).getName(), row.getIndex());
				} else {
					row.getCell(column.getAlias()).setValue("", row.getIndex());
				}
			}
			formTemplate.getHeaders().add(row);
			getView().setHeaderData(formTemplate.getHeaders());
		}
	}

	@Override
	public void onAddNumberedHeaderButton(DataRow<HeaderCell> row) {
		if (row != null) {
			int colNum = 1;
			for (Column column: formTemplate.getColumns()) {
				if (column.getWidth() > 0) {
					row.getCell(column.getAlias()).setValue(colNum++, row.getIndex());
				} else {
					row.getCell(column.getAlias()).setValue("", row.getIndex());
				}
			}
			formTemplate.getHeaders().add(row);
			getView().setHeaderData(formTemplate.getHeaders());
		}
	}

	public void onDataViewChanged(){
		formTemplateMainPresenter.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
	}

}
