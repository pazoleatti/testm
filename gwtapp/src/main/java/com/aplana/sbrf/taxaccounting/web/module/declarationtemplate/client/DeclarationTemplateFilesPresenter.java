package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.DeclarationTemplateFile;
import com.aplana.sbrf.taxaccounting.web.main.api.client.DownloadUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DeclarationTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTemplateEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.EndLoadFileEvent;
import com.aplana.sbrf.taxaccounting.web.widget.fileupload.event.StartLoadFileEvent;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.*;
import com.gwtplatform.mvp.client.proxy.LockInteractionEvent;
import com.gwtplatform.mvp.client.proxy.TabContentProxyPlace;

import java.util.List;

public class DeclarationTemplateFilesPresenter
		extends Presenter<DeclarationTemplateFilesPresenter.MyView, DeclarationTemplateFilesPresenter.MyProxy>
		implements DeclarationTemplateFilesUiHandlers, UpdateTemplateEvent.MyHandler, DeclarationTemplateFlushEvent.MyHandler {

    @Title("Шаблоны")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplateFiles)
	@TabInfo(container = DeclarationTemplateMainPresenter.class, label = DeclarationTemplateTokens.declarationTemplateFilesLabel, priority = DeclarationTemplateTokens.declarationTemplateFilesPriority)
	public interface MyProxy extends
			TabContentProxyPlace<DeclarationTemplateFilesPresenter> {
	}

	public interface MyView extends View, HasUiHandlers<DeclarationTemplateFilesUiHandlers> {
        DeclarationTemplateFile getDeclarationTemplateFile();
        void setDeclarationTemplateFiles(List<DeclarationTemplateFile> declarationTemplateFiles);
	}

    private DeclarationTemplateMainPresenter declarationTemplateMainPresenter;

	@Inject
	public DeclarationTemplateFilesPresenter(final EventBus eventBus,
                                             final MyView view, final MyProxy proxy,
                                             DeclarationTemplateMainPresenter declarationTemplateMainPresenter) {
		super(eventBus, view, proxy, DeclarationTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
        this.declarationTemplateMainPresenter = declarationTemplateMainPresenter;
	}
	
	@Override
	protected void onBind() {
		super.onBind();
    }

    @Override
    @ProxyEvent
    public void onUpdateTemplate(UpdateTemplateEvent event) {
        getView().setDeclarationTemplateFiles(declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().getDeclarationTemplateFiles());
    }

    @Override
    public void onFlush(DeclarationTemplateFlushEvent event) {

    }

    @Override
    public void downloadFile() {
        if (getView().getDeclarationTemplateFile().getBlobDataId() != null) {
            DownloadUtils.openInIframe(GWT.getHostPageBaseURL() + "download/downloadByUuid/" + getView().getDeclarationTemplateFile().getBlobDataId());
        }
    }

    @Override
    public void onStartLoad(StartLoadFileEvent event) {
        // Чистим логи и блокируем форму
        LogCleanEvent.fire(this);
        LockInteractionEvent.fire(this, true);
    }

    @Override
    public void onEndLoad(EndLoadFileEvent event) {
        // Разблокируем форму и выводим логи
        LockInteractionEvent.fire(this, false);
        LogAddEvent.fire(this, event.getUuid());
        if (!event.isHasError()){
            onTemplateFilesChanged();
        }
    }

    @Override
    public void onTemplateFilesChanged() {
        declarationTemplateMainPresenter.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
    }
}
