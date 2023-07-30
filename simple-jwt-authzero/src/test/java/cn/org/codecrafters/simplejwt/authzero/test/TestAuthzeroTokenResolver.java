/*
 * Copyright (C) 2023 CodeCraftersCN.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.org.codecrafters.simplejwt.authzero.test;

import cn.org.codecrafters.simplejwt.authzero.AuthzeroTokenResolver;
import cn.org.codecrafters.simplejwt.constants.TokenAlgorithm;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * TestAuthzeroTokenResolver
 *
 * @author Zihlu Wang
 */
public class TestAuthzeroTokenResolver {

    @Test
    public void test01() {
        var tokenResolver = new AuthzeroTokenResolver(TokenAlgorithm.HS384, "Test Issuer", "Test Secret");
        var testToken = tokenResolver.createToken(Duration.ofMinutes(30), "Test Audience", "User00001");
        System.out.println(testToken);
    }

}
