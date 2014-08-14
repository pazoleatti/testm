print ''
print '********************************************'
print '* Start create JDBC resources and J2C script'
print '********************************************'
print ''
print '- Load settings'

import settings

print ''
print '--------------------------------------------'
print '- System init'

# get line separator
import java
lineSeparator = java.lang.System.getProperty('line.separator')

# server info
resourceRootLocation   = '/Server:'+ settings.serverName
resourceRootLocationId = AdminConfig.getid(resourceRootLocation)
print 'Found resource root location ID = '+ resourceRootLocationId
nodeName               = AdminControl.getNode()
print 'Found node name = '+ nodeName
scope = 'Node='+nodeName+',Server='+settings.serverName
print 'Scope="'+scope+'"'

# auth alias
jaasAlias             = 'TAX'+ settings.suffixForResources
migrationJaasAlias    = 'TAX_MIGRATION'

print ''
print '--------------------------------------------'
print '- JAASAuthData'

jassAuthDataIds = AdminConfig.list('JAASAuthData').split(lineSeparator)
jassAuthDataNotFound = 1
if jassAuthDataIds[0] != '':
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
	print 'id='+ AdminConfig.create('JAASAuthData', AdminConfig.getid('/Security:/'), [['alias', jaasAlias], ['userId', settings.jassUserId], ['password', settings.jassUserPass]])
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'
jassAuthDataIds = AdminConfig.list('JAASAuthData').split(lineSeparator)
jassAuthDataNotFound = 1
if jassAuthDataIds[0] != '':
	for jassAuthDataId in jassAuthDataIds:
		jassAuthDataAlias = AdminConfig.showAttribute(jassAuthDataId, 'alias')
		if jassAuthDataAlias[-len(migrationJaasAlias):] == migrationJaasAlias:
			jassAuthDataNotFound = 0
			print 'Found existing JAASAuthData:'
			print 'jassAuthDataAlias='+ jassAuthDataAlias
			print 'jassAuthDataId='+ jassAuthDataId
			break
if jassAuthDataNotFound:
	print 'Initiated the creation of an JAASAuthData'
	print 'id='+ AdminConfig.create('JAASAuthData', AdminConfig.getid('/Security:/'), [['alias', migrationJaasAlias], ['userId', settings.migrationJassUserId], ['password', settings.migrationJassUserPass]])
	AdminConfig.save()
	print 'Configuration is saved.'


# JDBC provider
jdbcProviderName = 'Oracle JDBC Driver (XA)'+ settings.suffixForResources


print ''
print '--------------------------------------------'
print '- JDBC Provider'
jdbcProviderId = AdminConfig.getid(resourceRootLocation +'/JDBCProvider:'+ jdbcProviderName +'/')
if len(jdbcProviderId):
	print 'Found existing JDBC Provider id='+ jdbcProviderId
else:
	print "Initiated the creation of an JDBC Provider"
	jdbcProviderId = AdminTask.createJDBCProvider(['-scope', scope, '-databaseType', 'Oracle' ,'-providerType', 'Oracle JDBC Driver' ,'-implementationType', 'XA data source', '-name', jdbcProviderName, '-description', "Oracle JDBC Driver (XA)", '-classpath', settings.jdbcDriverPath, '-nativePath', "" ])
	print 'id='+ jdbcProviderId
	AdminConfig.save()
	print 'Configuration is saved.'	
	
# datasource info
dataSuorceName               = 'TAX Datasource'+ settings.suffixForResources
dataSourceJndi               = 'jdbc/TaxAccDS'+ settings.suffixForResources
dataSourceHelpClass          = 'com.ibm.websphere.rsadapter.Oracle11gDataStoreHelper'
dataSourceUrl                = 'jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST='+ settings.dataBaseHost +')(PORT='+ settings.dataBasePort +'))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME='+ settings.dataBaseSvcName +')))'
migrationDataSuorceName      = 'TAX Datasource Migration'+ settings.suffixForResources
migrationDataSourceJndi      = 'jdbc/TaxAccDS_MIGRATION'+ settings.suffixForResources
migrationDataSourceHelpClass = 'com.ibm.websphere.rsadapter.Oracle11gDataStoreHelper'
migrationDataSourceUrl       = 'jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST='+ settings.migrationDataBaseHost +')(PORT='+ settings.migrationDataBasePort +'))(CONNECT_DATA=(SERVER=DEDICATED)(SERVICE_NAME='+ settings.migrationDataBaseSvcName +')))'

print ''
print '--------------------------------------------'
print '- Data source'
dataSuorceId = AdminConfig.getid(resourceRootLocation +'/JDBCProvider:'+ jdbcProviderName +'/DataSource:'+ dataSuorceName +'/')
if len(dataSuorceId):
	print 'Found existing data source id='+ dataSuorceId
else:
	print 'Initiated the creation of an data source'
	dataSuorceId = AdminTask.createDatasource(jdbcProviderId, ['-name', dataSuorceName, '-jndiName', dataSourceJndi, '-dataStoreHelperClassName', dataSourceHelpClass, '-containerManagedPersistence', 'true', '-componentManagedAuthenticationAlias', jaasAlias, '-xaRecoveryAuthAlias', jaasAlias, '-configureResourceProperties', [['URL', 'java.lang.String', dataSourceUrl]]])
	print 'id='+ dataSuorceId
	AdminConfig.create('J2EEResourceProperty', AdminConfig.showAttribute(dataSuorceId, 'propertySet'), [['name', 'connectionProperties'], ['type', 'java.lang.String'], ['value', 'defaultRowPrefetch=1000']])
	AdminConfig.create('MappingModule', dataSuorceId, [['authDataAlias', jaasAlias], ['mappingConfigAlias', '']])
	AdminConfig.modify(dataSuorceId, [['authDataAlias', jaasAlias], ['xaRecoveryAuthAlias', jaasAlias]])
	print 'Parameters are set'
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'	
migrationDataSuorceId = AdminConfig.getid(resourceRootLocation +'/JDBCProvider:'+ jdbcProviderName +'/DataSource:'+ migrationDataSuorceName +'/')
if len(migrationDataSuorceId):
	print 'Found existing data source id='+ migrationDataSuorceId
else:
	print 'Initiated the creation of an data source'
	migrationDataSuorceId = AdminTask.createDatasource(jdbcProviderId, ['-name', migrationDataSuorceName, '-jndiName', migrationDataSourceJndi, '-dataStoreHelperClassName', migrationDataSourceHelpClass, '-containerManagedPersistence', 'true', '-componentManagedAuthenticationAlias', migrationJaasAlias, '-xaRecoveryAuthAlias', migrationJaasAlias, '-configureResourceProperties', [['URL', 'java.lang.String', migrationDataSourceUrl]]])
	print 'id='+ migrationDataSuorceId
	AdminConfig.create('MappingModule', migrationDataSuorceId, [['authDataAlias', migrationJaasAlias], ['mappingConfigAlias', '']])
	AdminConfig.modify(migrationDataSuorceId, [['authDataAlias', migrationJaasAlias], ['xaRecoveryAuthAlias', migrationJaasAlias]])	
	print 'Parameters are set'
	AdminConfig.save()
	print 'Configuration is saved.'
	
# scheduler
taskSchedulerName = 'TAX Scheduler'+ settings.suffixForResources
taskSchedulerJndi = 'sched/TaskScheduler'+ settings.suffixForResources

print ''
print '--------------------------------------------'
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
