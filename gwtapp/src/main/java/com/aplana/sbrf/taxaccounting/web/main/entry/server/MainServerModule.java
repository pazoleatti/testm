package com.aplana.sbrf.taxaccounting.web.main.entry.server;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.aplana.sbrf.taxaccounting.web.module.formdata.server.FormDataServerModule;
import com.aplana.sbrf.taxaccounting.web.module.formdatalist.server.FormDataListServerModule;
import com.aplana.sbrf.taxaccounting.web.widget.signin.server.SigninControlServerModule;

@Configuration
@Import(value = {MainHandlerModule.class,  FormDataListServerModule.class,
		FormDataServerModule.class, SigninControlServerModule.class })
@ComponentScan(basePackageClasses = MainServerModule.class)
public class MainServerModule {


}
