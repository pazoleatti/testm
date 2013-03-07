package com.aplana.sbrf.taxaccounting.web.main.entry.client;

import com.aplana.sbrf.taxaccounting.web.module.declarationdata.client.DeclarationDataGinjector;
import com.aplana.sbrf.taxaccounting.web.module.declarationlist.client.DeclarationListGinjector;
import com.aplana.sbrf.taxaccounting.web.module.declarationtemplate.client.DeclarationTemplateGinjector;
import com.aplana.sbrf.taxaccounting.web.module.error.client.ErrorGinjector;
import com.aplana.sbrf.taxaccounting.web.module.formdata.client.FormDataGinjector;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.client.FormDataListGinjector;
import com.aplana.sbrf.taxaccounting.web.module.formtemplate.client.gin.AdminGinjector;
import com.aplana.sbrf.taxaccounting.web.module.home.client.HomeGinjector;
import com.aplana.sbrf.taxaccounting.web.widget.logarea.client.LogAreaGinjector;
import com.aplana.sbrf.taxaccounting.web.widget.menu.client.MainMenuClientGinjector;
import com.aplana.sbrf.taxaccounting.web.widget.signin.client.SignInClientGinjector;
import com.aplana.sbrf.taxaccounting.web.widget.version.client.ProjectVersionGinjector;
import com.google.gwt.inject.client.GinModules;

@GinModules({ ClientModule.class })
public interface ClientGinjector extends ClientGinjectorBase,
		SignInClientGinjector,
		FormDataListGinjector,
		FormDataGinjector,
		HomeGinjector,
		MainMenuClientGinjector,
		AdminGinjector,
		DeclarationTemplateGinjector,
		ErrorGinjector,
		DeclarationListGinjector,
		DeclarationDataGinjector,
		LogAreaGinjector,
		ProjectVersionGinjector
{

}