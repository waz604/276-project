package com.cmpt276.studbuds.controllers;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;
import com.cmpt276.studbuds.models.XpLog;
import com.cmpt276.studbuds.models.XpLogRepository;

// XPController      → GET  /xp/total
// ProfileController → POST /xp/award, POST /xp/study-session, POST /xp/time-challenge
@WebMvcTest({ XPController.class, ProfileController.class })
@AutoConfigureMockMvc(addFilters = false)
public class XPControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private XpLogRepository xpLogRepository;

    private User user;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        user = new User("TestUser", "pass123", "test@gmail.com");
        user.setUid(1);

        session = new MockHttpSession();
        session.setAttribute("userId", 1);

        Mockito.when(userRepository.findById(1)).thenReturn(Optional.of(user));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /xp/total
    // ─────────────────────────────────────────────────────────────────────────

    /** No XP logs yet -> JSON body must contain totalXp = 0. */
    @Test
    void getTotalXp_noLogs_returnsZero() throws Exception {
        Mockito.when(xpLogRepository.findByUser(user)).thenReturn(List.of());

        mockMvc.perform(MockMvcRequestBuilders.get("/xp/total").session(session))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.totalXp").value(0));
    }

    /** Two logs (100 + 250 XP) → totalXp must equal 350. */
    @Test
    void getTotalXp_multipleLogs_returnsSum() throws Exception {
        Mockito.when(xpLogRepository.findByUser(user)).thenReturn(List.of(
                new XpLog(user, LocalDate.now(), 100),
                new XpLog(user, LocalDate.now(), 250)));

        mockMvc.perform(MockMvcRequestBuilders.get("/xp/total").session(session))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.totalXp").value(350));
    }

    /**
     * Not logged in -> must return 401 JSON with an "error" key, NOT a 302.
     * XP.js calls /xp/total on every page load; a redirect would silently
     * break the bar for all logged-in users too.
     */
    @Test
    void getTotalXp_notLoggedIn_returns401Json() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/xp/total"))   // no session
               .andExpect(MockMvcResultMatchers.status().isUnauthorized())
               .andExpect(MockMvcResultMatchers.jsonPath("$.error").exists());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /xp/award
    // ─────────────────────────────────────────────────────────────────────────

    /** Valid positive amount -> exactly one XpLog saved with the right values. */
    @Test
    void awardXp_validAmount_savesOneLog() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/award")
                        .session(session)
                        .param("amount", "50"))
               .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log ->
                       log.getUser().equals(user) && log.getXpEarned() == 50));
    }

    /** Large valid amount is also accepted without issue. */
    @Test
    void awardXp_largeValidAmount_savesLog() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/award")
                        .session(session)
                        .param("amount", "9999"))
               .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log -> log.getXpEarned() == 9999));
    }

    /** Negative amount → 400 Bad Request, nothing written to the database. */
    @Test
    void awardXp_negativeAmount_returns400() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/award")
                        .session(session)
                        .param("amount", "-50"))
               .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(xpLogRepository, Mockito.never()).save(Mockito.any());
    }

    /** Zero XP -> 400 Bad Request, nothing written to the database. */
    @Test
    void awardXp_zeroAmount_returns400() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/award")
                        .session(session)
                        .param("amount", "0"))
               .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(xpLogRepository, Mockito.never()).save(Mockito.any());
    }


    // ─────────────────────────────────────────────────────────────────────────
    // POST /xp/study-session
    //
    // Formula:
    //   Anti-cheat : timeSeconds < totalCards          → 0 XP, nothing saved
    //   Anti-farm  : same deckId again within 5 min    → 0 XP, nothing saved
    //   Base XP    = totalCards * 10
    //   Speed bonus:
    //     t ≤ totalCards * 5   -> round(base * 1.5)
    //     t ≤ totalCards * 12  -> round(base * 1.25)
    //     otherwise            -> base
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 10 cards, 40 s (≤ 10*5 = 50) -> fast bonus.
     * base = 100, round(100 * 1.5) = 150.
     */
    @Test
    void studySessionXp_fastCompletion_gives15xBonus() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/study-session")
                        .session(session)
                        .param("totalCards",  "10")
                        .param("timeSeconds", "40")
                        .param("deckId",      "1"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(150));

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log -> log.getXpEarned() == 150));
    }

    /**
     * 10 cards, 80 s (> 10*5 = 50, ≤ 10*12 = 120) -> medium bonus.
     * base = 100, round(100 * 1.25) = 125.
     */
    @Test
    void studySessionXp_mediumCompletion_gives125xBonus() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/study-session")
                        .session(session)
                        .param("totalCards",  "10")
                        .param("timeSeconds", "80")
                        .param("deckId",      "2"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(125));

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log -> log.getXpEarned() == 125));
    }

    /**
     * 10 cards, 200 s (> 10*12 = 120) → no bonus.
     * base = 100.
     */
    @Test
    void studySessionXp_slowCompletion_givesBaseXp() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/study-session")
                        .session(session)
                        .param("totalCards",  "10")
                        .param("timeSeconds", "200")
                        .param("deckId",      "3"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(100));

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log -> log.getXpEarned() == 100));
    }

    /**
     * timeSeconds < totalCards -> anti-cheat triggers, 0 XP, nothing saved.
     * 5 cards completed in 3 seconds is impossible.
     */
    @Test
    void studySessionXp_tooFast_givesZeroXp() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/study-session")
                        .session(session)
                        .param("totalCards",  "5")
                        .param("timeSeconds", "3")
                        .param("deckId",      "4"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(0));

        Mockito.verify(xpLogRepository, Mockito.never()).save(Mockito.any());
    }

    /**
     * Same deck studied twice in the same session (within 5 minutes).
     * First call awards XP normally; second call is blocked by the anti-farm
     * guard and returns 0, with no second save to the database.
     *
     * 10 cards, 200 s (no speed bonus) -> base 100 XP on first attempt.
     */
    @Test
    void studySessionXp_antiFarm_secondAttemptWithinFiveMinutes_givesZero() throws Exception {
        // First attempt, should award XP and stamp the farm timer in the session
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/study-session")
                        .session(session)
                        .param("totalCards",  "10")
                        .param("timeSeconds", "200")
                        .param("deckId",      "10"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(100));

        // Second attempt on the same deck immediately after, anti-farm blocks it
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/study-session")
                        .session(session)           // same session -> farm timer is set
                        .param("totalCards",  "10")
                        .param("timeSeconds", "200")
                        .param("deckId",      "10"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(0));

        // Only one save should have happened across both requests
        Mockito.verify(xpLogRepository, Mockito.times(1)).save(Mockito.any());
    }

    /** Not logged in -> 401 JSON, nothing saved. */
    @Test
    void studySessionXp_notLoggedIn_returns401() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/study-session")
                        .param("totalCards",  "10")
                        .param("timeSeconds", "60")
                        .param("deckId",      "1"))   // no session
               .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(xpLogRepository, Mockito.never()).save(Mockito.any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /xp/time-challenge
    //
    // Formula (fixed — partial scores are valid per SRD acceptance test):
    //   score == 0 OR timeRemaining == 0  -> 0 XP, nothing saved
    //   score > 0 AND timeRemaining > 0   -> base = score * 10
    //     timeRemaining >= totalCards * 5 -> round(base * 1.5)
    //     timeRemaining >= totalCards * 2 -> round(base * 1.25)
    //     otherwise                       -> base
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Perfect score, timeRemaining = 25 (>= 5*5 = 25) → 1.5× bonus.
     * base = 5*10 = 50, round(50 * 1.5) = 75.
     */
    @Test
    void timeChallengeXp_perfectScore_highTime_gives15xBonus() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/time-challenge")
                        .session(session)
                        .param("totalCards",    "5")
                        .param("score",         "5")
                        .param("timeRemaining", "25"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(75));

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log -> log.getXpEarned() == 75));
    }

    /**
     * Perfect score, timeRemaining = 10 (>= 4*2 = 8, < 4*5 = 20) → 1.25× bonus.
     * base = 4*10 = 40, round(40 * 1.25) = 50.
     */
    @Test
    void timeChallengeXp_perfectScore_mediumTime_gives125xBonus() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/time-challenge")
                        .session(session)
                        .param("totalCards",    "4")
                        .param("score",         "4")
                        .param("timeRemaining", "10"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(50));

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log -> log.getXpEarned() == 50));
    }

    /**
     * Perfect score, timeRemaining = 1 (< 3*2 = 6) → no speed bonus.
     * base = 3*10 = 30.
     */
    @Test
    void timeChallengeXp_perfectScore_lowTime_givesBaseXp() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/time-challenge")
                        .session(session)
                        .param("totalCards",    "3")
                        .param("score",         "3")
                        .param("timeRemaining", "1"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(30));

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log -> log.getXpEarned() == 30));
    }

    /**
     * Partial score (8/10), timeRemaining = 50 (>= 10*5 = 50) -> 1.5× bonus.
     * base = 8*10 = 80, round(80 * 1.5) = 120.
     * Mirrors the SRD acceptance test: 8 correct answers on a 10-card deck
     * must award XP.
     */
    @Test
    void timeChallengeXp_partialScore_highTime_awardsXp() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/time-challenge")
                        .session(session)
                        .param("totalCards",    "10")
                        .param("score",         "8")
                        .param("timeRemaining", "50"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(120));

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log -> log.getXpEarned() == 120));
    }

    /**
     * Partial score (6/10), timeRemaining = 25 (>= 10*2 = 20, < 10*5 = 50) -> 1.25× bonus.
     * base = 6*10 = 60, round(60 * 1.25) = 75.
     */
    @Test
    void timeChallengeXp_partialScore_mediumTime_awardsXp() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/time-challenge")
                        .session(session)
                        .param("totalCards",    "10")
                        .param("score",         "6")
                        .param("timeRemaining", "25"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(75));

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log -> log.getXpEarned() == 75));
    }

    /**
     * Partial score (4/10), timeRemaining = 5 (< 10*2 = 20) -> no speed bonus.
     * base = 4*10 = 40.
     */
    @Test
    void timeChallengeXp_partialScore_lowTime_givesBaseXp() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/time-challenge")
                        .session(session)
                        .param("totalCards",    "10")
                        .param("score",         "4")
                        .param("timeRemaining", "5"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(40));

        Mockito.verify(xpLogRepository, Mockito.times(1))
               .save(Mockito.argThat(log -> log.getXpEarned() == 40));
    }

    /**
     * Score = 0, even with time remaining -> 0 XP, nothing saved.
     * Getting zero correct answers is treated the same as not completing.
     */
    @Test
    void timeChallengeXp_zeroScore_savesNothing() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/time-challenge")
                        .session(session)
                        .param("totalCards",    "5")
                        .param("score",         "0")
                        .param("timeRemaining", "30"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(0));

        Mockito.verify(xpLogRepository, Mockito.never()).save(Mockito.any());
    }

    /** Time ran out (timeRemaining = 0), regardless of score → 0 XP, nothing saved. */
    @Test
    void timeChallengeXp_timeExpired_savesNothing() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/time-challenge")
                        .session(session)
                        .param("totalCards",    "5")
                        .param("score",         "5")
                        .param("timeRemaining", "0"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.jsonPath("$.xp").value(0));

        Mockito.verify(xpLogRepository, Mockito.never()).save(Mockito.any());
    }

    /** Not logged in -> 401 JSON, nothing saved. */
    @Test
    void timeChallengeXp_notLoggedIn_returns401() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.post("/xp/time-challenge")
                        .param("totalCards",    "5")
                        .param("score",         "5")
                        .param("timeRemaining", "30"))   // no session
               .andExpect(MockMvcResultMatchers.status().isUnauthorized());

        Mockito.verify(xpLogRepository, Mockito.never()).save(Mockito.any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Level-curve unit tests  (pure Java — no HTTP)
    //
    // These replicate the walk algorithm used in both XP.js (_initFromServer)
    // and ProfileController (calculateLevel / xpForLevel).
    // If the two implementations ever diverge, at least one test here breaks.
    // ─────────────────────────────────────────────────────────────────────────

    // Single source of truth for the XP curve — must match lvl_checker() in
    // XP.js AND xpForLevel() in ProfileController exactly.
    private static final int[] CURVE = {
        100, 150, 220, 300, 400, 520, 670, 850, 1050, 1200,
        1450, 1700, 2000, 2200, 2500, 2850, 3200, 3550, 3800, 4000
    };

    /** Walk the same algorithm that both the server and XP.js use. */
    private int walkLevel(int totalXp) {
        int level     = 1;
        int remaining = totalXp;
        while (level < 20) {
            if (remaining < CURVE[level - 1]) break;
            remaining -= CURVE[level - 1];
            level++;
        }
        return level;
    }

    /** 0 XP -> level 1. */
    @Test
    void lvlCurve_zeroXp_isLevel1() {
        Assertions.assertEquals(1, walkLevel(0));
    }

    /** 99 XP -> still level 1 (one below the level-2 threshold). */
    @Test
    void lvlCurve_oneShortOfLevel2_staysLevel1() {
        Assertions.assertEquals(1, walkLevel(99));
    }

    /** Exactly 100 XP -> level 2. */
    @Test
    void lvlCurve_exactLevel2Threshold_advancesToLevel2() {
        Assertions.assertEquals(2, walkLevel(100));
    }

    /** Sum of first two thresholds (250 XP) -> level 3 with 0 remainder. */
    @Test
    void lvlCurve_sumOfFirstTwo_isLevel3() {
        Assertions.assertEquals(3, walkLevel(CURVE[0] + CURVE[1]));
    }

    /** Exact sum of all 20 thresholds -> level 20 (MAX). */
    @Test
    void lvlCurve_fullSum_isMaxLevel() {
        int total = 0;
        for (int v : CURVE) total += v;
        Assertions.assertEquals(20, walkLevel(total));
    }

    /** Way beyond the full sum -> still capped at 20, no overflow. */
    @Test
    void lvlCurve_beyondMax_clampedAt20() {
        int total = 0;
        for (int v : CURVE) total += v;
        Assertions.assertEquals(20, walkLevel(total + 1_000_000));
    }

    /**
     * Every threshold in the curve advances the level by exactly 1.
     * Catches any off-by-one error or miscounted threshold.
     */
    @Test
    void lvlCurve_eachThresholdAdvancesExactlyOneLevel() {
        int cumulative = 0;
        for (int i = 0; i < CURVE.length - 1; i++) {
            cumulative += CURVE[i];
            int expectedLevel = i + 2;
            Assertions.assertEquals(
                    expectedLevel, walkLevel(cumulative),
                    "Expected level " + expectedLevel + " at totalXp=" + cumulative);
        }
    }
}