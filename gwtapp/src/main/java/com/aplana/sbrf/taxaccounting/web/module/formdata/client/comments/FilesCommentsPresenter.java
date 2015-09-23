package com.aplana.sbrf.taxaccounting.web.module.formdata.client.comments;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.FormData;
import com.aplana.sbrf.taxaccounting.model.FormDataFile;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CompositeCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.*;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Презентер попап модального окна "Файлы и комментарии",
 * данное окно вызывается с формы нф
 *
 * @author Lhaziev
 */
public class FilesCommentsPresenter extends PresenterWidget<FilesCommentsPresenter.MyView> implements FilesCommentsUiHandlers {
    public interface MyView extends PopupView, HasUiHandlers<FilesCommentsUiHandlers> {
        void setTableData(List<FormDataFile> tableData);
        boolean isReadOnlyMode();
        void setReadOnlyMode(boolean readOnlyMode);
        void setNote(String note);
        void addFileUploadValueChangeHandler(ValueChangeHandler<String> changeHandler);
        void addFile(List<FormDataFile> files);
        HandlerRegistration addEndLoadFileHandler(EndLoadFileEvent.EndLoadFileHandler handler);
        HandlerRegistration addStartLoadFileHandler(EndLoadFileEvent.EndLoadFileHandler handler);
    }

    private static final String ERROR_MSG = "Операция не выполнена";

    private final DispatchAsync dispatcher;
    private HandlerRegistration closeFormDataHandlerRegistration;
    private FormData formData;

    @Inject
    public FilesCommentsPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    public void setFormData(FormData formData) {
        this.formData = formData;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        GetFilesCommentsAction action = new GetFilesCommentsAction();
        action.setFormData(formData);
        dispatcher.execute(action, getCallback(false));
    }

    CompositeCallback<GetFilesCommentsResult> getCallback(final boolean exit) {
        return CallbackUtils
                .defaultCallback(new AbstractCallback<GetFilesCommentsResult>() {
                    @Override
                    public void onSuccess(GetFilesCommentsResult result) {
                        LogCleanEvent.fire(FilesCommentsPresenter.this);
                        LogAddEvent.fire(FilesCommentsPresenter.this, result.getUuid());
                        if (exit) {
                            getView().hide();
                        } else {
                            getView().setReadOnlyMode(result.isReadOnlyMode());
                            getView().setNote(result.getNote());
                            getView().setTableData(result.getFiles());
                            if (closeFormDataHandlerRegistration != null)
                                closeFormDataHandlerRegistration.removeHandler();
                            if (!result.isReadOnlyMode()) {
                                closeFormDataHandlerRegistration = Window.addCloseHandler(new CloseHandler<Window>() {
                                    @Override
                                    public void onClose(CloseEvent<Window> event) {
                                        closeFormDataHandlerRegistration.removeHandler();
                                        unlockForm();
                                    }
                                });
                            }
                        }
                    }
                }, this);
    }

    @Override
    public void onHide() {
        super.onHide();
        if (closeFormDataHandlerRegistration != null)
            closeFormDataHandlerRegistration.removeHandler();
        unlockForm();
    }

    private void unlockForm() {
        if (!getView().isReadOnlyMode()) {
            UnlockFilesCommentsAction action = new UnlockFilesCommentsAction();
            action.setFormId(formData.getId());
            dispatcher.execute(action, CallbackUtils.emptyCallback());
        }
    }

    @Override
    public void onSaveClicked(String note, final List<FormDataFile> files, boolean exit) {
        SaveFilesCommentsAction action = new SaveFilesCommentsAction();
        List<FormDataFile> formDataFiles = new ArrayList<FormDataFile>();
        formDataFiles.addAll(files);
        action.setFormData(formData);
        action.setNote(note);
        action.setFiles(formDataFiles);
        dispatcher.execute(action, getCallback(exit));
    }


    @Override
    protected void onBind() {
        super.onBind();
        getView().addFileUploadValueChangeHandler( new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                AddFileAction action = new AddFileAction();
                action.setUuid(event.getValue());
                action.setFormData(formData);
                dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<AddFileResult>() {
                            @Override
                            public void onSuccess(AddFileResult result) {
                                getView().addFile(result.getFiles());
                            }
                        }, FilesCommentsPresenter.this));
            }
        });
        getView().addEndLoadFileHandler(new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                if (event.isHasError()) {
                    Dialog.errorMessage("Ошибка", ERROR_MSG);
                }
                LogAddEvent.fire(FilesCommentsPresenter.this, event.getUuid());
                LockInteractionEvent.fire(FilesCommentsPresenter.this, false);
            }
        });
        getView().addStartLoadFileHandler(new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                LockInteractionEvent.fire(FilesCommentsPresenter.this, true);
            }
        });
    }
}

