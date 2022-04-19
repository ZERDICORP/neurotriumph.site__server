package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
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
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ConfirmationRequestBody;
import site.neurotriumph.www.pojo.ErrorResponseBody;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class ConfirmRegistrationIntegrationTest {
  private final String baseUrl = "/register/confirm";

  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnUserAlreadyConfirmedError() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.REGISTRATION_CONFIRMATION));

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.USER_ALREADY_CONFIRMED))));
  }

  @Test
  public void shouldReturnUserDoesNotExistError() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.REGISTRATION_CONFIRMATION));

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.USER_DOES_NOT_EXIST))));
  }

  @Test
  public void shouldReturnInvalidTokenError() throws Exception {
    List<String> invalidTokens = new ArrayList<>();
    /*
     * invalid because..
     * ..removed one character (this will cause the jwt verifier to
     * throw an exception).
     * */
    invalidTokens.add("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjF9.1hF426wB0xOtHGdBwzgDo2LNs91fw5yF3tZ91aEqVy");
    /*
     * invalid because..
     * ..dots removed (this will prevent the token from passing regex
     * validation).
     * */
    invalidTokens.add("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9eyJ1aWQiOjF91hF426wB0xOtHGdBwzgDo2LNs91fw5yF3tZ91aEqVyg");
    /*
     * invalid because..
     * ..token expired.
     * */
    invalidTokens.add("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEsImV4cCI6MTY1MDM0MzUzMX0." +
      "kJF2Nt6ga_4iW3iS-Q7o53qigoP3KBT5Gzq6Y7sW7OY");

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(null);

    for (String invalidToken : invalidTokens) {
      confirmationRequestBody.setToken(invalidToken);

      this.mockMvc.perform(post(baseUrl)
          .content(objectMapper.writeValueAsString(confirmationRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.INVALID_TOKEN))));
    }
  }

  @Test
  @Sql(value = {"/sql/insert_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatus() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.REGISTRATION_CONFIRMATION));

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk());
  }
}
