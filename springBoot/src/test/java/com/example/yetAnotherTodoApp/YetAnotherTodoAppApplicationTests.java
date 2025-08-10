package com.example.yetAnotherTodoApp;

import com.example.yetAnotherTodoApp.models.Todo;
import com.example.yetAnotherTodoApp.models.TodoUpdateRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanData.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class YetAnotherTodoAppApplicationTests {
	@Autowired
	TestRestTemplate restTemplate;

	static DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

	@Test
	public void shouldCreateNewTodo() {
		String todoTitle = "Go to sleep by 20 pm";
		Todo newTodo = new Todo();
		newTodo.setTitle(todoTitle);

		ResponseEntity<Todo> createdTodo = restTemplate.postForEntity("/todos", newTodo, Todo.class);
		assertEquals(HttpStatus.CREATED, createdTodo.getStatusCode());
		assertNotNull(createdTodo.getBody());
		checkTodoField(createdTodo.getBody(), todoTitle);

		URI createdResource = createdTodo.getHeaders().getLocation();
		ResponseEntity<Todo> createdTodoByLocationHeader = restTemplate.getForEntity(createdResource, Todo.class);
		assertEquals(HttpStatus.OK, createdTodoByLocationHeader.getStatusCode());
		assertNotNull(createdTodoByLocationHeader.getBody());
		checkTodoField(createdTodoByLocationHeader.getBody(), todoTitle);
	}

	static private void checkTodoField(Todo todo, String expectedTitle) {
		assertNotNull(todo.getId());
		assertEquals(expectedTitle, todo.getTitle());
		assertEquals(false, todo.getCompleted());
		assertNotNull(todo.getCreationDate());
		assertNotNull(todo.getModificationDate());
	}

	@Test
	public void shouldNotCreateTodoWithoutTitle() {
		Todo todoToCreate = new Todo();

		ResponseEntity<Map<String, Object>> createdTodoResponse = restTemplate.exchange("/todos", HttpMethod.POST, new HttpEntity<>(todoToCreate), new ParameterizedTypeReference<Map<String, Object>>() {});
		System.out.println(createdTodoResponse.getBody());
		assertEquals(HttpStatus.BAD_REQUEST, createdTodoResponse.getStatusCode());
        assertNotNull(createdTodoResponse.getBody());
        assertEquals("TodoUnexpectedSyntax", createdTodoResponse.getBody().get("error"));
	}

	@Test
	public void shouldNotCreateTodoWithDuplicatedTitle() {
		Todo todoToCreate = new Todo();
		todoToCreate.setTitle("Buy bread");

		ResponseEntity<Map<String, Object>> createdTodoResponse = restTemplate.exchange("/todos", HttpMethod.POST, new HttpEntity<>(todoToCreate), new ParameterizedTypeReference<Map<String, Object>>() {});
		assertEquals(HttpStatus.CONFLICT, createdTodoResponse.getStatusCode());
		assertNotNull(createdTodoResponse.getBody());
		assertEquals("TodoAlreadyExistsException", createdTodoResponse.getBody().get("error"));
	}

	@Test
	public void shouldGetAllTodos() {
		List<Todo> expectedTodos = List.of(
			new Todo(
				1,
				"Buy bread",
				false,
				LocalDateTime.of(2025, 8, 6, 12, 0, 0),
				LocalDateTime.of(2025, 8, 6, 12, 0, 0)
			),
			new Todo(
				2,
				"Repair chair",
				true,
				LocalDateTime.of(2025, 8, 7, 14, 0, 0),
				LocalDateTime.of(2025, 8, 7, 10, 0, 0)
			),
			new Todo(
				3,
				"Call a friend",
				false,
				LocalDateTime.of(2025, 8, 8, 15, 0, 0),
				LocalDateTime.of(2025, 8, 8, 15, 0, 0)
			),
			new Todo(
				4,
				"Tidy up my room",
				true,
				LocalDateTime.of(2025, 8, 9, 9, 0, 0),
				LocalDateTime.of(2025, 8, 8, 23, 0, 0)
			),
			new Todo(
				5,
				"Dishes",
				false,
				LocalDateTime.of(2025, 8, 10, 8, 0, 0),
				LocalDateTime.of(2025, 8, 10, 8, 0, 0)
			)
		);

		ResponseEntity<List<Todo>> todosList = restTemplate.exchange("/todos", HttpMethod.GET, null, new ParameterizedTypeReference<List<Todo>>() {});

		assertEquals(HttpStatus.OK, todosList.getStatusCode());
        assertNotNull(todosList.getBody());
        assertEquals(expectedTodos.size(), todosList.getBody().size());

		for (int i = 0; i < todosList.getBody().size(); i++) {
			assertEquals(expectedTodos.get(i), todosList.getBody().get(i));
		}
	}

	@Test
	public void shouldGetSingleTodo() {
		LocalDateTime time = LocalDateTime.of(2025, 8, 6, 12, 0, 0);
		Todo expectedTodo = new Todo(1, "Buy bread", false, time, time);

		ResponseEntity<Todo> todoResponse = restTemplate.getForEntity("/todos/{id}", Todo.class, expectedTodo.getId());

		assertEquals(HttpStatus.OK, todoResponse.getStatusCode());
		assertNotNull(todoResponse.getBody());

		Todo fetchedTodo = todoResponse.getBody();
		assertEquals(expectedTodo.getId(), fetchedTodo.getId());
		assertEquals(expectedTodo.getTitle(), fetchedTodo.getTitle());
		assertEquals(expectedTodo.getCompleted(), fetchedTodo.getCompleted());
		assertEquals(expectedTodo.getCreationDate().format(dateTimeFormat), fetchedTodo.getCreationDate().format(dateTimeFormat));
		assertEquals(expectedTodo.getModificationDate().format(dateTimeFormat), fetchedTodo.getModificationDate().format(dateTimeFormat));
	}

	@Test
	public void shouldNotGetASingleTodoWithWrongIndex() {
		ResponseEntity<Map<String, Object>> todoResponse = restTemplate.exchange("/todos/{id}", HttpMethod.GET, null, new ParameterizedTypeReference<>() {}, 9999);

		assertEquals(HttpStatus.NOT_FOUND, todoResponse.getStatusCode());
		assertNotNull(todoResponse.getBody());
		assertEquals("TodoNotFoundException", todoResponse.getBody().get("error"));
	}

	@Test
	public void shouldUpdateTodoWithTitleAndStatus() {
		Todo originalTodo = restTemplate.getForObject("/todos/{id}", Todo.class, 4);
		assertNotNull(originalTodo);

		TodoUpdateRequest todoUpdateRequest = new TodoUpdateRequest("Don't play video games", true);
		HttpEntity<TodoUpdateRequest> request = new HttpEntity<>(todoUpdateRequest);
		ResponseEntity<Todo> todoResponse = restTemplate.exchange("/todos/{id}", HttpMethod.PUT, request, Todo.class, originalTodo.getId());

		assertEquals(HttpStatus.OK, todoResponse.getStatusCode());
		assertNotNull(todoResponse.getBody());
		checkTodoFieldsAfterUpdate(todoResponse.getBody(), originalTodo);
		assertEquals(todoUpdateRequest.getTitle(), todoResponse.getBody().getTitle());
		assertEquals(todoUpdateRequest.getCompleted(), todoResponse.getBody().getCompleted());

		// Check if the todo was really updated in the database
		ResponseEntity<Todo> fetchedTodo = restTemplate.getForEntity("/todos/{id}", Todo.class, originalTodo.getId());
		assertEquals(HttpStatus.OK, fetchedTodo.getStatusCode());
		assertNotNull(fetchedTodo.getBody());
		checkTodoFieldsAfterUpdate(fetchedTodo.getBody(), originalTodo);
		assertEquals(todoUpdateRequest.getTitle(), fetchedTodo.getBody().getTitle());
		assertEquals(todoUpdateRequest.getCompleted(), fetchedTodo.getBody().getCompleted());
	}

	@Test
	public void shouldUpdateTodoWithTitle() {
		Todo originalTodo = restTemplate.getForObject("/todos/{id}", Todo.class, 2);
		assertNotNull(originalTodo);

		TodoUpdateRequest todoUpdateRequest = new TodoUpdateRequest("Go to the gym");
		HttpEntity<TodoUpdateRequest> request = new HttpEntity<>(todoUpdateRequest);
		ResponseEntity<Todo> todoResponse = restTemplate.exchange("/todos/{id}", HttpMethod.PUT, request, Todo.class, originalTodo.getId());

		assertEquals(HttpStatus.OK, todoResponse.getStatusCode());
		assertNotNull(todoResponse.getBody());
		checkTodoFieldsAfterUpdate(todoResponse.getBody(), originalTodo);
		assertEquals(todoUpdateRequest.getTitle(), todoResponse.getBody().getTitle());
		assertEquals(originalTodo.getCompleted(), todoResponse.getBody().getCompleted());

		// Check if the todo was really updated in the database
		ResponseEntity<Todo> fetchedTodo = restTemplate.getForEntity("/todos/{id}", Todo.class, originalTodo.getId());
		assertEquals(HttpStatus.OK, fetchedTodo.getStatusCode());
		assertNotNull(fetchedTodo.getBody());
		checkTodoFieldsAfterUpdate(fetchedTodo.getBody(), originalTodo);
		assertEquals(todoUpdateRequest.getTitle(), fetchedTodo.getBody().getTitle());
		assertEquals(originalTodo.getCompleted(), fetchedTodo.getBody().getCompleted());
	}

	@Test
	public void shouldUpdateTodoWithStatus() {
		Todo originalTodo = restTemplate.getForObject("/todos/{id}", Todo.class, 4);
		assertNotNull(originalTodo);

		TodoUpdateRequest todoUpdateRequest = new TodoUpdateRequest(false);
		HttpEntity<TodoUpdateRequest> request = new HttpEntity<>(todoUpdateRequest);
		ResponseEntity<Todo> todoResponse = restTemplate.exchange("/todos/{id}", HttpMethod.PUT, request, Todo.class, originalTodo.getId());

		assertEquals(HttpStatus.OK, todoResponse.getStatusCode());
		assertNotNull(todoResponse.getBody());
		checkTodoFieldsAfterUpdate(todoResponse.getBody(), originalTodo);
		assertEquals(originalTodo.getTitle(), todoResponse.getBody().getTitle());
		assertEquals(todoUpdateRequest.getCompleted(), todoResponse.getBody().getCompleted());

		// Check if the todo was really updated in the database
		ResponseEntity<Todo> fetchedTodo = restTemplate.getForEntity("/todos/{id}", Todo.class, originalTodo.getId());
		assertEquals(HttpStatus.OK, fetchedTodo.getStatusCode());
		assertNotNull(fetchedTodo.getBody());
		checkTodoFieldsAfterUpdate(fetchedTodo.getBody(), originalTodo);
		assertEquals(originalTodo.getTitle(), fetchedTodo.getBody().getTitle());
		assertEquals(todoUpdateRequest.getCompleted(), fetchedTodo.getBody().getCompleted());
	}

	@Test
	public void shouldNotUpdateTodoWithInvalidProperties() {
		Todo originalTodo = restTemplate.getForObject("/todos/{id}", Todo.class, 1);

		ResponseEntity<Map<String, Object>> responseWithoutRequest = restTemplate.exchange("/todos/{id}", HttpMethod.PUT, null, new ParameterizedTypeReference<>() {}, originalTodo.getId());
		assertEquals(HttpStatus.BAD_REQUEST, responseWithoutRequest.getStatusCode());

		TodoUpdateRequest todoUpdateRequest = new TodoUpdateRequest("");
		HttpEntity<TodoUpdateRequest> request = new HttpEntity<>(todoUpdateRequest);
		ResponseEntity<Map<String, Object>> responseWithEmptyText = restTemplate.exchange("/todos/{id}", HttpMethod.PUT, request, new ParameterizedTypeReference<Map<String, Object>>() {}, originalTodo.getId());
		assertEquals(HttpStatus.BAD_REQUEST, responseWithEmptyText.getStatusCode());
		assertNotNull(responseWithoutRequest.getBody());
		assertEquals("TodoUnexpectedSyntax", responseWithEmptyText.getBody().get("error"));
	}

	@Test
	public void shouldNotUpdateTodoIfTitleIsDuplicated() {
		Todo originalTodo = restTemplate.getForObject("/todos/{id}", Todo.class, 2);

		TodoUpdateRequest todoUpdateRequest = new TodoUpdateRequest("Buy bread");
		HttpEntity<TodoUpdateRequest> request = new HttpEntity<>(todoUpdateRequest);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange("/todos/{id}", HttpMethod.PUT, request, new ParameterizedTypeReference<Map<String, Object>>() {}, 2);
		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("TodoAlreadyExistsException", response.getBody().get("error"));

		Todo shouldBeTheSameTodo = restTemplate.getForObject("/todos/{id}", Todo.class, 2);
		assertEquals(originalTodo.getId(), shouldBeTheSameTodo.getId());
		assertEquals(originalTodo.getTitle(), shouldBeTheSameTodo.getTitle());
		assertEquals(originalTodo.getCompleted(), shouldBeTheSameTodo.getCompleted());
		assertEquals(originalTodo.getModificationDate(), shouldBeTheSameTodo.getModificationDate());
		assertEquals(originalTodo.getCreationDate(), shouldBeTheSameTodo.getCreationDate());
	}


	private void checkTodoFieldsAfterUpdate(Todo todoToCheck, Todo originalTodo) {
		assertNotNull(todoToCheck);
		assertEquals(originalTodo.getId(), todoToCheck.getId());
		assertNotEquals(originalTodo.getModificationDate().format(dateTimeFormat), todoToCheck.getModificationDate().format(dateTimeFormat));
		assertEquals(originalTodo.getCreationDate().format(dateTimeFormat), todoToCheck.getCreationDate().format(dateTimeFormat));
	}

	@Test
	public void shouldRemoveTodo() {
		long todoId = 5;

		ResponseEntity<Void> response = restTemplate.exchange("/todos/{id}", HttpMethod.DELETE, null, Void.class, todoId);
		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		assertNull(response.getBody());

		ResponseEntity<Todo> getRemovedTodo = restTemplate.getForEntity("/todos/{id}", Todo.class, todoId);
		assertEquals(HttpStatus.NOT_FOUND, getRemovedTodo.getStatusCode());
	}

	@Test
	public void shouldNotRemoveTodoWithWrongIndex() {
		long todoId = 9999;

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange("/todos/{id}", HttpMethod.DELETE, null, new ParameterizedTypeReference<>() {}, todoId);
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("TodoNotFoundException", response.getBody().get("error"));
	}
}
