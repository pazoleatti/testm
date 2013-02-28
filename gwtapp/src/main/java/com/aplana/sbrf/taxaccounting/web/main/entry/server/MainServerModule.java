
package com.aplana.sbrf.taxaccounting.web.main.entry.server;

import com.aplana.sbrf.taxaccounting.web.main.page.server.MainPageServerModule;
import com.aplana.sbrf.taxaccounting.web.module.admin.server.AdminServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationdata.server.DeclarationDataServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.server.DeclarationServerModule;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.server.DeclarationTemplateServerModule;
import com.aplana.sbrf.taxaccounting.web.module.formdata.server.FormDataServerModule;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.server.FormDataListServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.dictionarypicker.server.DictionaryPickerServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.menu.server.MainMenuServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.signin.server.SigninControlServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.version.server.ProjectVersionControlServerModule;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {MainHandlerModule.class,  FormDataListServerModule.class,
		FormDataServerModule.class, SigninControlServerModule.class, MainMenuServerModule.class, DictionaryPickerServerModule.class,
		AdminServerModule.class, DeclarationTemplateServerModule.class, DeclarationDataServerModule.class, MainPageServerModule.class,
		DeclarationServerModule.class,ProjectVersionControlServerModule.class})
@ComponentScan(basePackageClasses = MainServerModule.class)
public class MainServerModule {


}
