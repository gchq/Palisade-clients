/*
 * Copyright 2018-2021 Crown Copyright
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.client.shell.shell;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.jline.reader.EndOfFileException;
import org.springframework.shell.ExitRequest;
import org.springframework.shell.result.ThrowableResultHandler;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * Spring shell does not follow standard UNIX behaviour
 */
@Aspect
@SuppressWarnings("java:S3011") // Reflect over declared (non-public) fields
public class ExceptionHandlingAspect {
    private static List<Field> superclassFields = Arrays.asList(ThrowableResultHandler.class.getSuperclass().getDeclaredFields());

    /**
     * Around aspect for method calls, new-prompt on Ctrl-C, exit on Ctrl-D
     *
     * @param joinPoint method call metadata
     * @return the joinPoint procession
     * @throws Throwable if the joinPoint procession throws
     */
    @Around("execution(* org.springframework.shell.result.ThrowableResultHandler.*(..))")
    public Object handleMethodCall(final ProceedingJoinPoint joinPoint) throws Throwable {
        // For whatever reason, superclass fields are not proxied correctly
        // Copy protected superclass fields from target (real) object to this (proxy) object
        for (Field field : superclassFields) {
            field.setAccessible(true);
            field.set(joinPoint.getThis(), field.get(joinPoint.getTarget()));
            field.setAccessible(false);
        }
        // Inspect exception handler argument
        Throwable result = (Throwable) joinPoint.getArgs()[0];
        if (result instanceof EndOfFileException) {
            // Respect Ctrl-D
            System.exit(0);
        } else if (result instanceof ExitRequest) {
            // Respect Ctrl-C
            return null;
        }
        // Proceed as usual
        return joinPoint.proceed(joinPoint.getArgs());
    }
}
