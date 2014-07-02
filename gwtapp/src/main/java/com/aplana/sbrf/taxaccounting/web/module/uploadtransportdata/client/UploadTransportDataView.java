package com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client;

import com.aplana.sbrf.taxaccounting.model.Department;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.fileupload.FileUploadHandler;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.departmentpicker.DepartmentPickerPopupWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Загрузка ТФ в каталог загрузки
 *
 * @author Dmitriy Levykin
 */
public class UploadTransportDataView extends ViewWithUiHandlers<UploadTransportDataUiHandlers>
        implements UploadTransportDataPresenter.MyView {

    interface Binder extends UiBinder<Widget, UploadTransportDataView> {
    }

    @UiField
    FileUploadWidget uploadWidget;

    @UiField
    DepartmentPickerPopupWidget departmentPicker;

    @Inject
    @UiConstructor
    public UploadTransportDataView(final Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
        initListeners();
    }

    private void initListeners() {
        uploadWidget.addStartLoadHandler(new StartLoadFileEvent.StartLoadFileHandler() {
            @Override
            public void onStartLoad(StartLoadFileEvent event) {
                getUiHandlers().onStartLoad(event);
            }
        });
        uploadWidget.addEndLoadHandler(new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                getUiHandlers().onEndLoad(event);
            }
        });
        departmentPicker.addValueChangeHandler(new ValueChangeHandler<List<Integer>>() {
            @Override
            public void onValueChange(ValueChangeEvent<List<Integer>> event) {
                List<Integer> selectedDepartments = departmentPicker.getValue();
                String departmentStr = "0";
                if (selectedDepartments != null && !selectedDepartments.isEmpty()) {
                    departmentStr = selectedDepartments.get(0).toString();
                    uploadWidget.setEnabled(true);
                } else {
                    uploadWidget.setEnabled(false);
                }
                System.out.println("url = " + getUiHandlers().ACTION_URL + departmentStr);
                uploadWidget.setActionUrl(getUiHandlers().ACTION_URL + departmentStr);
            }
        });
    }

    @Override
    public void setFileUploadHandler(FileUploadHandler fileUploadHandler) {
        uploadWidget.setFileUploadHandler(fileUploadHandler);
    }

    @Override
    public void setDepartments(List<Department> departments, Set<Integer> availableDepartments, final Integer defaultDepartmentId) {
        departmentPicker.setAvalibleValues(departments, availableDepartments);
        if (defaultDepartmentId != null) {
            departmentPicker.setValue(new ArrayList<Integer>(1) {{
                add(defaultDepartmentId);
            }}, true);
        }
    }

    @Override
    public void setCanChooseDepartment(boolean canChooseDepartment) {
        departmentPicker.setEnabled(canChooseDepartment);
    }
}