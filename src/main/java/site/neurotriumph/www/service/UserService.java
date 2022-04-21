package site.neurotriumph.www.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.entity.User;
import site.neurotriumph.www.pojo.GetUserResponseBody;
import site.neurotriumph.www.repository.UserRepository;

@Service
public class UserService {
  @Autowired
  private UserRepository userRepository;

  public GetUserResponseBody getUser(DecodedJWT decodedJWT) {
    User user = userRepository.findConfirmedById(decodedJWT.getClaim(Field.USER_ID).asLong())
      .orElseThrow(() -> new IllegalStateException(Message.USER_DOES_NOT_EXIST));

    return new GetUserResponseBody(user.getEmail());
  }
}
