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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    this.mockMvc.perform(put(baseUrl)
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

    this.mockMvc.perform(put(baseUrl)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.USER_DOES_NOT_EXIST))));
  }

  @Test
  public void shouldReturnTokenCannotBeBlankError() throws Exception {
    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(null);

    this.mockMvc.perform(put(baseUrl)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.TOKEN_CANNOT_BE_BLANK))));
  }

  @Test
  public void shouldReturnInvalidTokenError() throws Exception {
    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(
      /*
       * This token expired.
       * */
      "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEsImV4cCI6MTY1MDUxMDYxOX0." +
        "CPkP7Mco5Qnlhb5It1BklWEiBq2Ocnpqzot4is41W9I");

    this.mockMvc.perform(put(baseUrl)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.INVALID_TOKEN))));
  }

  @Test
  @Sql(value = {"/sql/insert_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatus() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.REGISTRATION_CONFIRMATION));

    ConfirmationRequestBody confirmationRequestBody = new ConfirmationRequestBody(token);

    this.mockMvc.perform(put(baseUrl)
        .content(objectMapper.writeValueAsString(confirmationRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk());
  }
}
