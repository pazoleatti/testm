print ''
print '********************************************'
print '* Start create JMS for MQ script'
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

# MQ messaging provider
MQQueueName                 = 'TAX Rate Queue MQ'+ settings.suffixForResources
MQQueueJndi                 = 'jms/rateQueue'+ settings.suffixForResources
MQActivationSpecName        = 'TAX Activation specifications MQ'+ settings.suffixForResources
MQActivationSpecJndi        = 'jms/transportMQ'+ settings.suffixForResources
MQActivationSpecDest        = 'jms/transportQueueMQ'+ settings.suffixForResources

print '- MQ messaging provider'

MQQueuesId = AdminTask.listWMQQueues(resourceRootLocationId).split(lineSeparator)
MQQueueNotFound = 1
if MQQueuesId[0] != '':
	for MQQueueId in MQQueuesId:
		if AdminConfig.showAttribute(MQQueueId, 'jndiName') == MQQueueJndi:
			MQQueueNotFound = 0
			print 'Found existing MQ queue id='+ MQQueueId
			break
if MQQueueNotFound:
	print 'Initiated the creation of an MQ queue'
	print 'id='+ AdminTask.createWMQQueue(resourceRootLocationId, ['-name', MQQueueName, '-jndiName', MQQueueJndi, '-queueName', settings.MQServerQueueName, '-qmgr', settings.MQServerQueueManager])
	AdminConfig.save()
	print 'Configuration is saved.'

print ''
print '____________________________________________'
MQActivationSpecsId = AdminTask.listWMQActivationSpecs(resourceRootLocationId).split(lineSeparator)
MQActivationSpecNotFound = 1
if MQActivationSpecsId[0] != '':
	for MQActivationSpecId in MQActivationSpecsId:
		if AdminConfig.showAttribute(MQActivationSpecId, 'jndiName') == MQActivationSpecJndi:
			MQActivationSpecNotFound = 0
			print 'Found existing MQ activation specifications id='+ MQActivationSpecId
			break
if MQActivationSpecNotFound:
	print 'Initiated the creation of an MQ activation specifications'
	MQActivationSpecsId = AdminTask.createWMQActivationSpec(resourceRootLocationId, ['-name', MQActivationSpecName, '-jndiName', MQActivationSpecJndi, '-qmgrName', settings.MQServerQueueManager, '-wmqTransportType', 'BINDINGS_THEN_CLIENT', '-qmgrHostname', settings.MQServerHost, '-qmgrPortNumber', settings.MQServerPort, '-qmgrSvrconnChannel', settings.MQServerChanel, '-destinationJndiName', MQQueueJndi, '-destinationType', 'javax.jms.Queue'])
	print 'id='+MQActivationSpecsId
	AdminTask.modifyWMQActivationSpec(MQActivationSpecsId, ['-sslType', 'CENTRAL', '-stopEndpointIfDeliveryFails', 'false'])
	AdminConfig.save()
	print 'Configuration is saved.'