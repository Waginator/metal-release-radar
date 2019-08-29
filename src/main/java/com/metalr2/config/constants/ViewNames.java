package com.metalr2.config.constants;

public class ViewNames {

  public static class Guest {
    // Authentication
    public static final String INDEX                     = "guest/index";
    public static final String LOGIN                     = "guest/auth/login";
    public static final String REGISTER                  = "guest/auth/register";
    public static final String FORGOT_PASSWORD           = "guest/auth/forgot-password";
    public static final String RESET_PASSWORD            = "guest/auth/reset-password";

    // Error pages
    public static final String ERROR                     = "error/default";
    public static final String ERROR_403                 = "error/403";
    public static final String ERROR_404                 = "error/404";
    public static final String ERROR_500                 = "error/500";
  }

  public static class Frontend {
    public static final String FOLLOW_ARTISTS            = "frontend/follow-artists";
    public static final String SETTINGS                  = "frontend/settings";
    public static final String PROFILE                   = "frontend/profile";
    public static final String ARTISTS_RELEASES          = "frontend/artists-releases";
    public static final String ALL_RELEASES              = "frontend/all-releases";
    public static final String MY_ARTISTS                = "frontend/my-artists";
    public static final String REPORT_ARTIST_RELEASE     = "frontend/report-artist-release";
    public static final String ABOUT                     = "frontend/about";
    public static final String TEAM                      = "frontend/team";
    public static final String CONTACT                   = "frontend/contact";
    public static final String IMPRINT                   = "frontend/imprint";
    public static final String STATUS                    = "frontend/status";
  }

  public static class AdminArea {
    public static final String USERS_LIST                = "admin/users/list";
    public static final String USERS_CREATE              = "admin/users/create";
    public static final String USERS_EDIT                = "admin/users/edit";
  }

  public static class EmailTemplates {
    public static final String REGISTRATION_VERIFICATION = "email/registration-verification-email";
    public static final String FORGOT_PASSWORD           = "email/forgot-password-email";
  }

}
