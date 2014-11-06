print ''
print '********************************************'
print '* Start application deploy'
print '********************************************'
print ''
print '--------------------------------------------'
print '- User data'

# application path
applicationPath = 'C:/taxaccounting-ear'+suffixForResources+'.ear'

print '--------------------------------'
print '- Initialization'

# application
applicationName = 'taxaccounting'+ suffixForResources
applicationContext = '/taxaccounting'+ suffixForResources +'/gwtapp'

# datasource info
dataSourceJndi               = 'jdbc/TaxAccDS'+ suffixForResources
migrationDataSourceJndi      = 'jdbc/TaxAccDS_MIGRATION'+ suffixForResources

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
SIBJMSLongAsyncConnectionFactoryJndi	= 'jms/longAsyncConnectionFactory'+ suffixForResources
SIBJMSConnectionFactoryJndi				= 'jms/transportConnectionFactory'+ suffixForResources
SIBJMSShortAsyncConnectionFactoryJndi	= 'jms/shortAsyncConnectionFactory'+ suffixForResources

SIBJMSActivationSpecJndi	= 'jms/transportAS'+ suffixForResources
SIBJMSQueueJndi				= 'jms/transportQueue'+ suffixForResources

SIBJMSShortAsyncActivationSpecJndi	= 'jms/shortAsyncAS'+ suffixForResources
SIBJMSShortAsyncQueueJndi			= 'jms/shortAsyncQueue'+ suffixForResources

SIBJMSLongAsyncActivationSpecJndi	= 'jms/longAsyncAS'+ suffixForResources
SIBJMSLongAsyncQueueJndi			= 'jms/longAsyncQueue'+ suffixForResources

# MQ messaging provider
MQActivationSpecJndi	= 'jms/transportMQ'+ suffixForResources
MQQueuesJndi			= 'jms/rateQueue'+ suffixForResources

# scheduler
taskSchedulerJndi ='sched/TaskScheduler'+ suffixForResources

# options
applicationOptions = [
	'-appname', applicationName,
	'-CtxRootForWebMod', [
		['gwtapp.war',	'gwtapp.war,WEB-INF/web.xml',	applicationContext]
	],
	'-MapResRefToEJB', [
		['gwtapp.war',	'',	'gwtapp.war,WEB-INF/web.xml',	'services/cache/aplana/taxaccounting/FormType',				'com.ibm.websphere.cache.DistributedMap',	formTypeJndi],
		['gwtapp.war',	'',	'gwtapp.war,WEB-INF/web.xml',	'services/cache/aplana/taxaccounting/FormTemplate',			'com.ibm.websphere.cache.DistributedMap',	formTemplateJndi],
		['gwtapp.war',	'',	'gwtapp.war,WEB-INF/web.xml',	'services/cache/aplana/taxaccounting/Department',			'com.ibm.websphere.cache.DistributedMap',	departmentJndi],
		['gwtapp.war',	'',	'gwtapp.war,WEB-INF/web.xml',	'services/cache/aplana/taxaccounting/User',					'com.ibm.websphere.cache.DistributedMap',	userCacheJndi],
		['gwtapp.war',	'',	'gwtapp.war,WEB-INF/web.xml',	'services/cache/aplana/taxaccounting/PermanentData',		'com.ibm.websphere.cache.DistributedMap',	permanentDataJndi],
		['gwtapp.war',	'',	'gwtapp.war,WEB-INF/web.xml',	'services/cache/aplana/taxaccounting/DeclarationType',		'com.ibm.websphere.cache.DistributedMap',	declarationTypeJndi],
		['gwtapp.war',	'',	'gwtapp.war,WEB-INF/web.xml',	'services/cache/aplana/taxaccounting/DeclarationTemplate',	'com.ibm.websphere.cache.DistributedMap',	declarationTemplateJndi],
		['gwtapp.war',	'',	'gwtapp.war,WEB-INF/web.xml',	'services/cache/aplana/taxaccounting/DataBlobsCache',		'com.ibm.websphere.cache.DistributedMap',	dataBlobsCacheJndi],
		['async-core',		'AsyncManagerBean',	'async-core.jar,META-INF/ejb-jar.xml',		'jms/longAsyncConnectionFactory',	'javax.jms.ConnectionFactory',	SIBJMSLongAsyncConnectionFactoryJndi],
		['migration-ejb',	'MigrationBean',	'migration-ejb.jar,META-INF/ejb-jar.xml',	'jms/transportConnectionFactory',	'javax.jms.ConnectionFactory',	SIBJMSConnectionFactoryJndi],
		['async-core',		'AsyncManagerBean',	'async-core.jar,META-INF/ejb-jar.xml',		'jms/shortAsyncConnectionFactory',	'javax.jms.ConnectionFactory',	SIBJMSShortAsyncConnectionFactoryJndi],
		['mdb',					'DataSourceHolderBean',		'mdb.jar,META-INF/ejb-jar.xml',				'jdbc/TaxAccDS_MIGRATION',	'javax.sql.DataSource',	migrationDataSourceJndi],
		['mdb',					'DataSourceHolderBean',		'mdb.jar,META-INF/ejb-jar.xml',				'jdbc/TaxAccDS',			'javax.sql.DataSource',	dataSourceJndi],
		['async-task.jar',		'DataSourceHolderBean',		'async-task.jar,META-INF/ejb-jar.xml',		'jdbc/TaxAccDS',			'javax.sql.DataSource',	dataSourceJndi],
		['audit-ejb',			'AuditDSHolderBean',		'audit-ejb.jar,META-INF/ejb-jar.xml',		'jdbc/TaxAccDS',			'javax.sql.DataSource',	dataSourceJndi],
		['department-ejb',		'DepartmentDSHolderBean',	'department-ejb.jar,META-INF/ejb-jar.xml',	'jdbc/TaxAccDS',			'javax.sql.DataSource',	dataSourceJndi],
		['gwtapp.war',			'',							'gwtapp.war,WEB-INF/web.xml',				'jdbc/TaxAccDS',			'javax.sql.DataSource',	dataSourceJndi],
		['scheduler-task.jar',	'DataSourceHolderBean',		'scheduler-task.jar,META-INF/ejb-jar.xml',	'jdbc/TaxAccDS',			'javax.sql.DataSource',	dataSourceJndi]
	],
	'-BindJndiForEJBMessageBinding', [
		['mdb',			'TransportMDB',		'mdb.jar,META-INF/ejb-jar.xml',			'',	SIBJMSActivationSpecJndi,			SIBJMSQueueJndi],
		['mdb',			'RateMDB',			'mdb.jar,META-INF/ejb-jar.xml',			'',	MQActivationSpecJndi,				MQQueuesJndi],
		['async-core',	'ShortAsyncMDB',	'async-core.jar,META-INF/ejb-jar.xml',	'',	SIBJMSShortAsyncActivationSpecJndi,	SIBJMSShortAsyncQueueJndi],
		['async-core',	'LongAsyncMDB',		'async-core.jar,META-INF/ejb-jar.xml',	'',	SIBJMSLongAsyncActivationSpecJndi,	SIBJMSLongAsyncQueueJndi]
	],
	'-MapResEnvRefToRes', [
		['scheduler-core',	'TaskManagerBean',	'scheduler-core.jar,META-INF/ejb-jar.xml',	'sched/TaskScheduler',	'com.ibm.websphere.scheduler.Scheduler',	taskSchedulerJndi]
	],
	'-MapMessageDestinationRefToEJB', [
		['migration-ejb',	'MigrationBean',	'migration-ejb.jar,META-INF/ejb-jar.xml',	'jms/transportQueue',	SIBJMSQueueJndi],
		['async-core',		'AsyncManagerBean',	'async-core.jar,META-INF/ejb-jar.xml',		'jms/shortAsyncQueue',	SIBJMSShortAsyncQueueJndi],
		['async-core',		'AsyncManagerBean',	'async-core.jar,META-INF/ejb-jar.xml',		'jms/longAsyncQueue',	SIBJMSLongAsyncQueueJndi]
	]
]

print '--------------------------------'
print '- Deploy applications'
AdminApp.install(applicationPath, applicationOptions)
AdminConfig.save()

print '--------------------------------'
print '* End script'
print '********************************'