package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.aplana.sbrf.taxaccounting.web.main.page.client.MainPagePresenter;
import com.aplana.sbrf.taxaccounting.web.main.page.client.MainPageView;
import com.aplana.sbrf.taxaccounting.web.main.page.client.MessageDialogPresenter;
import com.aplana.sbrf.taxaccounting.web.main.page.client.MessageDialogView;
import com.aplana.sbrf.taxaccounting.web.module.about.client.AboutModule;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.client.CommonParameterModule;
import com.aplana.sbrf.taxaccounting.web.module.configuration.client.ConfigurationModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.changestatused.ChangeStatusEDClientModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.client.DeclarationVersionListModule;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client.DepartmentConfigModule;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.client.DepartmentConfigPropertyModule;
import com.aplana.sbrf.taxaccounting.web.module.error.client.ErrorModule;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeModule;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeNameTokens;
import com.aplana.sbrf.taxaccounting.web.module.periods.client.PeriodsModule;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.client.RefBookDataModule;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.client.RefBookListModule;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.client.SchedulerModule;
import com.aplana.sbrf.taxaccounting.web.module.scriptexecution.client.ScriptExecutionModule;
import com.aplana.sbrf.taxaccounting.web.module.scriptsimport.client.ScriptsImportModule;
import com.aplana.sbrf.taxaccounting.web.module.sources.client.SourcesModule;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.client.TaxFormNominationModule;
import com.aplana.sbrf.taxaccounting.web.module.testpage.client.TestPageModule;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.client.UploadTransportDataModule;
import com.aplana.sbrf.taxaccounting.web.widget.history.client.HistoryClientModule;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.client.LogAreaClientModule;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.MainMenuClientModule;
import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInClientModule;
import com.aplana.sbrf.taxaccounting.web.widget.version.client.ProjectVersionModule;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;
import com.gwtplatform.dispatch.client.gin.DispatchAsyncModule;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.proxy.ParameterTokenFormatter;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.TokenFormatter;

public class ClientModule extends AbstractPresenterModule {
	@Override
	protected void configure() {
		bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
		bind(TokenFormatter.class).to(ParameterTokenFormatter.class).in(Singleton.class);
		bind(TaRootPresenter.class).asEagerSingleton();
		bind(PlaceManager.class).to(TaPlaceManagerImpl.class).in(Singleton.class);

		install(new DispatchAsyncModule());

		requestStaticInjection(EventBus.class);

		bindConstant().annotatedWith(DefaultPlace.class).to(HomeNameTokens.homePage);

		bindPresenter(MainPagePresenter.class, MainPagePresenter.MyView.class,
				MainPageView.class, MainPagePresenter.MyProxy.class);

		bindSingletonPresenterWidget(MessageDialogPresenter.class,
				MessageDialogPresenter.MyView.class, MessageDialogView.class);

		install(new HomeModule());
		install(new AboutModule());
		install(new SignInClientModule());
		install(new MainMenuClientModule());
		install(new DeclarationTemplateModule());
		install(new DeclarationDataModule());
		install(new ErrorModule());
		install(new DeclarationListModule());
		install(new LogAreaClientModule());
		install(new HistoryClientModule());
		install(new ProjectVersionModule());
        install(new ConfigurationModule());
		install(new PeriodsModule());
		install(new SourcesModule());
        install(new DepartmentConfigModule());
        install(new DepartmentConfigPropertyModule());
        install(new RefBookListModule());
        install(new TaxFormNominationModule());
		install(new RefBookDataModule());
        install(new SchedulerModule());
        install(new TestPageModule());
        install(new DeclarationVersionListModule());
        install(new ScriptExecutionModule());
        install(new ScriptsImportModule());
        install(new UploadTransportDataModule());
        install(new CommonParameterModule());
        install(new ChangeStatusEDClientModule());
	}
}