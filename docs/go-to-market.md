# Deep Reps -- Go-to-Market Strategy

## Document Purpose

This is the operational go-to-market plan for Deep Reps, covering market analysis through 12-month targets. All numbers assume Android-only launch in a single market (English-speaking, US-primary with global English reach). **Deep Reps is a 100% free app** (see product-strategy.md Section 4). Marketing strategy emphasizes "free" as a core differentiator against paid competitors.

---

## 1. Market Analysis

### 1.1 Total Addressable Market (TAM)

| Segment | Size | Source Basis |
|---------|------|--------------|
| Global fitness app market (2025) | ~$19B revenue | Statista, Grand View Research |
| Strength/gym tracking sub-segment | ~$2.8B (est. 15% of fitness app revenue) | Segment extrapolation |
| Android share of fitness apps | ~55% of downloads, ~35% of revenue | App Annie / data.ai benchmarks |
| Serviceable Addressable Market (SAM) | ~$980M (Android strength tracking) | TAM x Android revenue share |
| Serviceable Obtainable Market (SOM) Year 1 | ~$500K-$2M | Realistic capture for a new entrant |

The gym/strength tracking niche is a subset of the broader fitness app market. Key data points:

- Google Play has 400+ gym tracking apps with 10K+ installs
- Top 5 (Strong, JEFIT, Hevy, Fitbod, GymShark Training) capture ~60% of category revenue
- Long tail of apps with under 50K installs represents fragmented opportunity
- Category grows at ~12-15% CAGR driven by gym membership recovery post-COVID and AI integration trends

### 1.2 Market Trends

1. **AI-powered personalization** -- Users expect adaptive plans, not static templates. Fitbod pioneered this; most competitors still use rigid programs. Deep Reps' Gemini-powered plan generation is table stakes entering 2026, but execution quality (context-aware plans using real training history) is the differentiator.

2. **Offline-first is underserved** -- Most AI-powered competitors require connectivity. Gym WiFi is notoriously bad. Deep Reps' offline fallback (cached plans, baseline profiles, manual entry) addresses a genuine pain point that competitors ignore.

3. **Simplicity backlash** -- Power users are leaving bloated apps (JEFIT) for cleaner alternatives (Hevy, Strong). The market rewards focused UX over feature count.

4. **Android-first gap** -- Many top gym apps prioritize iOS. Strong launched Android years after iOS. Hevy's Android version trails in feature parity. There is a real audience of Android users underserved by half-baked ports.

5. **Exercise science credibility** -- Users increasingly distinguish between apps built by developers who Googled exercises vs. apps backed by certified professionals. CSCS involvement is a marketing asset, not just a product asset.

### 1.3 Competitive Landscape

| Competitor | Strengths | Weaknesses | Deep Reps Angle |
|-----------|-----------|------------|-----------------|
| **Strong** | Clean UX, large user base, Apple Watch | Minimal AI, limited free tier, iOS-first | Better AI plans, true Android-first, offline reliability |
| **Hevy** | Free core features, social, growing fast | No AI programming, social features can distract | AI plan generation, distraction-free design |
| **JEFIT** | Huge exercise library, community | Dated UI, bloated, slow | Modern UX, curated library over quantity |
| **Fitbod** | AI-powered, adaptive | Expensive subscription, iOS-centric, black-box AI | Transparent plan logic, CSCS-validated, Android-native |
| **GymShark Training** | Brand power, video content | Tied to GymShark ecosystem, limited tracking | Independent, deeper tracking, better progress analytics |

### 1.4 Positioning Statement

**For** serious gym-goers on Android **who** want intelligent workout plans without the bloat, **Deep Reps is** a strength training tracker **that** generates AI-powered session plans grounded in real exercise science, works offline, and learns from every workout. **Unlike** Fitbod (expensive, iOS-first) or Strong (no AI), **Deep Reps** combines CSCS-validated programming with adaptive AI that uses your actual training history -- not generic templates.

### 1.5 Differentiation Matrix

| Differentiator | Deep Reps | Strong | Hevy | Fitbod |
|----------------|-----------|--------|------|--------|
| AI plan generation | Yes (Gemini, history-aware) | No | No | Yes (proprietary) |
| CSCS-validated exercise science | Yes (credentialed specialist) | No | No | Unclear |
| Offline-first architecture | Full (except AI generation) | Partial | Partial | No |
| Android-native quality | Primary platform | Port | Good | Port |
| Superset/circuit support | Phase 2 | Yes | Yes | Limited |
| Exercise detail (anatomy, cues) | 2D anatomy + CSCS cues | Basic | Basic | Yes |
| Cold-start experience profiles | 3 tiers (beginner/inter/adv) | Manual | Manual | Yes |
| Price | TBD | $4.99/mo or $74.99 lifetime | Free (premium TBD) | $12.99/mo |

---

## 2. App Store Optimization (ASO)

### 2.1 App Title and Subtitle

**Title:** Deep Reps: AI Gym Tracker
**Short Description (80 chars):** AI workout plans, strength tracking & progress. Built by certified trainers.

The title targets three high-value keyword clusters: "AI," "gym," and "tracker." The word "Deep" is brandable and memorable. "Reps" immediately signals strength training context.

### 2.2 Keyword Strategy

**Primary keywords** (high volume, high competition -- target in title + short description):
- gym tracker
- workout tracker
- strength training app
- AI workout planner

**Secondary keywords** (medium volume, medium competition -- target in long description):
- weight lifting log
- gym log app
- workout plan generator
- exercise tracker
- progressive overload tracker
- personal record tracker
- 1RM calculator

**Long-tail keywords** (lower volume, lower competition -- target in description body):
- AI personal trainer Android
- offline gym tracker
- barbell workout planner
- compound exercise order
- CSCS workout program
- superset timer app
- workout template app

**Localized keyword targets** (English variations):
- "gym tracker" (US)
- "gym planner" (UK)
- "weight training log" (AU)
- "lifting tracker" (general)

### 2.3 Full Description Structure

The Google Play long description (4000 chars max) should follow this structure:

1. **Opening hook** (2 sentences) -- Pain point + solution. "Tired of guessing your weights every session? Deep Reps uses AI to build your workout plan from your actual training history."
2. **3 key benefits** with emoji-free bullet formatting:
   - AI-generated plans that adapt to your progress
   - Works offline -- log sets even with no signal
   - Built by a Certified Strength & Conditioning Specialist
3. **Feature list** (8-10 items, keyword-rich)
4. **Social proof line** (post-launch: "Trusted by X lifters" or rating callout)
5. **Closing CTA** -- "Download and get your first AI workout plan in 60 seconds."

### 2.4 Screenshot Strategy

**8 screenshots**, designed for the Google Play carousel (phone mockups on solid color backgrounds):

| Position | Content | Purpose |
|----------|---------|---------|
| 1 | Headline: "AI Plans Your Workout" + workout plan screen | Hook -- primary value prop |
| 2 | Muscle group selection screen | Show the core flow start |
| 3 | Active workout logging screen | Demonstrate mid-workout UX |
| 4 | Progress chart (1RM over time) | Appeal to data-driven users |
| 5 | Exercise detail with anatomy diagram | Show exercise science depth |
| 6 | Workout complete summary with PR highlights | Reward/gamification appeal |
| 7 | Headline: "Works Offline" + offline indicator | Address connectivity pain |
| 8 | Template save/load screen | Power user feature |

**Design requirements:**
- Dark background (matches gym app conventions and the app's likely dark theme)
- Bold, sans-serif headline text on each screenshot (max 5 words)
- Device frame: recent Pixel model
- Consistent brand color accent throughout
- No cluttered UI -- show clean, focused states

### 2.5 Promo Video

**30-second video** for the Play Store listing:

- 0-5s: Problem statement (text overlay on gym footage: "Your workout plan shouldn't be a guess")
- 5-15s: App demo -- select muscles, generate AI plan, see proposed sets
- 15-22s: Mid-workout logging demo -- fast set completion, rest timer
- 22-27s: Progress screen with PR animation
- 27-30s: Logo + tagline + download CTA

Produce at 1080x1920 (portrait, Play Store spec). No voiceover -- text overlays + music. Keep under 10MB for fast loading.

### 2.6 Category and Content Rating

- **Primary category:** Health & Fitness
- **Content rating:** PEGI 3 / Everyone (no user-generated content at launch, no social features)
- **Tags:** Fitness, Workout, Health, Training

### 2.7 Review Management

**Pre-launch:**
- Prepare templated responses for common review themes (5-star thank you, bug report acknowledgment, feature request redirection, negative experience recovery)
- Set up daily review monitoring via AppFollow or equivalent

**Post-launch:**
- Respond to every 1-3 star review within 24 hours
- Respond to feature requests with "noted, added to our backlog" (only if genuinely considered)
- Never argue with reviewers. Acknowledge, fix if valid, clarify if misunderstanding
- Target response rate: 100% for 1-3 stars, 50% for 4 stars, 20% for 5 stars

**In-app review prompt:**
- Trigger after the user completes their 5th workout (proven engagement threshold)
- Never prompt during a workout
- If user rates 1-3 stars in-app, redirect to feedback form (not Play Store)
- If user rates 4-5 stars in-app, redirect to Play Store review page
- Maximum 1 prompt per 90 days
- Never prompt if user dismissed previously

---

## 3. Launch Strategy

### 3.1 Pre-Launch (T-12 weeks to T-0)

#### Beta Program (T-12 to T-4)

| Week | Activity | Target |
|------|----------|--------|
| T-12 | Internal alpha release to team + 10 trusted gym-goers | Core flow validation |
| T-10 | Closed beta via Google Play internal testing track | 50 users |
| T-8 | Expand beta via closed testing track, recruit from fitness subreddits and Discord servers | 200 users |
| T-6 | Incorporate beta feedback, fix critical bugs, polish UX | Beta NPS > 40 |
| T-4 | Open beta via open testing track on Google Play | 500+ users |
| T-2 | Feature freeze. Bug fixes and performance optimization only | Crash-free rate > 99.5% |
| T-1 | Final beta build. Prepare production release. | Ready for launch |

**Beta recruitment channels:**
- r/fitness, r/weightroom, r/bodybuilding, r/GYM (Reddit) -- post seeking beta testers, position as "built by a CSCS"
- Fitness Discord servers (Stronger by Science, Jeff Nippard, Renaissance Periodization communities)
- Local gym partnerships (2-3 gyms, offer free memberships or merchandise for tester recruitment)
- Personal networks of the CSCS (coaching clients, training partners)

**Beta incentives:**
- "Founding Member" badge in the app (visible in profile, permanent)
- Founding Member early access and priority feature requests
- Direct Slack/Discord channel with the development team

#### Waitlist and Social (T-8 to T-0)

- **Landing page** at deepreps.app: Email capture, feature preview, "Get early access" CTA
- Target: 2,000 email signups before launch
- **Social accounts** (Instagram, TikTok, X/Twitter): Start posting at T-8
  - Content: Exercise science tips from the CSCS (short-form video), behind-the-scenes app development, beta user testimonials
  - Posting cadence: 3x/week Instagram, 4x/week TikTok, daily X/Twitter
  - Goal: 1,000 followers across platforms before launch

#### Pre-Launch PR Prep (T-6 to T-0)

- Write press kit: App description, founder story, CSCS credential highlight, screenshots, logo, brand assets
- Identify 20 Android/fitness tech journalists and bloggers
- Draft personalized pitch emails (not mass blast)
- Prepare 3 different angles:
  1. "AI-powered gym app built by a certified strength coach, not just developers"
  2. "The first gym tracker designed offline-first for real gym conditions"
  3. "Android-first gym app challenges iOS-dominant competitors"

### 3.2 Launch Week (T-0 to T+7)

| Day | Activity |
|-----|----------|
| T+0 (Monday) | Production release on Google Play. Email blast to waitlist. Social media announcement across all channels. |
| T+0 | Push to Product Hunt (schedule for Tuesday 12:01 AM PT for maximum visibility). Prepare PH page with GIFs, description, maker comment. |
| T+1 | Reddit posts: r/fitness daily thread, r/Android, r/bodybuilding, r/GYM. Follow subreddit self-promotion rules precisely. |
| T+1 | Send press kit to the 20 pre-identified journalists/bloggers. |
| T+2 | Influencer posts go live (see Section 7 for influencer plan). |
| T+3 | Respond to all initial reviews. Fix any launch-day bugs with hotfix release. |
| T+4-5 | Monitor metrics. Adjust paid campaigns if running. |
| T+6-7 | Publish a "Week 1" retrospective on social media (download count, user feedback highlights). |

**Launch week targets:**
- 5,000 downloads in first 7 days
- 4.0+ average rating
- 500+ DAU by day 7
- Crash-free rate > 99.5%

### 3.3 Post-Launch 90-Day Plan

#### Month 1 (Days 1-30): Stabilize and Learn

- **Product:** Hotfix any critical bugs. Ship no new features. Focus on stability and performance.
- **Growth:** Monitor organic acquisition sources. Optimize Play Store listing based on search term data from Google Play Console.
- **Retention:** Analyze D1/D7 retention cohorts. Identify the biggest drop-off point in the user funnel.
- **Content:** 4 blog posts (exercise guides targeting SEO keywords). 12 social media posts per week.
- **Target:** 15,000 cumulative downloads. D1 retention > 30%. D7 retention > 16%.

#### Month 2 (Days 31-60): Optimize and Experiment

- **Product:** Ship 1-2 user-requested features based on review/feedback analysis. A/B test onboarding flow.
- **Growth:** Launch initial paid campaigns (Google Ads App Campaigns). Test 3 creative variants. Begin ASO A/B tests (screenshots, short description).
- **Retention:** Implement push notification sequences (see Section 5). Launch first in-app challenge/milestone system.
- **Content:** 4 blog posts. Begin YouTube exercise tutorial series. 15 social posts per week.
- **Target:** 35,000 cumulative downloads. D7 retention > 18%. Paid CAC < $2.50.

#### Month 3 (Days 61-90): Scale What Works

- **Product:** Ship features that drive retention (identified from Month 1-2 data). Begin A/B testing onboarding and AI plan presentation variants.
- **Growth:** Scale winning paid channels by 2x. Expand influencer partnerships. Submit for "Editor's Choice" consideration on Google Play.
- **Retention:** Analyze D30 cohort. Refine push notification strategy based on engagement data. Launch re-engagement campaign for churned users.
- **Content:** 4 blog posts. Launch email newsletter (weekly). 15 social posts per week.
- **Target:** 60,000 cumulative downloads. D30 retention > 8%. 4.3+ average rating.

---

## 4. User Acquisition Channels

### 4.1 Organic Channels

| Channel | Strategy | Estimated Monthly Volume (Steady State) | CAC |
|---------|----------|----------------------------------------|-----|
| **Google Play Search (ASO)** | Keyword optimization, review volume, rating | 3,000-8,000 downloads/mo | $0 (organic) |
| **Reddit** | Weekly value posts in fitness subreddits (not spam -- genuine exercise science content with app mention) | 500-1,500 downloads/mo | $0 (time cost only) |
| **Blog/SEO** | Exercise guides, "best exercises for X" articles, comparison posts | 1,000-3,000 downloads/mo (ramp over 6 months) | $0 (content production cost amortized) |
| **Word of mouth** | In-app sharing (workout summary share cards), referral program | 500-2,000 downloads/mo | $0 |
| **YouTube** | Exercise tutorials, app walkthroughs, CSCS educational content | 300-1,000 downloads/mo | $0 (production cost amortized) |
| **Product Hunt / HackerNews** | One-time launch spike, periodic "Show HN" for major updates | 1,000-3,000 (launch spike) | $0 |

**Organic total (steady state, month 6+):** 5,000-15,000 downloads/month

### 4.2 Paid Channels

| Channel | Strategy | Estimated Monthly Volume | Target CAC | Creative Approach |
|---------|----------|-------------------------|------------|-------------------|
| **Google Ads App Campaigns** | Universal App Campaigns targeting gym/fitness intent | 3,000-10,000 installs/mo | $1.50-$2.50 | Screen recordings showing AI plan generation, before/after progress charts |
| **Meta (Instagram/Facebook)** | Interest targeting: gym, weightlifting, fitness apps. Lookalike audiences from beta users | 2,000-6,000 installs/mo | $2.00-$3.50 | Short-form video: "I let AI plan my workout" format, UGC-style |
| **TikTok Ads** | In-feed ads targeting fitness content consumers, age 18-34 | 1,500-4,000 installs/mo | $1.00-$2.00 | Native TikTok format: gym POV, screen recording overlay, trending audio |
| **YouTube Pre-roll** | Target fitness/gym channels, exercise tutorial viewers | 500-1,500 installs/mo | $2.50-$4.00 | 15-second non-skippable: "Your AI gym coach" with app demo |
| **Reddit Ads** | Promoted posts in r/fitness, r/GYM, r/bodybuilding | 300-800 installs/mo | $2.00-$3.00 | Text-heavy format matching Reddit conventions, not polished ads |

**Paid total (steady state):** 7,000-22,000 installs/month at blended CAC of $1.80-$2.80

### 4.3 Creative Strategy

**Core creative themes:**

1. **"AI Plans It, You Lift It"** -- Focus on the AI plan generation moment. Screen recording of selecting muscles -> generating plan -> seeing sets/reps appear.
2. **"Built by a Strength Coach, Not a Developer"** -- CSCS credential as trust signal. Show exercise detail cards with anatomy diagrams and form cues.
3. **"Works When Your Gym WiFi Doesn't"** -- Offline reliability. Show airplane mode icon with app still fully functional.
4. **"Your Progress, Visualized"** -- Charts, PRs, volume tracking. Appeal to data-driven lifters.

**Creative production cadence:**
- 4 new static creatives per month
- 2 new video creatives per month
- A/B test every creative for minimum 7 days / 1,000 impressions before killing underperformers
- Refresh all ad creatives every 6 weeks to combat fatigue

### 4.4 Six-Month Budget Allocation

Assumes total marketing budget of $50,000 for first 6 months.

| Month | Google Ads | Meta | TikTok | YouTube | Reddit | Influencers | Content Production | Tools/ASO | Total |
|-------|-----------|------|--------|---------|--------|-------------|-------------------|-----------|-------|
| 1 | $1,500 | $1,000 | $500 | $0 | $300 | $2,000 | $1,500 | $500 | $7,300 |
| 2 | $2,000 | $1,500 | $1,000 | $500 | $300 | $1,500 | $1,000 | $500 | $8,300 |
| 3 | $2,500 | $2,000 | $1,500 | $500 | $300 | $2,000 | $1,000 | $400 | $10,200 |
| 4 | $2,500 | $2,000 | $1,500 | $500 | $300 | $1,500 | $800 | $400 | $9,500 |
| 5 | $2,500 | $2,000 | $1,500 | $500 | $300 | $1,000 | $800 | $400 | $9,000 |
| 6 | $2,000 | $1,500 | $1,000 | $500 | $300 | $500 | $800 | $400 | $7,000 |
| **Total** | **$13,000** | **$10,000** | **$7,000** | **$2,500** | **$1,800** | **$8,500** | **$5,900** | **$2,600** | **$51,300** |

**Budget rationale:**
- Months 1-3 are heavier spend (launch momentum). Months 4-6 shift toward optimized channels with proven CAC.
- Google Ads gets the largest share because App Campaigns provide the most scalable, measurable installs on Android.
- Influencer spend front-loaded for launch amplification, then reduced as organic and paid channels mature.
- Content production declines over time as the initial exercise media library is built out and blog content compounds.

### 4.5 Organic/Paid Split Targets

| Timeframe | Organic % | Paid % |
|-----------|-----------|--------|
| Month 1 | 60% (launch hype, PR, Product Hunt) | 40% |
| Month 3 | 45% | 55% |
| Month 6 | 50% | 50% |
| Month 12 | 60% | 40% |

The long-term goal is 60/40 organic/paid. SEO, ASO, and word-of-mouth compound over time, reducing dependence on paid channels. If organic exceeds 60% at month 12, reallocate paid budget to retention experiments and content marketing.

---

## 5. Retention Marketing

### 5.1 Push Notification Strategy

#### Triggers and Templates

| Trigger | Timing | Message Template | Goal |
|---------|--------|------------------|------|
| **Onboarding incomplete** | 24h after install, no workout started | "Your first AI workout plan is ready in 60 seconds. Select your muscles and go." | Activate new users |
| **First workout completed** | Immediately after | "First workout logged. You're building your training history -- Deep Reps gets smarter with every session." | Reinforce value loop |
| **2-day inactivity** | 48h since last workout | "Rest day or skipped day? Your [muscle group] is ready for round [N+1]." | Re-engagement |
| **5-day inactivity** | 120h since last workout | "It's been 5 days. Your last [muscle group] session hit [X] total volume. Ready to beat it?" | Re-engagement with data |
| **14-day inactivity** | 14 days | "We saved your templates and progress. Pick up where you left off whenever you're ready." | Win-back (soft) |
| **PR opportunity** | When next workout could beat a PR based on trend | "Based on your last 3 sessions, you're on track for a new [exercise] PR. Ready?" | Motivation |
| **Weekly summary** | Sunday evening | "This week: [X] workouts, [Y] total volume, [Z] PRs. Keep building." | Consistency reinforcement |
| **Streak milestone** | On 3/7/14/30-day streaks | "3 weeks straight. That's consistency. Your [muscle group] volume is up [X]% this month." | Gamification |

#### Notification Caps

- Maximum 3 push notifications per week per user
- Maximum 1 per day
- Never send notifications between 10 PM and 8 AM (user's local time)
- If user dismisses 3 consecutive notifications without opening the app, stop all non-critical notifications for 14 days
- Users who disable notifications in OS settings: fall back to in-app messaging only

#### Notification A/B Testing Plan

- Test message framing: data-driven ("Your volume is up 12%") vs. motivational ("Don't break the streak") vs. action-oriented ("Your push day template is ready")
- Test timing: morning (7-9 AM) vs. afternoon (12-2 PM) vs. evening (5-7 PM)
- Minimum sample size per variant: 500 users, 7-day test duration
- Primary metric: notification-to-session conversion rate

### 5.2 In-App Messaging

| Trigger | Message Type | Content |
|---------|-------------|---------|
| First app open | Tooltip tour | Highlight 3 key features: muscle selection, AI plan, progress charts |
| After 3rd workout | Bottom sheet | "Save this as a template for quick access next time" |
| After 5th workout | Card in home screen | "You have enough data for accurate progress charts. Check your trends." |
| After 10th workout | Modal (dismissible) | In-app review prompt (gated: 4-5 stars to Play Store, 1-3 stars to feedback form) |
| After 1st PR | Celebration animation | Full-screen confetti + PR details + "Share" option (generates image card) |
| Feature discovery | Contextual tooltip | Highlight supersets, notes, timer customization when user reaches relevant screens |

### 5.3 Email Marketing

Email is secondary to push notifications. Used for users who opt in during waitlist signup or in-app.

| Email | Trigger | Content |
|-------|---------|---------|
| Welcome | Account creation | App value overview, quick start guide, link to exercise library |
| Week 1 recap | 7 days post-install | Summary of first week, tips for getting more from the app |
| Monthly progress | 1st of each month | Monthly stats: workouts completed, volume trend, PRs hit |
| Win-back | 30 days inactive | "Your training data is still here. Here's what you were working on." |
| Product updates | Major releases | New features, improvements, changelog highlights |

**Email cadence cap:** Maximum 4 emails per month. Unsubscribe in every email. Segment by engagement level (active / lapsed / churned).

### 5.4 Gamification and Engagement Loops

| Mechanic | Implementation | Retention Impact |
|----------|----------------|-----------------|
| **Streak counter** | Consecutive workout days/weeks displayed on home screen | D7 retention +8-15% (industry benchmark) |
| **PR celebrations** | Animated celebration when any PR type is hit | Session satisfaction, share-worthy moments |
| **Workout milestones** | Badges at 10/25/50/100/250/500 workouts | Long-term retention anchor |
| **Volume milestones** | Total tonnage milestones (10,000 kg / 100,000 kg / 1,000,000 kg) | Sense of cumulative progress |
| **Consistency badges** | "4-week streak," "Never missed a Monday" | Habit formation |
| **Share cards** | Auto-generated image cards for workout summaries and PRs | Organic acquisition via social sharing |

**Constraints:**
- No leaderboards (no social features per FEATURES.md scope)
- No competitive gamification between users
- All gamification is self-referential (you vs. your past self)
- Badges and celebrations must feel earned, not patronizing -- this audience is experienced gym-goers, not casual fitness consumers

---

## 6. Content Strategy

### 6.1 Exercise Media Production

The exercise library is the app's content foundation. Every exercise needs visual and educational media.

**Phase 1 (Pre-launch):**
- 2D anatomical diagram: single shared template SVG (`resources/anatomy_template.svg`) with programmatic muscle group highlighting per exercise (primary at 85% opacity, secondary at 30%)
- Production method: Stock SVG cleaned and neutralized in-house. No external commissioning. Budget: $0.
- Text content (descriptions, cues, pros) written by CSCS

**Phase 2 (Post-launch, Month 2-4):**
- Short-form video demonstrations (5-10 seconds per exercise, looping)
- Production: Hire a videographer for 2-3 shoot days at a gym. CSCS demonstrates. Budget: $3,000-$5,000 per shoot day including talent, location, editing.
- Format: MP4, compressed for mobile (<2MB per clip), cached locally for offline access
- Shoot priority: Compound exercises first (squat, deadlift, bench, row, OHP), then isolations

**Phase 3 (Month 4-6):**
- Full exercise tutorial videos (30-60 seconds with cues overlay) for top 20 most-used exercises
- Repurpose for YouTube, Instagram Reels, TikTok

### 6.2 Blog and SEO Strategy

**Target:** Rank for mid-tail fitness keywords that drive app discovery.

**Content pillars:**

1. **Exercise guides** -- "How to [Exercise]: Form, Benefits, and Common Mistakes"
   - Target keywords: "how to deadlift," "bench press form," "lat pulldown technique"
   - Volume: 4 articles/month in Month 1-3, then 2/month ongoing
   - Word count: 1,500-2,500 words
   - Include: CSCS author bio, embedded anatomy diagrams from the app, CTA to download

2. **Workout programming** -- "Best [Muscle Group] Workout for [Level]"
   - Target keywords: "chest workout for beginners," "back workout routine," "leg day exercises"
   - Volume: 2 articles/month
   - Include: Sample AI-generated plan from Deep Reps, CTA to "get a personalized version in the app"

3. **Strength standards and progress** -- "How Much Should I [Exercise]?" / "Am I Strong Enough?"
   - Target keywords: "average bench press," "strength standards by body weight"
   - Volume: 1 article/month
   - Include: Reference to Deep Reps' strength milestone tracking

4. **App comparisons** (Month 3+) -- "Deep Reps vs Strong" / "Best Gym Tracking Apps for Android"
   - Transparent, fair comparisons. Highlight genuine advantages. Acknowledge competitor strengths.
   - Volume: 1 article/month

**Technical SEO requirements:**
- Blog hosted on deepreps.app/blog (subdirectory, not subdomain, for domain authority)
- Schema markup for articles (author, date, FAQ)
- Internal linking between related exercise guides
- Mobile-optimized (this audience likely reads on phones)

### 6.3 Social Media Calendar

**Platforms and cadence:**

| Platform | Posts/Week | Content Mix | Primary Audience |
|----------|-----------|-------------|-----------------|
| Instagram | 5 | 2 exercise tips (Reels), 1 app feature highlight, 1 user spotlight/PR share, 1 CSCS Q&A | 18-35, visual learners, gym culture |
| TikTok | 5 | 3 exercise form videos, 1 "AI planned my workout" POV, 1 gym humor/relatable | 18-28, discovery-driven |
| X/Twitter | 7 | 3 exercise science threads, 2 app updates, 1 community engagement, 1 industry commentary | Fitness nerds, tech-fitness crossover |
| YouTube | 1 | Long-form exercise tutorials or "week of training with Deep Reps" vlogs | 20-40, deep-dive learners |

**Content creation workflow:**
- CSCS provides exercise science accuracy review for all content
- Batch-produce 2 weeks of content at a time
- Use scheduling tools (Buffer, Later, or native platform schedulers)
- Repurpose: 1 YouTube video becomes 3-4 short clips for Reels/TikTok/Shorts

### 6.4 Community Building

**Phase 1 (Pre-launch to Month 3): Seeding**
- Create a Discord server for beta testers. Keep it open post-launch for engaged users.
- Channels: #feature-requests, #bug-reports, #workout-logs, #form-check, #general
- Target: 500 active Discord members by Month 3
- CSCS does weekly "Ask Me Anything" sessions in Discord (30 min)

**Phase 2 (Month 3-6): Growing**
- Launch monthly challenges ("Squat volume challenge: log 10,000 kg total this month")
- Feature community member workouts/PRs on social media (with permission)
- Start a subreddit r/DeepReps if Discord reaches 1,000 members

**Phase 3 (Month 6+): Sustaining**
- Community moderators from engaged users
- User-generated content program (submit gym clips using Deep Reps for social media feature)
- CSCS monthly "Programming Masterclass" live stream (YouTube + Discord)

---

## 7. Influencer Partnership Plan

### 7.1 Target Tiers

| Tier | Follower Range | Count to Partner | Purpose | Budget per Partnership |
|------|---------------|------------------|---------|----------------------|
| **Nano** | 1K-10K | 15-20 | Authentic gym-goer endorsements, high engagement rates | Early access + Founding Member badge + $50-$150 per post |
| **Micro** | 10K-100K | 5-8 | Credible fitness content creators, niche audiences | $200-$500 per post/video |
| **Mid-tier** | 100K-500K | 2-3 | Broader reach, category authority | $1,000-$3,000 per video integration |
| **Macro** | 500K+ | 0-1 (opportunistic) | Mass awareness, only if ROI-positive | $5,000-$10,000 per integration |

### 7.2 Ideal Influencer Profile

- Active gym-goer (not just fitness model -- actually tracks workouts)
- Android user (this is critical -- iPhone influencers cannot authentically demo an Android app)
- Audience is primarily male 18-35 (aligns with strength training demographics)
- Content style: educational or training-log format, not purely motivational/aesthetic
- Already uses or has used a workout tracking app (can speak to the comparison authentically)
- No history of promoting questionable supplements or making dangerous training claims

### 7.3 Partnership Structures

| Structure | Description | Best For |
|-----------|-------------|----------|
| **Sponsored post** | Single Instagram/TikTok post or Reel featuring the app | Nano and micro tiers, quick awareness |
| **Integration video** | YouTube or TikTok video where influencer uses Deep Reps during an actual workout | Micro and mid-tier, authentic demonstration |
| **Long-term ambassador** | 3-month partnership: 2 posts/month + Story mentions + discount code | 2-3 top-performing micro/mid-tier influencers |
| **Affiliate/commission** | Unique referral link + commission on conversions | All tiers as add-on to flat fee |
| **Product feedback** | Influencer provides genuine feedback, we incorporate it, they document the process | Mid-tier with engaged audiences |

### 7.4 Influencer Budget (Included in Section 4.4 Total)

| Month | Spend | Focus |
|-------|-------|-------|
| 1 | $2,000 | Launch: 5 nano + 2 micro partnerships for launch week amplification |
| 2 | $1,500 | 3 nano + 1 micro ongoing content |
| 3 | $2,000 | 1 mid-tier integration video + 3 nano posts |
| 4 | $1,500 | Ambassador program starts (2 micro influencers) |
| 5 | $1,000 | Ambassador content + 2 nano posts |
| 6 | $500 | Ambassador renewal evaluation. Scale or cut based on CAC data. |
| **Total** | **$8,500** | |

### 7.5 Tracking and Attribution

- Every influencer gets a unique UTM-tagged link and/or promo code
- Track: clicks, installs, D7 retention of referred users, conversion to premium (if applicable)
- Calculate per-influencer CAC: (payment + product cost) / installs attributed
- Benchmark: Influencer CAC should be < $3.00 to continue partnership
- Report to each influencer: their install count and engagement (transparency builds long-term relationships)
- Tool: Use a mobile attribution platform (Adjust, AppsFlyer, or Branch) for accurate install attribution

---

## 8. Metrics and Targets

### 8.1 Download Targets

| Timeframe | Cumulative Downloads | Monthly Run Rate |
|-----------|---------------------|-----------------|
| Month 1 | 15,000 | 15,000 |
| Month 3 | 60,000 | 20,000-25,000 |
| Month 6 | 150,000 | 25,000-35,000 |
| Month 12 | 400,000 | 35,000-50,000 |

**Assumptions behind these targets:**
- Targets assume $50K marketing spend over first 6 months, then $8K-$12K/month ongoing
- Organic growth compounds via ASO, SEO, and word of mouth
- No viral moments assumed (if one occurs, these numbers are conservative)
- These targets place Deep Reps in the top 100 Health & Fitness apps on Google Play by month 12

### 8.2 CAC by Channel (Targets)

| Channel | Target CAC (Month 1-3) | Target CAC (Month 6+) |
|---------|------------------------|----------------------|
| Google Ads | < $2.50 | < $2.00 |
| Meta | < $3.50 | < $2.50 |
| TikTok | < $2.00 | < $1.50 |
| YouTube | < $4.00 | < $3.00 |
| Reddit Ads | < $3.00 | < $2.50 |
| Influencers | < $3.00 | < $2.00 |
| Blended paid | < $2.80 | < $2.00 |
| Blended all (incl. organic) | < $1.50 | < $0.80 |

**CAC guardrails:**
- If any channel exceeds 2x target CAC for 2 consecutive weeks, pause and diagnose
- If blended paid CAC exceeds $3.50, reduce spend and shift budget to best-performing channel
- CAC must remain below the cost threshold where paid acquisition is sustainable given founder budget (see product-strategy.md Section 4.2 for operating cost tiers)

### 8.3 Organic/Paid Split Targets

| Timeframe | Organic | Paid |
|-----------|---------|------|
| Month 1 | 60% | 40% |
| Month 3 | 45% | 55% |
| Month 6 | 50% | 50% |
| Month 12 | 60% | 40% |

### 8.4 Retention Targets

| Metric | Month 1 Target | Month 3 Target | Month 6 Target | Month 12 Target |
|--------|---------------|----------------|----------------|-----------------|
| D1 retention | 25% | 30% | 35% | 40% |
| D7 retention | 12% | 16% | 20% | 22% |
| D30 retention | 5% | 8% | 10% | 12% |
| D90 retention | -- | -- | 8% | 10% |
| WAU/MAU ratio | 0.25 | 0.30 | 0.35 | 0.40 |

**Context:** Industry benchmarks for Health & Fitness apps are approximately D1: 20-25%, D7: 12-15%, D30: 4-6%. Launch targets are at industry average; 12-month targets represent top-quartile performance. Targets aligned with product-strategy.md Section 6.2. If D7 retention falls below 12% in Month 2, treat as a product problem (not a marketing problem) and escalate to Product Owner.

### 8.5 App Store Rating Target

| Timeframe | Target Rating | Strategy |
|-----------|--------------|----------|
| Month 1 | 4.0+ | Respond to all negative reviews, hotfix bugs within 48h |
| Month 3 | 4.3+ | In-app review prompt targeting engaged users (5+ workouts) |
| Month 6 | 4.5+ | Sustained quality + proactive review solicitation |
| Month 12 | 4.5+ (maintain) | Continuous review management, feature requests addressed |

**Rating defense tactics:**
- Never ship a release without 48h on the internal/closed test track first
- Monitor crash-free rate: must stay above 99.5% at all times
- Any release that drops the crash-free rate below 99% triggers immediate rollback
- Rating below 4.0 at any point triggers a "war room" response: freeze features, fix all critical bugs, personally respond to every negative review

### 8.6 Engagement Targets

| Metric | Month 3 Target | Month 6 Target | Month 12 Target |
|--------|---------------|----------------|-----------------|
| Avg. workouts/user/week | 2.5 | 3.0 | 3.0 |
| Avg. session duration | 35 min | 40 min | 40 min |
| AI plan generation rate | 70% of sessions | 75% | 80% |
| Template usage rate | 20% of sessions | 30% | 40% |
| PR detection engagement | 50% view their PRs | 60% | 70% |

### 8.7 Primary Growth Metric

> **Note:** The product-level North Star is "Workouts completed per week per active user" (target: 2.5 at steady state), defined in `product-strategy.md` Section 6.1. The metric below is the GTM operational growth metric, not the North Star.

**Weekly Active Users (WAU) who complete at least 2 workouts per week.**

This metric captures the intersection of acquisition (they installed), activation (they started using it), and retention (they keep coming back). It filters out casual downloaders and measures genuine product-market fit.

| Timeframe | WAU (2+ workouts/week) Target |
|-----------|-------------------------------|
| Month 1 | 1,500 |
| Month 3 | 5,000 |
| Month 6 | 15,000 |
| Month 12 | 40,000 |

---

## Dependencies and Open Questions

This strategy has several dependencies on decisions documented as TBD in CLAUDE.md and FEATURES.md:

1. **Operating budget** -- Deep Reps is a free app (product-strategy.md Section 4). CAC targets are bounded by the founder's operating budget, not LTV. The GTM budget assumes the founder commits to funding user acquisition through product-market fit validation (D30 > 15%, 50K+ DAU).

2. **Cloud sync vs. local-only** -- If data stays local-only, there is no email/account system, which eliminates email marketing as a channel and limits cross-device retention. Cloud sync enables richer retention marketing.

3. **Exercise library size at launch** -- Content production budget depends on how many exercises need visual media. Estimate 80-120 exercises across 7 muscle groups based on typical curated libraries.

4. **Brand identity and design system** -- ASO screenshots, ad creatives, and social media content all depend on the final visual identity. Screenshot strategy in Section 2.4 assumes a dark theme based on market conventions.

5. **Launch market** -- This plan assumes US-primary with global English reach. If launching in a specific regional market first, ASO keywords and influencer selection need adjustment.
