package site.neurotriumph.www.constant;

public interface Message {
  String USER_ALREADY_EXISTS = "user already exists";
  String USER_DOES_NOT_EXIST = "user does not exist";
  String AUTH_TOKEN_NOT_SPECIFIED = "authentication token not specified";
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
  String AUTH_TOKEN_EXPIRED = "authentication token expired";
  String NOTHING_TO_UPDATE = "nothing to update";
  String NN_NAME_CANNOT_BE_BLANK = "neural network name cannot be blank";
  String NN_API_ROOT_CANNOT_BE_BLANK = "neural network api root cannot be blank";
  String NN_API_SECRET_CANNOT_BE_BLANK = "neural network api secret cannot be blank";
  String NN_NAME_ALREADY_IN_USE = "neural network name already in use";
  String INVALID_NN_NAME = "invalid neural network name";
  String INVALID_NN_API_ROOT = "invalid neural network api root";
  String NN_API_ROOT_ALREADY_IN_USE = "neural network api root already in use";
}
