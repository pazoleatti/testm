package com.aplana.sbrf.taxaccounting.scheduler.core.task;

import com.ibm.websphere.scheduler.TaskHandler;
import com.ibm.websphere.scheduler.TaskHandlerHome;

import javax.ejb.CreateException;
import java.rmi.RemoteException;

public interface TaskExecutorRemoteHome extends TaskHandlerHome {
    TaskHandler create() throws CreateException, RemoteException;
}
