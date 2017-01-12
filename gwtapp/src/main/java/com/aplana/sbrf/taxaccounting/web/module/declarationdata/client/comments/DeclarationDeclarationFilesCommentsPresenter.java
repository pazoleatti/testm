package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments;

import com.aplana.gwt.client.dialog.Dialog;
import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.DeclarationDataFile;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CompositeCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.shared.*;
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
public class DeclarationDeclarationFilesCommentsPresenter extends PresenterWidget<DeclarationDeclarationFilesCommentsPresenter.MyView> implements DeclarationFilesCommentsUiHandlers {
    public interface MyView extends PopupView, HasUiHandlers<DeclarationFilesCommentsUiHandlers> {
        void setTableData(List<DeclarationDataFile> tableData);
        boolean isReadOnlyMode();
        void setReadOnlyMode(boolean readOnlyMode);
        void setNote(String note);
        void addFileUploadValueChangeHandler(ValueChangeHandler<String> changeHandler);
        void addFile(List<DeclarationDataFile> files);
        HandlerRegistration addEndLoadFileHandler(EndLoadFileEvent.EndLoadFileHandler handler);
        HandlerRegistration addStartLoadFileHandler(EndLoadFileEvent.EndLoadFileHandler handler);
    }

    private static final String ERROR_MSG = "Операция не выполнена";

    private final DispatchAsync dispatcher;
    private HandlerRegistration closeFormDataHandlerRegistration;
    private DeclarationData declarationData;

    @Inject
    public DeclarationDeclarationFilesCommentsPresenter(final EventBus eventBus, final MyView view, DispatchAsync dispatcher) {
        super(eventBus, view);
        this.dispatcher = dispatcher;
        getView().setUiHandlers(this);
    }

    public void setFormData(DeclarationData declarationData) {
        this.declarationData = declarationData;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        GetDeclarationFilesCommentsAction action = new GetDeclarationFilesCommentsAction();
        action.setDeclarationData(declarationData);
        dispatcher.execute(action, getCallback(false));
    }

    CompositeCallback<GetDeclarationFilesCommentsResult> getCallback(final boolean exit) {
        return CallbackUtils
                .defaultCallback(new AbstractCallback<GetDeclarationFilesCommentsResult>() {
                    @Override
                    public void onSuccess(GetDeclarationFilesCommentsResult result) {
                        LogCleanEvent.fire(DeclarationDeclarationFilesCommentsPresenter.this);
                        LogAddEvent.fire(DeclarationDeclarationFilesCommentsPresenter.this, result.getUuid());
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
            UnlockDeclarationFilesCommentsAction action = new UnlockDeclarationFilesCommentsAction();
            action.setDeclarationId(declarationData.getId());
            dispatcher.execute(action, CallbackUtils.emptyCallback());
        }
    }

    @Override
    public void onSaveClicked(String note, final List<DeclarationDataFile> files, boolean exit) {
        SaveDeclarationFilesCommentsAction action = new SaveDeclarationFilesCommentsAction();
        List<DeclarationDataFile> declarationDataFiles = new ArrayList<DeclarationDataFile>();
        declarationDataFiles.addAll(files);
        action.setDeclarationData(declarationData);
        action.setNote(note);
        action.setFiles(declarationDataFiles);
        dispatcher.execute(action, getCallback(exit));
    }


    @Override
    protected void onBind() {
        super.onBind();
        getView().addFileUploadValueChangeHandler( new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                AddDeclarationFileAction action = new AddDeclarationFileAction();
                action.setUuid(event.getValue());
                action.setDeclarationData(declarationData);
                dispatcher.execute(action, CallbackUtils
                        .defaultCallback(new AbstractCallback<AddDeclarationFileResult>() {
                            @Override
                            public void onSuccess(AddDeclarationFileResult result) {
                                getView().addFile(result.getFiles());
                            }
                        }, DeclarationDeclarationFilesCommentsPresenter.this));
            }
        });
        getView().addEndLoadFileHandler(new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                if (event.isHasError()) {
                    Dialog.errorMessage("Ошибка", ERROR_MSG);
                }
                LogAddEvent.fire(DeclarationDeclarationFilesCommentsPresenter.this, event.getUuid());
                LockInteractionEvent.fire(DeclarationDeclarationFilesCommentsPresenter.this, false);
            }
        });
        getView().addStartLoadFileHandler(new EndLoadFileEvent.EndLoadFileHandler() {
            @Override
            public void onEndLoad(EndLoadFileEvent event) {
                LockInteractionEvent.fire(DeclarationDeclarationFilesCommentsPresenter.this, true);
            }
        });
    }
}

