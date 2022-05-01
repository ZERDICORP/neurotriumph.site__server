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
import site.neurotriumph.www.constant.Header;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.CreateNeuralNetworkRequestBody;
import site.neurotriumph.www.pojo.ErrorResponseBody;
import site.neurotriumph.www.pojo.UpdateNeuralNetworkApiRootRequestBody;
import site.neurotriumph.www.pojo.UpdateNeuralNetworkNameRequestBody;

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
public class UpdateNeuralNetworkApiRootIntegrationTest {
  private final String baseUrl = "/user/nn/api_root";

  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnNothingToUpdateError() throws Exception {
    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v1/api");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.NOTHING_TO_UPDATE))));
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/insert_neural_network_with_owner_id_2.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnNeuralNetworkDoesNotExist() throws Exception {
    List<Long> invalidIds = new ArrayList<>();
    /*
     * There is no neural network with this id.
     * */
    invalidIds.add(1L);
    /*
     * The neural network with this id belongs to another user.
     * */
    invalidIds.add(2L);

    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    for (Long invalidId : invalidIds) {
      updateNeuralNetworkApiRootRequestBody.setId(invalidId);

      this.mockMvc.perform(put(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, token)
          .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.NN_DOES_NOT_EXIST))));
    }
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

    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

    for (String authToken : authTokens) {
      this.mockMvc.perform(put(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, authToken)
          .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.USER_DOES_NOT_EXIST))));
    }
  }

  @Test
  public void shouldReturnTokenExpiredError() throws Exception {
    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() - 1000))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, authToken)
        .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_EXPIRED))));
  }

  @Test
  public void shouldReturnInvalidTokenError() throws Exception {
    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, "")
        .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.INVALID_TOKEN))));
  }

  @Test
  public void shouldReturnAuthTokenNotSpecifiedError() throws Exception {
    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

    this.mockMvc.perform(put(baseUrl)
        .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_NOT_SPECIFIED))));
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

    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    for (String invalidApiRoot : invalidApiRoots) {
      updateNeuralNetworkApiRootRequestBody.setNew_api_root(invalidApiRoot);

      this.mockMvc.perform(put(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, token)
          .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
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
    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(1L, null);

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.NN_API_ROOT_CANNOT_BE_BLANK))));
  }

  @Test
  public void shouldReturnIdCannotBeBlankError() throws Exception {
    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(null, "http://188.187.188.37:5000/v2/api");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.ID_CANNOT_BE_BLANK))));
  }

  @Test
  public void shouldReturnInvalidIdError() throws Exception {
    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(-1L, "http://188.187.188.37:5000/v2/api");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.INVALID_ID))));
  }


  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatus() throws Exception {
    UpdateNeuralNetworkApiRootRequestBody updateNeuralNetworkApiRootRequestBody =
      new UpdateNeuralNetworkApiRootRequestBody(1L, "http://188.187.188.37:5000/v2/api");

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(updateNeuralNetworkApiRootRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk());
  }
}
