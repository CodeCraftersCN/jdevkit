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

package com.onixbyte.simplejwt.authzero;

import com.onixbyte.devkit.utils.Base64Util;
import com.onixbyte.guid.GuidCreator;
import com.onixbyte.simplejwt.SecretCreator;
import com.onixbyte.simplejwt.TokenPayload;
import com.onixbyte.simplejwt.TokenResolver;
import com.onixbyte.simplejwt.annotations.ExcludeFromPayload;
import com.onixbyte.simplejwt.annotations.TokenEnum;
import com.onixbyte.simplejwt.authzero.config.AuthzeroTokenResolverConfig;
import com.onixbyte.simplejwt.config.TokenResolverConfig;
import com.onixbyte.simplejwt.constants.PredefinedKeys;
import com.onixbyte.simplejwt.constants.TokenAlgorithm;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * The {@code AuthzeroTokenResolver} class is an implementation of the {@link TokenResolver}
 * interface. It uses the {@code com.auth0:java-jwt} library to handle JSON Web Token (JWT)
 * resolution. This resolver provides functionality to create, extract, verify, and renew JWT
 * tokens using various algorithms and custom payload data.
 * <p>
 * <b>Usage:</b>
 * To use the {@code AuthzeroTokenResolver}, first, create an instance of this class:
 * <pre>{@code
 * TokenResolver<DecodedJWT> tokenResolver =
 *     new AuthzeroTokenResolver(TokenAlgorithm.HS256,
 *                               "Token Subject",
 *                               "Token Issuer",
 *                               "Token Secret");
 * }</pre>
 * <p>
 * Then, you can utilize the various methods provided by this resolver to handle JWT tokens:
 * <pre>{@code
 * // Creating a new JWT token
 * String token =
 *     tokenResolver.createToken(Duration.ofHours(1),
 *                               "your_subject",
 *                               "your_audience",
 *                               customPayloads);
 *
 * // Extracting payload data from a JWT token
 * DecodedJWT decodedJWT = tokenResolver.resolve(token);
 * T payloadData = decodedJWT.extract(token, T.class);
 *
 * // Renewing an existing JWT token
 * String renewedToken =
 *     tokenResolver.renew(token, Duration.ofMinutes(30), customPayloads);
 * }</pre>
 * <p>
 * <b>Note:</b>
 * It is essential to configure the appropriate algorithms, secret, and issuer according to your
 * specific use case when using this resolver. Additionally, ensure that the
 * {@code com.auth0:java-jwt} library is correctly configured in your project's dependencies.
 *
 * @author Zihlu Wang
 * @version 1.1.1
 * @see GuidCreator
 * @see Algorithm
 * @see JWTVerifier
 * @see JWTCreator
 * @see JWTCreator.Builder
 * @since 1.0.0
 */
@Slf4j
public class AuthzeroTokenResolver implements TokenResolver<DecodedJWT> {

    /**
     * Creates a new instance of {@code AuthzeroTokenResolver} with the provided configurations.
     *
     * @param jtiCreator   the {@link GuidCreator} used for generating unique identifiers for "jti"
     *                     claim in JWT tokens
     * @param algorithm    the algorithm used for signing and verifying JWT tokens
     * @param issuer       the issuer claim value to be included in JWT tokens
     * @param privateKey   the secret used for HMAC-based algorithms (HS256, HS384, HS512) for
     *                     token signing and verification, or the private key for ECDSA-based
     *                     algorithms
     * @param publicKey    the public key for ECDSA-based algorithms
     * @param objectMapper JSON handler
     */
    public AuthzeroTokenResolver(GuidCreator<?> jtiCreator,
                                 TokenAlgorithm algorithm,
                                 String issuer,
                                 String privateKey,
                                 String publicKey,
                                 ObjectMapper objectMapper) {
        if (TokenResolverConfig.HMAC_ALGORITHMS.contains(algorithm)) {
            if (privateKey == null || privateKey.isBlank()) {
                throw new IllegalArgumentException("A secret is required to build a JSON Web Token.");
            }

            if (privateKey.length() < 32) {
                log.warn("The provided secret which owns {} characters is too weak. Please consider" +
                        " replacing it with a stronger one.", privateKey.length());
            }
        }

        this.jtiCreator = jtiCreator;
        this.algorithm = config
                .getAlgorithm(algorithm)
                .apply(privateKey, publicKey);
        this.issuer = issuer;
        this.verifier = JWT.require(this.algorithm).build();
        this.objectMapper = objectMapper;
    }

    /**
     * Creates a new instance of {@link AuthzeroTokenResolver} with the provided configurations
     * and a simple UUID GuidCreator.
     *
     * @param algorithm    the algorithm used for signing and verifying JWT tokens
     * @param issuer       the issuer claim value to be included in JWT tokens
     * @param privateKey   the secret used for HMAC-based algorithms (HS256, HS384, HS512) for
     *                     token signing and verification, or the private key for ECDSA-based
     *                     algorithms
     * @param publicKey    the public key for ECDSA-based algorithms
     * @param objectMapper Jackson Databind JSON Handler
     */
    public AuthzeroTokenResolver(TokenAlgorithm algorithm,
                                 String issuer,
                                 String privateKey,
                                 String publicKey,
                                 ObjectMapper objectMapper) {
        this(UUID::randomUUID, algorithm, issuer, privateKey, publicKey, objectMapper);
    }

    /**
     * Creates a new instance of {@link AuthzeroTokenResolver} with the provided configurations
     * and a simple UUID GuidCreator.
     *
     * @param algorithm  the algorithm used for signing and verifying JWT tokens
     * @param issuer     the issuer claim value to be included in JWT tokens
     * @param privateKey the secret used for HMAC-based algorithms (HS256, HS384, HS512) for
     *                   token signing and verification, or the private key for ECDSA-based
     *                   algorithms
     * @param publicKey  the public key for ECDSA-based algorithms
     */
    public AuthzeroTokenResolver(TokenAlgorithm algorithm, String issuer, String privateKey, String publicKey) {
        this(UUID::randomUUID, algorithm, issuer, privateKey, publicKey, new ObjectMapper());
    }

    /**
     * Creates a new instance of {@link AuthzeroTokenResolver} with the
     * provided configurations, HMAC256 algorithm and a simple
     * UUID GuidCreator.
     *
     * @param issuer the issuer claim value to be included in JWT tokens
     * @param secret the secret used for HS256 algorithms for token signing and verification
     */
    public AuthzeroTokenResolver(String issuer, String secret) {
        this(UUID::randomUUID, TokenAlgorithm.HS256, issuer, secret, "", new ObjectMapper());
    }

    /**
     * Creates a new instance of {@link AuthzeroTokenResolver} with the
     * provided configurations, HMAC256 algorithm and a simple
     * UUID GuidCreator.
     *
     * @param issuer the issuer claim value to be included in JWT tokens
     */
    public AuthzeroTokenResolver(String issuer) {
        var secret = SecretCreator.createSecret(32, true, true, true);

        this.jtiCreator = UUID::randomUUID;
        this.algorithm = config
                .getAlgorithm(TokenAlgorithm.HS256)
                .apply(secret, "");
        this.issuer = issuer;
        this.verifier = JWT.require(this.algorithm).build();
        this.objectMapper = new ObjectMapper();

        log.info("The secret has been set to {}.", secret);
    }

    /**
     * Creates a new token with the specified expiration duration, subject, and
     * audience.
     *
     * @param expireAfter the duration after which the token will expire
     * @param subject     the subject of the token
     * @param audience    the audience for which the token is intended
     * @return the generated token as a {@code String}
     */
    @Override
    public String createToken(Duration expireAfter, String audience, String subject) {
        final var builder = JWT.create();
        buildBasicInfo(builder, expireAfter, subject, audience);
        return buildToken(builder);
    }

    /**
     * Creates a new token with the specified expiration time, subject,
     * audience, and custom payload data.
     *
     * @param expireAfter the duration after which the token will expire
     * @param subject     the subject of the token
     * @param audience    the audience for which the token is intended
     * @param payloads    the custom payload data to be included in the token
     * @return the generated token as a {@code String}
     */
    @Override
    public String createToken(Duration expireAfter, String audience, String subject, Map<String, Object> payloads) {
        // Create token.
        final var builder = JWT.create();
        buildBasicInfo(builder, expireAfter, subject, audience);
        buildMapClaims(builder, payloads);
        return buildToken(builder);
    }

    /**
     * Creates a new token with the specified expiration time, subject,
     * audience, and strongly-typed payload data.
     *
     * @param expireAfter the duration after which the token will expire
     * @param subject     the subject of the token
     * @param audience    the audience for which the token is intended
     * @param payload     the strongly-typed payload data to be included in the
     *                    token
     * @return the generated token as a {@code String}
     */
    @Override
    public <T extends TokenPayload> String createToken(Duration expireAfter, String audience, String subject, T payload) {
        final JWTCreator.Builder builder = JWT.create();
        buildBasicInfo(builder, expireAfter, subject, audience);

        var payloadClass = payload.getClass();
        var fields = payloadClass.getDeclaredFields();

        for (var field : fields) {
            try {
                var fieldName = field.getName();
                // Skip the fields which are annotated with ExcludeFromPayload
                if (field.isAnnotationPresent(ExcludeFromPayload.class))
                    continue;

                Object invokeObj = payload;
                var getter = payloadClass.getDeclaredMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
                if (field.isAnnotationPresent(TokenEnum.class)) {
                    var tokenEnum = field.getAnnotation(TokenEnum.class);
                    invokeObj = getter.invoke(payload);
                    getter = field.getType().getDeclaredMethod("get" + tokenEnum.propertyName().substring(0, 1).toUpperCase() + tokenEnum.propertyName().substring(1));
                }

                // Build Claims
                addClaim(builder, fieldName, getter.invoke(invokeObj));
            } catch (IllegalAccessException e) {
                log.error("Cannot access field {}!", field.getName());
            } catch (NoSuchMethodException e) {
                log.error("Unable to find setter according to given field name.", e);
            } catch (InvocationTargetException e) {
                log.info("Cannot invoke method.", e);
            }
        }

        return buildToken(builder);
    }

    /**
     * Resolves the given token into a {@link DecodedJWT} object.
     *
     * @param token the token to be resolved
     * @return a {@link DecodedJWT} object
     */
    @Override
    public DecodedJWT resolve(String token) {
        return verifier.verify(token);
    }

    /**
     * Extracts the payload information from the given token and maps it to the
     * specified target type.
     *
     * @param token      the token from which to extract the payload
     * @param targetType the target class representing the payload data type
     * @return an instance of the specified target type with the extracted
     * payload data, or {@code null} when extraction fails
     */
    @Override
    public <T extends TokenPayload> T extract(String token, Class<T> targetType) {
        try {
            // Get claims from token.
            var payloads = objectMapper.readValue(Base64Util.decodeUrlComponents(resolve(token).getPayload()), new MapTypeReference());
            // Get the no-argument constructor to create an instance.
            var bean = targetType.getConstructor().newInstance();

            for (var entry : payloads.entrySet()) {
                // Jump all JWT pre-defined properties and the fields that are annotated to be excluded.
                if (PredefinedKeys.KEYS.contains(entry.getKey()) || targetType.getDeclaredField(entry.getKey()).isAnnotationPresent(ExcludeFromPayload.class))
                    continue;

                var field = targetType.getDeclaredField(entry.getKey());
                var setter = targetType.getDeclaredMethod("set" + entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1), field.getType());
                var fieldValue = entry.getValue();
                if (field.isAnnotationPresent(TokenEnum.class)) {
                    var annotation = field.getAnnotation(TokenEnum.class);
                    var enumStaticLoader = field.getType().getDeclaredMethod("loadBy" + annotation.propertyName().substring(0, 1).toUpperCase() + annotation.propertyName().substring(1), annotation.dataType().getMappedClass());
                    fieldValue = enumStaticLoader.invoke(null, fieldValue);
                }

                if (setter.canAccess(bean)) {
                    setter.invoke(bean, fieldValue);
                } else {
                    log.error("Setter for field {} can't be accessed.", entry.getKey());
                }
            }
            return bean;
        } catch (JsonProcessingException e) {
            log.error("Unable to read payload as a Map<String, Object>.", e);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            log.error("Unable to load the constructor or setter.", e);
        } catch (NoSuchFieldException e) {
            log.error("Unable to load the field.", e);
        }
        return null;
    }

    /**
     * Re-generate a new token with the payload in the old one.
     *
     * @param oldToken    the old token
     * @param expireAfter how long the new token can be valid for
     * @return re-generated token with the payload in the old one or
     * {@code null} if an {@link JsonProcessingException} occurred.
     */
    @Override
    public String renew(String oldToken, Duration expireAfter) {
        var resolved = resolve(oldToken);

        try {
            var payload = objectMapper.readValue(Base64Util.decodeUrlComponents(resolved.getPayload()), ObjectNode.class);
            payload.remove(PredefinedKeys.KEYS);

            var payloadMap = objectMapper.convertValue(payload, new MapTypeReference());
            return createToken(expireAfter, resolved.getAudience().get(0), resolved.getSubject(), payloadMap);
        } catch (JsonProcessingException e) {
            log.error("Cannot read payload content, error details:", e);
        }

        return null;
    }

    /**
     * Renews the given expired token with the specified custom payload data.
     *
     * @param oldToken the expired token to be renewed
     * @param payload  the custom payload data to be included in the renewed
     *                 token
     * @return the renewed token as a {@code String}
     */
    @Override
    public String renew(String oldToken, Duration expireAfter, Map<String, Object> payload) {
        final var resolvedToken = this.resolve(oldToken);
        var audience = resolvedToken.getAudience().get(0);

        return createToken(expireAfter, audience, resolvedToken.getSubject(), payload);
    }

    /**
     * Renews the given expired token with the specified custom payload data.
     *
     * @param oldToken the expiring token to be renewed
     * @param payload  the custom payload data to be included in the renewed
     *                 token
     * @return the renewed token as a {@code String}
     */
    @Override
    public String renew(String oldToken, Map<String, Object> payload) {
        return renew(oldToken, Duration.ofMinutes(30), payload);
    }

    /**
     * Renews the given expired token with the new specified strongly-typed
     * payload data.
     *
     * @param oldToken the expiring token to be renewed
     * @param payload  the strongly-typed payload data to be included in the
     *                 renewed token
     * @return the renewed token as a {@code String}
     */
    @Override
    public <T extends TokenPayload> String renew(String oldToken, Duration expireAfter, T payload) {
        final var resolvedToken = this.resolve(oldToken);
        var audience = resolvedToken.getAudience().get(0);

        return createToken(expireAfter, audience, resolvedToken.getSubject(), payload);
    }

    /**
     * Renews the given expired token with the new specified strongly-typed
     * payload data.
     *
     * @param <T>      the type of the payload data, must implement
     *                 {@link TokenPayload}
     * @param oldToken the expired token to be renewed
     * @param payload  the strongly-typed payload data to be included in the
     *                 renewed token
     * @return the renewed token as a {@link String}
     */
    @Override
    public <T extends TokenPayload> String renew(String oldToken, T payload) {
        return renew(oldToken, Duration.ofMinutes(30), payload);
    }

    /**
     * Builds the basic information of the JSON Web Token (JWT) using the
     * provided parameters and adds it to the JWTCreator.Builder.
     *
     * @param subject     the subject claim value to be included in the JWT
     * @param audience    an array of audience claim values to be included in
     *                    the JWT
     * @param expireAfter the duration after which the JWT will expire
     * @param builder     the JWTCreator.Builder instance to which the basic
     *                    information will be added
     */
    private void buildBasicInfo(JWTCreator.Builder builder, Duration expireAfter, String subject, String... audience) {
        var now = LocalDateTime.now();

        // bind issuer (iss)
        builder.withIssuer(issuer);
        // bind issued at (iat)
        builder.withIssuedAt(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
        // bind not before (nbf)
        builder.withNotBefore(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()));
        // bind audience (aud)
        builder.withAudience(audience);
        // bind subject (sub)
        builder.withSubject(subject);
        // bind expire at (exp)
        builder.withExpiresAt(Date.from(now.plus(expireAfter).atZone(ZoneId.systemDefault()).toInstant()));
        // bind JWT Id (jti)
        builder.withJWTId(jtiCreator.nextId().toString());
    }

    /**
     * Add a claim to a builder.
     *
     * @param builder the builder to build this JSON Web Token
     * @param name    the property name
     * @param value   the property value
     */
    private void addClaim(JWTCreator.Builder builder, String name, Object value) {
        if (Objects.nonNull(value)) {
            if (value instanceof Boolean v) {
                builder.withClaim(name, v);
            } else if (value instanceof Double v) {
                builder.withClaim(name, v);
            } else if (value instanceof Float v) {
                builder.withClaim(name, v.doubleValue());
            } else if (value instanceof Integer v) {
                builder.withClaim(name, v);
            } else if (value instanceof Long v) {
                builder.withClaim(name, v);
            } else if (value instanceof String v) {
                builder.withClaim(name, v);
            } else if (value instanceof Date v) {
                builder.withClaim(name, v);
            } else if (value instanceof List<?> v) {
                builder.withClaim(name, v);
            } else {
                log.warn("""
                        Unable to determine the type of field {}, we will handle it as a String.""", name);
                builder.withClaim(name, value.toString());
            }
        } else {
            builder.withNullClaim(name);
        }
    }

    /**
     * Builds the custom claims of the JSON Web Token (JWT) using the provided
     * Map of claims and adds them to the JWTCreator.Builder.
     * <p>
     * This method is used to add custom claims to the JWT. It takes a Map of
     * claims, where each entry represents a custom claim name (key) and its
     * corresponding value (value). The custom claims will be added to the JWT
     * using the JWTCreator.Builder.
     *
     * @param claims  a Map containing the custom claims to be added to the JWT
     * @param builder the JWTCreator.Builder instance to which the custom
     *                claims will be added
     */
    private void buildMapClaims(JWTCreator.Builder builder, Map<String, Object> claims) {
        if (Objects.nonNull(claims)) {
            for (var e : claims.entrySet()) {
                addClaim(builder, e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Finish creating a token.
     * <p>
     * This is the final step of create a token, to sign this token.
     *
     * @param builder the builder to build this JWT
     * @return the generated token as a {@code String}
     */
    private String buildToken(JWTCreator.Builder builder) {
        return builder.sign(algorithm);
    }

    /**
     * Default type reference for Map.
     */
    private static class MapTypeReference extends TypeReference<Map<String, Object>> {
        MapTypeReference() {
        }
    }

    /**
     * GuidCreator used for generating unique identifiers for "jti" claim in
     * JWT tokens.
     */
    private final GuidCreator<?> jtiCreator;

    /**
     * The algorithm used for signing and verifying JWT tokens.
     */
    private final Algorithm algorithm;

    /**
     * The issuer claim value to be included in JWT tokens.
     */
    private final String issuer;

    /**
     * The JSON Web Token resolver.
     */
    private final JWTVerifier verifier;

    /**
     * Jackson JSON handler.
     */
    private final ObjectMapper objectMapper;

    private final AuthzeroTokenResolverConfig config = AuthzeroTokenResolverConfig.getInstance();
}
