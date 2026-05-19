package com.digitalcow.dashboard.dto;

import java.util.List;
import java.util.Map;

public record DashboardSummary(
    Totals totals,
    List<ByRanchItem> byRanch,
    List<ByBreedItem> byBreed,
    Map<String, Long> bySex,
    Map<String, Long> byPurpose,
    RecentAdditions recentAdditions
) {
    public record Totals(long totalAnimals, long activeAnimals, long soldThisYear,
                         long deadThisYear, long ranches, long lots) {}
    public record ByRanchItem(Long ranchId, String ranchName, long count) {}
    public record ByBreedItem(Long breedId, String breedCode, long count) {}
    public record RecentAdditions(List<String> labels, List<Long> counts) {}
}
