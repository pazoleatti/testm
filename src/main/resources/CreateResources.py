print '********************************'
print '* Start create resources script'

print '--------------------------------'
print '- System init'

# get line separator
import java
lineSeparator = java.lang.System.getProperty('line.separator')

# server info
serverName             = 'server1'
resourceRootLocation   = '/Server:'+ serverName
resourceRootLocationId = AdminConfig.getid(resourceRootLocation)
print 'Found resource root location ID='+ resourceRootLocationId
nodeName               = AdminControl.getNode()
print 'Found node name='+ nodeName
cacheProviderId = AdminConfig.getid(resourceRootLocation +'/CacheProvider:CacheProvider/')
print 'Found cache provider ID='+ cacheProviderId

# prefix for resources
suffixForResources = '_test'

# database info
dataBaseHost        = 'nalog-db.aplana.local'
dataBasePort        = '1521'
dataBaseSvcName     = 'orcl.aplana.local'

# auth info
jaasAlias    = 'TAX'+ suffixForResources
jassUserId   = 'TAX'+ suffixForResources
jassUserPass = 'TAX'

# JDBC provider
jdbcProviderName = 'Oracle JDBC Driver'+ suffixForResources
jdbcDriverPath   = 'C:/ojdbc6.jar'
jdbcDriverClass  = 'oracle.jdbc.pool.OracleConnectionPoolDataSource'

# datasource info
dataSuorceName      = 'TAX Datasource'+ suffixForResources
dataSourceJndi      = 'jdbc/TaxAccDS'+ suffixForResources
dataSourceHelpClass = 'com.ibm.websphere.rsadapter.Oracle11gDataStoreHelper'
dataSourceUrl       = 'jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST='+ dataBaseHost +')(PORT='+ dataBasePort +'))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME='+ dataBaseSvcName +')))'

# scheduler
taskSchedulerName = 'TAX Scheduler'+ suffixForResources
taskSchedulerJndi = 'sched/TaskScheduler'+ suffixForResources

# cache instance
declarationTemplateName = 'TaxAccounting - DeclarationTemplate'+ suffixForResources
declarationTemplateJndi = 'services/cache/aplana/taxaccounting/DeclarationTemplate'+ suffixForResources
declarationTypeName     = 'TaxAccounting - DeclarationType'+ suffixForResources
declarationTypeJndi     = 'services/cache/aplana/taxaccounting/DeclarationType'+ suffixForResources
departmentName          = 'TaxAccounting - Department'+ suffixForResources
departmentJndi          = 'services/cache/aplana/taxaccounting/Department'+ suffixForResources
formTemplateName        = 'TaxAccounting - FormTemplate'+ suffixForResources
formTemplateJndi        = 'services/cache/aplana/taxaccounting/FormTemplate'+ suffixForResources
formTypeName            = 'TaxAccounting - FormType'+ suffixForResources
formTypeJndi            = 'services/cache/aplana/taxaccounting/FormType'+ suffixForResources
userCacheName           = 'TaxAccounting - User'+ suffixForResources
userCacheJndi           = 'services/cache/aplana/taxaccounting/User'+ suffixForResources
dataBlobsCacheName      = 'TaxAccounting - DataBlobsCache'+ suffixForResources
dataBlobsCacheJndi      = 'services/cache/aplana/taxaccounting/DataBlobsCache'+ suffixForResources
permanentDataName      = 'TaxAccounting - PermanentData'+ suffixForResources
permanentDataJndi      = 'services/cache/aplana/taxaccounting/PermanentData'+ suffixForResources

# service integration bus
SIBusName                   = 'TAX JMS Bus'+ suffixForResources
SIBDestinationName          = 'transportQueue'+ suffixForResources
SIBJMSConnectionFactoryName = 'TAX Connection factories'+ suffixForResources
SIBJMSConnectionFactoryJndi = 'jms/transportConnectionFactory'+ suffixForResources
SIBJMSQueueName             = 'TAX Queue'+ suffixForResources
SIBJMSQueueJndi             = 'jms/transportQueue'+ suffixForResources
SIBJMSActivationSpecName    = 'TAX Activation specifications'+ suffixForResources
SIBJMSActivationSpecJndi    = 'jms/transportAS'+ suffixForResources

print '--------------------------------'
print '- JAASAuthData'
jassAuthDataIds = AdminConfig.list('JAASAuthData').split(lineSeparator)
jassAuthDataNotFound = 1
for jassAuthDataId in jassAuthDataIds:
	jassAuthDataAlias = AdminConfig.showAttribute(jassAuthDataId, 'alias')
	if jassAuthDataAlias[-len(jaasAlias):] == jaasAlias:
		jassAuthDataNotFound = 0
		print 'Found existing JAASAuthData:'
		print 'jassAuthDataAlias='+ jassAuthDataAlias
		print 'jassAuthDataId='+ jassAuthDataId
		break
if jassAuthDataNotFound:
	print 'Initiated the creation of an JAASAuthData'
	print 'id='+ AdminConfig.create('JAASAuthData', AdminConfig.getid('/Security:/'), [['alias', jaasAlias], ['userId', jassUserId], ['password', jassUserPass]])
	AdminConfig.save()
	print 'Configuration is saved.'

print '--------------------------------'
print '- JDBC Provider'
jdbcProviderId = AdminConfig.getid(resourceRootLocation +'/JDBCProvider:'+ jdbcProviderName +'/')
if len(jdbcProviderId):
	print 'Found existing JDBC Provider id='+ jdbcProviderId
else:
	print "Initiated the creation of an JDBC Provider"
	jdbcProviderId = AdminConfig.create('JDBCProvider', resourceRootLocationId, [['classpath', jdbcDriverPath], ['implementationClassName', jdbcDriverClass], ['name', jdbcProviderName]])
	print 'id='+ jdbcProviderId
	AdminConfig.save()
	print 'Configuration is saved.'

print '--------------------------------'
print '- Data source'
dataSuorceId = AdminConfig.getid(resourceRootLocation +'/JDBCProvider:'+ jdbcProviderName +'/DataSource:'+ dataSuorceName +'/')
if len(dataSuorceId):
	print 'Found existing data source id='+ dataSuorceId
else:
	print 'Initiated the creation of an data source'
	dataSuorceId = AdminConfig.create('DataSource', jdbcProviderId, [['name', dataSuorceName], ['jndiName', dataSourceJndi], ['datasourceHelperClassname', dataSourceHelpClass], ['authDataAlias', jaasAlias]])
	print 'id='+ dataSuorceId
	AdminConfig.create('J2EEResourceProperty', AdminConfig.create('J2EEResourcePropertySet', dataSuorceId, []),	[['name', 'URL'], ['type', 'java.lang.String'], ['value', dataSourceUrl]])
	print 'Parameters are set'
	AdminConfig.save()
	print 'Configuration is saved.'

print '--------------------------------'
print '- Scheduler'
taskSchedulerId = AdminConfig.getid('/SchedulerConfiguration:'+ taskSchedulerName +'/')
if len(taskSchedulerId):
	print 'Found existing scheduler id='+ taskSchedulerId
else:
	taskSchedulerProviderId = AdminConfig.getid(resourceRootLocation +'/SchedulerProvider:SchedulerProvider/')
	print 'Found scheduler provider id='+ taskSchedulerProviderId
	workManagerJndi = AdminConfig.showAttribute(AdminConfig.getid(resourceRootLocation +'/WorkManagerProvider:/WorkManagerInfo:DefaultWorkManager/'), 'jndiName')
	print 'Found work manager provider id='+ workManagerJndi
	print 'Initiated the creation of an scheduler'
	taskSchedulerId = AdminConfig.create('SchedulerConfiguration', taskSchedulerProviderId, [['name', taskSchedulerName], ['datasourceJNDIName', dataSourceJndi], ['datasourceAlias', jaasAlias], ['jndiName', taskSchedulerJndi], ['pollInterval', 30], ['tablePrefix', 'sched_'], ['useAdminRoles', 'false'], ['workManagerInfoJNDIName', workManagerJndi]])
	print 'id='+ taskSchedulerId
	AdminConfig.save()
	print 'Configuration is saved.'
taskSchedulerHelperName = AdminControl.queryNames('WebSphere:*,type=WASSchedulerCfgHelper')
try:
	AdminControl.invoke(taskSchedulerHelperName, 'verifyTables', taskSchedulerId)
	print 'Tables verified successfully.'
except:
	print 'Error verifying tables. Trying to create...'
	try:
		AdminControl.invoke(taskSchedulerHelperName, 'createTables', taskSchedulerId)
		print 'Successfully created the tables.'
	except:
		print 'Error creating tables.'

print '--------------------------------'
print '- Cache instance'
declarationTemplateId = AdminConfig.getid('/ObjectCacheInstance:'+ declarationTemplateName +'/')
if len(declarationTemplateId):
	print 'Found existing object cache instance ='+ declarationTemplateId
else:
	print 'Initiated the creation of an object cache instance '+ declarationTemplateName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', declarationTemplateName], ['jndiName', declarationTemplateJndi], ['cacheSize', 100], ['defaultPriority', 1], ['disableDependencyId', 1]])
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
declarationTypeId = AdminConfig.getid('/ObjectCacheInstance:'+ declarationTypeName +'/')
if len(declarationTypeId):
	print 'Found existing object cache instance ='+ declarationTypeId
else:
	print 'Initiated the creation of an object cache instance '+ declarationTypeName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', declarationTypeName], ['jndiName', declarationTypeJndi], ['cacheSize', 100], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
departmentId = AdminConfig.getid('/ObjectCacheInstance:'+ departmentName +'/')
if len(departmentId):
	print 'Found existing object cache instance ='+ departmentId
else:
	print 'Initiated the creation of an object cache instance '+ departmentName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', departmentName], ['jndiName', departmentJndi], ['cacheSize', 200], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
formTemplateId = AdminConfig.getid('/ObjectCacheInstance:'+ formTemplateName +'/')
if len(formTemplateId):
	print 'Found existing object cache instance ='+ formTemplateId
else:
	print 'Initiated the creation of an object cache instance '+ formTemplateName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', formTemplateName], ['jndiName', formTemplateJndi], ['cacheSize', 200], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
formTypeId = AdminConfig.getid('/ObjectCacheInstance:'+ formTypeName +'/')
if len(formTypeId):
	print 'Found existing object cache instance ='+ formTypeId
else:
	print 'Initiated the creation of an object cache instance '+ formTypeName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', formTypeName], ['jndiName', formTypeJndi], ['cacheSize', 200], ['defaultPriority', 1], ['disableDependencyId', 'false']])
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
userCacheId = AdminConfig.getid('/ObjectCacheInstance:'+ userCacheName +'/')
if len(userCacheId):
	print 'Found existing object cache instance ='+ userCacheId
else:
	print 'Initiated the creation of an object cache instance '+ userCacheName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', userCacheName], ['jndiName', userCacheJndi], ['cacheSize', 2000], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
dataBlobsCacheId = AdminConfig.getid('/ObjectCacheInstance:'+ dataBlobsCacheName +'/')
if len(dataBlobsCacheId):
	print 'Found existing object cache instance ='+ dataBlobsCacheId
else:
	print 'Initiated the creation of an object cache instance '+ dataBlobsCacheName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', dataBlobsCacheName], ['jndiName', dataBlobsCacheJndi], ['cacheSize', 100], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'
	
print '________________________________'
permanentDataId = AdminConfig.getid('/ObjectCacheInstance:'+ permanentDataName +'/')
if len(permanentDataId):
	print 'Found existing object cache instance ='+ permanentDataId
else:
	print 'Initiated the creation of an object cache instance '+ permanentDataName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', permanentDataName], ['jndiName', permanentDataJndi], ['cacheSize', 100], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'

print '--------------------------------'
print '- Service integration bus'
SIBusesId = AdminTask.listSIBuses().split(lineSeparator)
SIBusNotFound = 1
for SIBusId in SIBusesId:
	if SIBusId[1:1+len(SIBusName)] == SIBusName:
		SIBusNotFound = 0
		print 'Found existing service integration bus id='+ SIBusId
		break
if SIBusNotFound:
	print 'Initiated the creation of an service integration bus'
	print 'id='+ AdminTask.createSIBus('[-bus "'+ SIBusName +'" -busSecurity false]')
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
SIBusMemberId = AdminTask.listSIBusMembers('[-bus "'+ SIBusName +'"]')
if len(SIBusMemberId):
	print 'Found existing service integration bus members id='+ SIBusMemberId
else:
	print 'Initiated the creation of an service integration bus member '+ nodeName +':'+ serverName
	AdminTask.addSIBusMember('[-bus "'+ SIBusName +'" -node "'+ nodeName +'" -server "'+ serverName +'" -fileStore]')
	print 'id='+ AdminTask.listSIBusMembers('[-bus "'+ SIBusName +'"]')
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
SIBDestinationsId = AdminTask.listSIBDestinations('-bus "'+ SIBusName +'" -type Queue').split(lineSeparator)
SIBDestinationNotFound = 1
for SIBDestinationId in SIBDestinationsId:
	SIBDestinationIdentifier = AdminConfig.showAttribute(SIBDestinationId, 'identifier')
	if SIBDestinationIdentifier[-len(SIBDestinationName):] == SIBDestinationName:
		SIBDestinationNotFound = 0
		print 'Found existing service integration bus destinations:'
		print 'SIBDestinationIdentifier='+ SIBDestinationIdentifier
		print 'SIBDestinationId='+ SIBDestinationId
		break
if SIBDestinationNotFound:
	print 'Initiated the creation of an service integration bus destinations'
	print 'id='+ AdminTask.createSIBDestination('[-bus "'+ SIBusName +'" -name "'+ SIBDestinationName +'" -type Queue -node "'+ nodeName +'" -server "'+ serverName +'"]')
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
SIBJMSConnectionFactoriesId = AdminTask.listSIBJMSConnectionFactories(resourceRootLocationId).split(lineSeparator)
SIBJMSConnectionFactoryNotFound = 1
for SIBJMSConnectionFactoryId in SIBJMSConnectionFactoriesId:
	if SIBJMSConnectionFactoryId[1:1+len(SIBJMSConnectionFactoryName)] == SIBJMSConnectionFactoryName:
		SIBJMSConnectionFactoryNotFound = 0
		print 'Found existing service integration bus connection factory id='+ SIBJMSConnectionFactoryId
		break
if SIBJMSConnectionFactoryNotFound:
	print 'Initiated the creation of an service integration bus connection factory'
	print 'id='+ AdminTask.createSIBJMSConnectionFactory(resourceRootLocationId, ['-name', SIBJMSConnectionFactoryName, '-jndiName', SIBJMSConnectionFactoryJndi, '-busName', SIBusName])
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
SIBJMSQueuesId = AdminTask.listSIBJMSQueues(resourceRootLocationId).split(lineSeparator)
SIBJMSQueueNotFound = 1
for SIBJMSQueueId in SIBJMSQueuesId:
	if SIBJMSQueueId[1:1+len(SIBJMSQueueName)] == SIBJMSQueueName:
		SIBJMSQueueNotFound = 0
		print 'Found existing service integration bus queue id='+ SIBJMSQueueId
		break
if SIBJMSQueueNotFound:
	print 'Initiated the creation of an service integration bus queue'
	print 'id='+ AdminTask.createSIBJMSQueue(resourceRootLocationId, ['-name', SIBJMSQueueName, '-jndiName', SIBJMSQueueJndi, '-queueName', SIBDestinationName, '-busName', SIBusName])
	AdminConfig.save()
	print 'Configuration is saved.'

print '________________________________'
SIBJMSActivationSpecsId = AdminTask.listSIBJMSActivationSpecs(resourceRootLocationId).split(lineSeparator)
SIBJMSActivationSpecNotFound = 1
for SIBJMSActivationSpecId in SIBJMSActivationSpecsId:
	if SIBJMSActivationSpecId[1:1+len(SIBJMSActivationSpecName)] == SIBJMSActivationSpecName:
		SIBJMSActivationSpecNotFound = 0
		print 'Found existing service integration bus activation specifications id='+ SIBJMSActivationSpecId
		break
if SIBJMSActivationSpecNotFound:
	print 'Initiated the creation of an service integration bus activation specifications'
	print 'id='+ AdminTask.createSIBJMSActivationSpec(resourceRootLocationId, ['-name', SIBJMSActivationSpecName, '-jndiName', SIBJMSActivationSpecJndi, '-destinationJndiName', SIBJMSQueueJndi, '-busName', SIBusName, '-maxBatchSize', 1, '-maxConcurrency', 1]) 
	AdminConfig.save()
	print 'Configuration is saved.'

print '--------------------------------'
print '* End script'
print '********************************'