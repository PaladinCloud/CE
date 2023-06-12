package com.tmobile.pacman.api.admin.repository.service;

import com.tmobile.pacman.api.admin.service.CommonService;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.tmobile.pacman.api.admin.common.AdminConstants.ALLOW_NO_INDICES_PARAM;
import static com.tmobile.pacman.api.admin.common.AdminConstants.DELIMITER_QUESTION_MARK;
import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;

@Service
public class EsCommonServiceImpl implements EsCommonService{

    private static final Logger logger = LoggerFactory.getLogger(EsCommonServiceImpl.class);
    private final CommonService commonService;

    public EsCommonServiceImpl(CommonService commonService) {
        this.commonService = commonService;
    }

    @Override
    public boolean isDataStreamOrIndexOrAliasExists(String target) {
        try {
            Response response = commonService.invokeAPI(HttpMethod.GET.name(), target + DELIMITER_QUESTION_MARK
                    + ALLOW_NO_INDICES_PARAM, null);
            if (Objects.isNull(response) || Objects.isNull(response.getStatusLine())) {
                return false;
            }
            return response.getStatusLine().getStatusCode() == 200;
        } catch (Exception exception) {
            logger.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return false;
        }
    }
}
