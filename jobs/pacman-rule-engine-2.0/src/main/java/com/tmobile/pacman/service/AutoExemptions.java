package com.tmobile.pacman.service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AutoExemptions {
    static public final String PARAMS_ENABLED_FIELD = "isExemptionEnabled";
    static public final String PARAMS_EXPIRE_DATE_FIELD = "exemptionexpireddate";
    static public final String PARAMS_REASON_FIELD = "exemptionReason";
    static public final String PARAMS_ACCOUNTS_FIELD = "exemptionAccounts";


    static public Rule ruleFromPolicyParams(Map<String, String> params) {
        String enabledStr = params.getOrDefault(PARAMS_ENABLED_FIELD, "false").toLowerCase();

        String expireDate = params.getOrDefault(PARAMS_EXPIRE_DATE_FIELD, null);
        String reason = params.getOrDefault(PARAMS_REASON_FIELD, "");

        String accountsStr = params.getOrDefault(PARAMS_ACCOUNTS_FIELD, "");
        List<String> accounts = Arrays.asList(accountsStr.split(",")).stream()
                .map(s -> s.trim())
                .collect(Collectors.toList());

        return new Rule(
                Boolean.parseBoolean(enabledStr),
                expireDate,
                reason,
                accounts);
    }

    static public class Rule {
        protected boolean enabled;
        protected String expireDate;
        protected String reason;
        protected List<String> accounts;

        static private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        static {
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        public Rule(boolean enabled, String expireDate, String reason, List<String> accounts) {
            this.enabled = enabled;
            this.expireDate = expireDate;
            this.reason = reason;
            this.accounts = accounts;
        }

        public String getReason() {
            return reason;
        }

        public String getExpireDate() {
            return expireDate;
        }

        public boolean isExempted(Map<String, String> asset) {
            if (asset == null) {
                return false;
            }

            if (!this.enabled) {
                return false;
            }

            if (this.expireDate != null && !this.expireDate.isEmpty()) {
                String today = sdf.format(new Date());
                if (today.compareTo(this.expireDate) > 0) {
                    return false;
                }
            }

            if (accounts.contains(getAccountId(asset))) {
                return true;
            }

            return false;
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
