package com.aplana.sbrf.taxaccounting.web.module.admin.client.presenter;

import com.aplana.sbrf.taxaccounting.model.Column;
import com.aplana.sbrf.taxaccounting.model.FormTemplate;
import com.aplana.sbrf.taxaccounting.model.StringColumn;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.AdminNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.event.FormTemplateSetEvent;
import com.aplana.sbrf.taxaccounting.web.module.admin.client.view.FormTemplateColumnUiHandlers;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;

public class FormTemplateColumnPresenter extends Presenter<FormTemplateColumnPresenter.MyView, FormTemplateColumnPresenter.MyProxy>
		implements FormTemplateColumnUiHandlers, FormTemplateSetEvent.MyHandler, FormTemplateFlushEvent.MyHandler {

	@Title("Администрирование")
	@ProxyCodeSplit
	@NameToken(AdminNameTokens.formTemplateColumnPage)
	@TabInfo(container = FormTemplateMainPresenter.class,
			label = "Описание столбцов",
			priority = 2)
	public interface MyProxy extends TabContentProxyPlace<FormTemplateColumnPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<FormTemplateColumnUiHandlers> {
		void setColumnList(List<Column> columnList);
		void flush();
	}

	private FormTemplate formTemplate;

	@Inject
	public FormTemplateColumnPresenter(final EventBus eventBus, final MyView view, final MyProxy proxy) {
		super(eventBus, view, proxy);
		getView().setUiHandlers(this);
	}

	@ProxyEvent
	@Override
	public void onSet(FormTemplateSetEvent event) {
		formTemplate = event.getFormTempltate();
		getView().setColumnList(formTemplate.getColumns());
	}

	@ProxyEvent
	@Override
	public void onFlush(FormTemplateFlushEvent event) {
		getView().flush();
	}

	@Override
	public void prepareFromRequest(PlaceRequest request) {
		super.prepareFromRequest(request);
	}

	@Override
	protected void revealInParent() {
		RevealContentEvent.fire(this, FormTemplateMainPresenter.TYPE_SetTabContent, this);
	}

	@Override
	public void addColumn() {
		Column newColumn = new StringColumn();
		newColumn.setName("Новый столбец");
		newColumn.setAlias("Новый");
		newColumn.setWidth(5);
		newColumn.setOrder(formTemplate.getColumns().size() + 1);
		formTemplate.getColumns().add(newColumn);
		getView().setColumnList(formTemplate.getColumns());
	}

	@Override
	public void removeColumn(int index) {
		formTemplate.getColumns().remove(index);
		getView().setColumnList(formTemplate.getColumns());
	}

}
