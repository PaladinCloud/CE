package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.domain.AssetGroupExceptionDetailsRequest;
import com.tmobile.pacman.api.admin.repository.model.AssetGroupException;
import com.tmobile.pacman.api.admin.repository.model.Policy;
import com.tmobile.pacman.api.admin.repository.model.PolicyExemption;

import static com.tmobile.pacman.api.commons.Constants.*;


import java.util.List;

public interface NotificationService {
    void triggerNotificationForCreateStickyEx(AssetGroupExceptionDetailsRequest assetGroupExceptionDetails, String userId, String subject, List<String> policyIds, Actions action);
    void triggerNotificationForDelStickyException(AssetGroupException assetGroupException, String userId, String subject, String deletedBy);
    void triggerNotificationForEnableDisablePolicy(Policy policy, PolicyExemption exemption);
}
