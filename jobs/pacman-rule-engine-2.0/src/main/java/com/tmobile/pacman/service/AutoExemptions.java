package com.tmobile.pacman.service;

import com.google.gson.Gson;
import lombok.Getter;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AutoExemptions {
    static public final String PARAMS_ENABLED_FIELD = "isExemptionEnabled";
    static public final String PARAMS_EXPIRE_DATE_FIELD = "exemptionExpireDate";
    static public final String PARAMS_REASON_FIELD = "exemptionReason";
    static public final String PARAMS_ACCOUNTS_FIELD = "exemptionAccounts";
    static public final String PARAMS_RULES_FIELD = "exemptionRules";


    static public Rule ruleFromPolicyParams(Map<String, String> params) {
        String enabledStr = params.getOrDefault(PARAMS_ENABLED_FIELD, "false").toLowerCase();

        String expireDate = params.getOrDefault(PARAMS_EXPIRE_DATE_FIELD, null);
        String reason = params.getOrDefault(PARAMS_REASON_FIELD, "");

        // If both accounts and rules field are set, default to using the rules
        String rulesStr = params.getOrDefault(PARAMS_RULES_FIELD, null);
        if (rulesStr != null) {
            // json array, each entry is a clause - any clause that's true is a match.
            // within a clause, each property must be true
            Gson gson = new Gson();
            List<Map<String, Object>> clauses = gson.fromJson(rulesStr, List.class);
            return Rule.createRulesWithClauses(
                    Boolean.parseBoolean(enabledStr),
                    expireDate,
                    reason,
                    clauses);
        } else {
            String accountsStr = params.getOrDefault(PARAMS_ACCOUNTS_FIELD, "");
            List<String> accounts = Arrays.stream(accountsStr.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());

            return Rule.createAccountsRule(
                    Boolean.parseBoolean(enabledStr),
                    expireDate,
                    reason,
                    accounts);
        }
    }

    static public class Rule {
        protected boolean enabled;
        @Getter
        protected String expireDate;
        @Getter
        protected String reason;
        protected List<String> accounts;
        protected List<Map<String, Object>> clauses;

        @Getter
        protected String matchExplanation;

        static private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        static {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        static Rule createAccountsRule(boolean enabled, String expireDate, String reason, List<String> accounts) {
            Rule rule = new Rule(enabled, expireDate, reason);
            rule.accounts = accounts;
            return rule;
        }

        static Rule createRulesWithClauses(boolean enabled, String expireDate, String reason, List<Map<String, Object>> clauses) {
            Rule rule = new Rule(enabled, expireDate, reason);
            rule.clauses = clauses;
            return rule;
        }

        private Rule(boolean enabled, String expireDate, String reason) {
            this.enabled = enabled;
            this.expireDate = expireDate;
            this.reason = reason;
        }

        public boolean isExempted(Map<String, String> asset) {
            if (asset == null) {
                matchExplanation = "Invalid asset";
                return false;
            }

            if (!this.enabled) {
                matchExplanation = "Rule is disabled";
                return false;
            }

            if (this.expireDate != null && !this.expireDate.isEmpty()) {
                String today = sdf.format(new Date());
                if (today.compareTo(this.expireDate) > 0) {
                    matchExplanation = "Rule has expired";
                    return false;
                }
            }

            if (clauses != null) {
                for (int idx = 0; idx < clauses.size(); idx++) {
                    Map<String, Object> clause = clauses.get(idx);
                    if (doesClauseMatch(clause, asset)) {
                        matchExplanation = String.format("Matches clause #%d", idx + 1);
                        return true;
                    }
                    matchExplanation = "No clauses matched";
                }
            } else if (accounts != null) {
                String accountId = getAccountId(asset);
                if (accounts.contains(accountId)) {
                    matchExplanation = String.format("Matches account %s", accountId);
                    return true;
                }

                matchExplanation = "No accounts match";
            }

            return false;
        }

        private boolean doesClauseMatch(Map<String, Object> clause, Map<String, String> asset) {
            if (clauses.isEmpty()) {
                return false;
            }

            for (Map.Entry<String, Object> entry : clause.entrySet()) {
                Object assetValue = getAssetField(entry.getKey(), asset);
                if (!entry.getValue().equals(assetValue)) {
                    return false;
                }
            }

            return true;
        }

        private Object getAssetField(String field, Map<String, String> asset) {
            if (field.equals("accountid")) {
                return asset.getOrDefault("account_id", asset.get("accountid"));
            }
            return asset.get(field);
        }

        private String getAccountId(Map<String, String> asset) {
            String id = asset.getOrDefault("account_id", null);
            if (id == null || id.isEmpty()) {
                id = asset.getOrDefault("accountid", null);
            }

            return id;
        }
    }
}
