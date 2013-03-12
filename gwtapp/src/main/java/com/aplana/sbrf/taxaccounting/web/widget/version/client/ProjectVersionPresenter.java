package com.aplana.sbrf.taxaccounting.web.widget.version.client;

import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.AbstractCallback;
import com.aplana.sbrf.taxaccounting.web.main.api.client.dispatch.CallbackUtils;
import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersion;
import com.aplana.sbrf.taxaccounting.web.widget.version.shared.GetProjectVersionResult;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.dispatch.shared.DispatchAsync;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public class ProjectVersionPresenter extends
		PresenterWidget<ProjectVersionView> {

	public interface MyView extends View {
		void setProjectVersion(String projectVersion);
	}

	private final DispatchAsync dispatchAsync;

	@Inject
	public ProjectVersionPresenter(EventBus eventBus, ProjectVersionView view,
			DispatchAsync dispatchAsync) {
		super(eventBus, view);
		this.dispatchAsync = dispatchAsync;
	}

	@Override
	protected void onReveal() {
		GetProjectVersion action = new GetProjectVersion();
		dispatchAsync.execute(action, CallbackUtils
				.defaultCallback(new AbstractCallback<GetProjectVersionResult>() {
					@Override
					public void onSuccess(GetProjectVersionResult result) {
						getView().setProjectVersion(result.getProjectVersion());
					}
				}));
		super.onReveal();
	}

}
