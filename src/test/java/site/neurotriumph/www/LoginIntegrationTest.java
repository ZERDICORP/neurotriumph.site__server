package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MvcResult;
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ErrorResponseBody;
import site.neurotriumph.www.pojo.LoginRequestBody;
import site.neurotriumph.www.pojo.LoginResponseBody;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class LoginIntegrationTest {
  private final String baseUrl = "/login";

  @Value("${app.secret}")
  private String appSecret;

  @Value("${spring.mail.username}")
  private String senderEmail;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnWrongPasswordError() throws Exception {
    LoginRequestBody loginRequestBody = new LoginRequestBody(senderEmail, "Qwerty1234");

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(loginRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.WRONG_PASSWORD))));
  }

  @Test
  @Sql(value = {"/sql/insert_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnUserNotConfirmedError() throws Exception {
    LoginRequestBody loginRequestBody = new LoginRequestBody(senderEmail, "Qwerty123");

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(loginRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.USER_NOT_CONFIRMED))));
  }

  @Test
  public void shouldReturnUserDoesNotExistError() throws Exception {
    LoginRequestBody loginRequestBody = new LoginRequestBody(senderEmail, "Qwerty123");

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(loginRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.USER_DOES_NOT_EXIST))));
  }

  @Test
  public void shouldReturnPasswordCannotBeBlankError() throws Exception {
    LoginRequestBody loginRequestBody = new LoginRequestBody(senderEmail, null);

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(loginRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.PASSWORD_CANNOT_BE_BLANK))));
  }

  @Test
  public void shouldReturnPasswordTooShortError() throws Exception {
    LoginRequestBody loginRequestBody = new LoginRequestBody(senderEmail,
      "a".repeat(Const.MIN_PASSWORD_LENGTH - 1));

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(loginRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.PASSWORD_IS_TOO_SHORT))));
  }

  @Test
  public void shouldReturnEmailCannotBeBlankError() throws Exception {
    List<String> invalidEmails = new ArrayList<>();
    invalidEmails.add("");
    invalidEmails.add(null);

    LoginRequestBody loginRequestBody = new LoginRequestBody(null, "Qwerty123");

    for (String invalidEmail : invalidEmails) {
      loginRequestBody.setEmail(invalidEmail);

      this.mockMvc.perform(post(baseUrl)
          .content(objectMapper.writeValueAsString(loginRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.EMAIL_CANNOT_BE_BLANK))));
    }
  }

  @Test
  public void shouldReturnInvalidEmailError() throws Exception {
    List<String> invalidEmails = new ArrayList<>();
    invalidEmails.add("myemail.com");
    invalidEmails.add("@email.com");
    invalidEmails.add("my@");

    LoginRequestBody loginRequestBody = new LoginRequestBody(null, "Qwerty123");

    for (String invalidEmail : invalidEmails) {
      loginRequestBody.setEmail(invalidEmail);

      this.mockMvc.perform(post(baseUrl)
          .content(objectMapper.writeValueAsString(loginRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.INVALID_EMAIL))));
    }
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatusAndAuthTokenWithUserId() throws Exception {
    LoginRequestBody loginRequestBody = new LoginRequestBody(senderEmail,
      "Qwerty123");

    MvcResult mvcResult = this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(loginRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn();

    LoginResponseBody loginResponseBody = objectMapper.readValue(
      mvcResult.getResponse().getContentAsString(), LoginResponseBody.class);

    assertTrue(loginResponseBody.getUid() > 0);

    /*
     * Checking the token for validity.
     * */

    assertDoesNotThrow(() -> {
      JWT.require(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION))
        .build()
        .verify(loginResponseBody.getToken());
    });

    assertDoesNotThrow(() -> {
      DecodedJWT decodedJWT = JWT.decode(loginResponseBody.getToken());

      assertNotNull(decodedJWT.getClaim(Field.USER_ID));
      assertNotNull(decodedJWT.getClaim(Field.EXPIRATION_TIME));
    });
  }
}
