package site.neurotriumph.www;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import site.neurotriumph.www.constant.Field;
import site.neurotriumph.www.constant.Header;
import site.neurotriumph.www.constant.Message;
import site.neurotriumph.www.constant.TokenMarker;
import site.neurotriumph.www.pojo.ErrorResponseBody;
import site.neurotriumph.www.pojo.GetUserNeuralNetworksResponseBodyItem;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class GetUserNeuralNetworksIntegrationTest {
  private final String baseUrl = "/user/nn/all/";

  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

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

    for (String authToken : authTokens) {
      this.mockMvc.perform(get(baseUrl + "/1")
          .header(Header.AUTHENTICATION_TOKEN, authToken))
        .andDo(print())
        .andExpect(status().isBadRequest())
        .andExpect(content().string(objectMapper.writeValueAsString(
          new ErrorResponseBody(Message.USER_DOES_NOT_EXIST))));
    }
  }

  @Test
  public void shouldReturnTokenExpiredError() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .withExpiresAt(new Date(System.currentTimeMillis() - 1000))
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    this.mockMvc.perform(get(baseUrl + "/1")
        .header(Header.AUTHENTICATION_TOKEN, token))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_EXPIRED))));
  }

  @Test
  public void shouldReturnInvalidTokenError() throws Exception {
    this.mockMvc.perform(get(baseUrl + "/1")
        .header(Header.AUTHENTICATION_TOKEN, ""))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.INVALID_TOKEN))));
  }

  @Test
  public void shouldReturnAuthTokenNotSpecifiedError() throws Exception {
    this.mockMvc.perform(get(baseUrl + "/1"))
      .andDo(print())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(objectMapper.writeValueAsString(
        new ErrorResponseBody(Message.AUTH_TOKEN_NOT_SPECIFIED))));
  }

  @Test
  public void shouldReturnNotFoundBecauseIdDoesNotMatchRegex() throws Exception {
    List<String> invalidPages = new ArrayList<>();
    invalidPages.add("-1");
    invalidPages.add("abc");

    for (String invalidId : invalidPages) {
      this.mockMvc.perform(get(baseUrl + "/" + invalidId))
        .andDo(print())
        .andExpect(status().isNotFound());
    }
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatusAndEmptyList() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    MvcResult mvcResult = this.mockMvc.perform(get(baseUrl + "/1")
        .header(Header.AUTHENTICATION_TOKEN, token))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn();

    GetUserNeuralNetworksResponseBodyItem[] getUserNeuralNetworksResponseBodyItems = objectMapper.readValue(
      mvcResult.getResponse().getContentAsString(), GetUserNeuralNetworksResponseBodyItem[].class);

    assertNotNull(getUserNeuralNetworksResponseBodyItems);
    assertEquals(0, getUserNeuralNetworksResponseBodyItems.length);
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatusAndListOfNeuralNetworks() throws Exception {
    String token = JWT.create()
      .withClaim(Field.USER_ID, 1L)
      .sign(Algorithm.HMAC256(appSecret + TokenMarker.AUTHENTICATION));

    MvcResult mvcResult = this.mockMvc.perform(get(baseUrl + "/0")
        .header(Header.AUTHENTICATION_TOKEN, token))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn();

    GetUserNeuralNetworksResponseBodyItem[] getUserNeuralNetworksResponseBodyItems = objectMapper.readValue(
      mvcResult.getResponse().getContentAsString(), GetUserNeuralNetworksResponseBodyItem[].class);

    assertNotNull(getUserNeuralNetworksResponseBodyItems);
    assertEquals(1, getUserNeuralNetworksResponseBodyItems.length);

    GetUserNeuralNetworksResponseBodyItem getUserNeuralNetworksResponseBodyItem =
      getUserNeuralNetworksResponseBodyItems[0];

    assertNotNull(getUserNeuralNetworksResponseBodyItem);
    assertEquals(0, getUserNeuralNetworksResponseBodyItem.getCoeff().longValue());
    assertEquals(1, getUserNeuralNetworksResponseBodyItem.getId().longValue());
    assertEquals("human_killer", getUserNeuralNetworksResponseBodyItem.getName());
    assertFalse(getUserNeuralNetworksResponseBodyItem.isInvalid_api());
    assertTrue(getUserNeuralNetworksResponseBodyItem.isActive());
  }
}
