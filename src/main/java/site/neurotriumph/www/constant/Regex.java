package site.neurotriumph.www.constant;

public interface Regex {
  String JWT_TOKEN = "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*$";
  String NN_NAME = "^[^0-9][A-Za-z0-9]+([A-Za-z0-9]*|[._-]?[A-Za-z0-9]+)*$";
  String URL = "^((([A-Za-z]{3,9}:(?:\\/\\/)?)(?:[-;:&=\\+\\$,\\w]+@)?[A-Za-z0-9.-]+" +
    "(:[0-9]+)?|(?:www.|[-;:&=\\+\\$,\\w]+@)[A-Za-z0-9.-]+)((?:\\/[\\+~%\\/.\\w\\-_]*)?" +
    "\\??(?:[-\\+=&;%@.\\w_]*)#?(?:[\\w]*))?)$";
}
