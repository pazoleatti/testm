package com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.declarationDestinationsDialog;

import com.aplana.sbrf.taxaccounting.model.FormDataFilter;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.style.ListBoxWithTooltip;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.PopupViewWithUiHandlers;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author auldanov
 */
public class DeclarationDestinationsView extends PopupViewWithUiHandlers<DeclarationDestinationsUiHandlers>
        implements DeclarationDestinationsPresenter.MyView, Editor<FormDataFilter>{

    public interface Binder extends UiBinder<PopupPanel, DeclarationDestinationsView> {
    }

    // значения выпадающего списка типы деклараций, для виджета ValueListBox
    private Map<Integer, String> formTypesMap = new LinkedHashMap<Integer, String>();

    @UiField(provided = true)
    ValueListBox declarationTypeId;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @Inject
    public DeclarationDestinationsView(Binder uiBinder, EventBus eventBus) {
        super(eventBus);

        declarationTypeId = new ListBoxWithTooltip<Integer>(new AbstractRenderer<Integer>() {
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

}
