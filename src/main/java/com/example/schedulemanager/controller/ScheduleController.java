// src/main/java/com/example/schedulemanager/controller/ScheduleController.java
package com.example.schedulemanager.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.schedulemanager.model.Schedule;
import com.example.schedulemanager.service.ScheduleService;
import com.example.schedulemanager.util.CalendarHelper;

@Controller
public class ScheduleController {
    
    @Autowired
    private ScheduleService scheduleService;
    
    @GetMapping("/")
    public String index(@RequestParam(required = false) Integer year,
                        @RequestParam(required = false) Integer month,
                        Model model) {
        
        // 年と月が指定されていない場合は現在の年月を使用
        LocalDate now = LocalDate.now();
        year = year != null ? year : now.getYear();
        month = month != null ? month : now.getMonthValue();
        
        // カレンダーデータの取得
        List<List<LocalDate>> calendar = CalendarHelper.getCalendarDates(year, month);
        
        // 月の範囲を取得
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        
        // この月のすべての予定を取得
        List<Schedule> schedules = scheduleService.getSchedulesForMonth(
                firstDay.minusDays(7),  // 前月の表示された日も含める
                lastDay.plusDays(7));   // 翌月の表示された日も含める
        
        // 日付別に予定をマッピング
        Map<LocalDate, List<Schedule>> scheduleMap = new HashMap<>();
        for (LocalDate date : firstDay.datesUntil(lastDay.plusDays(1)).toList()) {
            scheduleMap.put(date, scheduleService.getSchedulesByDate(date));
        }
        
        model.addAttribute("calendar", calendar);
        model.addAttribute("currentYear", year);
        model.addAttribute("currentMonth", month);
        model.addAttribute("prevMonth", month > 1 ? month - 1 : 12);
        model.addAttribute("prevYear", month > 1 ? year : year - 1);
        model.addAttribute("nextMonth", month < 12 ? month + 1 : 1);
        model.addAttribute("nextYear", month < 12 ? year : year + 1);
        model.addAttribute("scheduleMap", scheduleMap);
        
        return "index";
    }
    
    @GetMapping("/schedule/create")
    public String showCreateForm(@RequestParam String date, Model model) {
        Schedule schedule = new Schedule();
        schedule.setDate(LocalDate.parse(date));
        
        // デフォルトの開始時間と終了時間を設定（オプション）
        LocalTime now = LocalTime.now();
        LocalTime roundedStartTime = LocalTime.of(now.getHour(), 0);
        LocalTime roundedEndTime = roundedStartTime.plusHours(1);
        
        schedule.setStartTime(roundedStartTime);
        schedule.setEndTime(roundedEndTime);
        
        model.addAttribute("schedule", schedule);
        model.addAttribute("dateString", date);
        return "create";
    }
    
    @PostMapping("/schedule/save")
    public String saveSchedule(@ModelAttribute Schedule schedule, Model model) {
        // 開始時間が終了時間より前であることを確認（サーバー側の追加検証）
        if (schedule.getStartTime().isAfter(schedule.getEndTime()) || 
            schedule.getStartTime().equals(schedule.getEndTime())) {
            model.addAttribute("error", "終了時間は開始時間より後に設定してください。");
            model.addAttribute("schedule", schedule);
            model.addAttribute("dateString", schedule.getDate().toString());
            return "create";
        }
        
        scheduleService.saveSchedule(schedule);
        model.addAttribute("message", "予定を登録しました");
        return "success";
    }
    
    @GetMapping("/schedule/{id}")
    public String showScheduleDetails(@PathVariable Long id, Model model) {
        Optional<Schedule> optionalSchedule = scheduleService.getScheduleById(id);
        if (optionalSchedule.isPresent()) {
            model.addAttribute("schedule", optionalSchedule.get());
            return "detail";
        } else {
            return "redirect:/";
        }
    }
    
    @PostMapping("/schedule/delete/{id}")
    public String deleteSchedule(@PathVariable Long id) {
        scheduleService.deleteSchedule(id);
        return "redirect:/";
    }
}