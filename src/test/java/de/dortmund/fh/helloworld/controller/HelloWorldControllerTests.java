package de.dortmund.fh.helloworld.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class HelloWorldControllerTests {

	@InjectMocks
	private HelloWorldController controller;

	@Test
	public void getHelloText() {
		ResponseEntity<String> helloText = controller.getMessage();
		assertNotNull(helloText);
		assertEquals(helloText.getBody().contains("Hello World!!"), true);
	}

}
