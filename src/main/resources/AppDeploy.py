print '********************************'
print '* Start application deploy script'

print '--------------------------------'
print '- Initialization'

# prefix for resources
suffixForResources = '_test'

# application
applicationPath = 'c:/taxaccounting.ear'
applicationName = 'taxaccounting'+ suffixForResources
applicationContext = '/taxaccounting'+ suffixForResources +'/gwtapp'

# datasource info
dataSourceJndi      = 'jdbc/TaxAccDS'+ suffixForResources

# cache instance
declarationTemplateJndi = 'services/cache/aplana/taxaccounting/DeclarationTemplate'+ suffixForResources
declarationTypeJndi     = 'services/cache/aplana/taxaccounting/DeclarationType'+ suffixForResources
departmentJndi          = 'services/cache/aplana/taxaccounting/Department'+ suffixForResources
formTemplateJndi        = 'services/cache/aplana/taxaccounting/FormTemplate'+ suffixForResources
formTypeJndi            = 'services/cache/aplana/taxaccounting/FormType'+ suffixForResources
userCacheJndi           = 'services/cache/aplana/taxaccounting/User'+ suffixForResources
dataBlobsCacheJndi      = 'services/cache/aplana/taxaccounting/DataBlobsCache'+ suffixForResources
permanentDataJndi       = 'services/cache/aplana/taxaccounting/PermanentData'+ suffixForResources

# service integration bus
SIBJMSConnectionFactoryJndi = 'jms/transportConnectionFactory'+ suffixForResources
SIBJMSQueueJndi             = 'jms/transportQueue'+ suffixForResources
SIBJMSActivationSpecJndi    = 'jms/transportAS'+ suffixForResources

# scheduler
taskSchedulerJndi ='sched/TaskScheduler'+ suffixForResources

print '--------------------------------'
print '- Deploy applications'
AdminApp.install(applicationPath, [\
	'-appname', applicationName,\
	'-CtxRootForWebMod', [['gwtapp.war', 'gwtapp.war,WEB-INF/web.xml', applicationContext]],\
	'-MapResRefToEJB', [\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'jdbc/TaxAccDS', 'javax.sql.DataSource', dataSourceJndi],\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'jms/transportConnectionFactory', 'javax.jms.ConnectionFactory', SIBJMSConnectionFactoryJndi],\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'jms/transportQueue', 'javax.jms.Queue', SIBJMSQueueJndi],\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'services/cache/aplana/taxaccounting/FormType', 'com.ibm.websphere.cache.DistributedMap', formTypeJndi],\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'services/cache/aplana/taxaccounting/FormTemplate', 'com.ibm.websphere.cache.DistributedMap', formTemplateJndi],\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'services/cache/aplana/taxaccounting/Department', 'com.ibm.websphere.cache.DistributedMap', departmentJndi],\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'services/cache/aplana/taxaccounting/User', 'com.ibm.websphere.cache.DistributedMap', userCacheJndi],\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'services/cache/aplana/taxaccounting/DeclarationType', 'com.ibm.websphere.cache.DistributedMap', declarationTypeJndi],\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'services/cache/aplana/taxaccounting/DeclarationTemplate', 'com.ibm.websphere.cache.DistributedMap', declarationTemplateJndi],\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'services/cache/aplana/taxaccounting/DataBlobsCache', 'com.ibm.websphere.cache.DistributedMap', dataBlobsCacheJndi]\
		['gwtapp.war', '', 'gwtapp.war,WEB-INF/web.xml', 'services/cache/aplana/taxaccounting/PermanentData', 'com.ibm.websphere.cache.DistributedMap', permanentDataJndi]\
	],\
	'-BindJndiForEJBMessageBinding', [\
		['mdb', 'TransportMDB', 'mdb.jar,META-INF/ejb-jar.xml', '', SIBJMSActivationSpecJndi, SIBJMSQueueJndi],\
		['scheduler-core', 'CommandListenerMDB', 'scheduler-core.jar,META-INF/ejb-jar.xml', '', SIBJMSActivationSpecJndi, SIBJMSQueueJndi],\
	],\
	'-MapResEnvRefToRes', [\
		['scheduler-core', 'TaskManagerBean', 'scheduler-core.jar,META-INF/ejb-jar.xml', 'sched/TaskScheduler', 'com.ibm.websphere.scheduler.Scheduler', taskSchedulerJndi],\
	]\
])
AdminConfig.save()

print '--------------------------------'
print '* End script'
print '********************************'