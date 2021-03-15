/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.graph.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.TimestampStringMap;
import org.testng.Assert;

/**
 *
 * @author LEVALLOIS
 */
public class TestMain {

    /**
     * use this class to run tests
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TimestampStringMap map1 = new TimestampStringMap();
        Assert.assertEquals(map1.toString(TimeFormat.DATE), "<empty>");

        map1.put(AttributeUtils.parseDateTime("2012-02-29"), "foo");
        Assert.assertEquals(map1.toString(TimeFormat.DATE), "<[2012-02-29, foo]>");

        map1.put(AttributeUtils.parseDateTime("2012-02-29T00:02:21"), "bar");
        Assert.assertEquals(map1.toString(TimeFormat.DATE), "<[2012-02-29, foo]; [2012-02-29, bar]>");
        Assert.assertEquals(map1.toString(TimeFormat.DOUBLE), "<[1330473600000.0, foo]; [1330473741000.0, bar]>");

        // Test with time zone printing:
        Assert.assertEquals(map1.toString(TimeFormat.DATE, ZonedDateTime.now(ZoneId.of("UTC"))), "<[2012-02-29, foo]; [2012-02-29, bar]>");
        Assert.assertEquals(map1.toString(TimeFormat.DATE, ZonedDateTime.now(ZoneId.of("+03:00"))), "<[2012-02-29, foo]; [2012-02-29, bar]>");
        Assert.assertEquals(map1.toString(TimeFormat.DATE, ZonedDateTime.now(ZoneId.of("-03:00"))), "<[2012-02-28, foo]; [2012-02-28, bar]>");
    }

}
