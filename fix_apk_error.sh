#!/bin/bash

# Fix APK Asset Path Error - Clean Build Script
# This script will clean all build artifacts and rebuild the app

echo "🧹 Cleaning build directories..."

# Navigate to project root
cd /home/violet-nyirenda/AndroidStudioProjects/Student-Intelligent-System

# Clean all build artifacts
./gradlew clean

echo "🗑️  Removing build cache..."
rm -rf app/build
rm -rf app/.caches
rm -rf build
rm -rf .gradle

echo "✨ Invalidating caches..."
# Remove Android Studio caches
rm -rf ~/.gradle/caches

echo "🔄 Syncing Gradle..."
./gradlew --refresh-dependencies

echo "🔨 Building clean APK..."
./gradlew assembleDebug

echo "✅ Done! Now try installing the APK again."

