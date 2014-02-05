package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter;

import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.model.TaxType;
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

public class FilterDeclarationTemplatePresenter extends PresenterWidget<FilterDeclarationTemplatePresenter.MyView> implements FilterDeclarationTemplateUIHandlers {

	public interface MyView extends View, HasUiHandlers<FilterDeclarationTemplateUIHandlers> {
		
		// Установка/получение значений фильтра
		
		void setTemplateFilter(TemplateFilter formDataFilter);

		TemplateFilter getDataFilter();
		
		// Установка доступных значений

		void setTaxTypes(List<TaxType> taxTypes);

	}

	private final DispatchAsync dispatchAsync;

	@Inject
	public FilterDeclarationTemplatePresenter(EventBus eventBus, MyView view, DispatchAsync dispatchAsync) {
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
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetFilterFormTemplateDataResult>() {
					@Override
					public void onSuccess(GetFilterFormTemplateDataResult result) {
						getView().setTaxTypes(result.getTaxTypes());
						FilterDeclarationTemplateReadyEvent.fire(FilterDeclarationTemplatePresenter.this, true);
					}

					@Override
					public void onFailure(Throwable caught) {
						super.onFailure(caught);
						FilterDeclarationTemplateReadyEvent.fire(FilterDeclarationTemplatePresenter.this, false);
					}

				}, this));
	}

	@Override
	public void onApplyClicked() {
		DeclarationTemplateApplyEvent.fire(this);
	}
}
