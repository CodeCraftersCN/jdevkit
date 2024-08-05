/*
 * Copyright (C) 2024-2024 OnixByte.
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

package com.onixbyte.simplejwt.config;

import com.onixbyte.simplejwt.TokenResolver;
import com.onixbyte.simplejwt.constants.TokenAlgorithm;

import java.util.List;

/**
 * The {@code TokenResolverConfig} provides a mechanism to configure an
 * implementation of {@link TokenResolver} with algorithm functions.
 * <p>
 * This generic interface is used to define the configuration details for a
 * {@link TokenResolver} that utilizes algorithm functions. The interface
 * allows for specifying algorithm functions corresponding to different {@link
 * TokenAlgorithm} instances supported by the {@link TokenResolver}
 * implementation.
 *
 * @param <Algo> the type representing algorithm functions used by the
 *               {@link TokenResolver}
 * @author Zihlu Wang
 * @version 1.0.0
 * @since 1.0.0
 */
public interface TokenResolverConfig<Algo> {

    /**
     * Gets the algorithm function corresponding to the specified {@link
     * TokenAlgorithm}.
     * <p>
     * This method returns the algorithm function associated with the given
     * {@link TokenAlgorithm}. The provided TokenAlgorithm represents the
     * specific algorithm for which the corresponding algorithm function is
     * required. The returned {@code Algo} represents the function
     * implementation that can be used by the {@link TokenResolver} to handle
     * the specific algorithm.
     *
     * @param algorithm the {@link TokenAlgorithm} for which the algorithm function is required
     * @return the algorithm function associated with the given {@link TokenAlgorithm}
     */
    Algo getAlgorithm(TokenAlgorithm algorithm);

    List<TokenAlgorithm> ECDSA_ALGORITHMS =
            List.of(TokenAlgorithm.ES256, TokenAlgorithm.ES384, TokenAlgorithm.ES512);

    List<TokenAlgorithm> HMAC_ALGORITHMS =
            List.of(TokenAlgorithm.HS256, TokenAlgorithm.HS384, TokenAlgorithm.HS512);

}
