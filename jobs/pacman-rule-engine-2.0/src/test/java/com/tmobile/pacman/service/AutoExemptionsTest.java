package com.tmobile.pacman.service;

import java.text.SimpleDateFormat;
import java.util.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AutoExemptionsTest {
    static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    static {
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void testAccountMatchIsExempted() throws Exception {
        Map<String, String> asset = mapOf("accountid", "345");
        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", null, "goldfish", "123,234,345"));
        boolean check = rule.isExempted(asset);
        assertTrue(check);
    }

    @Test
    public void testDisabledIsNotExempted() throws Exception {
        Map<String, String> asset = mapOf("accountid", "345");
        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("false", null, "guppies", "123,234,345"));
        boolean check = rule.isExempted(asset);
        assertFalse(check);
    }

    @Test
    public void testNoAccountMatchIsNotExempted() throws Exception {
        Map<String, String> asset = mapOf("accountid", "789");
        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", null, "toads", "123,234,345"));
        boolean check = rule.isExempted(asset);
        assertFalse(check);
    }

    @Test
    public void testFutureDateIsExempted() throws Exception {
        Map<String, String> asset = mapOf("accountid", "345");
        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", adjustDate(new Date(), 1), "frogs", "123,234,345"));
        boolean check = rule.isExempted(asset);
        assertTrue(check);
    }

    @Test
    public void testTodayIsExempted() throws Exception {
        Map<String, String> asset = mapOf("accountid", "345");
        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", adjustDate(new Date(), 0), "frogs", "123,234,345"));
        boolean check = rule.isExempted(asset);
        assertTrue(check);
    }

    @Test
    public void testPastDateIsNotExempted() throws Exception {
        Map<String, String> asset = mapOf("accountid", "345");
        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", adjustDate(new Date(), -1), "frogs", "123,234,345"));
        boolean check = rule.isExempted(asset);
        assertFalse(check);
    }

    @Test
    public void testBlankDateIsIgnored() throws Exception {
        Map<String, String> asset = mapOf("accountid", "345");
        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", "", "frogs", "123,234,345"));
        boolean check = rule.isExempted(asset);
        assertTrue(check);
    }

    private Map<String, String> mapOf(String key, String value) {
        Map<String, String> map = new HashMap();
        map.put(key, value);
        return map;
    }

    private Map<String, String> params(String enabled, String date, String reason, String accounts) {
        Map<String, String> map = new HashMap();
        map.put(AutoExemptions.PARAMS_ENABLED_FIELD, enabled);
        map.put(AutoExemptions.PARAMS_EXPIRE_DATE_FIELD, date);
        map.put(AutoExemptions.PARAMS_REASON_FIELD, reason);
        map.put(AutoExemptions.PARAMS_ACCOUNTS_FIELD, accounts);
        return map;
    }

    public static String adjustDate(Date dt, int amount) {
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(dt);
        calendarDate.add(Calendar.DATE, amount);
        return sdf.format(calendarDate.getTime());
    }
}
