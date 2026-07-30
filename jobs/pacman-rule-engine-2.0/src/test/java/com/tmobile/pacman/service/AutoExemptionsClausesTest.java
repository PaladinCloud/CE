package com.tmobile.pacman.service;

import com.google.gson.Gson;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AutoExemptionsClausesTest {
    @Test
    public void testOneClauseOneFieldIsExempted() throws Exception {
        Map<String, String> asset = mapOf("account_id", "345", "tags.component", "EFS");
        List<Map<String, Object>> clauses = Arrays.asList(
                clauseOf("accountid", "345"));

        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", null, "goldfish", toJson(clauses)));
        boolean check = rule.isExempted(asset);
        assertTrue(check);
        assertEquals("Matches clause #1", rule.getMatchExplanation());
    }

    @Test
    public void testTwoTagsIsExempted() throws Exception {
        Map<String, String> asset = mapOf("tags.component", "EFS", "tags.app", "Purchase", "account_id", "345");
        List<Map<String, Object>> clauses = Arrays.asList(
                clauseOf("tags.app", "Purchase", "tags.component", "EFS"));

        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", null, "goldfish", toJson(clauses)));
        boolean check = rule.isExempted(asset);
        assertTrue(check);
        assertEquals("Matches clause #1", rule.getMatchExplanation());
    }
    @Test
    public void testOneClause2FieldsIsExempted() throws Exception {
        Map<String, String> asset = mapOf("accountid", "345", "tags.component", "EFS");
        List<Map<String, Object>> clauses = Arrays.asList(
                clauseOf("accountid", "345", "tags.component", "EFS"));

        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", null, "goldfish", toJson(clauses)));
        boolean check = rule.isExempted(asset);
        assertTrue(check);
    }

    @Test
    public void testTwoClause2FieldsIsExempted() throws Exception {
        Map<String, String> asset = mapOf("account_id", "345", "tags.component", "EFS");
        List<Map<String, Object>> clauses = Arrays.asList(
                clauseOf("accountid", "192", "tags.component", "EFS"),
                clauseOf("accountid", "345", "tags.component", "EFS"));

        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", null, "goldfish", toJson(clauses)));
        boolean check = rule.isExempted(asset);
        assertTrue(check);
    }

    @Test
    public void testManyClause2FieldsIsExempted() throws Exception {
        Map<String, String> asset = mapOf("account_id", "345", "tags.component", "EFS");
        List<Map<String, Object>> clauses = Arrays.asList(
                clauseOf("accountid", "193", "tags.component", "EFS"),
                clauseOf("accountid", "194", "tags.component", "EFS"),
                clauseOf("accountid", "195", "tags.component", "EFS"),
                clauseOf("accountid", "196", "tags.component", "EFS"),
                clauseOf("accountid", "197", "tags.component", "EFS"),
                clauseOf("accountid", "198", "tags.component", "EFS"),
                clauseOf("accountid", "199", "tags.component", "EFS"),
                clauseOf("accountid", "200", "tags.component", "EFS"),
                clauseOf("accountid", "201", "tags.component", "EFS"),
                clauseOf("accountid", "345", "tags.component", "EFS"));

        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", null, "goldfish", toJson(clauses)));
        boolean check = rule.isExempted(asset);
        assertTrue(check);
        assertEquals("Matches clause #10", rule.getMatchExplanation());
    }

    @Test
    public void testOneClauseNoMatchIsNotExempted() throws Exception {
        Map<String, String> asset = mapOf("account_id", "345", "tags.component", "EFS");
        List<Map<String, Object>> clauses = Arrays.asList(clauseOf("accountid", "193", "tags.component", "EFS"));
        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", null, "goldfish", toJson(clauses)));
        boolean check = rule.isExempted(asset);
        assertFalse(check);
    }

    @Test
    public void testNoClauseIsNotExempted() throws Exception {
        Map<String, String> asset = mapOf("account_id", "345", "tags.component", "EFS");
        List<Map<String, Object>> clauses = Arrays.asList();
        AutoExemptions.Rule rule = AutoExemptions.ruleFromPolicyParams(params("true", null, "goldfish", toJson(clauses)));
        boolean check = rule.isExempted(asset);
        assertFalse(check);
    }

    private Map<String, String> params(String enabled, String date, String reason, String rules) {
        Map<String, String> map = new HashMap<>();
        map.put(AutoExemptions.PARAMS_ENABLED_FIELD, enabled);
        map.put(AutoExemptions.PARAMS_EXPIRE_DATE_FIELD, date);
        map.put(AutoExemptions.PARAMS_REASON_FIELD, reason);
        map.put(AutoExemptions.PARAMS_RULES_FIELD, rules);
        return map;
    }

    private String toJson(List<Map<String,Object>> clauses) {
        Gson gson = new Gson();
        return gson.toJson(clauses);
    }

    private Map<String, Object> clauseOf(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    private Map<String, Object> clauseOf(String key1, Object value1, String key2, Object value2) {
        Map<String, Object> map = clauseOf(key1, value1);
        map.put(key2, value2);
        return map;
    }

    private Map<String, String> mapOf(String key1, String value1, String key2, String value2) {
        Map<String, String> map = new HashMap<>();
        map.put(key1, value1);
        map.put(key2, value2);
        return map;
    }

    private Map<String, String> mapOf(String key1, String value1, String key2, String value2, String key3, String value3) {
        Map<String, String> map = mapOf(key1, value1, key2, value2);
        map.put(key3, value3);
        return map;
    }

}
