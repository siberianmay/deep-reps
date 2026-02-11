# Deep Reps — Core Team Roster

## Why This Composition

The gym app market is brutally saturated (Strong, JEFIT, Hevy, Fitbod, etc.). Most fail not because of bad code, but because of **wrong exercise science, generic UX, weak monetization, or zero retention strategy**. Every role below exists to solve a specific failure mode.

---

## 1. Product Owner / CEO

**Role:** Strategic leader. Owns the P&L, product roadmap, monetization model, and go-to-market strategy.

**Why this person exists:** Without a single decision-maker who thinks in terms of LTV, churn, and market positioning, the app becomes a feature graveyard. This person kills bad ideas fast and doubles down on what converts.

**Profile:**
- 10+ years in mobile product leadership, preferably with at least 2 fitness/health apps shipped to 500K+ downloads
- Deep understanding of subscription economics (free-to-paid conversion funnels, trial optimization, paywall psychology)
- Proven track record of scaling apps from 0 to $1M+ ARR
- Fluent in competitive analysis — knows exactly why users leave Strong for Hevy and vice versa
- Makes data-driven decisions but trusts domain experts on exercise science

**Key deliverables:** Business model, pricing strategy, feature prioritization, investor/stakeholder communication, KPI ownership (DAU, retention D1/D7/D30, conversion rate, ARPU)

---

## 2. Certified Strength & Conditioning Specialist (CSCS) / Head of Fitness

**Role:** The domain authority. Every exercise, every program template, every progression model, every form cue flows through this person.

**Why this person exists:** Most gym apps are built by developers who Google "best chest exercises." The result is mediocre exercise libraries with incorrect biomechanics, dangerous progression logic, and cookie-cutter programs. This person prevents all of that.

**Profile:**
- NSCA-CSCS or equivalent certification (not a weekend cert — a real credential)
- 8+ years of hands-on coaching across general population, powerlifting, hypertrophy, and athletic performance
- Published or peer-reviewed work in exercise science is a strong plus
- Experience consulting for at least one fitness tech product (app, wearable, or platform)
- Deep knowledge of periodization models (linear, undulating, block), RPE/RIR-based autoregulation, and progressive overload mechanics
- Understands how to translate complex training science into simple, actionable UX copy

**Key deliverables:** Exercise database with correct muscle group mappings and form cues, program templates, progression/deload algorithms, workout generation logic validation, safety guardrails (e.g., flagging dangerous volume jumps)

---

## 3. UX Researcher

**Role:** Discovers what users actually need (not what they say they need) and validates design decisions with real data.

**Why this person exists:** Gym apps live or die on mid-workout usability. If logging a set takes more than 2 taps, users abandon. This person ensures the app is built around real user behavior, not assumptions.

**Profile:**
- 6+ years in UX research, with at least 2 years on mobile consumer products
- Expert in qualitative methods (contextual inquiry, usability testing, diary studies) and quantitative methods (surveys, A/B test analysis, funnel analytics)
- Has conducted research in gym/fitness or health contexts — understands the unique constraints (sweaty hands, loud environments, time pressure between sets)
- Skilled at translating research findings into actionable design requirements
- Experienced with tools like Maze, UserTesting, Lookback, or Dovetail

**Key deliverables:** User personas grounded in real interviews (not marketing fiction), competitive UX audit, usability test reports, feature validation studies, retention driver analysis

---

## 4. Senior UI/UX Designer

**Role:** Designs every screen, interaction, and visual element. Owns the design system.

**Why this person exists:** The gym app market has a clear visual language — dark themes, bold typography, high contrast. But most apps within that space feel identical. This person creates a distinctive, usable identity.

**Profile:**
- 7+ years in mobile UI/UX design, with a portfolio of shipped Android apps (Material Design 3 fluency is non-negotiable)
- Expert in designing for one-handed use, glanceable data, and high-speed input (critical for mid-workout logging)
- Strong motion design skills — meaningful transitions, not gratuitous animation
- Experience designing data-dense interfaces (charts, progress tracking, workout history) without visual clutter
- Proficiency in Figma, with a systematic approach to design tokens, component libraries, and responsive layouts
- Has designed for fitness, health, or sports apps (understands the domain's visual conventions and where to break them)

**Key deliverables:** Complete design system (typography, color, spacing, components), all screen designs with interaction specs, micro-interaction prototypes, dark/light theme, accessibility audit (WCAG AA minimum)

---

## 5. Lead Android Developer

**Role:** Architect and technical lead. Owns the codebase architecture, tech stack decisions, and code quality standards.

**Why this person exists:** A gym app has deceptively complex requirements — offline-first data sync, real-time timer management, background services, wearable integration, complex state management during workouts. The architecture must be right from day one or you'll rewrite within a year.

**Profile:**
- 8+ years Android development, 3+ years as tech lead
- Expert in Kotlin, Jetpack Compose, and modern Android architecture (MVVM/MVI with Clean Architecture layers)
- Deep experience with Room (offline-first is mandatory — gyms have bad WiFi), WorkManager, and background services
- Proven ability to build performant, smooth UI at 60fps even on mid-range devices
- Experience with Wear OS integration (smartwatch companion is a competitive differentiator)
- Familiar with health/fitness APIs (Google Health Connect, Samsung Health SDK)
- Strong opinions on testing (unit, integration, UI tests) backed by practice
- Has shipped at least 2 apps with 100K+ installs on Google Play

**Key deliverables:** Architecture design document, tech stack selection, CI/CD pipeline setup, code review standards, performance benchmarks, Play Store deployment pipeline

---

## 6. Mid-Senior Android Developer

**Role:** Primary feature implementer. Works under the lead's architecture to build screens, features, and integrations.

**Why this person exists:** The lead architect shouldn't be writing every screen. You need a strong second developer who can take a design spec and ship pixel-perfect, well-tested features autonomously.

**Profile:**
- 4+ years Android development with Kotlin and Jetpack Compose
- Solid understanding of coroutines, Flow, dependency injection (Hilt/Koin)
- Experience implementing complex UI components (custom charts, drag-and-drop reordering, gesture-based interactions)
- Comfortable writing unit and UI tests
- Fast learner who can adapt to the lead's architectural decisions without friction

**Key deliverables:** Feature implementation (workout logging, exercise library, progress charts, settings, profile), UI component development, bug fixes, test coverage

---

## 7. QA Engineer / Mobile Test Specialist

**Role:** Ensures the app works correctly across devices, OS versions, and edge cases. Owns the test strategy.

**Why this person exists:** Gym apps are used in hostile conditions — interrupted by phone calls mid-set, backgrounded for 20 minutes, used on cheap devices with 2GB RAM. A single data loss bug (losing a workout log) destroys user trust permanently.

**Profile:**
- 5+ years in mobile QA, with deep Android expertise
- Expert in both manual exploratory testing and automated testing (Espresso, UI Automator, Maestro)
- Experience with device farms (Firebase Test Lab, BrowserStack) for fragmentation testing
- Strong understanding of Android-specific edge cases: process death, configuration changes, battery optimization killing background services, Doze mode
- Familiar with performance testing (memory leaks, janky frames, startup time)
- Has worked on data-critical apps where data integrity is paramount

**Key deliverables:** Test strategy document, automated test suite, device compatibility matrix, regression test plans, bug reports with reproduction steps, performance test results

---

## 8. DevOps / Platform Engineer

**Role:** Owns CI/CD, infrastructure, monitoring, and deployment pipelines for both the app and backend.

**Why this person exists:** Manual deployments, flaky builds, and zero observability will slow the team to a crawl. This person ensures the team ships fast and knows immediately when something breaks in production.

**Profile:**
- 5+ years in DevOps/SRE, with mobile app deployment experience
- Expert in CI/CD for Android (GitHub Actions, Bitrise, or Gradle-based pipelines)
- Strong with containerization (Docker), orchestration (Kubernetes or Cloud Run), and IaC (Terraform/Pulumi)
- Experience with monitoring and alerting (Grafana, Datadog, or Firebase Crashlytics + Performance Monitoring)
- Familiar with Google Play Console — staged rollouts, release tracks (internal/closed/open testing), crash reporting
- Security-conscious: manages secrets, signing keys, and service accounts properly

**Key deliverables:** CI/CD pipeline (build, test, lint, deploy), infrastructure provisioning, monitoring dashboards, alerting rules, Play Store release automation, environment management (dev/staging/prod)

---

## 9. Data Analyst / Growth Analyst

**Role:** Turns raw user behavior data into actionable insights. Owns analytics implementation, A/B test design, and KPI reporting.

**Why this person exists:** Without data, every product decision is a guess. This person tells you why D7 retention dropped 3%, which onboarding step loses the most users, and whether the new paywall converts better.

**Profile:**
- 5+ years in product/growth analytics, preferably in mobile consumer apps
- Expert in event taxonomy design (naming conventions, property schemas — get this wrong early and your data is useless forever)
- Proficient with analytics tools (Mixpanel, Amplitude, or Firebase Analytics) and SQL
- Experience designing and analyzing A/B tests with statistical rigor (not just "version B had more clicks")
- Familiar with subscription analytics: trial-to-paid conversion, churn cohort analysis, LTV modeling
- Can build dashboards that the entire team actually uses (Looker, Metabase, or similar)

**Key deliverables:** Analytics event taxonomy, dashboard suite (acquisition, engagement, retention, revenue), A/B test framework, weekly/monthly KPI reports, churn analysis, feature impact measurement

---

## 10. Growth & Content Marketing Manager

**Role:** Owns the entire marketing surface — user acquisition, ASO, exercise media production, community engagement, and retention marketing. The single person responsible for getting users in, keeping them engaged, and turning them into advocates.

**Why this person exists:** Building a gym app and waiting for organic downloads is a death sentence. The market is too crowded. At the same time, an exercise library with stock photos is amateur hour, and ignoring app store reviews kills rankings. Pre-scale, one sharp generalist who can operate across all three domains (growth, content, community) is more effective than three specialists with fragmented ownership. Hire specialists later when any single domain outgrows one person's capacity.

**Profile:**
- 7+ years in mobile app marketing, with at least 3 years in the health/fitness vertical
- Expert in ASO (keyword optimization, screenshot/video A/B testing, review management, category ranking strategies)
- Deep experience with paid acquisition channels (Google Ads App Campaigns, Meta, TikTok, influencer partnerships)
- Proven ability to achieve CAC < LTV with measurable attribution
- Strong in retention marketing: push notification strategy, email/in-app messaging cadences, re-engagement campaigns
- Experience producing fitness content — capable of directing exercise demonstration shoots (video or 3D animation), understanding proper angles for movement patterns, and optimizing media for mobile delivery
- Skilled at community management: turning negative app store reviews into positive ones, synthesizing user feedback into product requests, running engagement programs (challenges, leaderboards, milestones)
- Familiar with fitness influencer ecosystem and partnership structures

**Key deliverables:** Go-to-market strategy, ASO plan and execution, paid acquisition campaigns, retention marketing automation, exercise demonstration media (directing production, outsourcing execution as needed), app store review management, user feedback pipeline, community engagement programs, influencer partnership pipeline

---

## Team Size Summary

| Phase | Roles | Headcount |
|-------|-------|-----------|
| **Pre-launch (MVP)** | Product Owner, CSCS, UI/UX Designer, Lead Android Dev, Mid Android Dev, Backend Dev, QA | **7** |
| **Launch** | + Growth & Content Marketing, Data Analyst, DevOps | **10** |
| **Post-launch Growth** | + Nutritionist, UX Researcher | **12** |

---

## Roles Deliberately Excluded

| Role | Why Not |
|------|---------|
| **Dedicated Project Manager** | Product Owner handles this at this team size. Add a PM only if the team exceeds 15 people. |
| **iOS Developer** | Android first. Validate the product before doubling engineering cost. Port to iOS after product-market fit is confirmed. |
| **Dedicated Data Engineer** | The backend developer and data analyst can handle the data pipeline at this scale. Revisit at 100K+ users. |
| **Legal Counsel** | Use an external firm for Terms of Service, Privacy Policy, and health disclaimer review. Not a full-time role. |
| **Customer Support** | Growth & Content Marketing Manager handles this initially. Hire dedicated support only when ticket volume exceeds what one person can manage. |

---

## Critical Dependencies Between Roles

```
CSCS ──────► Exercise DB Logic ──────► Android Devs (implementation)
                                  ──► Growth & Content Marketing (exercise media)
                                  ──► UI Designer (exercise card design)

Product Owner ► Feature Priorities ──► All development roles
             ► Monetization Model ──► Growth & Content Marketing (pricing/paywall)
             ► KPI Targets ────────► Data Analyst (measurement)

UX Researcher ► User Insights ─────► UI Designer (design decisions)
                                 ──► Product Owner (feature validation)

Data Analyst ► Metrics ────────────► Product Owner (decisions)
            ► A/B Results ─────────► Growth & Content Marketing (optimization)
```

---

## Non-Negotiable Hiring Principles

1. **Domain experience over general talent.** A brilliant designer who has never designed for fitness will make fundamental mistakes (e.g., designing workout logging that requires two hands).
2. **Ship speed over perfection.** Every hire must have a track record of shipping, not just designing or planning.
3. **The CSCS is not optional.** This is the single highest-leverage hire. Bad exercise science in the app is worse than no app at all — it's a liability.
4. **Delay hires aggressively.** Only hire for Phase 1 initially. Every person added before product-market fit is confirmed increases burn rate without proportional value.
