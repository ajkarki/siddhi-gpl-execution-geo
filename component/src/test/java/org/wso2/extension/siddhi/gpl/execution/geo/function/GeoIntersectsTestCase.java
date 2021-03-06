/*
 * Copyright (C) 2017 WSO2 Inc. (http://wso2.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.wso2.extension.siddhi.gpl.execution.geo.function;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.event.Event;
import io.siddhi.core.query.output.callback.QueryCallback;
import io.siddhi.core.util.SiddhiTestHelper;
import org.apache.log4j.Logger;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.extension.siddhi.gpl.execution.geo.GeoTestCase;

import java.util.concurrent.atomic.AtomicInteger;

public class GeoIntersectsTestCase extends GeoTestCase {
    private static Logger logger = Logger.getLogger(GeoIntersectsTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @BeforeMethod
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testGeometry() throws Exception {
        logger.info("TestGeometry");

        data.clear();
        expectedResult.clear();
        eventCount = 0;

        data.add(new Object[]{"{'type':'Polygon','coordinates':[[[0.5, 0.5]," +
                "[0.5, 1.5],[1.5, 1.5],[1.5, 0.5],[0.5, 0.5]]]}"});
        expectedResult.add(true);
        data.add(new Object[]{"{'type':'Circle','coordinates':[-1, -1], 'radius':221148}"});
        expectedResult.add(true);
        data.add(new Object[]{"{'type':'Point','coordinates':[2, 0]}"});
        expectedResult.add(false);
        data.add(new Object[]{"{'type':'Polygon','coordinates':[[[2, 2],[2, 1],[1, 1],[1, 2],[2, 2]]]}"});
        expectedResult.add(true);

        String siddhiApp = "@config(async = 'true') define stream dataIn (geometry string);"
                + "@info(name = 'query1') from dataIn" +
                " select geo:intersects(geometry, \"{'type':'Polygon','coordinates':[[[0, 0]," +
                "[0, 1],[1, 1],[1, 0],[0, 0]]]}\") as intersects \n" +
                " insert into dataOut";

        long start = System.currentTimeMillis();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        long end = System.currentTimeMillis();
        logger.info(String.format("Time to create siddhiAppRuntime: [%f sec]", ((end - start) / 1000f)));
        siddhiAppRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                for (Event event : inEvents) {
                    count.incrementAndGet();
                    logger.info(event);
                    if (count.get() == 1) {
                        Boolean isWithin = (Boolean) event.getData(0);
                        AssertJUnit.assertEquals(expectedResult.get(eventCount++), isWithin);
                        eventArrived = true;
                    }
                    if (count.get() == 2) {
                        Boolean isWithin = (Boolean) event.getData(0);
                        AssertJUnit.assertEquals(expectedResult.get(eventCount++), isWithin);
                        eventArrived = true;
                    }
                    if (count.get() == 3) {
                        Boolean isWithin = (Boolean) event.getData(0);
                        AssertJUnit.assertEquals(expectedResult.get(eventCount++), isWithin);
                        eventArrived = true;
                    }
                    if (count.get() == 4) {
                        Boolean isWithin = (Boolean) event.getData(0);
                        AssertJUnit.assertEquals(expectedResult.get(eventCount++), isWithin);
                        eventArrived = true;
                    }
                }
            }
        });
        siddhiAppRuntime.start();
        generateEvents(siddhiAppRuntime);
        SiddhiTestHelper.waitForEvents(100, 4, count, 60000);
        AssertJUnit.assertEquals(4, count.get());
        AssertJUnit.assertTrue(eventArrived);
        siddhiAppRuntime.shutdown();
    }
}
