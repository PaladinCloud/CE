package com.tmobile.cloud.gcprules.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tmobile.cloud.constants.PacmanRuleConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static com.tmobile.cloud.constants.PacmanRuleConstants.SOURCE_RANGE_REGEX;

public class GCPFirewallUtils {

    private static final Logger logger = LoggerFactory.getLogger(GCPFirewallUtils.class);

    public static boolean verifyPorts(String vmEsURL, Map<String, Object> mustFilter, String[] ports,
                                      String direction) throws Exception {
        logger.debug("========verify-ports  started=========");
        JsonArray hitsJsonArray = GCPUtils.getHitsArrayFromEs(vmEsURL, mustFilter);
        if (hitsJsonArray.isEmpty()) {
            logger.info(PacmanRuleConstants.RESOURCE_DATA_NOT_FOUND);
            return true;
        }
        logger.debug("========verify-ports hit array=========");

        JsonObject vpcFirewall = (JsonObject) ((JsonObject) hitsJsonArray.get(0))
                .get(PacmanRuleConstants.SOURCE);

        logger.debug("Validating the data item: {}", vpcFirewall.toString());

        if (!Objects.isNull(vpcFirewall.getAsJsonObject()) &&
                !Objects.isNull(vpcFirewall.getAsJsonObject().get(PacmanRuleConstants.DISABLED)) &&
                vpcFirewall.getAsJsonObject().get(PacmanRuleConstants.DISABLED).getAsBoolean()) {
            return true;
        }
        if (!Objects.isNull(vpcFirewall.getAsJsonObject().get(PacmanRuleConstants.DIRECTION)) &&
                !vpcFirewall.getAsJsonObject().get(PacmanRuleConstants.DIRECTION).getAsString()
                        .equalsIgnoreCase(direction)) {
            return true;
        }
        JsonArray sourceRanges = vpcFirewall.getAsJsonObject().get(PacmanRuleConstants.SOURCERANGES)
                .getAsJsonArray();
        JsonArray allow = vpcFirewall.getAsJsonObject().get(PacmanRuleConstants.ALLOW.toLowerCase())
                .getAsJsonArray();
        if (!validateSourceRanges(sourceRanges)) {
            return true;
        }

        for (JsonElement jsonElement : allow) {
            String resourceProtocol = jsonElement.getAsJsonObject().get(PacmanRuleConstants.PROTOCOL).getAsString();
            if (resourceProtocol.equalsIgnoreCase(PacmanRuleConstants.ICMP) ||
                    resourceProtocol.equalsIgnoreCase(PacmanRuleConstants.ALL)) {
                return true;
            }
            JsonArray portsJson = jsonElement.getAsJsonObject().get(PacmanRuleConstants.PORTS).getAsJsonArray();
            logger.debug("Checking ports {}, resourceProtocol {}, portsJson {}",
                    Arrays.toString(ports), resourceProtocol, portsJson);
            if (checkPorts(ports, portsJson)) {
                return false;
            }
        }
        return true;
    }

    private static boolean validateSourceRanges(JsonArray sourceRanges) {
        logger.debug("Validating source Ranges {}", sourceRanges);
        for (JsonElement jsonElement : sourceRanges) {
            String sourceRange = jsonElement.toString().replaceAll(SOURCE_RANGE_REGEX, StringUtils.EMPTY);
            if (sourceRange.equals(PacmanRuleConstants.SOURCERANGE) ||
                    sourceRange.equals(PacmanRuleConstants.SOURCE_RANGE_IPV6)) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkPorts(String[] paramsPorts, JsonArray portsJson) {
        for (String paramPort : paramsPorts) {
            if (paramPort.contains(PacmanRuleConstants.DELIMITER_COLON)) {
                String[] portArray = paramPort.split(PacmanRuleConstants.DELIMITER_COLON);
                if (portArray[0].equalsIgnoreCase(PacmanRuleConstants.ICMP) ||
                        portArray[0].equalsIgnoreCase(PacmanRuleConstants.ALL)) {
                    return true;
                }
                paramPort = portArray[1];
            }
            for (JsonElement jsonElement : portsJson) {
                String port = jsonElement.toString().replaceAll(SOURCE_RANGE_REGEX, StringUtils.EMPTY);
                if (port.equalsIgnoreCase(paramPort)) {
                    return true;
                }
                if (port.contains(PacmanRuleConstants.DELIMITER_MINUS) &&
                        !paramPort.contains(PacmanRuleConstants.DELIMITER_MINUS)) {
                    String[] portArray = port.split(PacmanRuleConstants.DELIMITER_MINUS);
                    if (isInClosedRange(Integer.parseInt(paramPort), Integer.parseInt(portArray[0]),
                            Integer.parseInt(portArray[1]))) {
                        return true;
                    }
                }
                if (paramPort.contains(PacmanRuleConstants.DELIMITER_MINUS) &&
                        !port.contains(PacmanRuleConstants.DELIMITER_MINUS)) {
                    String[] portArray = paramPort.split(PacmanRuleConstants.DELIMITER_MINUS);
                    if (isInClosedRange(Integer.parseInt(port), Integer.parseInt(portArray[0]),
                            Integer.parseInt(portArray[1]))) {
                        return true;
                    }
                }
                if (paramPort.contains(PacmanRuleConstants.DELIMITER_MINUS) &&
                        port.contains(PacmanRuleConstants.DELIMITER_MINUS)) {
                    String[] portArrayJson = port.split(PacmanRuleConstants.DELIMITER_MINUS);
                    final Range<Integer> range = Range.between(Integer.parseInt(portArrayJson[0]),
                            Integer.parseInt(portArrayJson[1]));
                    String[] portArray = paramPort.split(PacmanRuleConstants.DELIMITER_MINUS);
                    if (isOverlappedBy(range, Integer.parseInt(portArray[0]), Integer.parseInt(portArray[1]))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isOverlappedBy(final Range<Integer> otherRange, Integer lowerBound, Integer upperBound) {
        if (otherRange == null) {
            return false;
        }
        return otherRange.contains(lowerBound) || otherRange.contains(upperBound);
    }

    private static boolean isInClosedRange(Integer number, Integer lowerBound, Integer upperBound) {
        final Range<Integer> range = Range.between(lowerBound, upperBound);
        return range.contains(number);
    }
}
