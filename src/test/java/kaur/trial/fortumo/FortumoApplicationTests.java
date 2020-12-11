package kaur.trial.fortumo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NumberController.class)
class FortumoApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void testEmptyRequest() throws Exception {
		mockMvc.perform(post("/")).andExpect(status().is4xxClientError());
	}

	@Test
	void testInvalidParameter() throws Exception {
		mockMvc.perform(post("/").content("string")).andExpect(status().is4xxClientError());
	}

	@Test
	void testEndParameterForNoData() throws Exception {
		mockMvc.perform(post("/").content("end")).andExpect(status().is2xxSuccessful()).andExpect(content().string("0"));
	}

	@Test
	void testNumberParameterTooLarge() throws Exception {
		mockMvc.perform(post("/").content("1000000000000000000000000000000000")).andExpect(status().is4xxClientError());
	}

	@Test
	void testSumNumbersTooLarge() throws Exception {
		CompletableFuture<String> req1 = CompletableFuture.supplyAsync(() -> {
			try {
				return mockMvc.perform(post("/").content("55")).andReturn().getResponse().getContentAsString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
		CompletableFuture<String> req2 = CompletableFuture.supplyAsync(() -> {
			try {
				return mockMvc.perform(post("/").content("10000000000")).andReturn().getResponse().getContentAsString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
		Thread.sleep(200);
		CompletableFuture<String> reqEnd = CompletableFuture.supplyAsync(() -> {
			try {
				return mockMvc.perform(post("/").content("end")).andReturn().getResponse().getContentAsString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
		String res1 = req1.get();
		String res2 = req2.get();
		String resEnd = reqEnd.get();

		assertEquals(StringConstants.SUM_TOO_LARGE, resEnd);
		assertEquals(resEnd, res1);
		assertEquals(res1, res2);
	}

	@Test
	void testSumResponse() throws Exception {
		CompletableFuture<String> req1 = CompletableFuture.supplyAsync(() -> {
			try {
				return mockMvc.perform(post("/").content("55")).andReturn().getResponse().getContentAsString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
		CompletableFuture<String> req2 = CompletableFuture.supplyAsync(() -> {
			try {
				return mockMvc.perform(post("/").content("55")).andReturn().getResponse().getContentAsString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
		Thread.sleep(200);
		CompletableFuture<String> reqEnd = CompletableFuture.supplyAsync(() -> {
			try {
				return mockMvc.perform(post("/").content("end")).andReturn().getResponse().getContentAsString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		});
		String res1 = req1.get();
		String res2 = req2.get();
		String resEnd = reqEnd.get();

		assertEquals("110", resEnd);
		assertEquals(resEnd, res1);
		assertEquals(res1, res2);
	}
}
