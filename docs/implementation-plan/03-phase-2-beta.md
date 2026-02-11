# Phase 2: Beta & Pre-Launch (Weeks 23-28)

6 weeks of testing, feedback collection, polish, and launch preparation. Feature-complete MVP from Phase 1 is hardened for real users.

---

## Team Allocation (Phase 2)

| Role | Weeks 23-24 | Weeks 25-26 | Weeks 27-28 |
|------|-------------|-------------|-------------|
| **Lead Dev** | Performance profiling, critical bug fixes | Accessibility pass, final optimization | Release hardening, hotfix readiness |
| **Mid Dev** | Bug fixes from beta feedback | Bug fixes, edge case polish | Bug fixes, QA support |
| **QA** | Closed beta monitoring, bug triage | Device matrix testing, open beta monitoring | Full regression, release candidate validation |
| **DevOps** | Release pipeline validation, staged rollout setup | Play Store listing prep, signing verification | Production release dry-run |
| **Design** | Screenshot production, store listing assets | Visual QA, accessibility audit | Final polish review |
| **Growth** | ASO preparation, beta recruitment | Store listing copy, influencer outreach | Pre-launch PR, social media ramp |
| **DA** | Analytics validation in beta, dashboard setup | Cohort analysis on beta data, consent flow verification | KPI baseline establishment |
| **CSCS** | Validate AI plan quality from real beta sessions | Review safety guardrail triggers | Final exercise library sign-off |

---

## Epic 2.1: Closed Beta Launch

**Owner:** QA + CEO
**Duration:** Weeks 23-24
**Blocks:** Open beta, launch readiness

### Tasks

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 2.1.1 | Promote internal build to Google Play closed testing track | DevOps | 0.5d | Build live on closed track | APK installable via Play Store opt-in link |
| 2.1.2 | Recruit 50-100 closed beta testers | CEO | 3d | Tester list | 50+ opt-ins confirmed. Sources: gym contacts, Reddit fitness communities, CSCS network |
| 2.1.3 | Set up beta feedback collection channels | CEO | 0.5d | Discord server + Google Form | #bug-reports, #feature-requests, #general channels active. Structured feedback form linked in app settings |
| 2.1.4 | Distribute "Founding Member" badges | Mid | 0.5d | Badge logic deployed | Beta users receive permanent Founding Member badge in profile |
| 2.1.5 | Monitor crash reports daily (Crashlytics) | QA | ongoing | Daily crash triage | Every new crash signature reviewed within 24h |
| 2.1.6 | Monitor analytics events firing correctly | DA | 2d | Validation report | All P0 events (onboarding, workout start/complete/abandon, app lifecycle) confirmed in BigQuery debug view |
| 2.1.7 | Validate consent flow (analytics opt-in/out) | DA + QA | 1d | Test report | Analytics OFF by default. Toggling works. No events fire when declined. |
| 2.1.8 | Triage and prioritize beta bug reports | QA | ongoing | Prioritized backlog | All reported bugs classified P0-P4 per severity definitions |

---

## Epic 2.2: Beta Feedback Collection & Triage

**Owner:** CEO + QA
**Duration:** Weeks 23-26
**Blocks:** Polish priorities

### Tasks

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 2.2.1 | Conduct 5-8 moderated feedback sessions with beta testers | CEO | 5d | Session notes | Structured notes per tester: core loop friction, AI plan quality, crash/data-loss incidents |
| 2.2.2 | Aggregate feedback into themes | CEO | 1d | Feedback summary doc | Top 10 issues ranked by frequency and severity |
| 2.2.3 | CSCS reviews AI-generated plans from real beta sessions | CSCS | 2d | Quality report | 20+ real plans reviewed. Safety guardrails validated. Accuracy rating per plan. |
| 2.2.4 | Identify any exercise library data corrections | CSCS | 1d | Correction list | Typos, wrong muscle group assignments, missing tips — all flagged |
| 2.2.5 | Review beta analytics: onboarding completion rate, first-workout rate | DA | 1d | Funnel report | Identify biggest drop-off point. Baseline metrics established. |
| 2.2.6 | Review beta analytics: workout abandonment rate, AI plan acceptance | DA | 1d | Engagement report | Abandonment rate and plan modification rate baselined |

---

## Epic 2.3: Critical Bug Fixes

**Owner:** Lead Dev + Mid Dev
**Duration:** Weeks 25-27
**Blocks:** Open beta, launch readiness

### Tasks

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 2.3.1 | Fix all P0 bugs from beta | Lead | varies | Merged PRs | Zero P0 bugs open |
| 2.3.2 | Fix all P1 bugs from beta | Lead + Mid | varies | Merged PRs | Zero P1 bugs open |
| 2.3.3 | Fix P2 bugs with >3 reports | Mid | varies | Merged PRs | High-frequency P2 bugs resolved |
| 2.3.4 | Address exercise library corrections from CSCS | Mid | 1d | Updated pre-populated .db | Corrections applied, re-tested |
| 2.3.5 | Iterate AI prompt if CSCS quality review flags issues | Lead | 2-3d | Updated prompt version | CSCS re-validates improved outputs |

**Exit criteria:** Zero P0, zero P1 bugs open before proceeding to open beta.

---

## Epic 2.4: Open Beta

**Owner:** Growth + QA
**Duration:** Weeks 26-28
**Blocks:** Launch readiness

### Tasks

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 2.4.1 | Promote build to Google Play open testing track | DevOps | 0.5d | Open beta live | Anyone can opt in via Play Store |
| 2.4.2 | Recruit 500-1,000 open beta testers | Growth | 5d | 500+ installs | Recruitment via: r/fitness, r/GYM, r/bodybuilding, fitness Discord servers, CSCS coaching network |
| 2.4.3 | Monitor crash-free rate daily | QA | ongoing | Daily reports | Crash-free rate tracked toward >= 99.5% target |
| 2.4.4 | Monitor app store rating | QA + Growth | ongoing | Daily checks | Respond to all negative reviews within 24h |
| 2.4.5 | Collect NPS or satisfaction signal | CEO | 3d | NPS score | Target: NPS > 40 from open beta cohort |
| 2.4.6 | Validate analytics at scale (500+ users) | DA | 2d | Data quality report | Event volumes match expected rates. No missing events. BigQuery pipeline confirmed. |

---

## Epic 2.5: ASO & Store Listing Preparation

**Owner:** Growth + Design
**Duration:** Weeks 24-27
**Blocks:** Launch

### Tasks

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 2.5.1 | Write Play Store long description (4000 chars) | Growth | 1d | Description text | Follows structure from go-to-market.md Section 2.3. Keyword-optimized. |
| 2.5.2 | Write Play Store short description (80 chars) | Growth | 0.5d | Short description | "AI workout plans, strength tracking & progress. Built by certified trainers." |
| 2.5.3 | Produce 8 Play Store screenshots | Design | 3d | 8 PNG files at 1080x1920 | Dark background, Pixel device frame, bold headline per shot. Follows go-to-market.md Section 2.4 |
| 2.5.4 | Produce 30-second promo video | Design + Growth | 5d | MP4 at 1080x1920, <10MB | Structure per go-to-market.md Section 2.5. No voiceover. Text overlays + music. |
| 2.5.5 | Design feature graphic (1024x500) | Design | 0.5d | PNG feature graphic | Brand colors, app name, tagline, 1 screenshot |
| 2.5.6 | Design app icon (512x512, adaptive) | Design | 0.5d | Adaptive icon assets | Meets Play Store requirements. Tested on light/dark launchers. |
| 2.5.7 | Select category, content rating, tags | Growth | 0.5d | Play Console configured | Category: Health & Fitness. PEGI 3. Tags per go-to-market.md Section 2.6 |
| 2.5.8 | Prepare templated review responses | Growth | 1d | Response templates doc | Templates for: 5-star thanks, bug acknowledgment, feature request, negative recovery |
| 2.5.9 | Set up review monitoring (AppFollow or equivalent) | Growth | 0.5d | Monitoring active | Daily review alerts configured |
| 2.5.10 | Complete Google Play Data Safety section | Lead + DA | 1d | Data Safety submitted | Disclosures per analytics-plan.md Section 6.5 |

---

## Epic 2.6: Performance Optimization & Polish

**Owner:** Lead Dev
**Duration:** Weeks 27-28
**Blocks:** Launch readiness

### Tasks

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 2.6.1 | Run Macrobenchmark suite, establish baselines | Lead | 1d | Benchmark results | Cold startup < 1.5s, warm < 500ms on Pixel 6 |
| 2.6.2 | Generate and bundle baseline profile | Lead | 0.5d | Baseline profile committed | Covers startup, exercise list, workout screen, progress charts |
| 2.6.3 | Profile memory usage during 1-hour workout | Lead | 1d | Memory report | Peak < 150MB on mid-range. No leaks (LeakCanary clean). |
| 2.6.4 | Verify APK/AAB size targets | Lead | 0.5d | Size report | AAB download < 15MB per config. Install < 40MB. |
| 2.6.5 | Run Compose Compiler reports, fix unstable classes in workout UI | Lead | 1d | Compiler reports clean | No `UNSTABLE` classes in `:feature:workout` or `:core:ui` models |
| 2.6.6 | Accessibility pass: content descriptions, contrast ratios, touch targets | Mid + Design | 2d | Accessibility audit report | All interactive elements have content descriptions. Contrast >= 4.5:1. Touch targets >= 48dp. |
| 2.6.7 | Verify unit display consistency across all screens | QA | 1d | Test report | kg/lbs switching works consistently. No precision loss on round-trip. |
| 2.6.8 | Final R8 optimization, verify no critical code stripped | Lead | 0.5d | R8 validation | Release build functional. Proguard rules verified. Ktor + Serialization rules correct. |

---

## Epic 2.7: Device Matrix Testing

**Owner:** QA
**Duration:** Weeks 26-28
**Blocks:** Launch readiness

### Tasks

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 2.7.1 | Run full regression on Firebase Test Lab (7-device matrix) | QA | 2d | Test results | All 6 critical E2E flows pass on all devices. Per testing-strategy.md Section 6.4 |
| 2.7.2 | Test on Tier 1 (low-end) devices: Galaxy A14, Redmi 12 | QA | 2d | Test report | App usable on 4GB RAM devices. Cold start < 2.5s. No OOM during workout. |
| 2.7.3 | Test on Samsung devices (One UI-specific behavior) | QA | 1d | Test report | Foreground service survives Samsung battery optimization. Notifications delivered. |
| 2.7.4 | Test on Xiaomi devices (MIUI auto-start restriction) | QA | 1d | Test report | Document workaround if auto-start required. Verify foreground service behavior. |
| 2.7.5 | Test process death scenarios on 3 devices | QA | 1d | Test report | All 5 process death tests pass per testing-strategy.md Section 5.1 |
| 2.7.6 | Run Monkey testing (10,000 random events) | QA | 0.5d | Monkey report | No crashes from random input |
| 2.7.7 | Test foldable device (Galaxy Z Fold if available) | QA | 0.5d | Test report | No crash on fold/unfold. Layout renders acceptably. |

---

## Epic 2.8: Pre-Launch Marketing

**Owner:** Growth
**Duration:** Weeks 26-28
**Blocks:** Launch week execution

### Tasks

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 2.8.1 | Launch landing page at deepreps.app | Growth | 2d | Live landing page | Email capture, feature preview, "Get early access" CTA. Target: 2,000 signups by launch. |
| 2.8.2 | Start social media accounts (Instagram, TikTok, X) | Growth | 1d | Active accounts | Profile set up, first posts published |
| 2.8.3 | Produce 2 weeks of pre-launch content | Growth + CSCS | 5d | Scheduled posts | Exercise science tips (CSCS), behind-the-scenes dev, beta user testimonials |
| 2.8.4 | Write press kit | Growth | 2d | Press kit PDF + assets | App description, founder story, CSCS credential, screenshots, logo, brand assets |
| 2.8.5 | Identify and pitch 20 journalists/bloggers | Growth | 3d | Pitch emails sent | 3 angles per go-to-market.md Section 3.1: AI+CSCS, offline-first, Android-first |
| 2.8.6 | Secure 5-7 nano/micro influencer partnerships for launch week | Growth | 5d | Signed agreements | Android users, active gym-goers, 1K-100K followers. Budget per go-to-market.md Section 7.1 |
| 2.8.7 | Prepare Product Hunt page | Growth | 1d | PH page draft | GIFs, description, maker comment ready. Scheduled for T+1 (Tuesday 12:01 AM PT) |
| 2.8.8 | Set up email waitlist blast | Growth | 0.5d | Email draft + send list | Launch announcement email ready to send on T+0 |

---

## Epic 2.9: Release Hardening

**Owner:** DevOps + Lead Dev
**Duration:** Week 28
**Blocks:** Launch

### Tasks

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 2.9.1 | Validate release pipeline end-to-end (tag -> build -> sign -> upload) | DevOps | 1d | Successful dry-run | Tag triggers release build. AAB signed. Uploaded to closed testing track. |
| 2.9.2 | Verify Play App Signing configuration | DevOps | 0.5d | Configuration verified | Upload key works. Google manages signing key. |
| 2.9.3 | Configure staged rollout percentages in production promote workflow | DevOps | 0.5d | Workflow updated | Rollout stages: 1% -> 5% -> 20% -> 50% -> 100% per devops-pipeline.md Section 5.3 |
| 2.9.4 | Verify Crashlytics, Performance Monitoring, and Analytics in release build | Lead | 0.5d | Verification report | All Firebase services active in release build. Debug disabled. |
| 2.9.5 | Set up on-call rotation (PagerDuty or equivalent) | DevOps | 0.5d | On-call schedule | Primary: Lead Dev. Secondary: Mid Dev. Escalation: DevOps. |
| 2.9.6 | Document rollback procedure | DevOps | 0.5d | Runbook | Step-by-step: halt rollout, revert, hotfix, re-deploy |
| 2.9.7 | Feature freeze | CEO | -- | Announcement | No new features after this point. Bug fixes and performance only. |

---

## Phase 2 Gate (Week 28)

**Go/No-Go for public launch.** ALL must be true:

- [ ] Crash-free rate >= 99.5% over last 7 days of open beta
- [ ] Zero P0 bugs open
- [ ] Zero P1 bugs open
- [ ] Core loop completion rate >= 60% (start workout -> finish workout)
- [ ] App store rating >= 4.0 from beta reviews
- [ ] All 6 critical E2E flows pass on the 7-device Firebase Test Lab matrix
- [ ] Cold startup < 2.0s on Pixel 6 (release build)
- [ ] AAB download size < 15MB
- [ ] Play Store listing complete (screenshots, video, description, data safety)
- [ ] Release pipeline validated end-to-end
- [ ] On-call rotation configured
- [ ] Pre-launch marketing assets ready (press kit, influencer contracts, waitlist)
- [ ] Analytics pipeline validated (events -> BigQuery -> dashboard)
- [ ] Consent flow verified (analytics default OFF, toggle works)

**Decision maker:** CEO / Product Owner
