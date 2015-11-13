package com.aplana.sbrf.taxaccounting.web.module.scriptsimport.client;

import com.aplana.sbrf.taxaccounting.web.widget.fileupload.FileUploadWidget;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Представление модуля выполнения скриптов из конфигуратора
 * @author Stanislav Yasinskiy
 */
public class ScriptsImportView extends ViewWithUiHandlers<ScriptsImportUiHandlers> implements ScriptsImportPresenter.MyView {

    interface Binder extends UiBinder<Widget, ScriptsImportView> {
    }

    @UiField
    FileUploadWidget fileUploader;

    @Inject
    public ScriptsImportView(Binder uiBinder) {
        initWidget(uiBinder.createAndBindUi(this));
    }

    @Override
    public void addImportHandler(ValueChangeHandler<String> handler) {


        fileUploader.addStartLoadHandler(new StartLoadFileEvent.StartLoadFileHandler() {
            @Override
            public void onStartLoad(StartLoadFileEvent event) {
                getUiHandlers().onStartLoad();
            }
        });
        fileUploader.addValueChangeHandler(handler);
    }
}
