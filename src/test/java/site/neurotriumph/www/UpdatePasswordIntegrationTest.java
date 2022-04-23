package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
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
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Header;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ErrorResponseBody;
import site.neurotriumph.www.pojo.UpdatePasswordRequestBody;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class UpdatePasswordIntegrationTest {
   private final String baseUrl = "/user/password";

  @Value("${app.secret}")
  private String appSecret;

  @Value("${spring.mail.username}")
  private String senderEmail;

  @Value("${spring.mail.password}")
  private String senderPassword;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @RegisterExtension
  private GreenMail greenMail;

  private void startGreenMail() {
    greenMail = new GreenMail(ServerSetupTest.SMTP)
      .withConfiguration(GreenMailConfiguration.aConfig()
        .withUser(senderEmail, senderPassword));

    greenMail.start();
  }

  @Test
  public void shouldReturnTokenExpiredError() throws Exception {
    UpdatePasswordRequestBody updatePasswordRequestBody = new UpdatePasswordRequestBody("Qwerty123",
      "Qwerty1234");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() - 1000))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.TOKEN_EXPIRED))));
  }

  @Test
  public void shouldReturnInvalidTokenError() throws Exception {
    UpdatePasswordRequestBody updatePasswordRequestBody = new UpdatePasswordRequestBody("Qwerty123",
      "Qwerty1234");

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, "")
        .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.INVALID_TOKEN))));
  }

  @Test
  public void shouldReturnTokenNotSpecifiedError() throws Exception {
    UpdatePasswordRequestBody updatePasswordRequestBody = new UpdatePasswordRequestBody("Qwerty123",
      "Qwerty1234");

    this.mockMvc.perform(put(baseUrl)
        .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.TOKEN_NOT_SPECIFIED))));
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnDataIsNotChangedError() throws Exception {
    UpdatePasswordRequestBody updatePasswordRequestBody = new UpdatePasswordRequestBody("Qwerty123",
      "Qwerty123");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.AUTH_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.DATA_IS_NOT_CHANGED))));
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnWrongPasswordError() throws Exception {
    UpdatePasswordRequestBody updatePasswordRequestBody = new UpdatePasswordRequestBody("123123",
      "Qwerty1234");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.AUTH_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.WRONG_PASSWORD))));
  }

  @Test
  @Sql(value = {"/sql/insert_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnUserDoesNotExistError() throws Exception {
    UpdatePasswordRequestBody updatePasswordRequestBody = new UpdatePasswordRequestBody("Qwerty123",
      "Qwerty1234");

    List<String> tokens = new ArrayList<>();

    tokens.add(JWT.create()
      .withClaim(Field.USER_ID, 2L)
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.AUTH_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION)));

    /*
     * This token has id 1, and the user with id 1 exists,
     * but does not confirm, so it will also return an error.
     * */
    tokens.add(JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.AUTH_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION)));

    for (String token : tokens) {
      this.mockMvc.perform(put(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, token)
          .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.USER_DOES_NOT_EXIST))));
    }
  }

  @Test
  public void shouldReturnPasswordCannotBeBlankError() throws Exception {
    /*
     * Validation for the password field.
     * */

    UpdatePasswordRequestBody updatePasswordRequestBody = new UpdatePasswordRequestBody(null,
      "Qwerty1234");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.AUTH_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.PASSWORD_CANNOT_BE_BLANK))));

    /*
     * Validation for the new_password field.
     * */

    updatePasswordRequestBody = new UpdatePasswordRequestBody("Qwerty123", null);

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.PASSWORD_CANNOT_BE_BLANK))));
  }

  @Test
  public void shouldReturnPasswordTooShortError() throws Exception {
    /*
    * Validation for the password field.
    * */

    UpdatePasswordRequestBody updatePasswordRequestBody = new UpdatePasswordRequestBody("Qwerty123",
      "a".repeat(Const.MIN_PASSWORD_LENGTH - 1));

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.AUTH_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.PASSWORD_IS_TOO_SHORT))));

    /*
     * Validation for the new_password field.
     * */

    updatePasswordRequestBody = new UpdatePasswordRequestBody("a".repeat(Const.MIN_PASSWORD_LENGTH - 1),
      "Qwerty1234");

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.PASSWORD_IS_TOO_SHORT))));
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatus() throws Exception {
    startGreenMail();

    UpdatePasswordRequestBody updatePasswordRequestBody = new UpdatePasswordRequestBody("Qwerty123",
      "Qwerty1234");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() + Const.AUTH_TOKEN_LIFETIME))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updatePasswordRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk());

    await().atMost(2, SECONDS).untilAsserted(() -> {
      MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
      assertEquals(1, receivedMessages.length);

      MimeMessage receivedMessage = receivedMessages[0];
      assertEquals("Neuro Triumph", receivedMessage.getSubject());
      assertEquals(1, receivedMessage.getAllRecipients().length);
      assertEquals(senderEmail, receivedMessage.getAllRecipients()[0].toString());

      assertTrue(receivedMessage.getContent().toString().length() > 0);

      greenMail.stop();
    });
  }
}
