package com.aplana.sbrf.taxaccounting.scheduler.core.utils;

import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParam;
import junit.framework.Assert;
import org.junit.Test;
import com.aplana.sbrf.taxaccounting.scheduler.api.entity.TaskParamType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TaskUtilsTest {

    @Test
    public void serializeContextTest() {
        Map<String, TaskParam> params = new HashMap<String, TaskParam>();
        params.put("a", new TaskParam(1, "a", TaskParamType.INT, "1"));
        params.put("b", new TaskParam(0, "b", TaskParamType.DOUBLE, "1.2"));
        params.put("c", new TaskParam(2, "c", TaskParamType.DOUBLE, "1.2"));
        try {
            System.out.println(params);
            byte[] b = TaskUtils.serializeParams(params);
            Map<String, TaskParam> params2 = TaskUtils.deserializeParams(b);
            System.out.println(params2);
            Assert.assertEquals(params2.keySet().size(), 3);
            Assert.assertTrue(params2.containsKey("a"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
