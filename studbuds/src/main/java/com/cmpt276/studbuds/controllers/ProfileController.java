package com.cmpt276.studbuds.controllers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import com.cmpt276.studbuds.exceptions.NullUserException;
import com.cmpt276.studbuds.models.User;
import com.cmpt276.studbuds.models.UserRepository;
import com.cmpt276.studbuds.models.XpLog;
import com.cmpt276.studbuds.models.XpLogRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private XpLogRepository xpLogRepository;

    // ── Page routes ───────────────────────────────────────────────────────────

    @GetMapping("/profile")
    public String getProfile(Model model, HttpServletRequest request) {

        User user = findUser(request);
        model.addAttribute("username", user.getName());

        List<XpLog> logs = xpLogRepository.findByUser(user);

        // Aggregate XP by day
        Map<LocalDate, Integer> dailyXp = new LinkedHashMap<>();
        for (XpLog log : logs) {
            dailyXp.merge(log.getDate(), log.getXpEarned(), Integer::sum);
        }

        int totalXp = dailyXp.values().stream().mapToInt(Integer::intValue).sum();

        // Current streak — count backwards from today through consecutive active days
        int currentStreak = 0;
        LocalDate today = LocalDate.now(ZoneId.of("America/Vancouver"));
        LocalDate check = today;
        while (dailyXp.containsKey(check) && dailyXp.get(check) > 0) {
            currentStreak++;
            check = check.minusDays(1);
        }

        // Longest streak
        int longestStreak = 0;
        int tempStreak = 0;
        List<LocalDate> sortedDates = new ArrayList<>(dailyXp.keySet());
        Collections.sort(sortedDates);
        for (int i = 0; i < sortedDates.size(); i++) {
            if (i == 0 || sortedDates.get(i).minusDays(1).equals(sortedDates.get(i - 1))) {
                tempStreak++;
            } else {
                tempStreak = 1;
            }
            longestStreak = Math.max(longestStreak, tempStreak);
        }

        // Most active day of week
        Map<DayOfWeek, Integer> dayTotals = new EnumMap<>(DayOfWeek.class);
        for (Map.Entry<LocalDate, Integer> entry : dailyXp.entrySet()) {
            DayOfWeek day = entry.getKey().getDayOfWeek();
            dayTotals.merge(day, entry.getValue(), Integer::sum);
        }

        String mostActiveDay = "N/A";
        if (!dayTotals.isEmpty()) {
            DayOfWeek bestDay = null;
            for (Map.Entry<DayOfWeek, Integer> entry : dayTotals.entrySet()) {
                if (bestDay == null || entry.getValue() > dayTotals.get(bestDay)) {
                    bestDay = entry.getKey();
                }
            }
            String fullName = bestDay.name();
            mostActiveDay = fullName.charAt(0) + fullName.substring(1).toLowerCase();
        }

        // Heatmap — always show at least 7 weeks for visual alignment
        LocalDate sevenWeeksAgo   = today.minusWeeks(6).with(DayOfWeek.MONDAY);
        LocalDate accountWeekStart = (user.getCreatedAt() != null)
                ? user.getCreatedAt().with(DayOfWeek.MONDAY)
                : sevenWeeksAgo;
        LocalDate baseStart = accountWeekStart.isAfter(sevenWeeksAgo) ? accountWeekStart : sevenWeeksAgo;

        LocalDate startDate;
        if (sortedDates.isEmpty()) {
            startDate = baseStart;
        } else {
            LocalDate firstLog = sortedDates.get(0).with(DayOfWeek.MONDAY);
            startDate = firstLog.isBefore(baseStart) ? firstLog : baseStart;
        }

        List<List<int[]>> heatmap = new ArrayList<>();
        List<String> heatmapDates = new ArrayList<>();
        LocalDate cursor = startDate;
        LocalDate end    = today.with(DayOfWeek.SUNDAY);

        while (!cursor.isAfter(end)) {
            List<int[]> week = new ArrayList<>();
            for (int d = 0; d < 7; d++) {
                int xp = dailyXp.getOrDefault(cursor, 0);
                week.add(new int[]{xp});
                heatmapDates.add(cursor.toString());
                cursor = cursor.plusDays(1);
            }
            heatmap.add(week);
        }

        int level = calculateLevel(totalXp);
       

        model.addAttribute("totalXp",       totalXp);
        model.addAttribute("level",         level);
        model.addAttribute("currentStreak", currentStreak);
        model.addAttribute("longestStreak", longestStreak);
        model.addAttribute("mostActiveDay", mostActiveDay);
        model.addAttribute("heatmap",       heatmap);
        model.addAttribute("heatmapDates",  heatmapDates);

        return "profile";
    }

    @PostMapping("/profile/edit")
    public String editUsername(@RequestParam String username, HttpServletRequest request) {
        User user = findUser(request);
        user.setName(username.trim());
        userRepository.save(user);
        return "redirect:/profile";
    }

    @GetMapping("/tutorial")
    public String displayTutorial(HttpServletRequest request) {
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) throw new NullUserException("userId not found");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new NullUserException("user not found");
        return "tutorial";
    }

    // ── XP endpoints ──────────────────────────────────────────────────────────

    // POST /xp/award
    // Saves a raw XP amount (used by XP.js _saveXpToServer during study mode).
    // Rejects zero or negative amounts with 400 Bad Request.
    @PostMapping("/xp/award")
    @ResponseBody
    public void awardXp(@RequestParam int amount, HttpServletRequest request) {
        if (amount <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "XP amount must be positive");
        }
        User user = findUser(request);
        xpLogRepository.save(new XpLog(user, LocalDate.now(ZoneId.of("America/Vancouver")), amount));
    }

    // POST /xp/study-session
    // Awards XP for completing a regular (non-timed) study session.
    // All calculation is done server-side to prevent manipulation.
    //
    // Formula (SRD):
    //   Anti-cheat : timeSeconds < totalCards        → 0 XP (impossibly fast)
    //   Anti-farm  : same deckId within 5 min        → 0 XP
    //   Base XP    = totalCards * 10
    //   Speed bonus:
    //     t ≤ totalCards * 5   → +50%
    //     t ≤ totalCards * 12  → +25%
    //     otherwise            → no bonus
    //
    // Returns JSON: { "xp": N }
    @PostMapping("/xp/study-session")
    @ResponseBody
    public Map<String, Integer> awardStudySessionXp(
            @RequestParam int totalCards,
            @RequestParam long timeSeconds,
            @RequestParam int deckId,
            HttpServletRequest request) {

        User user = findUser(request);
        Map<String, Integer> result = new HashMap<>();

        // Anti-cheat: impossibly fast completion
        if (timeSeconds < (long) totalCards) {
            result.put("xp", 0);
            return result;
        }

        // Anti-farm: same deck studied within the last 5 minutes
        String farmTimeKey = "lastDeckTime_" + deckId;
        Long lastStudied   = (Long) request.getSession().getAttribute(farmTimeKey);
        if (lastStudied != null && (System.currentTimeMillis() - lastStudied) < 5 * 60 * 1000L) {
            result.put("xp", 0);
            return result;
        }

        // Base XP
        double xp = totalCards * 10.0;

        // Speed bonus
        if (timeSeconds <= (long) totalCards * 5) {
            xp *= 1.5;
        } else if (timeSeconds <= (long) totalCards * 12) {
            xp *= 1.25;
        }

        int finalXp = (int) Math.round(xp);

        if (finalXp > 0) {
            xpLogRepository.save(new XpLog(user, LocalDate.now(), finalXp));
            request.getSession().setAttribute(farmTimeKey, System.currentTimeMillis());
        }

        result.put("xp", finalXp);
        return result;
    }

    // POST /xp/time-challenge
    // Awards XP for a completed time challenge.
    // All calculation is done server-side — the client sends raw game results only.
    //
    // Formula (SRD):
    //   perfect score AND timeRemaining > 0 → base = totalCards * 10
    //     timeRemaining >= totalCards * 5   → base * 1.5
    //     timeRemaining >= totalCards * 2   → base * 1.25
    //     otherwise                         → base
    //   any other result                    → 0 XP, nothing saved
    //
    // Returns JSON: { "xp": N }
    @PostMapping("/xp/time-challenge")
    @ResponseBody
    public Map<String, Integer> awardTimeChallengeXp(
            @RequestParam int totalCards,
            @RequestParam int score,
            @RequestParam int timeRemaining,
            HttpServletRequest request) {

        User user = findUser(request);

        double xp = 0;

        if (score > 0 && timeRemaining > 0) {
            // Base XP scaled by accuracy: (correct / total) * totalCards * 10
            xp = ((double) score / totalCards) * totalCards * 10.0;

            // Speed bonus based on time remaining
            if (timeRemaining >= totalCards * 5) {
                xp *= 1.5;
            } else if (timeRemaining >= totalCards * 2) {
                xp *= 1.25;
            }
        }

        int finalXp = (int) Math.round(xp);
        if (finalXp > 0) {
            xpLogRepository.save(new XpLog(user, LocalDate.now(), finalXp));
        }

        Map<String, Integer> result = new HashMap<>();
        result.put("xp", finalXp);
        return result;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    // Calculates level from total XP — mirrors lvl_checker() in XP.js exactly.
    // If you change one, change both.
    private int calculateLevel(int totalXp) {
        int level     = 1;
        int remaining = totalXp;
        while (level < 20) {
            int needed = xpForLevel(level);
            if (remaining < needed) break;
            remaining -= needed;
            level++;
        }
        return level;
    }

    // XP required to advance FROM the given level.
    // Must stay in sync with lvl_checker() in XP.js.
    private int xpForLevel(int level) {
        switch (level) {
            case  1: return 100;
            case  2: return 150;
            case  3: return 220;
            case  4: return 300;
            case  5: return 400;
            case  6: return 520;
            case  7: return 670;
            case  8: return 850;
            case  9: return 1050;
            case 10: return 1200;
            case 11: return 1450;
            case 12: return 1700;
            case 13: return 2000;
            case 14: return 2200;
            case 15: return 2500;
            case 16: return 2850;
            case 17: return 3200;
            case 18: return 3550;
            case 19: return 3800;
            case 20: return 4000;
            default: return 4000;
        }
    }

    private User findUser(HttpServletRequest request) {
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) throw new NullUserException("userId not found");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) throw new NullUserException("user not found");
        return user;
    }

    // ── Exception handling ────────────────────────────────────────────────────

    // Returns 401 JSON (not a redirect) so XP.js fetch() calls degrade
    // gracefully — a 302 redirect would cause the XP bar to never seed on load.
    @ExceptionHandler(NullUserException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> nullUserHandler() {
        Map<String, String> err = new HashMap<>();
        err.put("error", "not logged in");
        return err;
    }
}