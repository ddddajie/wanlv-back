package com.example.wanlvback.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 预约运营看板聚合数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDashboardVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private List<ScenicAreaOptionVO> scenicAreas;
    private SummaryVO summary;
    private List<CapacitySpotVO> capacityRanks;
    private List<SourceDistributionVO> sourceDistribution;
    private List<HeatSpotVO> heatSpots;
    private List<TrendVO> trend;
    private List<StatusDistributionVO> statusDistribution;
    private List<PeakTimeVO> peakTimes;
    private List<HotSpotRankVO> hotSpotRanks;
    private List<WarningVO> warnings;
    private List<LiveActivityVO> liveActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScenicAreaOptionVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Integer orderCount;
        private String orderCompareText;
        private Integer visitorCount;
        private String visitorHint;
        private Double capacityUsageRate;
        private String capacityHint;
        private Double cancelRate;
        private String cancelHint;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapacitySpotVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long spotId;
        private String spotName;
        private Integer totalCapacity;
        private Integer reservedCount;
        private Integer remainingCount;
        private Double usageRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceDistributionVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String sourceType;
        private String sourceName;
        private Double rate;
        private Integer orderCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeatSpotVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long spotId;
        private String spotName;
        private Double x;
        private Double y;
        private String level;
        private Integer totalCapacity;
        private Integer reservedCount;
        private Integer remainingCount;
        private Double usageRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private LocalDate date;
        private String label;
        private Integer orderCount;
        private Integer visitorCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusDistributionVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String status;
        private String statusName;
        private Double rate;
        private String color;
        private Integer orderCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeakTimeVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private LocalTime startTime;
        private LocalTime endTime;
        private String timeRange;
        private Integer visitorCount;
        private String note;
        private String level;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotSpotRankVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private Long spotId;
        private String spotName;
        private Integer orderCount;
        private Double usageRate;
        private Integer remainingCount;
        private String level;
        private String note;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WarningVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String title;
        private String level;
        private String tag;
        private String description;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LiveActivityVO implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String title;
        private String timeText;
        private String description;
    }
}
