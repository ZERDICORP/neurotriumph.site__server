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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import site.neurotriumph.www.constant.Const;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Header;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ErrorResponseBody;
import site.neurotriumph.www.pojo.GetUserResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class GetUserIntegrationTest {
  private final String baseUrl = "/user";

  @Value("${app.secret}")
  private String appSecret;

  @Value("${spring.mail.username}")
  private String senderEmail;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void shouldReturnTokenExpiredError() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() - 1000))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(get(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_EXPIRED))));
  }

  @Test
  public void shouldReturnInvalidTokenError() throws Exception {
    String token = "";

    this.mockMvc.perform(get(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.INVALID_TOKEN))));
  }

  @Test
  public void shouldReturnAuthTokenNotSpecifiedError() throws Exception {
    this.mockMvc.perform(get(baseUrl))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_NOT_SPECIFIED))));
  }

  @Test
  @Sql(value = {"/sql/insert_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnUserDoesNotExistError() throws Exception {
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
      this.mockMvc.perform(get(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, token))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.USER_DOES_NOT_EXIST))));
    }
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatusAndUserEmail() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    MvcResult mvcResult = this.mockMvc.perform(get(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn();

    GetUserResponseBody getUserResponseBody = objectMapper.readValue(
      mvcResult.getResponse().getContentAsString(), GetUserResponseBody.class);

    assertNotNull(getUserResponseBody);
    assertEquals(senderEmail, getUserResponseBody.getEmail());
  }
}
