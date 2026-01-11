package com.team.ja.notification.enumeration;

/**
 * Enumeration of notification types supported by the notification service.
 */
public enum NotificationType {
    
    /**
     * Notification when a new job post matches user's search profile.
     */
    JOB_MATCH,
    
    /**
     * Notification when an application status is updated.
     */
    APPLICATION_STATUS_UPDATE,
    
    /**
     * Notification when an application is submitted successfully.
     */
    APPLICATION_SUBMITTED,
    
    /**
     * Notification when subscription is activated.
     */
    SUBSCRIPTION_ACTIVATED,
    
    /**
     * Notification when subscription is deactivated or expired.
     */
    SUBSCRIPTION_DEACTIVATED,
    
    /**
     * Notification when payment is completed.
     */
    PAYMENT_COMPLETED,
    
    /**
     * Notification when payment fails.
     */
    PAYMENT_FAILED,
    
    /**
     * General system notification.
     */
    SYSTEM,
    
    /**
     * Welcome notification for new users.
     */
    WELCOME
}

