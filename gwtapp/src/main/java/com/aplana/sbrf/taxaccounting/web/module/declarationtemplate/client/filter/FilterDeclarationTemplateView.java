package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.filter;

import com.aplana.sbrf.taxaccounting.model.TemplateFilter;
import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.List;

public class FilterDeclarationTemplateView extends ViewWithUiHandlers<FilterDeclarationTemplateUIHandlers> implements FilterDeclarationTemplatePresenter.MyView,
		Editor<TemplateFilter>{

    interface MyBinder extends UiBinder<Widget, FilterDeclarationTemplateView> {
    }

    interface MyDriver extends SimpleBeanEditorDriver<TemplateFilter, FilterDeclarationTemplateView>{
    }

    private final MyDriver driver;

    @UiField(provided = true)
    ValueListBox<TaxType> taxType;

    @UiField
    CheckBox active;

	@Ignore
	@UiField
	Label taxTypeLbl;

    @Inject
    public FilterDeclarationTemplateView(final MyBinder binder, final MyDriver driver) {
    	super();
    	
		taxType = new ListBoxWithTooltip<TaxType>(new AbstractRenderer<TaxType>() {
			@Override
			public String render(TaxType object) {
				if (object == null) {
					return "";
				}
				return object.getName();
			}
		});

		initWidget(binder.createAndBindUi(this));
        this.driver = driver;
        this.driver.initialize(this);
    }

    @Override
    public void setTemplateFilter(TemplateFilter formDataFilter) {
        driver.edit(formDataFilter);
    }


    @Override
    public TemplateFilter getDataFilter() {
    	return driver.flush();
    }

	@Override
	public void setTaxTypes(List<TaxType> taxTypes){
        taxTypes.add(0, null);
        taxType.setAcceptableValues(taxTypes);
	}

	@UiHandler("apply")
	void onAppyButtonClicked(ClickEvent event) {
		if (getUiHandlers() != null) {
			getUiHandlers().onApplyClicked();
		}
	}
}
