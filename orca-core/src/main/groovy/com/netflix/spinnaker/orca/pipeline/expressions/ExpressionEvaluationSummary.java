/*
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.orca.pipeline.expressions;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Represents an evaluation summary
 * Includes INO and ERROR Level messages
 * ERROR is anything evaluation that results in thrown Exception
 * INFO unresolved value returned that doesn't throw
 */
public class ExpressionEvaluationSummary {
  private Map<String, List<Result>> expressionResult;
  private Set<String> attempts;
  private AtomicInteger failureCount;
  private AtomicInteger totalEvaluated;

  public ExpressionEvaluationSummary() {
    this.expressionResult = new HashMap<>();
    this.failureCount = new AtomicInteger();
    this.totalEvaluated = new AtomicInteger();
    this.attempts = new HashSet<>();
  }

  public int getTotalEvaluated() {
    return totalEvaluated.get();
  }

  public int getFailureCount() {
    return failureCount.get();
  }

  public Map<String, List<Result>> getExpressionResult() {
    return expressionResult;
  }

  public void add(String escapedExpression, Result.Level level, String description, Class<?> exceptionType) {
    List<Result> messages = expressionResult.getOrDefault(escapedExpression, new ArrayList<>());
    messages.add(new Result(level, System.currentTimeMillis(), description, exceptionType));
    expressionResult.put(escapedExpression, messages);
    failureCount.incrementAndGet();
  }

  public void incrementTotalEvaluated() {
    totalEvaluated.incrementAndGet();
  }

  public void appendAttempted(String expression) {
    attempts.add(expression);
  }

  public String toString() {
    String attempted = attempts.stream().collect(Collectors.joining(","));
    String failed = expressionResult.keySet().stream().collect(Collectors.joining(","));
    return String.format("Evaluated %d expression(s) - (%s), %d failed - (%s)",
      getTotalEvaluated(),
      attempted,
      getFailureCount(),
      failed
    );
  }

  static class Result {
    private String description;
    private Class<?> exceptionType;
    private long timestamp;
    private Level level;

    enum Level {
      ERROR,
      INFO
    }

    public Result(Level level, long timestamp, String description, Class<?> exceptionType) {
      this.level = level;
      this.timestamp = timestamp;
      this.description = description;
      this.exceptionType = exceptionType;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public long getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(long timestamp) {
      this.timestamp = timestamp;
    }

    public Level getLevel() {
      return level;
    }

    public void setLevel(Level level) {
      this.level = level;
    }

    public Class<?> getExceptionType() {
      return exceptionType;
    }

    public void setExceptionType(Class<?> exceptionType) {
      this.exceptionType = exceptionType;
    }

    @Override
    public String toString() {
      return "Result {" +
        "description='" + description + '\'' +
        ", exceptionType=" + exceptionType +
        ", timestamp=" + timestamp +
        ", level=" + level +
        '}';
    }
  }
}