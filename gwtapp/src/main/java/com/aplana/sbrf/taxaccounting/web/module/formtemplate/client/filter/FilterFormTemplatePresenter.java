package com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.filter;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFilterFormTemplateData;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.shared.GetFilterFormTemplateDataResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import java.util.List;

public class FilterFormTemplatePresenter extends PresenterWidget<FilterFormTemplatePresenter.MyView> implements FilterFormTemplateUIHandlers {

	public interface MyView extends View, HasUiHandlers<FilterFormTemplateUIHandlers> {
		
		// Установка/получение значений фильтра
		
		void setTemplateFilter(TemplateFilter formDataFilter);

		TemplateFilter getDataFilter();
		
		// Установка доступных значений

		void setTaxTypes(List<TaxType> taxTypes);

	}

	private final DispatchAsync dispatchAsync;

	@Inject
	public FilterFormTemplatePresenter(EventBus eventBus, MyView view, DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
		getView().setUiHandlers(this);
	}

	public TemplateFilter getFilterData() {
		return getView().getDataFilter();
	}

	public void initFilter(final TemplateFilter filter) {
        getView().setTemplateFilter(filter);

        GetFilterFormTemplateData action = new GetFilterFormTemplateData();
        FilterFormTemplateReadyEvent.fire(FilterFormTemplatePresenter.this, true);
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFilterFormTemplateDataResult>() {
					@Override
					public void onSuccess(GetFilterFormTemplateDataResult result) {
						getView().setTaxTypes(result.getTaxTypes());
						FilterFormTemplateReadyEvent.fire(FilterFormTemplatePresenter.this, true);
					}

					@Override
					public void onFailure(Throwable caught) {
						super.onFailure(caught);
						FilterFormTemplateReadyEvent.fire(FilterFormTemplatePresenter.this, false);
					}

				}, this));
	}

	@Override
	public void onCreateClicked() {
		FormTemplateCreateEvent.fire(this);
	}

	@Override
	public void onApplyClicked() {
		FormTemplateApplyEvent.fire(this);
	}

}
