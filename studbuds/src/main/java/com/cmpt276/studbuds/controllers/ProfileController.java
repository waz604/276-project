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

    @GetMapping("/profile")
    public String getProfile(Model model, HttpServletRequest request) {

        User user = findUser(request);

        model.addAttribute("username", user.getName());

        // stats logic
        List<XpLog> logs = xpLogRepository.findByUser(user);

        //add xp per days
        Map<LocalDate, Integer> dailyXp = new LinkedHashMap<>();
        for (XpLog log : logs) {
            dailyXp.merge(log.getDate(), log.getXpEarned(), Integer::sum);

        }

        int totalXp = dailyXp.values().stream().mapToInt(Integer::intValue).sum();

        //current
        int currentStreak = 0;
        LocalDate today = LocalDate.now(ZoneId.of("America/Vancouver"));
        LocalDate check = today;

        while (dailyXp.containsKey(check) && dailyXp.get(check) > 0) {
            currentStreak++;
            check = check.minusDays(1);
        }

        //longest
        int longestStreak = 0;
        int tempStreak = 0;

        List<LocalDate> sortedDates = new ArrayList<>(dailyXp.keySet());
        Collections.sort(sortedDates);

        for (int i = 0; i < sortedDates.size(); i++) {
            if (i == 0 || sortedDates.get(i).minusDays(1).equals(sortedDates.get(i - 1))) {
                tempStreak++;

            } 
            else {
                tempStreak = 1; 

            }
            longestStreak = Math.max(longestStreak, tempStreak);
        }

        Map<DayOfWeek, Integer> dayTotals = new EnumMap<>(DayOfWeek.class);
        for (Map.Entry<LocalDate, Integer> entry : dailyXp.entrySet()) {

            DayOfWeek day = entry.getKey().getDayOfWeek();

            int xpForDay = entry.getValue();


            if (dayTotals.containsKey(day)) {
                dayTotals.put(day, dayTotals.get(day) + xpForDay);
            } 
            else {
                dayTotals.put(day, xpForDay);
            }

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

        // heatmap: always show at least 7 weeks then it will be aligned more beautifully,
        LocalDate sevenWeeksAgo = today.minusWeeks(6).with(DayOfWeek.MONDAY);
        // Use account creation date as the earliest start if available, otherwise fall back to 7 weeks ago
        LocalDate accountWeekStart;
        
        if (user.getCreatedAt() != null) {
            accountWeekStart = user.getCreatedAt().with(DayOfWeek.MONDAY);
        } else {
            accountWeekStart = sevenWeeksAgo;
        }

        // Always show at least 7 weeks; extend further back only if account was created earlier
        LocalDate baseStart = sevenWeeksAgo;
        if (accountWeekStart.isBefore(sevenWeeksAgo)) {
            baseStart = accountWeekStart;
        }

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
        LocalDate end = today.with(DayOfWeek.SUNDAY);

        //grid (i used the help of copilot to help me figure out the way to make the grid 2d)
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

        // calculate level from total XP (mirrors XP.js formula)
        int level = calculateLevel(totalXp);
        int[] levelProgress = calculateLevelProgress(totalXp);
        int currentLevel = levelProgress[0];
        int nextLevel = levelProgress[1];
        int levelProgressPercent = levelProgress[2];

        model.addAttribute("totalXp", totalXp);
        model.addAttribute("level", level);
        model.addAttribute("currentLevel", currentLevel);
        model.addAttribute("nextLevel", nextLevel);
        model.addAttribute("levelProgressPercent", levelProgressPercent);
        model.addAttribute("currentStreak", currentStreak);
        model.addAttribute("longestStreak", longestStreak);
        model.addAttribute("mostActiveDay", mostActiveDay);
        model.addAttribute("heatmap", heatmap);
        model.addAttribute("heatmapDates", heatmapDates);
        String joinedMonth = "Unknown";
        if (user.getCreatedAt() != null) {
            String month = user.getCreatedAt().getMonth().toString();
            joinedMonth = month.charAt(0) + month.substring(1).toLowerCase() + " " + user.getCreatedAt().getYear();
        }
        model.addAttribute("joinedMonth", joinedMonth);

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
        if(userId == null) throw new NullUserException("userId not found");
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) throw new NullUserException("user not found");
        
        return "tutorial";
    }

    @GetMapping("/leaderboard")
    public String displayLeaderboard(HttpServletRequest request, Model model) {
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if(userId == null) throw new NullUserException("userId not found");
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) throw new NullUserException("user not found");

        List<User> userList = userRepository.findAll(); 
        
        Map<Integer, Integer> userXpMap = new HashMap<>();
        Map<Integer, Integer> userLevelMap = new HashMap<>();

        for (User u : userList) {
            List<XpLog> userLogs = xpLogRepository.findByUser(u);
            
            int total = userLogs.stream()
                            .mapToInt(XpLog::getXpEarned)
                            .sum();
            int level = calculateLevel(total);

            userXpMap.put(u.getUid(), total);
            userLevelMap.put(u.getUid(), level);
        }

        model.addAttribute("xp", userXpMap);
        model.addAttribute("level", userLevelMap);
        
        // Sort list of users based on XP and take the top 10 users
        userList.sort((u1, u2) -> Integer.compare(userXpMap.get(u2.getUid()), userXpMap.get(u1.getUid())));
        List<User> topTen = userList.size() > 10 ? userList.subList(0, 10) : userList;

        model.addAttribute("us", topTen);
        return "leaderboard";
    }

    // Saves XP earned during a study session to the database
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

   

    // Calculates level from total XP
    
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

    private int[] calculateLevelProgress(int totalXp) {
        int level     = 1;
        int remaining = totalXp;
        while (level < 20) {
            int needed = xpForLevel(level);
            if (remaining < needed) break;
            remaining -= needed;
            level++;
        }
        if (level >= 20) {
            int cap = xpForLevel(20);
            return new int[] { cap, cap, 100 };
        }
        int xpToNextLevel = xpForLevel(level);
        int progressPercent = (int) Math.round((remaining * 100.0) / xpToNextLevel);
        return new int[] { remaining, xpToNextLevel, progressPercent };
    }


    private User findUser(HttpServletRequest request) {
        
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if(userId == null) throw new NullUserException("userId not found");
        
        User user = userRepository.findById(userId).orElse(null);
        if(user == null) throw new NullUserException("user not found");

        return user;
    }

    // === Exception Handling Methods === //
    @ExceptionHandler(NullUserException.class)
    public String nullUserHandler() {
        return "redirect:/login";
    }


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
}
