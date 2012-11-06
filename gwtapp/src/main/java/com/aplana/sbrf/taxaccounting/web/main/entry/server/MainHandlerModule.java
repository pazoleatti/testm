package com.aplana.sbrf.taxaccounting.web.main.entry.server;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.aplana.sbrf.taxaccounting.web.module.formdata.server.GetFormDataHandler;
import com.aplana.sbrf.taxaccounting.web.module.formdata.server.SaveFormDataHandler;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.GetFormData;
import com.aplana.sbrf.taxaccounting.web.module.formdata.shared.SaveFormDataAction;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.server.GetFormDataListHandler;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.shared.GetFormDataList;
import com.aplana.sbrf.taxaccounting.web.widget.signin.server.GetUserInfoActionHandler;
import com.aplana.sbrf.taxaccounting.web.widget.signin.shared.GetUserInfoAction;
import com.gwtplatform.dispatch.server.spring.DispatchModule;
import com.gwtplatform.dispatch.server.spring.HandlerModule;
import com.gwtplatform.dispatch.server.spring.configuration.DefaultModule;

@Configuration
@Import(value = { DefaultModule.class })
@ComponentScan(basePackageClasses = DispatchModule.class)
public class MainHandlerModule extends HandlerModule {

	protected void configureHandlers() {
		bindHandler(GetFormDataList.class, GetFormDataListHandler.class);
		bindHandler(GetFormData.class, GetFormDataHandler.class);
		bindHandler(SaveFormDataAction.class, SaveFormDataHandler.class);
		// TODO: Убрать отсюда
		bindHandler(GetUserInfoAction.class, GetUserInfoActionHandler.class);
	}
}