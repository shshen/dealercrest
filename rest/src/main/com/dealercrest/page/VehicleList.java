package com.dealercrest.page;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * VehicleSearchResult — result of a two-query DB search.
 *
 * Query 1: COUNT(*) → total matching rows (for pagination math)
 * Query 2: SELECT * LIMIT/OFFSET → one page of vehicles
 */
public class VehicleList {

    private final List<Map<String, Object>> vehicles;
    private final int total;
    private final int page;
    private final int limit;
    private final int totalPages;
    private final boolean hasPrev;
    private final boolean hasNext;

    public VehicleList(List<Map<String, Object>> vehicles,
            int total, int page, int limit) {
        this.vehicles = vehicles;
        this.total = total;
        this.page = page;
        this.limit = limit;
        this.totalPages = limit > 0 ? (int) Math.ceil((double) total / limit) : 0;
        this.hasPrev = page > 1;
        this.hasNext = page < this.totalPages;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public List<Map<String, Object>> getVehicles() {
        return vehicles;
    }

    public int getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getLimit() {
        return limit;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasPrev() {
        return hasPrev;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    /** Convert to Map for template context: ${data.total}, ${data.hasNext} etc. */
    public Map<String, Object> toMap() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("vehicles", vehicles);
        m.put("total", total);
        m.put("page", page);
        m.put("limit", limit);
        m.put("totalPages", totalPages);
        m.put("hasPrev", hasPrev);
        m.put("hasNext", hasNext);
        return m;
    }

    @Override
    public String toString() {
        return String.format("VehicleSearchResult{total=%d, page=%d/%d, count=%d}",
                total, page, totalPages, vehicles.size());
    }
}
