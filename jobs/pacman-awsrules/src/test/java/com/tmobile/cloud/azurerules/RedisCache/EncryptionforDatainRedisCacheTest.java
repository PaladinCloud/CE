package com.tmobile.cloud.azurerules.RedisCache;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.azurerules.MicrosoftSqlDatabase.UnrestrictedSqlDatabaseAccessRule;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import org.junit.Test;
import org.mockito.InjectMocks;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

public class EncryptionforDatainRedisCacheTest {

    @InjectMocks
    EncryptionforDatainRedisCache encryptionforDatainRedisCache;

    @Test
    public void executeTest() throws Exception {
        mockStatic(PacmanUtils.class);
        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(
                        true);
        when(PacmanUtils.formatUrl(anyObject(), anyString())).thenReturn("host");
        when(PacmanUtils.checkAccessibleToAll(anyObject(), anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(CommonTestUtils.getMapBoolean("r_123 "));
        assertThat(encryptionforDatainRedisCache.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")), is(notNullValue()));

        when(PacmanUtils.checkAccessibleToAll(anyObject(), anyString(), anyString(), anyString(), anyString(),
                anyString())).thenReturn(CommonTestUtils.getEmptyMapBoolean("r_123 "));
        assertThat(encryptionforDatainRedisCache.execute(CommonTestUtils.getMapString("r_123 "),
                CommonTestUtils.getMapString("r_123 ")), is(notNullValue()));

        when(PacmanUtils.checkAccessibleToAll(anyObject(), anyString(), anyString(), anyString(), anyString(),
                anyString())).thenThrow(new Exception());
        assertThatThrownBy(
                () -> encryptionforDatainRedisCache.execute(CommonTestUtils.getMapString("r_123 "),
                        CommonTestUtils.getMapString("r_123 ")))
                .isInstanceOf(RuleExecutionFailedExeption.class);

        assertThatThrownBy(
                () -> encryptionforDatainRedisCache.execute(CommonTestUtils.getOneMoreMapString("r_123 "),
                        CommonTestUtils.getMapString("r_123 ")))
                .isInstanceOf(RuleExecutionFailedExeption.class);

        when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(
                        false);
        assertThatThrownBy(
                () -> encryptionforDatainRedisCache.execute(CommonTestUtils.getMapString("r_123 "),
                        CommonTestUtils.getMapString("r_123 ")))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    public void getHelpTextTest() {
        assertThat(encryptionforDatainRedisCache.getHelpText(), is(notNullValue()));
    }
}
