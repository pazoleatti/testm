package com.aplana.sbrf.taxaccounting.web.main.entry.server;

import com.aplana.sbrf.taxaccounting.web.module.audit.server.AuditFormServerModule;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatements.server.BookerStatementsServerModule;
import com.aplana.sbrf.taxaccounting.web.module.bookerstatementsdata.server.BookerStatementsDataServerModule;
import com.aplana.sbrf.taxaccounting.web.module.configuration.server.ConfigurationServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.server.DeclarationVersionListServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.server.DeclarationDataServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.server.DeclarationServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server.DeclarationTemplateServerModule;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server.DepartmentConfigServerModule;
import com.aplana.sbrf.taxaccounting.web.module.formdata.server.FormDataServerModule;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.server.FormDataListServerModule;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.server.AdminServerModule;
import com.aplana.sbrf.taxaccounting.web.module.formtemplateversionlist.server.TemplateVersionListServerModule;
import com.aplana.sbrf.taxaccounting.web.module.members.server.MembersServerModule;
import com.aplana.sbrf.taxaccounting.web.module.migration.server.MigrationServerModule;
import com.aplana.sbrf.taxaccounting.web.module.periods.server.PeriodsServerModule;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.server.RefBookDataServerModule;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.server.RefBookListServerModule;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.server.SchedulerServerModule;
import com.aplana.sbrf.taxaccounting.web.module.sources.server.SourcesServerModule;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server.TaxFormNominationServerModule;
import com.aplana.sbrf.taxaccounting.web.module.testpage.server.TestPageServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.history.server.HistoryServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.historytemplatechanges.server.VersionHistoryServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.server.LogAreaModule;
import com.aplana.sbrf.taxaccounting.web.widget.menu.server.MainMenuServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.refbookmultipicker.server.RefBookMultiPickerServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.signin.server.SigninControlServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.version.server.ProjectVersionControlServerModule;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {MainHandlerModule.class, FormDataListServerModule.class,
        FormDataServerModule.class, SigninControlServerModule.class, MainMenuServerModule.class,
        AdminServerModule.class, DeclarationTemplateServerModule.class, DeclarationDataServerModule.class,
        TaxFormNominationServerModule.class, DeclarationServerModule.class, ProjectVersionControlServerModule.class,
        HistoryServerModule.class, TemplateVersionListServerModule.class, MembersServerModule.class, ConfigurationServerModule.class,
        AuditFormServerModule.class, PeriodsServerModule.class, LogAreaModule.class, SourcesServerModule.class,
        DepartmentConfigServerModule.class, BookerStatementsServerModule.class, BookerStatementsDataServerModule.class,
        RefBookListServerModule.class, RefBookDataServerModule.class, MigrationServerModule.class, SchedulerServerModule.class,
        TestPageServerModule.class, DeclarationVersionListServerModule.class,
        VersionHistoryServerModule.class, RefBookMultiPickerServerModule.class})
@ComponentScan(basePackageClasses = MainServerModule.class)
@EnableAspectJAutoProxy
public class MainServerModule {
}
