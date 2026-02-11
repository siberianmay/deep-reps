# Phase 4: Post-Launch Growth (Weeks 30-52)

23 weeks of stabilization, retention optimization, Phase 2 feature development, and scaling. The app is live and generating real user data. Decisions are now data-driven.

---

## Phase 4 Structure

Phase 4 is divided into four periods, each with distinct focus:

```
Month 1 (Weeks 30-33)    Stabilize & Learn
Month 2 (Weeks 34-37)    Retention Push + Phase 2 Features (Batch 1)
Month 3 (Weeks 38-42)    Feature Expansion + Phase 2 Features (Batch 2)
Months 4-6 (Weeks 43-52) Scale + Evaluate Platform Expansion
```

---

## Team Evolution (Phase 4)

Phase 4 adds Growth Marketing Manager and shifts existing roles. Per TEAM.md:

| Role | Phase 4 Focus |
|------|--------------|
| **Lead Dev** | Phase 2 feature architecture, performance, AI prompt iteration |
| **Mid Dev** | Phase 2 feature implementation, ongoing bug fixes |
| **QA** | Regression on new features, device matrix updates quarterly |
| **DevOps** | On-call, pipeline maintenance, backend infrastructure evaluation |
| **Growth** | Paid campaigns, ASO optimization, influencer partnerships, content marketing |
| **DA** | Cohort analysis, A/B test execution, churn prediction model, monthly reports |
| **CSCS** | AI plan quality review (monthly), exercise library updates, content creation |
| **Design** | Phase 2 feature design, A/B test variants, exercise video production |
| **CEO** | Product direction, fundraising (if applicable), community management |

**New hire (Week 30-33):** UX Researcher (part-time or contract) for retention deep-dive.

---

## Period 1: Stabilize & Learn (Weeks 30-33)

**Goal:** Ship zero new features. Fix launch bugs. Establish KPI baselines. Understand real user behavior.

### Epic 4.1: Launch Bug Stabilization

**Owner:** Lead Dev + Mid Dev
**Duration:** Weeks 30-31

| # | Task | Owner | Deliverable | Acceptance Criteria |
|---|------|-------|-------------|---------------------|
| 4.1.1 | Fix all P0/P1 bugs from launch week | Lead + Mid | Hotfix releases | Zero P0/P1 open. Hotfix deployed within 48h of report. |
| 4.1.2 | Address top 5 crash signatures from Crashlytics | Lead | Bug fixes merged | Top 5 crashes resolved. Crash-free rate >= 99.5%. |
| 4.1.3 | Fix any data integrity issues surfaced by real users | Lead | Merged PRs | No reported data loss or corruption. |
| 4.1.4 | Review and fix Gemini API error patterns | Lead | API improvements | AI plan failure rate < 5%. Timeout/retry logic tuned. |
| 4.1.5 | Address P2 bugs (batch of top 10 by frequency) | Mid | Bug fixes merged | Top 10 P2 bugs resolved. |

### Epic 4.2: KPI Baseline Establishment

**Owner:** DA
**Duration:** Weeks 30-33

| # | Task | Owner | Deliverable | Acceptance Criteria |
|---|------|-------|-------------|---------------------|
| 4.2.1 | Build Looker Studio executive dashboard | DA | Live dashboard | DAU/WAU/MAU, installs, retention, crash rate visible |
| 4.2.2 | Compute D1/D7/D30 retention for launch cohort | DA | Retention report | Baselines established. Targets: D1 > 25%, D7 > 12%, D30 > 5% |
| 4.2.3 | Analyze onboarding funnel drop-off | DA | Funnel analysis | Identify the step losing most users. Actionable recommendation. |
| 4.2.4 | Analyze workout abandonment by exercise count and duration | DA | Abandonment analysis | Identify when users quit mid-workout and why |
| 4.2.5 | Compute AI plan acceptance vs modification rate | DA | Plan quality report | Baseline modification rate. Share with CSCS for prompt tuning. |
| 4.2.6 | Segment users by engagement tier | DA | Segmentation report | Power / Consistent / Casual / Dormant / Churned per analytics-plan.md Section 4.4 |
| 4.2.7 | Set up automated daily Slack report | DA + DevOps | Automated alerts | DAU, installs, workouts, crash rate, AI failure rate posted to Slack at 09:00 |

### Epic 4.3: App Store Optimization (Round 1)

**Owner:** Growth
**Duration:** Weeks 30-33

| # | Task | Owner | Deliverable | Acceptance Criteria |
|---|------|-------|-------------|---------------------|
| 4.3.1 | Analyze Play Console search term data | Growth | Keyword report | Top converting keywords identified |
| 4.3.2 | A/B test short description in Play Store | Growth | Test result | Run for 7 days. Pick winner by conversion rate. |
| 4.3.3 | A/B test screenshot order | Growth | Test result | Test lead screenshot: AI plan vs workout logging |
| 4.3.4 | Respond to every 1-3 star review, 50% of 4-star | Growth | Review management | Response rate targets met. Response within 24h for negatives. |
| 4.3.5 | Implement in-app review prompt (after 5th workout) | Mid | Prompt deployed | Gated: 4-5 stars -> Play Store, 1-3 stars -> feedback form. Max 1 per 90 days. |

### Epic 4.4: Retention Analysis

**Owner:** DA + CEO
**Duration:** Weeks 32-33

| # | Task | Owner | Deliverable | Acceptance Criteria |
|---|------|-------|-------------|---------------------|
| 4.4.1 | Compute D30 retention for first cohort | DA | Retention curve | Baseline established. Compare to industry average (4-6%). |
| 4.4.2 | Identify top 3 churn predictors (manual analysis) | DA | Analysis doc | Actionable: e.g., users who don't complete 2nd workout within 7 days churn at 80% |
| 4.4.3 | Compare retention: AI plan users vs manual users | DA | Comparison report | Quantify retention lift from AI usage |
| 4.4.4 | Identify "aha moment" (feature usage correlated with retention) | DA | Insight report | e.g., "Users who save a template within 14 days retain 2x better" |
| 4.4.5 | Hire or contract UX Researcher for retention deep-dive | CEO | Researcher onboarded | Researcher conducts 8-10 interviews with churned and retained users |

---

## Period 2: Retention Push + Phase 2 Features Batch 1 (Weeks 34-37)

**Goal:** Ship features that directly impact retention. Run first A/B tests. Begin paid acquisition.

### Epic 4.5: Personal Records Detection & Celebration

**Owner:** Lead Dev + Mid Dev
**Duration:** Weeks 34-35

Per FEATURES.md: PR types are weight PR, rep PR, volume PR, estimated 1RM PR.

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.5.1 | Implement PR detection logic (4 types) | Lead | 2d | `DetectPersonalRecordsUseCase` | Weight, rep, volume, est. 1RM PRs detected on set completion |
| 4.5.2 | PR detection triggers on every set completion | Lead | 1d | Integration wired | PR check runs after each `completeSet()` |
| 4.5.3 | Implement PR celebration animation | Mid | 2d | Full-screen celebration composable | Confetti + PR details + share option |
| 4.5.4 | Implement PR history view (per exercise) | Mid | 2d | PR list screen | View all PRs for an exercise with dates |
| 4.5.5 | PR badge on workout summary | Mid | 1d | Summary updated | PR icon next to exercises that set new PRs |
| 4.5.6 | PR share card generation | Mid | 1d | Image generation | Auto-generated image card with PR details for social sharing |
| 4.5.7 | Analytics events for PR detection | DA + Mid | 0.5d | Events firing | `pr_achieved` event with exercise, type, value |
| 4.5.8 | CSCS validates PR thresholds and detection logic | CSCS | 1d | Validation report | Edge cases reviewed: deload weeks, form changes, warmup exclusion |

### Epic 4.6: Push Notification System

**Owner:** Mid Dev + Growth
**Duration:** Weeks 35-36

Per go-to-market.md Section 5.1.

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.6.1 | Implement push notification infrastructure (FCM) | Mid | 1d | FCM integrated | Token registration, notification display working |
| 4.6.2 | Implement notification triggers (8 types per GTM strategy) | Mid | 3d | All triggers coded | Onboarding incomplete, first workout, 2-day inactivity, 5-day, 14-day, PR opportunity, weekly summary, streak milestone |
| 4.6.3 | Implement notification caps (max 3/week, 1/day, quiet hours) | Mid | 1d | Caps enforced | No notifications 10PM-8AM. Max 3/week. Auto-suppress after 3 consecutive dismissals. |
| 4.6.4 | Implement notification opt-in/out in settings | Mid | 0.5d | Settings toggle | Users can disable specific notification types |
| 4.6.5 | A/B test notification framing (data-driven vs motivational) | DA + Growth | ongoing | Test design | Per go-to-market.md: min 500 users/arm, 7-day test, primary metric: notification-to-session conversion |
| 4.6.6 | Analytics events for notification delivery and open | DA + Mid | 0.5d | Events firing | `notification_sent`, `notification_opened`, `notification_dismissed` |

### Epic 4.7: Advanced Progress Analytics

**Owner:** Lead Dev
**Duration:** Weeks 36-37

Extends basic progress tracking from Phase 1 with richer analysis.

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.7.1 | Implement estimated 1RM trend chart (Epley formula) | Lead | 2d | 1RM chart composable | Time-series 1RM per exercise. 4-week and 12-week views. |
| 4.7.2 | Implement volume load trend chart (per muscle group) | Lead | 2d | Volume chart composable | Weekly volume aggregated across sessions for a muscle group |
| 4.7.3 | Implement training frequency heatmap (calendar view) | Mid | 2d | Heatmap composable | GitHub-style contribution heatmap showing workout days |
| 4.7.4 | Implement muscle group volume distribution chart | Mid | 1d | Distribution chart | Pie/bar showing volume split across groups over time range |
| 4.7.5 | Implement historical comparison on workout summary | Lead | 1d | Summary enhanced | "+5% volume vs last session" delta shown per exercise |
| 4.7.6 | Optimize complex progress queries for 6-month data volume | Lead | 1d | Query performance | All progress queries < 200ms with 200+ sessions seeded |

### Epic 4.8: First A/B Tests

**Owner:** DA + Growth
**Duration:** Weeks 34-37

Per analytics-plan.md Section 3.3.

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.8.1 | Set up Firebase Remote Config for A/B testing | Lead + DA | 1d | Remote Config integrated | Feature flags readable. A/B assignment via Firebase A/B Testing. |
| 4.8.2 | Run Experiment 1: Onboarding Length | DA + Mid | 14d+ | Test result | 2-step vs full onboarding. Primary: completion rate. |
| 4.8.3 | Run Experiment 5: Template Nudge After First Workout | DA + Mid | 14d+ | Test result | Modal prompt vs no prompt. Primary: template usage in 2nd workout. |
| 4.8.4 | Analyze results and ship winners | DA + Mid | 2d | Winning variants deployed | Statistically significant results (p < 0.05) implemented. |

### Epic 4.9: Paid Acquisition Launch

**Owner:** Growth
**Duration:** Weeks 34-37

Per go-to-market.md Section 4.

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.9.1 | Launch Google Ads App Campaigns | Growth | 2d | Campaigns live | 3 creative variants. Budget: $1,500/month (Month 1). |
| 4.9.2 | Launch Meta (Instagram/Facebook) campaigns | Growth | 2d | Campaigns live | Interest targeting: gym, weightlifting, fitness apps. Budget: $1,000/month. |
| 4.9.3 | Launch TikTok Ads | Growth | 1d | Campaigns live | In-feed ads targeting fitness content consumers. Budget: $500/month. |
| 4.9.4 | Set up attribution tracking (Adjust, AppsFlyer, or Branch) | Growth + DA | 2d | Attribution active | Per-channel install attribution. UTM tracking for influencers. |
| 4.9.5 | Monitor CAC by channel weekly | DA + Growth | ongoing | Weekly report | Google Ads < $2.50, Meta < $3.50, TikTok < $2.00. Pause channel if 2x target for 2 weeks. |

---

## Period 3: Feature Expansion + Phase 2 Features Batch 2 (Weeks 38-42)

**Goal:** Ship remaining Phase 2 features. Deepen retention. Scale what works.

### Epic 4.10: Superset & Circuit Support

**Owner:** Lead Dev + Mid Dev
**Duration:** Weeks 38-40

Per FEATURES.md: Supersets are Phase 2. Two exercises performed back-to-back, rest only after both.

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.10.1 | Design superset data model (superset_group_id on workout_exercises) | Lead | 1d | Schema migration | Migration tested. Backward compatible. |
| 4.10.2 | Implement superset creation UI (long-press to group) | Mid | 2d | Grouping interaction | Select 2 exercises -> "Create Superset" button appears |
| 4.10.3 | Implement superset compatibility validation | Lead | 1d | `SupersetCompatibilityValidator` | Per exercise-science.md Section 6.3: no same-group pairs, no two heavy compounds |
| 4.10.4 | Implement superset logging flow (alternating sets, rest after round) | Lead | 3d | Workout screen updated | After completing set of exercise A, UI prompts set of exercise B. Rest timer fires after both. |
| 4.10.5 | Implement superset visual grouping in workout screen | Mid | 1d | UI grouping | Supersetted exercises visually bracketed/indented |
| 4.10.6 | Implement superset in workout summary | Mid | 0.5d | Summary updated | Superset exercises shown grouped in summary |
| 4.10.7 | Update AI prompt to suggest supersets for appropriate pairings | Lead + CSCS | 2d | Prompt v3.0 | AI suggests antagonist pairings. CSCS validates suggestions. |
| 4.10.8 | CSCS validates superset rules implementation | CSCS | 1d | Validation report | All 5 compatibility rules from testing-strategy.md Section 2.12 verified |

### Epic 4.11: Data Export

**Owner:** Mid Dev
**Duration:** Weeks 40-41

Per FEATURES.md and analytics-plan.md Section 6.1 (GDPR right to data portability).

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.11.1 | Implement JSON export of all user data | Mid | 2d | Export use case | Workout history, profile, templates, PRs exported as JSON |
| 4.11.2 | Implement CSV export of workout history | Mid | 1d | CSV export | One row per set. Columns: date, exercise, set#, weight, reps, type |
| 4.11.3 | Add "Export Data" option in Profile/Settings | Mid | 0.5d | UI wired | Export triggers file save via Android share sheet |
| 4.11.4 | Implement "Delete All Data" with confirmation | Mid | 1d | Data deletion | Double confirmation. Clears Room DB. Returns to onboarding. |
| 4.11.5 | Analytics event: `data_export_requested`, `data_deletion_requested` | DA + Mid | 0.5d | Events firing | Track export/deletion frequency |

### Epic 4.12: Overtraining Warning System

**Owner:** Lead Dev + CSCS
**Duration:** Weeks 41-42

Per exercise-science.md Section 8.3 and testing-strategy.md Section 2.10.

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.12.1 | Implement `DetectOvertrainingWarningsUseCase` | Lead | 2d | Use case | Detects: MRV exceeded 2+ weeks, performance regression 3+ sessions, frequency > 6x/week for 2+ weeks, same group 4+ times in 7 days, session > 120 min |
| 4.12.2 | Implement warning UI (dismissible cards) | Mid | 1d | Warning composable | Severity-colored cards with explanation and dismiss button |
| 4.12.3 | Implement warning dismissal cooldown (7 days) | Lead | 0.5d | Cooldown logic | Dismissed warning suppressed for 7 days |
| 4.12.4 | Implement cross-group volume overlap warnings | Lead | 1d | `CrossGroupOverlapDetector` | Per exercise-science.md Section 2.2: chest+arms, back+arms, legs+lower back overlaps |
| 4.12.5 | CSCS validates warning thresholds | CSCS | 1d | Validation report | All 11 test cases from testing-strategy.md Section 2.10 verified |
| 4.12.6 | Analytics event: `overtraining_warning_shown`, `overtraining_warning_dismissed` | DA + Mid | 0.5d | Events firing | Track warning frequency and user response |

### Epic 4.13: Deeper A/B Testing

**Owner:** DA
**Duration:** Weeks 38-42

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.13.1 | Run Experiment 2: AI Plan Presentation (rationale text) | DA + Mid | 14d+ | Test result | Per analytics-plan.md: plain-language rationale vs no rationale |
| 4.13.2 | Run Experiment 3: Default Rest Timer Duration (90s vs 120s) | DA + Mid | 14d+ | Test result | Primary: rest timer skip rate |
| 4.13.3 | Run Experiment 4: Workout Summary Depth (basic vs visual) | DA + Mid | 14d+ | Test result | Primary: D7 retention. Muscle heatmap + trend chart variant. |
| 4.13.4 | Run Experiment 8: Progress Notification Cadence | DA + Growth | 14d+ | Test result | Weekly push with volume summary vs PR highlights vs none |
| 4.13.5 | Ship all winning experiment variants | Mid | varies | Variants deployed | Statistical significance achieved (p < 0.05) |

### Epic 4.14: Content Marketing Ramp

**Owner:** Growth + CSCS
**Duration:** Weeks 38-42

Per go-to-market.md Section 6.

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.14.1 | Launch blog at deepreps.app/blog | Growth | 2d | Blog live | SEO-optimized, mobile-friendly, hosted on subdirectory |
| 4.14.2 | Publish 8 exercise guide articles | CSCS + Growth | 8 articles over 4 weeks | Published articles | "How to [Exercise]" format. 1,500-2,500 words. CSCS author bio. CTA to download. |
| 4.14.3 | Publish 4 workout programming articles | CSCS + Growth | 4 articles over 4 weeks | Published articles | "Best [Group] Workout for [Level]" format. Include sample AI plan. |
| 4.14.4 | Launch YouTube channel with first 4 exercise tutorials | CSCS + Design | 4 videos | Videos published | CSCS demonstrates form. 30-60 second tutorials. Repurposed to Reels/TikTok. |
| 4.14.5 | Scale social media to full cadence | Growth | ongoing | Consistent posting | Instagram 5x/week, TikTok 5x/week, X 7x/week per go-to-market.md |

---

## Period 4: Scale & Evaluate (Weeks 43-52)

**Goal:** Scale acquisition. Ship remaining features. Evaluate platform expansion. Build toward product-market fit milestones.

### Epic 4.15: Strength Milestones & Bodyweight Tracking

**Owner:** Mid Dev
**Duration:** Weeks 43-45

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.15.1 | Implement strength milestone badges (10/25/50/100/250/500 workouts) | Mid | 2d | Badge system | Per go-to-market.md Section 5.4: workout count milestones |
| 4.15.2 | Implement volume milestones (10K/100K/1M kg total tonnage) | Mid | 1d | Tonnage milestones | Milestone celebration animation on achievement |
| 4.15.3 | Implement consistency badges (4-week streak, "Never missed Monday") | Mid | 2d | Streak system | Streak tracking, badge display in profile |
| 4.15.4 | Implement bodyweight tracking history (time-series chart) | Mid | 2d | BW chart | Weight entries with dates. Line chart in profile. |
| 4.15.5 | Implement bodyweight entry prompt (configurable frequency) | Mid | 1d | Prompt system | Weekly or bi-weekly reminder to log bodyweight |

### Epic 4.16: Cloud Sync Architecture Evaluation

**Owner:** Lead Dev + CEO
**Duration:** Weeks 45-48

NOT building cloud sync. Evaluating architecture for feasibility.

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.16.1 | Evaluate Firebase vs Cloud Run for backend | Lead | 3d | Architecture evaluation doc | Cost at 1K/10K/100K MAU. Latency. Offline sync complexity. Per devops-pipeline.md Section 9. |
| 4.16.2 | Design Firestore data model for sync | Lead | 2d | Data model design | How Room data maps to Firestore documents |
| 4.16.3 | Design conflict resolution strategy (local-first sync) | Lead | 2d | Sync strategy doc | Last-write-wins vs merge. Offline queue. Device-to-cloud flow. |
| 4.16.4 | Estimate implementation effort and timeline | Lead | 1d | Effort estimate | Week count for full implementation. Dependencies. Risks. |
| 4.16.5 | CEO decision: build cloud sync or defer | CEO | -- | Decision documented | Go/no-go based on user demand, cost, and PMF signals |

### Epic 4.17: AI Plan Quality Iteration

**Owner:** Lead Dev + CSCS
**Duration:** Weeks 43-48 (ongoing)

| # | Task | Owner | Frequency | Deliverable | Acceptance Criteria |
|---|------|-------|-----------|-------------|---------------------|
| 4.17.1 | Monthly AI plan quality review | CSCS | Monthly | Quality report | Review 50 real plans. Score accuracy, safety, progression logic. |
| 4.17.2 | Analyze actual-vs-planned weight/reps deviation | DA + Lead | Monthly | Deviation report | If users consistently modify AI suggestions, identify patterns |
| 4.17.3 | Iterate prompt based on real usage data | Lead + CSCS | As needed | Prompt v3.x/v4.0 | Prompt improvements backed by data. CSCS validates. |
| 4.17.4 | Evaluate Gemini model upgrades (if available) | Lead | Quarterly | Evaluation report | Compare plan quality on new vs current model. Cost impact. |

### Epic 4.18: Scaling Paid Acquisition

**Owner:** Growth + DA
**Duration:** Weeks 43-52

| # | Task | Owner | Deliverable | Acceptance Criteria |
|---|------|-------|-------------|---------------------|
| 4.18.1 | Scale winning paid channels by 2x | Growth | Increased budgets | Channels that hit CAC targets from Period 2 get doubled budget |
| 4.18.2 | Launch comparison blog posts ("Deep Reps vs Strong") | Growth | 1 article/month | Fair, transparent comparisons. Keyword-targeted. |
| 4.18.3 | Submit for Google Play "Editor's Choice" consideration | Growth | Application submitted | Follow Google Play editorial guidelines |
| 4.18.4 | Expand influencer program to mid-tier (100K-500K followers) | Growth | 2-3 mid-tier partnerships | Budget: $1K-$3K per video integration |
| 4.18.5 | Launch monthly community challenges | Growth + CSCS | Monthly challenges | "Squat volume challenge" type events in Discord/social |

### Epic 4.19: Platform Expansion Evaluation

**Owner:** CEO + Lead Dev
**Duration:** Weeks 48-52

| # | Task | Owner | Est. | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|---------------------|
| 4.19.1 | Evaluate iOS port feasibility | Lead | 3d | Feasibility report | KMP vs native Swift. Shared code percentage. Timeline. Cost. |
| 4.19.2 | Evaluate product-market fit signals | DA + CEO | 2d | PMF report | D30 > 15%? 50K+ DAU? If not, iOS port deferred. |
| 4.19.3 | Evaluate Wear OS companion app | Lead | 2d | Feasibility report | Rest timer + quick set logging on watch. Effort vs value. |
| 4.19.4 | Plan Year 2 roadmap | CEO | 5d | Roadmap doc | Prioritized feature list for Weeks 52-104. Based on all data from Year 1. |

---

## Phase 4 KPI Targets

Per product-strategy.md Section 6 and go-to-market.md Section 8.

### Acquisition

| Metric | Month 1 | Month 3 | Month 6 | Month 12 |
|--------|---------|---------|---------|----------|
| Cumulative downloads | 15,000 | 60,000 | 150,000 | 400,000 |
| Monthly run rate | 15,000 | 20-25K | 25-35K | 35-50K |
| Organic / Paid split | 60/40 | 45/55 | 50/50 | 60/40 |
| Blended CAC | < $1.50 | < $1.50 | < $1.00 | < $0.80 |

### Retention

| Metric | Month 1 | Month 3 | Month 6 | Month 12 |
|--------|---------|---------|---------|----------|
| D1 retention | 25% | 30% | 35% | 40% |
| D7 retention | 12% | 16% | 20% | 22% |
| D30 retention | 5% | 8% | 10% | 12% |
| WAU/MAU ratio | 0.25 | 0.30 | 0.35 | 0.40 |

### Engagement

| Metric | Month 3 | Month 6 | Month 12 |
|--------|---------|---------|----------|
| Avg workouts/user/week | 2.5 | 3.0 | 3.0 |
| AI plan generation rate | 70% | 75% | 80% |
| Template usage rate | 20% | 30% | 40% |
| North Star: workouts/week/active user | 2.0 | 2.5 | 2.5 |

### App Quality

| Metric | Target | Measurement |
|--------|--------|-------------|
| Play Store rating | 4.5+ by Month 6 | Play Console |
| Crash-free rate | >= 99.5% always | Crashlytics |
| Cold startup (P90) | < 1.5s | Macrobenchmark |
| WAU (2+ workouts/week) | 40,000 by Month 12 | Firebase Analytics |

---

## Reporting Cadence (Phase 4)

Per analytics-plan.md Section 7.

| Report | Frequency | Owner | Audience |
|--------|-----------|-------|----------|
| Daily metrics (Slack) | Daily 09:00 | Automated (DA setup) | PO, Lead Dev, DA |
| Weekly report | Monday | DA | Full team |
| Monthly deep-dive | First Monday | DA + CEO | Full team + stakeholders |
| Quarterly Business Review | Every 13 weeks | DA + CEO | Full team + investors (if applicable) |

---

## Decision Points

| Week | Decision | Decision Maker | Criteria |
|------|----------|---------------|----------|
| 33 | Start Phase 2 features or extend stabilization? | CEO | D7 retention >= 12% and crash-free >= 99.5% |
| 37 | Scale paid acquisition or optimize organic? | CEO + Growth | CAC within targets. D7 retention holds with paid users. |
| 42 | Build cloud sync? | CEO + Lead | User demand (top 3 feature request). PMF signals. Budget available. |
| 48 | Start iOS port? | CEO | D30 > 15%, 50K+ DAU, positive unit economics |
| 52 | Year 2 roadmap finalized | CEO | All Year 1 data analyzed. Product direction set. |

---

## Emergency Triggers

At any point during Phase 4, if these conditions occur, escalate immediately:

| Condition | Action |
|-----------|--------|
| D7 retention drops below 10% for 2 consecutive cohorts | Treat as product problem. Pause feature work. Full retention investigation. |
| Crash-free rate drops below 99% | Feature freeze. All hands on stability. |
| Play Store rating drops below 4.0 | War room: respond to every review, hotfix top issues, pause marketing spend. |
| Gemini API cost exceeds 2x budget for 2 consecutive months | Evaluate prompt optimization, caching improvements, or model downgrade. |
| Zero growth in WAU for 4 consecutive weeks | Reassess product-market fit. User research sprint. |
