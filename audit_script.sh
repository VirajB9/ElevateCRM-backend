#!/bin/bash
echo "--- Modules ---"
ls -d src/main/java/com/viraj/dmabackend/*
echo "--- Controllers ---"
find src -name "*Controller.java"
echo "--- Services ---"
find src -name "*Service.java"
find src -name "*ServiceImpl.java"
echo "--- Repositories ---"
find src -name "*Repository.java"
echo "--- Entities ---"
find src -name "entity" -type d | xargs -I {} find {} -name "*.java"
echo "--- Security Config ---"
cat src/main/java/com/viraj/dmabackend/config/SecurityConfig.java
echo "--- JWT Filter ---"
cat src/main/java/com/viraj/dmabackend/auth/security/JwtFilter.java
echo "--- Rate Limit ---"
cat src/main/java/com/viraj/dmabackend/config/RateLimitFilter.java
echo "--- GlobalExceptionHandler ---"
cat src/main/java/com/viraj/dmabackend/exception/GlobalExceptionHandler.java
