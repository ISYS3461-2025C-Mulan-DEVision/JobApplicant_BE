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
     * Topic for admin request user data
     * Producer: admin-service
     * Consumer: user-service
     */
    public static final String ADMIN_REQUEST_USER_DATA = "admin-request-user-data";

    /**
     * Topic for user data response to admin
     * Producer: user-service
     * Consumer: admin-service
     */
    public static final String ADMIN_REPLY_USER_DATA = "admin-reply-user-data";

    /**
     * Topic for admin delete user request
     * Producer: admin-service
     * Consumer: user-service
     */
    public static final String ADMIN_DEACTIVATE_USER = "admin-deactivate-user";

    /**
     * Topic for delete user response to admin
     * Producer: user-service
     * Consumer: admin-service
     */
    public static final String ADMIN_REPLY_DEACTIVATE_USER = "admin-reply-deactivate-user";

    /**
     * Topic for admin request skill data
     * Producer: admin-service
     * Consumer: user-service
     * 
     */
    public static final String ADMIN_REQUEST_SKILL_DATA = "admin-request-skill-data";
    /**
     * Topic for skill data response to admin
     * Producer: user-service
     * Consumer: admin-service
     */
    public static final String ADMIN_REPLY_SKILL_DATA = "admin-reply-skill-data";

    /**
     * Topic for admin create skill request
     * Producer: admin-service
     * Consumer: user-service
     */
    public static final String ADMIN_CREATE_SKILL = "admin-create-skill";

    /**
     * Topic for create skill response to admin
     * Producer: user-service
     * Consumer: admin-service
     */
    public static final String ADMIN_REPLY_CREATE_SKILL = "admin-reply-create-skill";
}
