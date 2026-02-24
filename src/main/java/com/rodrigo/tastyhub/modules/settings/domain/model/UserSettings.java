package com.rodrigo.tastyhub.modules.settings.domain.model;

import com.rodrigo.tastyhub.modules.user.domain.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "user_settings")
public class UserSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "default_theme")
    private ThemeEnum defaultTheme = ThemeEnum.SYSTEM;

    @Builder.Default
    @Column(name = "language_preference", length = 5)
    private String languagePreference = "en-US";

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "font_size")
    private FontSizeEnum fontSize = FontSizeEnum.NORMAL;

    @Builder.Default
    @Column(name = "high_contrast_mode")
    private boolean highContrastMode = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility")
    private VisibilityEnum profileVisibility = VisibilityEnum.PUBLIC;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "recipe_visibility_default")
    private VisibilityEnum recipeVisibilityDefault = VisibilityEnum.PUBLIC;

    @Builder.Default
    @Column(name = "show_birthday")
    private boolean showBirthday = false;

    @Builder.Default
    @Column(name = "share_activity_feed")
    private boolean shareActivityFeed = true;

    @Builder.Default
    @Column(name = "allow_recipe_sharing")
    private boolean allowRecipeSharing = true;

    @Builder.Default
    @Column(name = "follow_requests_manual")
    private boolean followRequestsManual = false;

    @Builder.Default
    @Column(name = "accept_direct_messages")
    private boolean acceptDirectMessages = true;

    @Builder.Default
    @Column(name = "allow_recipe_messages")
    private boolean allowRecipeMessages = true;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_channel")
    private NotificationChannelEnum notificationChannel = NotificationChannelEnum.BOTH;

    @Builder.Default
    @Column(name = "notify_on_new_comment")
    private boolean notifyOnNewComment = true;

    @Builder.Default
    @Column(name = "notify_on_new_follower")
    private boolean notifyOnNewFollower = true;

    @Builder.Default
    @Column(name = "notify_on_recipe_like")
    private boolean notifyOnRecipeLike = true;

    @Builder.Default
    @Column(name = "notify_on_direct_message")
    private boolean notifyOnDirectMessage = true;

    @Builder.Default
    @Column(name = "two_factor_auth")
    private boolean twoFactorAuth = false;

    @Builder.Default
    @Column(name = "login_alerts")
    private boolean loginAlerts = true;

    @Builder.Default
    @Column(name = "session_timeout_milliseconds")
    private Integer sessionTimeoutMilliseconds = 600000;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "password_change_reminder_interval")
    private PasswordRotationEnum passwordChangeReminderInterval = PasswordRotationEnum.DISABLED;

    @Builder.Default
    @Column(name = "password_change_reminder_last_update")
    private OffsetDateTime passwordChangeReminderLastUpdate = OffsetDateTime.now();
}
