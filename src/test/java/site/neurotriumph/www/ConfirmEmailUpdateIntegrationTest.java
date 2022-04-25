package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Header;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ConfirmationRequestBody;
import site.neurotriumph.www.pojo.ErrorResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class ConfirmEmailUpdateIntegrationTest {
  private final String baseUrl = "/user/email/confirm";

  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void shouldReturnTokenExpiredError() throws Exception {
    Long userId = 1L;

    String token = JWT.create()
      .withClaim(Field.USER_ID, userId)
      .withClaim(Field.NEW_EMAIL, "new_email@gmail.com")
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.EMAIL_UPDATE_CONFIRMATION));

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() - 1000))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, authToken)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_EXPIRED))));
  }

  @Test
  public void shouldReturnInvalidTokenError() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withClaim(Field.NEW_EMAIL, "new_email@gmail.com")
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.EMAIL_UPDATE_CONFIRMATION));

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, "")
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.INVALID_TOKEN))));
  }

  @Test
  public void shouldReturnAuthTokenNotSpecifiedError() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withClaim(Field.NEW_EMAIL, "new_email@gmail.com")
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.EMAIL_UPDATE_CONFIRMATION));

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

    this.mockMvc.perform(put(baseUrl)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_NOT_SPECIFIED))));
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnNothingToUpdateError() throws Exception {
    Long userId = 1L;

    String token = JWT.create()
      .withClaim(Field.USER_ID, userId)
      .withClaim(Field.NEW_EMAIL, "test@gmail.com")
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.EMAIL_UPDATE_CONFIRMATION));

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, authToken)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.NOTHING_TO_UPDATE))));
  }

  @Test
  @Sql(value = {"/sql/insert_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnUserDoesNotExistError() throws Exception {
    List<String> tokens = new ArrayList<>();

    tokens.add(JWT.create()
      .withClaim(Field.USER_ID, 2L)
      .withClaim(Field.NEW_EMAIL, "new_email@gmail.com")
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.EMAIL_UPDATE_CONFIRMATION)));

    /*
     * This token has id 1, and the user with id 1 exists,
     * but does not confirm, so it will also return an error.
     * */
    tokens.add(JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withClaim(Field.NEW_EMAIL,"new_email@gmail.com")
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.EMAIL_UPDATE_CONFIRMATION)));

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    for (String token : tokens) {
      ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

      this.mockMvc.perform(put(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, authToken)
          .content(objectMapper.writeValueAsString(confirmationRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.USER_DOES_NOT_EXIST))));
    }
  }

  @Test
  public void shouldReturnTokenCannotBeBlankErrorForConfirmationToken() throws Exception {
    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(null);

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, authToken)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.TOKEN_CANNOT_BE_BLANK))));
  }

  @Test
  public void shouldReturnInvalidTokenErrorForConfirmationToken() throws Exception {
    Long userId = 1L;

    String token = JWT.create()
      .withClaim(Field.USER_ID, userId)
      .withClaim(Field.NEW_EMAIL, "new_email@gmail.com")
      .withExpiresAt(new Date(System.currentTimeMillis() - 1000))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.EMAIL_UPDATE_CONFIRMATION));

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, userId)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, authToken)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.INVALID_TOKEN))));
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatus() throws Exception {
    Long userId = 1L;

    String token = JWT.create()
      .withClaim(Field.USER_ID, userId)
      .withClaim(Field.NEW_EMAIL, "new_email@gmail.com")
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.EMAIL_UPDATE_CONFIRMATION));

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, userId)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, authToken)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk());
  }
}
