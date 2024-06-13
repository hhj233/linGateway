package com.lin.core.filter.monitor;

import com.lin.common.constant.FilterConst;
import com.lin.core.context.GatewayContext;
import com.lin.core.filter.Filter;
import com.lin.core.filter.FilterAspect;
import io.micrometer.core.instrument.Timer;

/**
 * @author linzj
 */
@FilterAspect(id = FilterConst.MONITOR_FILTER_ID,
        name = FilterConst.MONITOR_FILTER_NAME,
        order = FilterConst.MONITOR_FILTER_ORDER)
public class MonitorFilter implements Filter {
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        ctx.setTimerSample(Timer.start());
    }
}
