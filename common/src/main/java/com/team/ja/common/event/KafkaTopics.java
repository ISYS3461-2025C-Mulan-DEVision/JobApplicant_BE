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
    public static final String SUBSCRIPTION_ACTIVATE = "subscription-activate";

    /**
     * Topic for subscription deactivation events.
     * Producer: subscription-service
     * Consumer: user-service (to update premium status)
     */
    public static final String SUBSCRIPTION_DEACTIVATE = "subscription-deactivate";

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

    /**
     * Topic for user search profile update events.
     * Producer: user-service
     * Consumer: subscription-service
     */
    public static final String USER_SEARCH_PROFILE_UPDATED = "user-search-profile-updated";

    /**
     * Topic for payment success response.
     * Producer: jm-payment-service
     * Consumer: subscription-service
     */
    public static final String APPLICANT_PAYMENT_COMPLETED = "payment.completed";

    /**
     * Topic for payment failure response.
     * Producer: jm-payment-service
     * Consumer: subscription-service
     */
    public static final String APPLICANT_PAYMENT_FAILED = "payment.failed";

    /**
     * Topic for payment cancelled response.
     * Producer: subscription-service
     * Consumer: user-service
     */
    public static final String APPLICANT_PAYMENT_CANCELLED = "payment.cancelled";

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

    /**
     * Topic for UserProfileCreateEvent messages.
     * Producer: user-service
     * Consumer: jm-search-service
     */
    public static final String USER_PROFILE_CREATE = "user-profile-create";

    /**
     * Topic for UserProfileUpdateEvent messages.
     * Producer: user-service
     * Consumer: jm-search-service
     */
    public static final String USER_PROFILE_UPDATE = "user-profile-update";

    public static final String JOB_POST_PUBLISHED = "jobpost.published";

    public static final String JOB_POST_SKILL_CHANGE = "jobpost.skill.change";

    public static final String JOB_POST_COUNTRY_CHANGE = "jobpost.country.change";
}
