package com.teraapi.stream;

/**
 * License Validation Service for tier-based access control
 */
public class LicenseValidationService {

    public enum TierLevel {
        FREE(1, 100, 10),
        STANDARD(2, 1000, 100),
        PREMIUM(3, 10000, 1000);

        private final int level;
        private final int maxRequests;
        private final int maxStreamSize;

        TierLevel(int level, int maxRequests, int maxStreamSize) {
            this.level = level;
            this.maxRequests = maxRequests;
            this.maxStreamSize = maxStreamSize;
        }

        public static TierLevel fromString(String tier) {
            try {
                return TierLevel.valueOf(tier.toUpperCase());
            } catch (IllegalArgumentException e) {
                return FREE; // Default tier
            }
        }

        public int getMaxRequests() {
            return maxRequests;
        }

        public int getMaxStreamSize() {
            return maxStreamSize;
        }
    }

    public boolean isValidLicense(String tier, String username, long streamSize) {
        TierLevel tierLevel = TierLevel.fromString(tier);
        return streamSize <= tierLevel.getMaxStreamSize();
    }

    public String getTierInfo(String tier) {
        TierLevel tierLevel = TierLevel.fromString(tier);
        return String.format("Tier: %s, Max Stream Size: %d bytes", tierLevel.name(), tierLevel.getMaxStreamSize());
    }
}
