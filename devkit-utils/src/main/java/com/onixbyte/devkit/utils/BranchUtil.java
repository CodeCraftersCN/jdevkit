/*
 * Copyright (C) 2024-2025 OnixByte.
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

package com.onixbyte.devkit.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * The {@link BranchUtil} class provides static methods to simplify conditional logic in Java
 * development by leveraging lambda expressions. It offers convenient methods to replace verbose
 * {@code if...else} statements with more concise and expressive functional constructs.
 * <p>
 * Developers can use methods in this utility class to streamline their code, enhance readability,
 * and promote a more functional style of programming when dealing with branching logic and
 * conditional statements.
 * <p>
 * <b>Example:</b>
 * <pre>
 * // If you want to simplify an if (exp1 || exp2), you can use the following code:
 * String r1 = BranchUtil.or(1 == 1, 2 == 1)
 *     .handle(() -> "1 is equal to 1 or 2 is equal to 1.");
 *
 * // If you have an else branch, you can use the following code:
 * String r2 = BranchUtil.or(1 == 1, 2 == 1)
 *     .handle(() -> "1 is equal to 1 or 2 is equal to 1.",
 *             () -> "1 is not equal to 1 and 2 is not equal to 1.");
 *
 * // If you only need to execute code without a return value:
 * BranchUtil.or(1 == 1, 2 == 1)
 *     .handle(() -> {
 *         // do something
 *     }, () -> {
 *         // do something
 *     });
 * // If you only need an if branch, you can remove the second Supplier instance.
 *
 * // To check if all boolean expressions are true, use the 'and' method:
 * BranchUtil.and(1 == 1, 2 == 1)
 *     .handle(() -> {
 *         // do something
 *     }, () -> {
 *         // do something
 *     });
 * </pre>
 * <p>
 * <b>Note:</b>
 * The {@link #and(Boolean...)} and {@link #or(Boolean...)} methods accept any number of boolean
 * expressions.
 *
 * @param <T> the type of the result to be handled by the methods
 * @author zihluwang
 * @version 1.6.1
 * @see java.util.function.Supplier
 * @see java.util.function.BooleanSupplier
 * @see java.lang.Runnable
 * @since 1.0.0
 */
public final class BranchUtil<T> {

    private final static Logger log = LoggerFactory.getLogger(BranchUtil.class);

    /**
     * Create a {@code BranchUtil} instance.
     *
     * @param result the result of the boolean expressions.
     */
    private BranchUtil(boolean result) {
        this.result = result;
    }

    /**
     * Creates a {@code BranchUtil} instance to evaluate a logical OR operation on the provided
     * boolean expressions.
     *
     * @param values the boolean expressions to be evaluated
     * @param <T>    the type of the result to be handled by the methods
     * @return a {@code BranchUtil} instance representing the result of the logical OR operation
     */
    public static <T> BranchUtil<T> or(Boolean... values) {
        return new BranchUtil<>(BoolUtil.or(values));
    }

    /**
     * Creates a {@code BranchUtil} instance to evaluate a logical AND operation on the provided
     * boolean expressions.
     *
     * @param values the boolean expressions to be evaluated
     * @param <T>    the type of the result to be handled by the methods
     * @return a {@code BranchUtil} instance representing the result of the logical AND operation
     */
    public static <T> BranchUtil<T> and(Boolean... values) {
        return new BranchUtil<>(BoolUtil.and(values));
    }

    /**
     * Creates a {@code BranchUtil} instance to evaluate a logical OR operation on the provided
     * boolean suppliers.
     *
     * @param valueSuppliers the boolean suppliers to be evaluated
     * @param <T>            the type of the result to be handled by the methods
     * @return a {@code BranchUtil} instance representing the result of the
     * logical OR operation
     */
    public static <T> BranchUtil<T> or(BooleanSupplier... valueSuppliers) {
        return new BranchUtil<>(BoolUtil.or(valueSuppliers));
    }

    /**
     * Creates a {@code BranchUtil} instance to evaluate a logical AND operation on the provided
     * boolean suppliers.
     *
     * @param valueSuppliers the boolean suppliers to be evaluated
     * @param <T>            the type of the result to be handled by the methods
     * @return a {@code BranchUtil} instance representing the result of the
     * logical AND operation
     */
    public static <T> BranchUtil<T> and(BooleanSupplier... valueSuppliers) {
        return new BranchUtil<>(BoolUtil.and(valueSuppliers));
    }

    /**
     * Handles the result of the boolean expressions by executing the appropriate handler based
     * on the result.
     * <p>
     * If the result is {@code true}, the {@code ifHandler} is executed. If the result is
     * {@code false} and an {@code elseHandler} is provided, it is executed.
     * <p>
     * Returns the result of the executed handler.
     *
     * @param ifHandler   the handler to be executed if the result is {@code true}
     * @param elseHandler the handler to be executed if the result is {@code false} (optional)
     * @return the result of the executed handler, or {@code null} if no {@code elseHandler} is
     * provided and the result of the evaluation is {@code false}
     */
    public T handle(Supplier<T> ifHandler, Supplier<T> elseHandler) {
        if (this.result && Objects.nonNull(ifHandler)) {
            return ifHandler.get();
        }

        if (Objects.isNull(elseHandler)) {
            return null;
        }

        return elseHandler.get();
    }

    /**
     * Handles the result of the boolean expressions by executing the provided handler if the
     * result is {@code true}.
     * <p>
     * Returns the result of the executed handler.
     *
     * @param ifHandler the handler to be executed if the result is {@code true}
     * @return the result of the executed handler, or {@code null} if result of evaluation is
     * {@code false}
     */
    public T handle(Supplier<T> ifHandler) {
        return handle(ifHandler, null);
    }

    /**
     * Handles the result of the boolean expressions by executing the appropriate handler based
     * on the result.
     * <p>
     * If the result is {@code true}, the {@code ifHandler} is executed. If the result is
     * {@code false} and an {@code elseHandler} is provided, it is executed.
     *
     * @param ifHandler   the handler to be executed if the result is {@code true}
     * @param elseHandler the handler to be executed if the result is {@code false} (optional)
     */
    public void handle(Runnable ifHandler, Runnable elseHandler) {
        if (this.result && Objects.nonNull(ifHandler)) {
            ifHandler.run();
            return;
        }

        if (Objects.isNull(elseHandler)) {
            return;
        }

        elseHandler.run();
    }

    /**
     * Handles the result of the boolean expressions by executing the provided handler if the
     * result is {@code true}.
     *
     * @param ifHandler the handler to be executed if the result is {@code true}
     */
    public void handle(Runnable ifHandler) {
        handle(ifHandler, null);
    }

    /**
     * The final result of the boolean expression.
     */
    private final boolean result;

    /**
     * Get the boolean result.
     *
     * @return the result
     */
    public boolean getResult() {
        return result;
    }

}
