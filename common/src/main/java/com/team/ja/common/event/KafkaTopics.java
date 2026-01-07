package com.team.ja.common.event;

/**
 * Kafka topic constants.
 * Centralized topic names to ensure consistency across services.
 */
public final class KafkaTopics {

    private KafkaTopics() {
        // Utility class
    }

    /**
     * Topic for user registration events.
     * Producer: auth-service
     * Consumer: user-service
     */
    public static final String USER_REGISTERED = "user-registered";

    /**
     * Topic for user profile update events.
     * Producer: user-service
     * Consumer: other services that cache user data
     */
    public static final String USER_UPDATED = "user-updated";

    /**
     * Topic for significant user profile updates (e.g., skills, country).
     * Producer: user-service
     * Consumer: notification-service (for job matching)
     */
    public static final String USER_PROFILE_UPDATED = "user-profile-updated";

    /**
     * Topic for subscription change events.
     * Producer: subscription-service
     * Consumer: user-service (to update premium status)
     */
    public static final String SUBSCRIPTION_CHANGED = "subscription-changed";

    /**
     * Topic for application submitted events.
     * Producer: application-service
     * Consumer: notification-service
     */
    public static final String APPLICATION_SUBMITTED = "application-submitted";

    /**
     * Topic for new skill added events.
     * Producer: user-service
     * Consumer: user-service shards (to sync skill catalog)
     */
    public static final String SKILL_CREATED = "skill-created";

    /**
     * Topic for user migration events.
     * Producer: user-service
     * Consumer: user-service shards (to handle user data migration)
     */
    public static final String USER_MIGRATION = "user-migration";
}

    /**
     * Topic for user search profile update events.
     * Producer: user-service
     * Consumer: subscription-service
     */
    public static final String USER_SEARCH_PROFILE_UPDATED = "user-search-profile-updated";

    /**
     * Topic for payment request.
     * Producer: subscription-service
     * Consumer: jm-payment-service
     */
    public static final String APPLICANT_PAYMENT_REQUEST = "applicant-payment-request";

    /**
     * Topic for payment response.
     * Producer: jm-payment-service
     * Consumer: subscription-service
     */
    public static final String APPLICANT_PAYMENT_RESPONSE = "applicant-payment-response";

    /**
     * Topic for enabling user premium status.
     * Producer: subscription-service
     * Consumer: user-service, auth-service
     */
    public static final String USER_PREMIUM_ENABLED = "user-premium-enabled";

    /**
     * Topic for job posted matched events.
     * 
     * NOTE: Only for premium users.
     * Producer: subscription-service
     * Consumer: notification-service
     */
    public static final String JOB_POSTED_MATCHED = "job-posted-matched";
}
