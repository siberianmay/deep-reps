# Phase 3: Public Launch (Week 29)

One week of coordinated execution. The app goes from closed beta to production on Google Play with staged rollout, press outreach, and real-time monitoring.

---

## Team Allocation (Launch Week)

| Role | Primary Responsibility |
|------|----------------------|
| **Lead Dev** | On-call primary. Monitor crashes. Hotfix anything P0/P1. Staged rollout promotion decisions. |
| **Mid Dev** | On-call secondary. Hotfix support. Monitor ANR reports. |
| **QA** | Monitor crash-free rate, ANR rate, beta feedback channels. Smoke test each rollout stage. |
| **DevOps** | Execute staged rollout promotions. Monitor CI/CD pipeline. Emergency rollback if needed. |
| **Growth** | Execute launch playbook: press outreach, social media, influencer posts, Product Hunt, Reddit. |
| **DA** | Monitor real-time analytics: DAU, installs, funnel metrics, event validation at scale. |
| **CSCS** | Monitor AI plan quality with real user data. Respond to exercise science questions in community. |
| **Design** | Monitor visual bug reports. Produce any emergency store listing tweaks. |
| **CEO** | Decision-maker on rollout promotions and halts. Respond to press and store reviews. |

---

## Epic 3.1: Staged Production Rollout

**Owner:** DevOps + Lead Dev + CEO
**Duration:** Days 1-7

### Tasks

| # | Task | Owner | Day | Deliverable | Acceptance Criteria |
|---|------|-------|-----|-------------|---------------------|
| 3.1.1 | Create release tag (e.g., `v1.0.0`) | Lead | Day 0 (pre-launch) | Git tag + GitHub Release | Release pipeline triggers automatically |
| 3.1.2 | Verify release build on closed testing track (48h soak) | QA | Day -2 to Day 0 | Soak test report | Crash rate < 0.5% over 48h on closed track. No P0 bugs. |
| 3.1.3 | Promote to production at 1% rollout | DevOps | Day 1 (Monday) | 1% live | Manually trigger `promote-to-production.yml` with `rollout_percentage: 1` |
| 3.1.4 | Monitor 1% rollout for 24h | QA + Lead | Day 1 | Monitoring report | Crash rate < 1%, ANR < 0.3%, no data loss reports |
| 3.1.5 | Promote to 5% rollout | DevOps | Day 2 | 5% live | CEO approves promotion based on Day 1 metrics |
| 3.1.6 | Monitor 5% rollout for 24h | QA + Lead | Day 2 | Monitoring report | Crash rate < 0.8%, no spike in negative reviews |
| 3.1.7 | Promote to 20% rollout | DevOps | Day 3 | 20% live | CEO approves |
| 3.1.8 | Monitor 20% rollout for 48h | QA + Lead | Days 3-4 | Monitoring report | Crash rate < 0.7%, metrics stable |
| 3.1.9 | Promote to 50% rollout | DevOps | Day 5 | 50% live | CEO approves |
| 3.1.10 | Promote to 100% rollout | DevOps | Day 7 | Full production | Crash rate < 0.6%, all metrics stable |

### Rollout Halt Criteria

Immediately halt the rollout and do NOT promote to the next stage if any of:

- Crash-free rate drops below 98.5% at any stage
- ANR rate exceeds 0.5%
- Any data loss bug reported (P0)
- 1-star reviews spike to > 10% of new reviews
- AI plan generation failure rate exceeds 10%

**Halt procedure:**
1. DevOps: Halt rollout in Play Console
2. Lead: Identify root cause from Crashlytics / logs
3. Lead + Mid: Hotfix on emergency branch
4. QA: Verify fix on internal track
5. DevOps: Restart staged rollout from 1%

---

## Epic 3.2: Launch Day Execution (Day 1)

**Owner:** Growth + CEO
**Duration:** Day 1

### Tasks

| # | Task | Owner | Time | Deliverable |
|---|------|-------|------|-------------|
| 3.2.1 | Confirm production release is live (1% rollout) | DevOps | 8:00 AM | Play Console verified |
| 3.2.2 | Send email blast to waitlist | Growth | 9:00 AM | Emails sent |
| 3.2.3 | Post launch announcement on all social channels | Growth | 9:00 AM | Posts live on Instagram, TikTok, X |
| 3.2.4 | Activate Product Hunt listing (schedule for next day, Tuesday 12:01 AM PT) | Growth | 9:00 AM | PH page scheduled |
| 3.2.5 | Send press kit to 20 pre-identified journalists | Growth | 10:00 AM | Emails sent with personalized pitches |
| 3.2.6 | Notify influencer partners to publish launch content | Growth | 10:00 AM | Influencer posts go live Day 2-3 |
| 3.2.7 | Post in r/fitness daily thread, r/Android, r/GYM | Growth | 12:00 PM | Reddit posts live (follow self-promotion rules) |
| 3.2.8 | Monitor initial crash reports and reviews | QA | All day | Hourly check-ins |
| 3.2.9 | Respond to all initial app store reviews | CEO + Growth | All day | 100% response rate |

---

## Epic 3.3: Launch Week Monitoring (Days 1-7)

**Owner:** DA + QA + Lead Dev
**Duration:** Days 1-7

### Tasks

| # | Task | Owner | Frequency | Deliverable | Target |
|---|------|-------|-----------|-------------|--------|
| 3.3.1 | Monitor DAU and install count | DA | Daily | Slack report | 5,000 downloads in 7 days |
| 3.3.2 | Monitor crash-free rate | QA | Hourly (Day 1-2), then daily | Alert if < 99% | >= 99.5% |
| 3.3.3 | Monitor ANR rate | QA | Daily | Report | < 0.3% |
| 3.3.4 | Monitor onboarding funnel (completion rate) | DA | Daily | Funnel report | Identify biggest drop-off |
| 3.3.5 | Monitor workout completion rate | DA | Daily | Report | Target: 60%+ of started workouts completed |
| 3.3.6 | Monitor AI plan generation success rate | Lead + DA | Daily | Report | > 90% success rate when online |
| 3.3.7 | Monitor app store rating | Growth | Daily | Rating tracker | Target: 4.0+ average |
| 3.3.8 | Monitor Gemini API latency and error rate | Lead | Daily | Performance report | P95 < 5s, error rate < 5% |
| 3.3.9 | Triage any launch-day bugs | Lead + Mid | As reported | Bug fixes or hotfix | P0: fix within 24h. P1: fix within 3 days. |
| 3.3.10 | Publish "Week 1" retrospective on social media | Growth | Day 7 | Social post | Download count, user feedback highlights |

---

## Epic 3.4: Hotfix Readiness

**Owner:** Lead Dev + DevOps
**Duration:** Days 1-7 (standby)

### Tasks

| # | Task | Owner | Deliverable | Acceptance Criteria |
|---|------|-------|-------------|---------------------|
| 3.4.1 | Keep hotfix branch strategy ready | Lead | `hotfix/v1.0.1` branch template | Can branch, fix, test, deploy within 4 hours |
| 3.4.2 | Verify hotfix CI pipeline (fast path: skip full regression, run smoke tests only) | DevOps | Tested pipeline | Hotfix build -> internal track in < 15 minutes |
| 3.4.3 | Pre-stage emergency communication templates | CEO | Slack templates | Templates for: hotfix deployment, rollback announcement, user-facing changelog |
| 3.4.4 | Deploy hotfix if any P0 bug surfaces | Lead + DevOps | Hotfix release | Internal -> closed (2h soak) -> production staged rollout |

---

## Launch Week Targets

| Metric | Target | Measurement |
|--------|--------|-------------|
| Downloads (7 days) | 5,000 | Play Console |
| DAU by Day 7 | 500+ | Firebase Analytics |
| Average rating | 4.0+ | Play Console |
| Crash-free rate | >= 99.5% | Crashlytics |
| ANR rate | < 0.3% | Play Console vitals |
| Core loop completion | >= 60% | Analytics (workout_started -> workout_completed) |
| AI plan success rate | >= 90% (online) | Analytics (ai_plan_requested vs ai_plan_generated) |
| P0 bugs | 0 open | Bug tracker |

---

## Launch Week Communication Cadence

| Time | Channel | Content | Audience |
|------|---------|---------|----------|
| 8:00 AM daily | Slack #launch-war-room | Overnight metrics summary (crashes, installs, reviews) | Entire team |
| 12:00 PM daily | Slack #launch-war-room | Midday check-in (any issues flagged?) | Lead + QA + DevOps |
| 5:00 PM daily | Slack #launch-war-room | End-of-day report (DAU, installs, rating, open bugs) | Entire team |
| As needed | Slack #incidents | Incident escalation | On-call + CEO |

---

## Post-Launch Week Transition

After Day 7, if all targets are met:

1. **Dissolve war room.** Return to normal Slack channels.
2. **Transition to Phase 4 cadence.** Weekly reports (Monday), monthly deep-dives.
3. **Lead Dev:** Shift from hotfix standby to Phase 2 feature planning.
4. **Growth:** Transition from launch blitz to sustained acquisition (paid campaigns, content marketing).
5. **DA:** Transition from real-time monitoring to cohort analysis and experiment design.

If targets are NOT met:

- Extend war room until metrics stabilize.
- CEO decides whether to increase rollout, hold, or roll back.
- No Phase 2 features start until launch stability is confirmed.
