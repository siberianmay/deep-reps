# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Deep Reps is a gym and strength training tracking app for Android. Currently in **pre-development** — no code exists yet. The repository contains planning documents only.

## Key Documents

- `README.md` — Project status and high-level summary
- `FEATURES.md` — Complete feature specification (what the app does, not how it's built)
- `TEAM.md` — Team composition across 3 launch phases (12 roles total)

## Domain Context

- **Platform:** Android only. iOS is explicitly out of scope until product-market fit.
- **Core flow:** Select muscle groups → pick exercises → AI generates a session plan (Gemini API) → log sets during workout → track progress over time.
- **7 muscle groups:** Legs, Lower Back, Chest, Back, Shoulders, Arms, Core. Each exercise belongs to exactly one primary group.
- **AI integration:** Gemini API for plan generation. Must be abstracted behind a provider interface — swappable to other LLMs without changes outside the provider layer.
- **Offline-first:** The app must function fully offline except for AI plan generation. Gyms have bad connectivity.
- **Exercise library is fixed at launch.** No user-created exercises.

## Architecture Decisions Not Yet Made

Tech stack, data architecture, cloud sync vs local-only, and monetization model are all undecided. Do not assume or recommend these unless explicitly asked.

## What's Out of Scope

Custom exercises, nutrition tracking, social features, wearable integration, video coaching, multi-language, iOS. See FEATURES.md "Feature Boundary" section for full list and rationale.
