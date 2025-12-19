package com.filmograph.auth_server.ranking.service;

import com.filmograph.auth_server.ranking.repo.NetflixWeeklyRankRepo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;

@Service
public class NetflixTop10Ingestor {

    private static final String XLSX =
            "https://www.netflix.com/tudum/top10/data/all-weeks-global.xlsx";

    private final NetflixWeeklyRankRepo repo;

    public NetflixTop10Ingestor(NetflixWeeklyRankRepo repo){ this.repo = repo; }

    @Transactional
    public int ingest() throws java.io.IOException {
        int saved = 0;
        try (InputStream in = new URL(XLSX).openStream();
             XSSFWorkbook wb = new XSSFWorkbook(in)) {

            XSSFSheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(0);

            DataFormatter formatter = new DataFormatter();
            Map<String,Integer> idx = new HashMap<>();
            for (int i=0;i<header.getLastCellNum();i++) {
                Cell c = header.getCell(i);
                if (c==null) continue;
                idx.put(formatter.formatCellValue(c).trim().toLowerCase(), i);
            }

            for (int r=1; r<=sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row==null) continue;

                LocalDate weekStart = LocalDate.parse(getStr(row, idx.get("week")));
                String category = getStr(row, idx.get("category"));
                Integer rank = getInt(row, idx.get("weekly_rank"));
                String title = getStr(row, idx.get("show_title"));
                String season = getOptStr(row, idx.get("season_title"));
                Long views = getOptLong(row, idx.get("weekly_views"));               // 신규 지표
                Long hours = getOptLong(row, idx.get("weekly_hours_viewed"));        // 구 지표
                Integer runtime = getOptInt(row, idx.get("runtime"));

                if (rank==null || title==null || title.isBlank()) continue;

                repo.upsert(weekStart, category, rank, title, season, views, hours, runtime);
                saved++;
            }
        }
        return saved;
    }

    // ---- helpers ----
    private static final DataFormatter formatter = new DataFormatter();
    private static String getStr(Row r, Integer i){ return Objects.requireNonNull(getOptStr(r,i)); }
    private static String getOptStr(Row r, Integer i){
        if (i==null || i<0) return null;
        Cell c = r.getCell(i); if (c==null) return null;
        String s = formatter.formatCellValue(c);
        return (s==null || s.isBlank())? null : s.trim();
    }
    private static Integer getInt(Row r, Integer i){ Long v=getOptLong(r,i); return v==null?null:v.intValue(); }
    private static Integer getOptInt(Row r, Integer i){ Long v=getOptLong(r,i); return v==null?null:v.intValue(); }
    private static Long getOptLong(Row r, Integer i){
        if (i==null || i<0) return null;
        Cell c = r.getCell(i); if (c==null) return null;
        if (c.getCellType()==CellType.NUMERIC) return (long)c.getNumericCellValue();
        if (c.getCellType()==CellType.STRING) {
            try { return Long.parseLong(c.getStringCellValue().replaceAll("[^0-9]", "")); }
            catch (Exception ignored) {}
        }
        return null;
    }
}
