package site.neurotriumph.www;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import site.neurotriumph.www.pojo.GetNeuralNetworksResponseBodyItem;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/test.properties")
public class GetNeuralNetworksIntegrationTest {
  private final String baseUrl = "/nn/";

  @Value("${app.secret}")
  private String appSecret;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void shouldReturnNotFoundBecausePageDoesNotMatchRegex() throws Exception {
    List<String> invalidPages = new ArrayList<>();
    invalidPages.add("-1");
    invalidPages.add("abc");

    for (String invalidPage : invalidPages) {
      this.mockMvc.perform(get(baseUrl + "/" + invalidPage))
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
    MvcResult mvcResult = this.mockMvc.perform(get(baseUrl + "/1"))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn();

    GetNeuralNetworksResponseBodyItem[] getNeuralNetworksResponseBodyItems = objectMapper.readValue(
      mvcResult.getResponse().getContentAsString(), GetNeuralNetworksResponseBodyItem[].class);

    assertNotNull(getNeuralNetworksResponseBodyItems);
    assertEquals(0, getNeuralNetworksResponseBodyItems.length);
  }

  @Test
  @Sql(value = {"/sql/insert_confirmed_user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/insert_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  @Sql(value = {"/sql/truncate_neural_network.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  @Sql(value = {"/sql/truncate_user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
  public void shouldReturnOkStatusAndListOfNeuralNetworks() throws Exception {
    MvcResult mvcResult = this.mockMvc.perform(get(baseUrl + "/0"))
      .andDo(print())
      .andExpect(status().isOk())
      .andReturn();

    GetNeuralNetworksResponseBodyItem[] getNeuralNetworksResponseBodyItems = objectMapper.readValue(
      mvcResult.getResponse().getContentAsString(), GetNeuralNetworksResponseBodyItem[].class);

    assertNotNull(getNeuralNetworksResponseBodyItems);
    assertEquals(1, getNeuralNetworksResponseBodyItems.length);

    GetNeuralNetworksResponseBodyItem getUserNeuralNetworksResponseBodyItem =
      getNeuralNetworksResponseBodyItems[0];

    assertNotNull(getUserNeuralNetworksResponseBodyItem);
    assertEquals(0, getUserNeuralNetworksResponseBodyItem.getCoeff().longValue());
    assertEquals("human_killer", getUserNeuralNetworksResponseBodyItem.getName());
  }
}
