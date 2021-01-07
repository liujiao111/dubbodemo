package com.liu;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.Filter;

@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class LogFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Long start = System.currentTimeMillis();
        try {
            return invoker.invoke(invocation);
        } finally {
            Long end = System.currentTimeMillis();
        }
    }
}
