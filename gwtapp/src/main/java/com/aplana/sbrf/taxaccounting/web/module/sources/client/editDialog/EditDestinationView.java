package com.aplana.sbrf.taxaccounting.web.module.sources.client.editDialog;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.LinkedHashMap;
import java.util.Map;

public class EditDestinationView extends PopupViewWithUiHandlers<EditDestinationUiHandlers>
        implements EditDeatinationPresenter.MyView,Editor<FormDataFilter> {

	public interface Binder extends UiBinder<PopupPanel, EditDestinationView> {
	}

    @UiField(provided = true)
    ValueListBox<FormDataKind> formDataKind;

    @UiField(provided = true)
    ListBoxWithTooltip<Integer> formTypeId;

	@UiField
	Button continueButton;

	@UiField
	Button cancelButton;

    private Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();

	@Inject
	public EditDestinationView(Binder uiBinder, EventBus eventBus) {
		super(eventBus);

        formDataKind = new ValueListBox<FormDataKind>(new AbstractRenderer<FormDataKind>() {
            @Override
            public String render(FormDataKind object) {
                if (object == null) {
                    return "";
                }
                return object.getName();
            }
        });

        formTypeId = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
            @Override
            public String render(Integer object) {
                if (object == null) {
                    return "";
                }
                return formTypesMap.get(object);
            }
        });

		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiHandler("continueButton")
	public void onSave(ClickEvent event){
		getUiHandlers().onConfirm();
	}

	@UiHandler("cancelButton")
	public void onCancel(ClickEvent event){
		hide();
	}

}
