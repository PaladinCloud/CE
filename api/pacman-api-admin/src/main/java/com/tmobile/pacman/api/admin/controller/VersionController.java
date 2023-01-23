package com.tmobile.pacman.api.admin.controller;

        import com.tmobile.pacman.api.admin.domain.Response;
        import com.tmobile.pacman.api.admin.repository.service.ConfigPropertyService;
        import com.tmobile.pacman.api.commons.utils.ResponseUtils;
        import io.swagger.annotations.Api;
        import io.swagger.annotations.ApiOperation;
        import org.slf4j.Logger;
        import org.slf4j.LoggerFactory;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.MediaType;
        import org.springframework.http.ResponseEntity;
        import org.springframework.security.access.prepost.PreAuthorize;
        import org.springframework.web.bind.annotation.*;
        import static com.tmobile.pacman.api.admin.common.AdminConstants.UNEXPECTED_ERROR_OCCURRED;


@Api(value = "/version", consumes = "application/json", produces = "application/json")
@RestController
@PreAuthorize("@securityService.hasPermission(authentication, 'readonly') or #oauth2.hasScope('API_OPERATION/READ')")

@RequestMapping("/version")
public class VersionController {

    private static final Logger log = LoggerFactory.getLogger(VersionController.class);

    @Autowired
    private ConfigPropertyService configPropertyService;

    @ApiOperation(httpMethod = "GET", value = "API to get current release version", response = Response.class, produces = MediaType.APPLICATION_JSON_VALUE)
    @RequestMapping(path = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> getCurrentVersion() {
        try {
            return ResponseUtils.buildSucessResponse(configPropertyService.listProperty("current-release",null));

        } catch (Exception exception) {
            log.error(UNEXPECTED_ERROR_OCCURRED, exception);
            return ResponseUtils.buildFailureResponse(exception, null, null);
        }
    }
}
