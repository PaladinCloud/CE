package com.paladincloud.common;

public record ProcessingDoneMessage(String jobName, String tenantId, String source) {

}
