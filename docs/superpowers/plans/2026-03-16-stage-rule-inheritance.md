# Stage Rule Inheritance Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add list-based stage options so stage 3 and stage 4 degradation rules inherit previous-stage mappings by default, with `inherit=false` opt-out and later-stage precedence.

**Architecture:** Extend `Config` with per-stage option lists and teach `BlockResolver` to parse stage-owned rules plus stage options into effective, first-match-wins rule lists. Keep `ChunkProcessor` unchanged so runtime behavior still flows through the existing lookup API.

**Tech Stack:** NeoForge 21.1.219, Minecraft 1.21.1, Java 21, ModConfigSpec

---

## Chunk 1: Config Surface

### Task 1: Add config support for inheritance behavior

**Files:**
- Modify: `src/main/java/net/tomato3017/nuclearwinter/Config.java`
- Modify: `src/main/java/net/tomato3017/nuclearwinter/radiation/BlockResolver.java`

- [ ] **Step 1: Add list-valued stage option config entries**
- [ ] **Step 2: Add parsing hooks in `BlockResolver` for stage options**

## Chunk 2: Merge Behavior

### Task 2: Build effective stage rule lists with inheritance

**Files:**
- Modify: `src/main/java/net/tomato3017/nuclearwinter/radiation/BlockResolver.java`

- [ ] **Step 1: Parse stage option lists into `inheritPrevious` state**
- [ ] **Step 2: Merge own rules ahead of inherited effective rules**
- [ ] **Step 3: Remove exact duplicate inherited matchers while preserving first-match ordering**
- [ ] **Step 4: Re-run `./gradlew build` and confirm the merged behavior compiles cleanly**

## Chunk 3: Verification

### Task 3: Verify build health

**Files:**
- Modify: `src/main/java/net/tomato3017/nuclearwinter/Config.java`
- Modify: `src/main/java/net/tomato3017/nuclearwinter/radiation/BlockResolver.java`

- [ ] **Step 1: Run `./gradlew build`**
- [ ] **Step 2: Review logs for config parsing warnings or test regressions**
- [ ] **Step 3: Summarize resulting config syntax and touched files**
