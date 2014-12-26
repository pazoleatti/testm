print ''
print '********************************************'
print '* Start create Cache script'
print '********************************************'
print ''
print '--------------------------------------------'
print '- System init'

# get line separator
import java
lineSeparator = java.lang.System.getProperty('line.separator')

# server info
resourceRootLocation   = '/Server:'+ serverName
resourceRootLocationId = AdminConfig.getid(resourceRootLocation)
print 'Found resource root location ID = '+ resourceRootLocationId
nodeName               = AdminControl.getNode()
print 'Found node name = '+ nodeName
cacheProviderId = AdminConfig.getid(resourceRootLocation +'/CacheProvider:CacheProvider/')
print 'Found cache provider ID = '+ cacheProviderId

print ''
print '--------------------------------------------'

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

print '- Cache instance'
declarationTemplateId = AdminConfig.getid('/ObjectCacheInstance:'+ declarationTemplateName +'/')
if len(declarationTemplateId):
	print 'Found existing object cache instance ='+ declarationTemplateId
else:
	print 'Initiated the creation of an object cache instance '+ declarationTemplateName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', declarationTemplateName], ['jndiName', declarationTemplateJndi], ['cacheSize', 100], ['defaultPriority', 1], ['disableDependencyId', 1]])
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'
declarationTypeId = AdminConfig.getid('/ObjectCacheInstance:'+ declarationTypeName +'/')
if len(declarationTypeId):
	print 'Found existing object cache instance ='+ declarationTypeId
else:
	print 'Initiated the creation of an object cache instance '+ declarationTypeName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', declarationTypeName], ['jndiName', declarationTypeJndi], ['cacheSize', 100], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'
departmentId = AdminConfig.getid('/ObjectCacheInstance:'+ departmentName +'/')
if len(departmentId):
	print 'Found existing object cache instance ='+ departmentId
else:
	print 'Initiated the creation of an object cache instance '+ departmentName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', departmentName], ['jndiName', departmentJndi], ['cacheSize', 200], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'
formTemplateId = AdminConfig.getid('/ObjectCacheInstance:'+ formTemplateName +'/')
if len(formTemplateId):
	print 'Found existing object cache instance ='+ formTemplateId
else:
	print 'Initiated the creation of an object cache instance '+ formTemplateName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', formTemplateName], ['jndiName', formTemplateJndi], ['cacheSize', 200], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'
formTypeId = AdminConfig.getid('/ObjectCacheInstance:'+ formTypeName +'/')
if len(formTypeId):
	print 'Found existing object cache instance ='+ formTypeId
else:
	print 'Initiated the creation of an object cache instance '+ formTypeName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', formTypeName], ['jndiName', formTypeJndi], ['cacheSize', 200], ['defaultPriority', 1], ['disableDependencyId', 'false']])
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'
userCacheId = AdminConfig.getid('/ObjectCacheInstance:'+ userCacheName +'/')
if len(userCacheId):
	print 'Found existing object cache instance ='+ userCacheId
else:
	print 'Initiated the creation of an object cache instance '+ userCacheName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', userCacheName], ['jndiName', userCacheJndi], ['cacheSize', 2000], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'
dataBlobsCacheId = AdminConfig.getid('/ObjectCacheInstance:'+ dataBlobsCacheName +'/')
if len(dataBlobsCacheId):
	print 'Found existing object cache instance ='+ dataBlobsCacheId
else:
	print 'Initiated the creation of an object cache instance '+ dataBlobsCacheName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', dataBlobsCacheName], ['jndiName', dataBlobsCacheJndi], ['cacheSize', 100], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'
	
print ''
print '____________________________________________'
permanentDataId = AdminConfig.getid('/ObjectCacheInstance:'+ permanentDataName +'/')
if len(permanentDataId):
	print 'Found existing object cache instance ='+ permanentDataId
else:
	print 'Initiated the creation of an object cache instance '+ permanentDataName
	print 'id='+ AdminConfig.create('ObjectCacheInstance', cacheProviderId, [['name', permanentDataName], ['jndiName', permanentDataJndi], ['cacheSize', 500], ['defaultPriority', 1], ['disableDependencyId', 'true']])
	AdminConfig.save()
	print 'Configuration is saved.'