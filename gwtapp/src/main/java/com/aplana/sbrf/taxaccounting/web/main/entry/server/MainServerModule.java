package com.aplana.sbrf.taxaccounting.web.main.entry.server;

import com.aplana.sbrf.taxaccounting.web.module.audit.server.AuditFormServerModule;
import com.aplana.sbrf.taxaccounting.web.module.commonparameter.server.CommonParameterServerModule;
import com.aplana.sbrf.taxaccounting.web.module.configuration.server.ConfigurationServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.server.DeclarationDataServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.server.DeclarationServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server.DeclarationTemplateServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationversionlist.server.DeclarationVersionListServerModule;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.server.DepartmentConfigServerModule;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfigproperty.server.DepartmentConfigPropertyServerModule;
import com.aplana.sbrf.taxaccounting.web.module.lock.server.LockServerModule;
import com.aplana.sbrf.taxaccounting.web.module.members.server.MembersServerModule;
import com.aplana.sbrf.taxaccounting.web.module.periods.server.PeriodsServerModule;
import com.aplana.sbrf.taxaccounting.web.module.refbookdata.server.RefBookDataServerModule;
import com.aplana.sbrf.taxaccounting.web.module.refbooklist.server.RefBookListServerModule;
import com.aplana.sbrf.taxaccounting.web.module.scheduler.server.SchedulerServerModule;
import com.aplana.sbrf.taxaccounting.web.module.scriptexecution.server.ScriptExecutionServerModule;
import com.aplana.sbrf.taxaccounting.web.module.scriptsimport.server.ScriptsImportServerModule;
import com.aplana.sbrf.taxaccounting.web.module.sources.server.SourcesServerModule;
import com.aplana.sbrf.taxaccounting.web.module.taxformnomination.server.TaxFormNominationServerModule;
import com.aplana.sbrf.taxaccounting.web.module.testpage.server.TestPageServerModule;
import com.aplana.sbrf.taxaccounting.web.module.uploadtransportdata.server.UploadTransportDataServerModule;
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
@Import(value = {MainHandlerModule.class, SigninControlServerModule.class, MainMenuServerModule.class,
        DeclarationTemplateServerModule.class, DeclarationDataServerModule.class,
        TaxFormNominationServerModule.class, DeclarationServerModule.class, ProjectVersionControlServerModule.class,
        HistoryServerModule.class, MembersServerModule.class,
        ConfigurationServerModule.class, AuditFormServerModule.class, PeriodsServerModule.class, LogAreaModule.class,
        SourcesServerModule.class, DepartmentConfigServerModule.class, DepartmentConfigPropertyServerModule.class,
        RefBookListServerModule.class, RefBookDataServerModule.class, SchedulerServerModule.class,
        TestPageServerModule.class, DeclarationVersionListServerModule.class, VersionHistoryServerModule.class,
        RefBookMultiPickerServerModule.class, ScriptExecutionServerModule.class, ScriptsImportServerModule.class,
        UploadTransportDataServerModule.class, LockServerModule.class, CommonParameterServerModule.class})
@ComponentScan(basePackageClasses = MainServerModule.class)
@EnableAspectJAutoProxy
public class MainServerModule {
}
