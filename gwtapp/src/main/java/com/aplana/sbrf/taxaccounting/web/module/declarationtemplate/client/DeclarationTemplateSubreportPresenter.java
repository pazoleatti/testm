package com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.web.main.api.client.DownloadUtils;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogAddEvent;
import com.aplana.sbrf.taxaccounting.web.main.api.client.event.log.LogCleanEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.DeclarationTemplateFlushEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.event.UpdateTemplateEvent;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.shared.DeclarationTemplateExt;
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

public class DeclarationTemplateSubreportPresenter
		extends Presenter<DeclarationTemplateSubreportPresenter.MyView, DeclarationTemplateSubreportPresenter.MyProxy>
		implements DeclarationTemplateSubreportUiHandlers, UpdateTemplateEvent.MyHandler, DeclarationTemplateFlushEvent.MyHandler {


    @Title("Шаблоны")
	@ProxyCodeSplit
	@NameToken(DeclarationTemplateTokens.declarationTemplateSubreports)
	@TabInfo(container = DeclarationTemplateMainPresenter.class, label = DeclarationTemplateTokens.declarationTemplateSubreportsLabel, priority = DeclarationTemplateTokens.declarationTemplateSubreportsPriority)
	public interface MyProxy extends
			TabContentProxyPlace<DeclarationTemplateSubreportPresenter> {
	}

	public interface MyView extends View,
			HasUiHandlers<DeclarationTemplateSubreportUiHandlers> {
        void setDeclarationTemplate(DeclarationTemplateExt declaration);
        void flush();
        DeclarationSubreport getSelectedSubreport();
	}

    private DeclarationTemplateMainPresenter declarationTemplateMainPresenter;

	@Inject
	public DeclarationTemplateSubreportPresenter(final EventBus eventBus,
                                                 final MyView view, final MyProxy proxy,
                                                 DeclarationTemplateMainPresenter declarationTemplateMainPresenter) {
		super(eventBus, view, proxy, DeclarationTemplateMainPresenter.TYPE_SetTabContent);
		getView().setUiHandlers(this);
        this.declarationTemplateMainPresenter = declarationTemplateMainPresenter;
	}
	
	@Override
	protected void onBind() {
		super.onBind();
        addRegisteredHandler(UpdateTemplateEvent.getType(), this);
        addRegisteredHandler(DeclarationTemplateFlushEvent.getType(), this);
    }

    @ProxyEvent
    @Override
    public void onUpdateTemplate(UpdateTemplateEvent event) {
        getView().setDeclarationTemplate(declarationTemplateMainPresenter.getDeclarationTemplateExt());
    }

    @Override
    public void addSubreport(DeclarationSubreport subreport) {
        declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().addSubreport(subreport);
    }

    @Override
    public void removeSubreport(DeclarationSubreport subreport) {
        declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate().removeSubreport(subreport);
    }

    @Override
    public void flushSubreport(DeclarationSubreport subreport) {
    }

    @Override
    public void onFlush(DeclarationTemplateFlushEvent event) {
        if (declarationTemplateMainPresenter.getDeclarationTemplateExt().getDeclarationTemplate() != null) {
            getView().flush();
        }
    }

    @Override
    public void downloadFile() {
        if (getView().getSelectedSubreport().getBlobDataId() != null) {
            DownloadUtils.openInIframe(GWT.getHostPageBaseURL() + "download/downloadByUuid/" + getView().getSelectedSubreport().getBlobDataId());
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
            onSubreportChanged();
        }
    }

    @Override
    public void onSubreportChanged(){
        declarationTemplateMainPresenter.setOnLeaveConfirmation("Вы подтверждаете отмену изменений?");
    }
}
