package site.neurotriumph.www.constant;

public interface Message {
  String USER_ALREADY_EXISTS = "user already exists";
  String ACCOUNT_DELETED = "account deleted";
  String TOKEN_NOT_SPECIFIED = "token not specified";
  String INVALID_TOKEN = "invalid token";
  String INVALID_EMAIL = "invalid email";
  String FORGOT_TO_SPECIFY_TOKEN = "forgot to specify token";
  String EMAIL_CANNOT_BE_BLANK = "email cannot be blank";
  String PASSWORD_CANNOT_BE_BLANK = "password cannot be blank";
  String PASSWORD_IS_TOO_SHORT = "password is too short (minimum " + Const.MIN_PASSWORD_LENGTH + " characters)";
}
