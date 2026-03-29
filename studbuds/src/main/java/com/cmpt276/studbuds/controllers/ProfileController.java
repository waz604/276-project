package com.cmpt276.studbuds.controllers;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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
        LocalDate today = LocalDate.now();
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

        // Don't go further back than 7 weeks
        LocalDate baseStart;
        if (accountWeekStart.isAfter(sevenWeeksAgo)) {
            baseStart = accountWeekStart;
        } else {
            baseStart = sevenWeeksAgo;
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

        model.addAttribute("totalXp", totalXp);
        model.addAttribute("currentStreak", currentStreak);
        model.addAttribute("longestStreak", longestStreak);
        model.addAttribute("mostActiveDay", mostActiveDay);
        model.addAttribute("heatmap", heatmap);
        model.addAttribute("heatmapDates", heatmapDates);

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

    // === Helper Methods === //
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
}
