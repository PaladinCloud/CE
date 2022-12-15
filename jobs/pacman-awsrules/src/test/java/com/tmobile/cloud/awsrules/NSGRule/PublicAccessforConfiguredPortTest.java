package com.tmobile.cloud.awsrules.NSGRule;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.tmobile.cloud.awsrules.utils.CommonTestUtils;
import com.tmobile.cloud.awsrules.utils.PacmanUtils;
import com.tmobile.cloud.azurerules.NSGRule.PublicAccessforConfiguredPort;
import com.tmobile.pacman.commons.exception.InvalidInputException;
import com.tmobile.pacman.commons.exception.RuleExecutionFailedExeption;
import com.tmobile.pacman.commons.policy.BasePolicy;

@PowerMockIgnore({ "javax.net.ssl.*", "javax.management.*" })
@RunWith(PowerMockRunner.class)
@PrepareForTest({ PacmanUtils.class, BasePolicy.class })
public class PublicAccessforConfiguredPortTest {
        @InjectMocks
        PublicAccessforConfiguredPort postgreeSecurityRule;

        @Test
        public void executeTest() throws Exception {
                mockStatic(PacmanUtils.class);
                when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString(),
                                anyString()))
                                .thenReturn(
                                                true);
                when(PacmanUtils.formatUrl(anyObject(), anyString())).thenReturn("host");
                when(PacmanUtils.checkAccessibleToAll(anyObject(), anyString(), anyString(), anyString(), anyString(),
                                anyString())).thenReturn(CommonTestUtils.getMapBoolean("r_123 "));
                assertThat(postgreeSecurityRule.execute(CommonTestUtils.getMapString("r_123 "),
                                CommonTestUtils.getMapString("r_123 ")), is(notNullValue()));

                when(PacmanUtils.checkAccessibleToAll(anyObject(), anyString(), anyString(), anyString(), anyString(),
                                anyString())).thenReturn(CommonTestUtils.getEmptyMapBoolean("r_123 "));
                assertThat(postgreeSecurityRule.execute(CommonTestUtils.getMapString("r_123 "),
                                CommonTestUtils.getMapString("r_123 ")), is(notNullValue()));

                when(PacmanUtils.checkAccessibleToAll(anyObject(), anyString(), anyString(), anyString(), anyString(),
                                anyString())).thenThrow(new Exception());
                assertThatThrownBy(
                                () -> postgreeSecurityRule.execute(CommonTestUtils.getMapString("r_123 "),
                                                CommonTestUtils.getMapString("r_123 ")))
                                .isInstanceOf(RuleExecutionFailedExeption.class);

                assertThatThrownBy(
                                () -> postgreeSecurityRule.execute(CommonTestUtils.getOneMoreMapString("r_123 "),
                                                CommonTestUtils.getMapString("r_123 ")))
                                .isInstanceOf(RuleExecutionFailedExeption.class);

                when(PacmanUtils.doesAllHaveValue(anyString(), anyString(), anyString(), anyString(), anyString(),
                                anyString()))
                                .thenReturn(
                                                false);
                assertThatThrownBy(
                                () -> postgreeSecurityRule.execute(CommonTestUtils.getMapString("r_123 "),
                                                CommonTestUtils.getMapString("r_123 ")))
                                .isInstanceOf(InvalidInputException.class);
        }

        @Test
        public void getHelpTextTest() {
                assertThat(postgreeSecurityRule.getHelpText(), is(notNullValue()));
        }

}
