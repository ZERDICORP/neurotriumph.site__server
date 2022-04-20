package site.neurotriumph.www.constant;

public interface Message {
  String USER_ALREADY_EXISTS = "user already exists";
  String USER_DOES_NOT_EXIST = "user does not exist";
  String TOKEN_NOT_SPECIFIED = "token not specified";
  String INVALID_TOKEN = "invalid token";
  String INVALID_EMAIL = "invalid email";
  String TOKEN_CANNOT_BE_BLANK = "token cannot be blank";
  String EMAIL_CANNOT_BE_BLANK = "email cannot be blank";
  String PASSWORD_CANNOT_BE_BLANK = "password cannot be blank";
  String PASSWORD_IS_TOO_SHORT = "password is too short (minimum " + Const.MIN_PASSWORD_LENGTH + " characters)";
  String CONFIRMATION_REQUEST_BODY_REQUIRED =
    "an object of type ConfirmationRequestBody is expected as a method parameter";
  String USER_ALREADY_CONFIRMED = "user already confirmed";
  String USER_NOT_CONFIRMED = "user not confirmed";
  String WRONG_PASSWORD = "wrong password";
}
