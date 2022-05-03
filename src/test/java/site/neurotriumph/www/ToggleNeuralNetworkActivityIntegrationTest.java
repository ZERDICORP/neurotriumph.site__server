package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Header;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ErrorResponseBody;
import site.neurotriumph.www.pojo.ToggleNeuralNetworkActivityRequestBody;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class ToggleNeuralNetworkActivityIntegrationTest {
  private final String baseUrl = "/user/nn/toggle_activity";

  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

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

    ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
      new ToggleNeuralNetworkActivityRequestBody(1L);

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    for (Long invalidId : invalidIds) {
      toggleNeuralNetworkActivityRequestBody.setId(invalidId);

      this.mockMvc.perform(put(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, token)
          .content(objectMapper.writeValueAsString(toggleNeuralNetworkActivityRequestBody))
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

    ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
      new ToggleNeuralNetworkActivityRequestBody(1L);

    for (String authToken : authTokens) {
      this.mockMvc.perform(put(baseUrl)
          .header(Header.AUTHENTICATION_TOKEN, authToken)
          .content(objectMapper.writeValueAsString(toggleNeuralNetworkActivityRequestBody))
          .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.USER_DOES_NOT_EXIST))));
    }
  }

  @Test
  public void shouldReturnTokenExpiredError() throws Exception {
    ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
      new ToggleNeuralNetworkActivityRequestBody(1L);

    String authToken = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() - 1000))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, authToken)
        .content(objectMapper.writeValueAsString(toggleNeuralNetworkActivityRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_EXPIRED))));
  }

  @Test
  public void shouldReturnInvalidTokenError() throws Exception {
    ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
      new ToggleNeuralNetworkActivityRequestBody(1L);

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, "")
        .content(objectMapper.writeValueAsString(toggleNeuralNetworkActivityRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.INVALID_TOKEN))));
  }

  @Test
  public void shouldReturnAuthTokenNotSpecifiedError() throws Exception {
    ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
      new ToggleNeuralNetworkActivityRequestBody(1L);

    this.mockMvc.perform(put(baseUrl)
        .content(objectMapper.writeValueAsString(toggleNeuralNetworkActivityRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_NOT_SPECIFIED))));
  }

  @Test
  public void shouldReturnIdCannotBeBlankError() throws Exception {
    ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
      new ToggleNeuralNetworkActivityRequestBody(null);

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(toggleNeuralNetworkActivityRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.ID_CANNOT_BE_BLANK))));
  }

  @Test
  public void shouldReturnInvalidIdError() throws Exception {
    ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
      new ToggleNeuralNetworkActivityRequestBody(-1L);

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(toggleNeuralNetworkActivityRequestBody))
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
    ToggleNeuralNetworkActivityRequestBody toggleNeuralNetworkActivityRequestBody =
      new ToggleNeuralNetworkActivityRequestBody(1L);

    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(put(baseUrl)
        .header(Header.AUTHENTICATION_TOKEN, token)
        .content(objectMapper.writeValueAsString(toggleNeuralNetworkActivityRequestBody))
        .contentType(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isOk());
  }
}
