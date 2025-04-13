// src/main/java/com/example/schedulemanager/util/CalendarHelper.java
package com.example.schedulemanager.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class CalendarHelper {
    
    public static List<List<LocalDate>> getCalendarDates(int year, int month) {
        List<List<LocalDate>> calendar = new ArrayList<>();
        
        // 選択した月の初日と最終日
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int lastDayOfMonth = yearMonth.lengthOfMonth();
        
        // 日本の週は日曜始まり (DayOfWeek.SUNDAY = 7)
        int dayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;
        
        List<LocalDate> week = new ArrayList<>();
        
        // 前月の日付を埋める
        for (int i = 0; i < dayOfWeek; i++) {
            week.add(firstDayOfMonth.minusDays(dayOfWeek - i));
        }
        
        // 今月の日付を埋める
        for (int day = 1; day <= lastDayOfMonth; day++) {
            LocalDate date = LocalDate.of(year, month, day);
            week.add(date);
            
            if (week.size() == 7) {
                calendar.add(week);
                week = new ArrayList<>();
            }
        }
        
        // 次月の日付を埋める
        if (!week.isEmpty()) {
            LocalDate lastDateInWeek = week.get(week.size() - 1);
            int daysToAdd = 7 - week.size();
            for (int i = 1; i <= daysToAdd; i++) {
                week.add(lastDateInWeek.plusDays(i));
            }
            calendar.add(week);
        }
        
        return calendar;
    }
}