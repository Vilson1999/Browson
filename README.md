# Browson

<div align="center">
  <img src="https://raw.githubusercontent.com/Vilson1999/Browson/main/app/src/main/res/mipmap-hdpi/ic_launcher.png" alt="Browson app icon" width="180" />
</div>

## App Information

- App Name: Browson
- Version: 1.0
- Developer: son skr
- Platform: Android
- Package: com.aistudio.operawave.bwzrxy

## Description

Browson is a modern Android browser and productivity app built with Kotlin and Jetpack Compose. It combines a fast browsing experience with tools for downloads, privacy, ad blocking, video capture, and customization.

## Main Features

- Advanced Download Manager
  - Multi-threaded / segmented downloads
  - Pause, resume, and retry support
  - Download queue and progress tracking
  - File naming and MIME handling

- Video Grabber
  - Detect downloadable video sources from web pages
  - Quick capture support for supported streaming media
  - Site-based allowlist / denylist controls

- Ad Blocker with uBlock-style filtering
  - Blocks intrusive ads and trackers
  - Focused on cleaner browsing and lower bandwidth usage

- Browser Features
  - Tabs and tab switcher
  - Bookmarks and browsing history
  - Speed dial / home shortcuts
  - Incognito-style browsing support
  - Fullscreen, desktop mode, and night mode

- Privacy and Security Tools
  - Secure DNS toggle
  - Image blocking option
  - Browser settings and customization panel

- UI/UX
  - Material 3 design
  - Dark mode / AMOLED support
  - Custom accent colors
  - Responsive top bar, toolbar, and bottom sheet controls

## Tech Stack

- Language: Kotlin
- UI Framework: Jetpack Compose
- Android SDK: API 36
- Architecture: Android ViewModel + repository pattern
- Networking: OkHttp
- Async: Kotlin Coroutines + Flow
- Persistence: Room / SharedPreferences
- Build Tool: Gradle Kotlin DSL

## Build Information

- Min SDK: 28
- Target SDK: 36
- Compile SDK: 36
- Language used: Kotlin

## Repository Summary

This repository contains a feature-rich Android browser app with browser UI components, download engine, repository layer, theme system, and app settings. It is designed as a complete mobile web experience with privacy and media-focused utilities.

## Notes

This app is currently version 1.0 and is intended for Android devices using modern Jetpack Compose-based UI patterns.
