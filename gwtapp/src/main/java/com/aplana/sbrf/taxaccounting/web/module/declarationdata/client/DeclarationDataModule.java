package com.aplana.sbrf.taxaccounting.web.module.declarationdata.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments.DeclarationDeclarationFilesCommentsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.comments.DeclarationFilesCommentsView;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.move_to_create.MoveToCreatePresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.move_to_create.MoveToCreateView;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.NdflReferencesEditPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.NdflReferencesEditView;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.search.NdflReferencesSearchPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.ndfl_references.search.NdflReferencesSearchView;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.sources.SourcesPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.sources.SourcesView;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams.SubreportParamsPresenter;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.subreportParams.SubreportParamsView;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class DeclarationDataModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bindPresenter(DeclarationDataPresenter.class, DeclarationDataPresenter.MyView.class,
				DeclarationDataView.class, DeclarationDataPresenter.MyProxy.class);
        bindSingletonPresenterWidget(SourcesPresenter.class, SourcesPresenter.MyView.class, SourcesView.class);
        bindSingletonPresenterWidget(SubreportParamsPresenter.class, SubreportParamsPresenter.MyView.class, SubreportParamsView.class);
        bindSingletonPresenterWidget(DeclarationDeclarationFilesCommentsPresenter.class, DeclarationDeclarationFilesCommentsPresenter.MyView.class, DeclarationFilesCommentsView.class);
        bindSingletonPresenterWidget(NdflReferencesEditPresenter.class, NdflReferencesEditPresenter.MyView.class, NdflReferencesEditView.class);
        bindSingletonPresenterWidget(NdflReferencesSearchPresenter.class, NdflReferencesSearchPresenter.SearchView.class, NdflReferencesSearchView.class);
        bindSingletonPresenterWidget(MoveToCreatePresenter.class, MoveToCreatePresenter.MyView.class, MoveToCreateView.class);
	}

}