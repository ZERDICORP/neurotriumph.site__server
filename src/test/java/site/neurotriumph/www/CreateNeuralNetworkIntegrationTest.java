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
import org.springframework.test.web.servlet.MvcResult;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Header;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.CreateNeuralNetworkRequestBody;
import site.neurotriumph.www.pojo.CreateNeuralNetworkResponseBody;
import site.neurotriumph.www.pojo.ErrorResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class CreateNeuralNetworkIntegrationTest {
  private final String baseUrl = "/nn";

  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnNeuralNetworkApiRootAlreadyInUseError() throws Exception {
    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "not_used_name",
      "http://188.187.188.37:5000/v1/api",
      "123");

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(post(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, authToken)
        .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.NN_API_ROOT_ALREADY_IN_USE))));
  }

  @Test
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnNeuralNetworkNameAlreadyInUseError() throws Exception {
    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "human_killer",
      "http://188.187.188.37:5000/v1/api",
      "123");

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(post(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, authToken)
        .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.NN_NAME_ALREADY_IN_USE))));
  }

  @Test
  @Sql(value = {"/sql/insert_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnUserDoesNotExistError() throws Exception {
    List<String> authTokens = new ArrayList<>();

    authTokens.add(JWT.create()
      .withClaim(Field.USER_ID, 2L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION)));

    /*
     * This token has id 1, and the user with id 1 exists,
     * but does not confirm, so it will also return an error.
     * */
    authTokens.add(JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION)));

    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "human_killer",
      "http://188.187.188.37:5000/v1/api",
      "123");

    for (String authToken : authTokens) {
      this.mockMvc.perform(post(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, authToken)
          .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.USER_DOES_NOT_EXIST))));
    }
  }

  @Test
  public void shouldReturnTokenExpiredError() throws Exception {
    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "human_killer",
      "http://188.187.188.37:5000/v1/api",
      "123");

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() - 1000))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(post(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, authToken)
        .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_EXPIRED))));
  }

  @Test
  public void shouldReturnInvalidTokenError() throws Exception {
    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "human_killer",
      "http://188.187.188.37:5000/v1/api",
      "123");

    this.mockMvc.perform(post(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, "")
        .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.INVALID_TOKEN))));
  }

  @Test
  public void shouldReturnAuthTokenNotSpecifiedError() throws Exception {
    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "human_killer",
      "http://188.187.188.37:5000/v1/api",
      "123");

    this.mockMvc.perform(post(baseUrl)
        .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_NOT_SPECIFIED))));
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnNeuralNetworkApiSecretCannotBeBlankError() throws Exception {
    List<String> invalidApiSecrets = new ArrayList<>();
    invalidApiSecrets.add("");
    invalidApiSecrets.add(null);

    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "human_killer",
      "http://188.187.188.37:5000/v1/api",
      "123");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    for (String invalidApiSecret : invalidApiSecrets) {
      createNeuralNetworkRequestBody.setApi_secret(invalidApiSecret);

      this.mockMvc.perform(post(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, token)
          .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.NN_API_SECRET_CANNOT_BE_BLANK))));
    }
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnInvalidNeuralNetworkApiRootError() throws Exception {
    List<String> invalidApiRoots = new ArrayList<>();
    invalidApiRoots.add("http://");
    invalidApiRoots.add("http://nn .org/");
    invalidApiRoots.add("http://nn.org/ !");
    invalidApiRoots.add("abc123");

    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "human_killer",
      null,
      "123");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    for (String invalidApiRoot : invalidApiRoots) {
      createNeuralNetworkRequestBody.setApi_root(invalidApiRoot);

      this.mockMvc.perform(post(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, token)
          .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.INVALID_NN_API_ROOT))));
    }
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnNeuralNetworkApiRootCannotBeBlankError() throws Exception {
    List<String> invalidApiRoots = new ArrayList<>();
    invalidApiRoots.add("");
    invalidApiRoots.add(null);

    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "human_killer",
      "http://188.187.188.37:5000/v1/api",
      "123");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    for (String invalidApiRoot : invalidApiRoots) {
      createNeuralNetworkRequestBody.setApi_root(invalidApiRoot);

      this.mockMvc.perform(post(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, token)
          .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.NN_API_ROOT_CANNOT_BE_BLANK))));
    }
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnInvalidNeuralNetworkNameError() throws Exception {
    List<String> invalidNames = new ArrayList<>();
    invalidNames.add("123");
    invalidNames.add("@!#,.");

    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      null,
      "http://188.187.188.37:5000/v1/api",
      "123");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    for (String invalidName : invalidNames) {
      createNeuralNetworkRequestBody.setName(invalidName);

      this.mockMvc.perform(post(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, token)
          .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.INVALID_NN_NAME))));
    }
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnNeuralNetworkNameCannotBeBlankError() throws Exception {
    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      null,
      "http://188.187.188.37:5000/v1/api",
      "123");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(post(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.NN_NAME_CANNOT_BE_BLANK))));
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatusAndNeuralNetworkId() throws Exception {
    CreateNeuralNetworkRequestBody createNeuralNetworkRequestBody = new CreateNeuralNetworkRequestBody(
      "human_killer",
      "http://188.187.188.37:5000/v1/api",
      "123");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    MvcResult mvcResult = this.mockMvc.perform(post(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(createNeuralNetworkRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn();

    CreateNeuralNetworkResponseBody createNeuralNetworkResponseBody = objectMapper.readValue(
      mvcResult.getResponse().getContentAsString(), CreateNeuralNetworkResponseBody.class);

    assertNotNull(createNeuralNetworkResponseBody.getId());
    assertEquals(1L, (long) createNeuralNetworkResponseBody.getId());
  }
}
