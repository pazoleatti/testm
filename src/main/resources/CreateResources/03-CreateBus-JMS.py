print ''
print '********************************************'
print '* Start create JMS and Bus script'
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

print ''
print '--------------------------------------------'

# Service integration bus
SIBusName                   	= 'TAX JMS Bus'+ settings.suffixForResources
SIBDestinationName          	= 'transportQueue'+ settings.suffixForResources
SIBDummyDestinationName	    	= 'transportQueueDummy'+ settings.suffixForResources
SIBShortAsyncDestinationName   	= 'shortAsyncQueue'+ settings.suffixForResources
SIBLongAsyncDestinationName    	= 'longAsyncQueue'+ settings.suffixForResources

# Connection factories
SIBJMSConnectionFactoryName 			= 'TAX Connection factories'+ settings.suffixForResources
SIBJMSConnectionFactoryJndi 			= 'jms/transportConnectionFactory'+ settings.suffixForResources
SIBJMSShortAsyncConnectionFactoryName 	= 'TAX Connection factories Async short'+ settings.suffixForResources
SIBJMSShortAsyncConnectionFactoryJndi 	= 'jms/shortAsyncConnectionFactory'+ settings.suffixForResources
SIBJMSLongAsyncConnectionFactoryName 	= 'TAX Connection factories Async long'+ settings.suffixForResources
SIBJMSLongAsyncConnectionFactoryJndi 	= 'jms/longAsyncConnectionFactory'+ settings.suffixForResources

# Queue
SIBJMSQueueName             	= 'TAX Queue'+ settings.suffixForResources
SIBJMSQueueJndi             	= 'jms/transportQueue'+ settings.suffixForResources
SIBJMSDummyQueueName        	= 'TAX Queue dummy'+ settings.suffixForResources
SIBJMSDummyQueueJndi        	= 'jms/transportQueueDummy'+ settings.suffixForResources
SIBJMSShortAsyncQueueName      	= 'TAX Queue Async short'+ settings.suffixForResources
SIBJMSShortAsyncQueueJndi      	= 'jms/shortAsyncQueue'+ settings.suffixForResources
SIBJMSLongAsyncQueueName       	= 'TAX Queue Async long'+ settings.suffixForResources
SIBJMSLongAsyncQueueJndi       	= 'jms/longAsyncQueue'+ settings.suffixForResources

# Activation specifications
SIBJMSActivationSpecName    			= 'TAX Activation specifications'+ settings.suffixForResources
SIBJMSActivationSpecJndi    			= 'jms/transportAS'+ settings.suffixForResources
SIBJMSDummyActivationSpecName   		= 'TAX Activation specifications dummy'+ settings.suffixForResources
SIBJMSDummyActivationSpecJndi   		= 'jms/transportASDummy'+ settings.suffixForResources
SIBJMSShortAsyncActivationSpecName    	= 'TAX Activation specifications Async short'+ settings.suffixForResources
SIBJMSShortAsyncActivationSpecJndi    	= 'jms/shortAsyncAS'+ settings.suffixForResources
SIBJMSLongAsyncActivationSpecName    	= 'TAX Activation specifications Async long'+ settings.suffixForResources
SIBJMSLongAsyncActivationSpecJndi    	= 'jms/longAsyncAS'+ settings.suffixForResources


print '- Service integration bus'
SIBusesId = AdminTask.listSIBuses().split(lineSeparator)
SIBusNotFound = 1
if SIBusesId[0] != '':
	for SIBusId in SIBusesId:
		if AdminConfig.showAttribute(SIBusId, 'name') == SIBusName:
			SIBusNotFound = 0
			print 'Found existing service integration bus name='+ SIBusId
			break
if SIBusNotFound:
	print 'Initiated the creation of an service integration bus'
	print 'id='+ AdminTask.createSIBus('[-bus "'+ SIBusName +'" -busSecurity false]')
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'
SIBusMemberId = AdminTask.listSIBusMembers('[-bus "'+ SIBusName +'"]')
if len(SIBusMemberId):
	print 'Found existing service integration bus members id='+ SIBusMemberId
else:
	print 'Initiated the creation of an service integration bus member '+ nodeName +':'+ settings.serverName
	AdminTask.addSIBusMember('[-bus "'+ SIBusName +'" -node "'+ nodeName +'" -server "'+ settings.serverName +'" -fileStore]')
	print 'id='+ AdminTask.listSIBusMembers('[-bus "'+ SIBusName +'"]')
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'
SIBDestinationsId = AdminTask.listSIBDestinations('-bus "'+ SIBusName +'" -type Queue').split(lineSeparator)
SIBDestinationNotFound = 1
if SIBDestinationsId[0] != '':
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
	print 'id='+ AdminTask.createSIBDestination('[-bus "'+ SIBusName +'" -name "'+ SIBDestinationName +'" -type Queue -node "'+ nodeName +'" -server "'+ settings.serverName +'"]')
	AdminConfig.save()
	print 'Configuration is saved.'
	
print ''
print '____________________________________________'
SIBDestinationsId = AdminTask.listSIBDestinations('-bus "'+ SIBusName +'" -type Queue').split(lineSeparator)
SIBDestinationNotFound = 1
if SIBDestinationsId[0] != '':
	for SIBDestinationId in SIBDestinationsId:
		SIBDestinationIdentifier = AdminConfig.showAttribute(SIBDestinationId, 'identifier')
		if SIBDestinationIdentifier[-len(SIBShortAsyncDestinationName):] == SIBShortAsyncDestinationName:
			SIBDestinationNotFound = 0
			print 'Found existing service integration bus destinations:'
			print 'SIBDestinationIdentifier='+ SIBDestinationIdentifier
			print 'SIBDestinationId='+ SIBDestinationId
			break
if SIBDestinationNotFound:
	print 'Initiated the creation of an service integration bus destinations'
	print 'id='+ AdminTask.createSIBDestination('[-bus "'+ SIBusName +'" -name "'+ SIBShortAsyncDestinationName +'" -type Queue -node "'+ nodeName +'" -server "'+ settings.serverName +'"]')
	AdminConfig.save()
	print 'Configuration is saved.'
	
print ''
print '____________________________________________'
SIBDestinationsId = AdminTask.listSIBDestinations('-bus "'+ SIBusName +'" -type Queue').split(lineSeparator)
SIBDestinationNotFound = 1
if SIBDestinationsId[0] != '':
	for SIBDestinationId in SIBDestinationsId:
		SIBDestinationIdentifier = AdminConfig.showAttribute(SIBDestinationId, 'identifier')
		if SIBDestinationIdentifier[-len(SIBLongAsyncDestinationName):] == SIBLongAsyncDestinationName:
			SIBDestinationNotFound = 0
			print 'Found existing service integration bus destinations:'
			print 'SIBDestinationIdentifier='+ SIBDestinationIdentifier
			print 'SIBDestinationId='+ SIBDestinationId
			break
if SIBDestinationNotFound:
	print 'Initiated the creation of an service integration bus destinations'
	print 'id='+ AdminTask.createSIBDestination('[-bus "'+ SIBusName +'" -name "'+ SIBLongAsyncDestinationName +'" -type Queue -node "'+ nodeName +'" -server "'+ settings.serverName +'"]')
	AdminConfig.save()
	print 'Configuration is saved.'
	
print ''
print '____________________________________________'
SIBDestinationsId = AdminTask.listSIBDestinations('-bus "'+ SIBusName +'" -type Queue').split(lineSeparator)
SIBDestinationNotFound = 1
if SIBDestinationsId[0] != '':
	for SIBDestinationId in SIBDestinationsId:
		SIBDestinationIdentifier = AdminConfig.showAttribute(SIBDestinationId, 'identifier')
		if SIBDestinationIdentifier[-len(SIBDummyDestinationName):] == SIBDummyDestinationName:
			SIBDestinationNotFound = 0
			print 'Found existing service integration bus destinations:'
			print 'SIBDestinationIdentifier='+ SIBDestinationIdentifier
			print 'SIBDestinationId='+ SIBDestinationId
			break
if SIBDestinationNotFound:
	print 'Initiated the creation of an service integration bus destinations'
	print 'id='+ AdminTask.createSIBDestination('[-bus "'+ SIBusName +'" -name "'+ SIBDummyDestinationName +'" -type Queue -node "'+ nodeName +'" -server "'+ settings.serverName +'"]')
	AdminConfig.save()
	print 'Configuration is saved.'

# TAX Connection factories
print ''
print '____________________________________________'
SIBJMSConnectionFactoriesId = AdminTask.listSIBJMSConnectionFactories(resourceRootLocationId).split(lineSeparator)
SIBJMSConnectionFactoryNotFound = 1
if SIBJMSConnectionFactoriesId[0] != '':
	for SIBJMSConnectionFactoryId in SIBJMSConnectionFactoriesId:
		if AdminConfig.showAttribute(SIBJMSConnectionFactoryId, 'jndiName') == SIBJMSConnectionFactoryJndi:
			SIBJMSConnectionFactoryNotFound = 0
			print 'Found existing service integration bus connection factory id='+ SIBJMSConnectionFactoryId
			break
if SIBJMSConnectionFactoryNotFound:
	print 'Initiated the creation of an service integration bus connection factory'
	print 'id='+ AdminTask.createSIBJMSConnectionFactory(resourceRootLocationId, ['-name', SIBJMSConnectionFactoryName, '-jndiName', SIBJMSConnectionFactoryJndi, '-busName', SIBusName])
	AdminConfig.save()
	print 'Configuration is saved.'

# TAX Connection factories Async short
print ''
print '____________________________________________'
SIBJMSConnectionFactoriesId = AdminTask.listSIBJMSConnectionFactories(resourceRootLocationId).split(lineSeparator)
SIBJMSConnectionFactoryNotFound = 1
if SIBJMSConnectionFactoriesId[0] != '':
	for SIBJMSConnectionFactoryId in SIBJMSConnectionFactoriesId:
		if AdminConfig.showAttribute(SIBJMSConnectionFactoryId, 'jndiName') == SIBJMSShortAsyncConnectionFactoryJndi:
			SIBJMSConnectionFactoryNotFound = 0
			print 'Found existing service integration bus connection factory id='+ SIBJMSConnectionFactoryId
			break
if SIBJMSConnectionFactoryNotFound:
	print 'Initiated the creation of an service integration bus connection factory'
	print 'id='+ AdminTask.createSIBJMSConnectionFactory(resourceRootLocationId, ['-name', SIBJMSShortAsyncConnectionFactoryName, '-jndiName', SIBJMSShortAsyncConnectionFactoryJndi, '-busName', SIBusName])
	AdminConfig.save()
	print 'Configuration is saved.'

# TAX Connection factories Async long
print ''
print '____________________________________________'
SIBJMSConnectionFactoriesId = AdminTask.listSIBJMSConnectionFactories(resourceRootLocationId).split(lineSeparator)
SIBJMSConnectionFactoryNotFound = 1
if SIBJMSConnectionFactoriesId[0] != '':
	for SIBJMSConnectionFactoryId in SIBJMSConnectionFactoriesId:
		if AdminConfig.showAttribute(SIBJMSConnectionFactoryId, 'jndiName') == SIBJMSLongAsyncConnectionFactoryJndi:
			SIBJMSConnectionFactoryNotFound = 0
			print 'Found existing service integration bus connection factory id='+ SIBJMSConnectionFactoryId
			break
if SIBJMSConnectionFactoryNotFound:
	print 'Initiated the creation of an service integration bus connection factory'
	print 'id='+ AdminTask.createSIBJMSConnectionFactory(resourceRootLocationId, ['-name', SIBJMSLongAsyncConnectionFactoryName, '-jndiName', SIBJMSLongAsyncConnectionFactoryJndi, '-busName', SIBusName])
	AdminConfig.save()
	print 'Configuration is saved.'
	
# TAX Queue
print ''
print '____________________________________________'
SIBJMSQueuesId = AdminTask.listSIBJMSQueues(resourceRootLocationId).split(lineSeparator)
SIBJMSQueueNotFound = 1
if SIBJMSQueuesId[0] != '':
	for SIBJMSQueueId in SIBJMSQueuesId:
		if AdminConfig.showAttribute(SIBJMSQueueId, 'jndiName') == SIBJMSQueueJndi:
			SIBJMSQueueNotFound = 0
			print 'Found existing service integration bus queue id='+ SIBJMSQueueId
			break
if SIBJMSQueueNotFound:
	print 'Initiated the creation of an service integration bus queue'
	print 'id='+ AdminTask.createSIBJMSQueue(resourceRootLocationId, ['-name', SIBJMSQueueName, '-jndiName', SIBJMSQueueJndi, '-queueName', SIBDestinationName, '-busName', SIBusName])
	AdminConfig.save()
	print 'Configuration is saved.'
	
# TAX Queue Async short
print ''
print '____________________________________________'
SIBJMSQueuesId = AdminTask.listSIBJMSQueues(resourceRootLocationId).split(lineSeparator)
SIBJMSQueueNotFound = 1
if SIBJMSQueuesId[0] != '':
	for SIBJMSQueueId in SIBJMSQueuesId:
		if AdminConfig.showAttribute(SIBJMSQueueId, 'jndiName') == SIBJMSShortAsyncQueueJndi:
			SIBJMSQueueNotFound = 0
			print 'Found existing service integration bus queue id='+ SIBJMSQueueId
			break
if SIBJMSQueueNotFound:
	print 'Initiated the creation of an service integration bus queue'
	print 'id='+ AdminTask.createSIBJMSQueue(resourceRootLocationId, ['-name', SIBJMSShortAsyncQueueName, '-jndiName', SIBJMSShortAsyncQueueJndi, '-queueName', SIBDestinationName, '-busName', SIBusName])
	AdminConfig.save()
	print 'Configuration is saved.'
	
# TAX Queue Async long
print ''
print '____________________________________________'
SIBJMSQueuesId = AdminTask.listSIBJMSQueues(resourceRootLocationId).split(lineSeparator)
SIBJMSQueueNotFound = 1
if SIBJMSQueuesId[0] != '':
	for SIBJMSQueueId in SIBJMSQueuesId:
		if AdminConfig.showAttribute(SIBJMSQueueId, 'jndiName') == SIBJMSLongAsyncQueueJndi:
			SIBJMSQueueNotFound = 0
			print 'Found existing service integration bus queue id='+ SIBJMSQueueId
			break
if SIBJMSQueueNotFound:
	print 'Initiated the creation of an service integration bus queue'
	print 'id='+ AdminTask.createSIBJMSQueue(resourceRootLocationId, ['-name', SIBJMSLongAsyncQueueName, '-jndiName', SIBJMSLongAsyncQueueJndi, '-queueName', SIBDestinationName, '-busName', SIBusName])
	AdminConfig.save()
	print 'Configuration is saved.'

# TAX Queue Dummy
print ''
print '____________________________________________'
SIBJMSQueuesId = AdminTask.listSIBJMSQueues(resourceRootLocationId).split(lineSeparator)
SIBJMSQueueNotFound = 1
if SIBJMSQueuesId[0] != '':
	for SIBJMSQueueId in SIBJMSQueuesId:
		if AdminConfig.showAttribute(SIBJMSQueueId, 'jndiName') == SIBJMSDummyQueueJndi:
			SIBJMSQueueNotFound = 0
			print 'Found existing service integration bus queue id='+ SIBJMSQueueId
			break
if SIBJMSQueueNotFound:
	print 'Initiated the creation of an service integration bus queue'
	print 'id='+ AdminTask.createSIBJMSQueue(resourceRootLocationId, ['-name', SIBJMSDummyQueueName, '-jndiName', SIBJMSDummyQueueJndi, '-queueName', SIBDummyDestinationName, '-busName', SIBusName])
	AdminConfig.save()
	print 'Configuration is saved.'

# TAX Activation specifications
print ''
print '____________________________________________'
SIBJMSActivationSpecsId = AdminTask.listSIBJMSActivationSpecs(resourceRootLocationId).split(lineSeparator)
SIBJMSActivationSpecNotFound = 1
if SIBJMSActivationSpecsId[0] != '':
	for SIBJMSActivationSpecId in SIBJMSActivationSpecsId:
		if AdminConfig.showAttribute(SIBJMSActivationSpecId, 'jndiName') == SIBJMSActivationSpecJndi:
			SIBJMSActivationSpecNotFound = 0
			print 'Found existing service integration bus activation specifications id='+ SIBJMSActivationSpecId
			break
if SIBJMSActivationSpecNotFound:
	print 'Initiated the creation of an service integration bus activation specifications'
	print 'id='+ AdminTask.createSIBJMSActivationSpec(resourceRootLocationId, ['-name', SIBJMSActivationSpecName, '-jndiName', SIBJMSActivationSpecJndi, '-destinationJndiName', SIBJMSQueueJndi, '-busName', SIBusName, '-maxBatchSize', 1, '-maxConcurrency', 1]) 
	AdminConfig.save()
	print 'Configuration is saved.'

# TAX Activation specifications Async short
print ''
print '____________________________________________'
SIBJMSActivationSpecsId = AdminTask.listSIBJMSActivationSpecs(resourceRootLocationId).split(lineSeparator)
SIBJMSActivationSpecNotFound = 1
if SIBJMSActivationSpecsId[0] != '':
	for SIBJMSActivationSpecId in SIBJMSActivationSpecsId:
		if AdminConfig.showAttribute(SIBJMSActivationSpecId, 'jndiName') == SIBJMSShortAsyncActivationSpecJndi:
			SIBJMSActivationSpecNotFound = 0
			print 'Found existing service integration bus activation specifications id='+ SIBJMSActivationSpecId
			break
if SIBJMSActivationSpecNotFound:
	print 'Initiated the creation of an service integration bus activation specifications'
	print 'id='+ AdminTask.createSIBJMSActivationSpec(resourceRootLocationId, ['-name', SIBJMSShortAsyncActivationSpecName, '-jndiName', SIBJMSShortAsyncActivationSpecJndi, '-destinationJndiName', SIBJMSQueueJndi, '-busName', SIBusName, '-maxBatchSize', 1, '-maxConcurrency', 1]) 
	AdminConfig.save()
	print 'Configuration is saved.'
	
# TAX Activation specifications Async long
print ''
print '____________________________________________'
SIBJMSActivationSpecsId = AdminTask.listSIBJMSActivationSpecs(resourceRootLocationId).split(lineSeparator)
SIBJMSActivationSpecNotFound = 1
if SIBJMSActivationSpecsId[0] != '':
	for SIBJMSActivationSpecId in SIBJMSActivationSpecsId:
		if AdminConfig.showAttribute(SIBJMSActivationSpecId, 'jndiName') == SIBJMSLongAsyncActivationSpecJndi:
			SIBJMSActivationSpecNotFound = 0
			print 'Found existing service integration bus activation specifications id='+ SIBJMSActivationSpecId
			break
if SIBJMSActivationSpecNotFound:
	print 'Initiated the creation of an service integration bus activation specifications'
	print 'id='+ AdminTask.createSIBJMSActivationSpec(resourceRootLocationId, ['-name', SIBJMSLongAsyncActivationSpecName, '-jndiName', SIBJMSLongAsyncActivationSpecJndi, '-destinationJndiName', SIBJMSQueueJndi, '-busName', SIBusName, '-maxBatchSize', 1, '-maxConcurrency', 1]) 
	AdminConfig.save()
	print 'Configuration is saved.'

# TAX Activation specifications Dummy
print ''
print '____________________________________________'
SIBJMSActivationSpecsId = AdminTask.listSIBJMSActivationSpecs(resourceRootLocationId).split(lineSeparator)
SIBJMSActivationSpecNotFound = 1
if SIBJMSActivationSpecsId[0] != '':
	for SIBJMSActivationSpecId in SIBJMSActivationSpecsId:
		if AdminConfig.showAttribute(SIBJMSActivationSpecId, 'jndiName') == SIBJMSDummyActivationSpecJndi:
			SIBJMSActivationSpecNotFound = 0
			print 'Found existing service integration bus activation specifications id='+ SIBJMSActivationSpecId
			break
if SIBJMSActivationSpecNotFound:
	print 'Initiated the creation of an service integration bus activation specifications'
	print 'id='+ AdminTask.createSIBJMSActivationSpec(resourceRootLocationId, ['-name', SIBJMSDummyActivationSpecName, '-jndiName', SIBJMSDummyActivationSpecJndi, '-destinationJndiName', SIBJMSDummyQueueJndi, '-busName', SIBusName, '-maxBatchSize', 1, '-maxConcurrency', 1]) 
	AdminConfig.save()
	print 'Configuration is saved.'