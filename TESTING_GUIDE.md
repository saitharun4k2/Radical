# Unit Testing Guide for MiniUdemy

## Overview
Comprehensive unit and integration testing framework has been implemented for the MiniUdemy application using:
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **Spring Boot Test** - Spring integration testing
- **AssertJ** - Fluent assertions
- **H2 Database** - In-memory test database

## Test Structure

### Directory Layout
```
src/test/java/com/online/MiniUdemy/
├── service/
│   ├── CourseServiceTest.java
│   ├── EmailServiceTest.java
│   └── UserServiceTest.java
├── controller/
│   ├── AuthControllerTest.java
│   └── ProfileControllerTest.java
├── repository/
│   ├── UserRepositoryTest.java
│   └── CourseRepositoryTest.java
├── CourseServiceIntegrationTest.java
├── UserIntegrationTest.java
├── TestConfig.java
└── utils/
    └── TestDataBuilder.java
```

## Test Types

### 1. Unit Tests (Service & Controller Tests)
Use Mockito to mock dependencies and test business logic in isolation.

**File:** `src/test/java/com/online/MiniUdemy/service/CourseServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {
    @Mock
    private CourseRepository courseRepository;
    
    @InjectMocks
    private CourseService courseService;
    
    @Test
    void testGetCourseById_Success() {
        // Test implementation
    }
}
```

### 2. Repository Tests
Test database queries with real database schema using `@DataJpaTest`.

**File:** `src/test/java/com/online/MiniUdemy/repository/UserRepositoryTest.java`

```java
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void testFindByEmail_Success() {
        // Test implementation
    }
}
```

### 3. Controller Tests
Test web layer with `@WebMvcTest` for isolated controller testing.

**File:** `src/test/java/com/online/MiniUdemy/controller/ProfileControllerTest.java`

```java
@WebMvcTest(ProfileController.class)
class ProfileControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testShowProfile_Authenticated() throws Exception {
        // Test implementation
    }
}
```

### 4. Integration Tests
Test complete workflows with `@SpringBootTest` and real beans.

**File:** `src/test/java/com/online/MiniUdemy/CourseServiceIntegrationTest.java`

```java
@SpringBootTest
@Transactional
class CourseServiceIntegrationTest {
    @Autowired
    private CourseService courseService;
    
    @Test
    void testEnrollmentFlow() {
        // Test complete enrollment flow
    }
}
```

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=CourseServiceTest
```

### Run Specific Test Method
```bash
mvn test -Dtest=CourseServiceTest#testGetCourseById_Success
```

### Run Tests with Coverage
```bash
mvn test jacoco:report
```

### Run Tests in IDE
- Right-click on test class → Run as JUnit Test
- Right-click on test method → Run as JUnit Test
- Use keyboard shortcut (usually Ctrl+Shift+F10 in IntelliJ)

## Test Utilities

### TestDataBuilder
Helper class for creating consistent test data.

```java
import com.online.MiniUdemy.utils.TestDataBuilder;

// Create test users
User student = TestDataBuilder.buildStudent("student@test.com", "John Student");
User instructor = TestDataBuilder.buildInstructor("instructor@test.com", "Jane Instructor");

// Create test courses
Course course = TestDataBuilder.buildCourse("Java Basics", "Learn Java", instructor);

// Password utilities
String encoded = TestDataBuilder.encodePassword("password123");
boolean matches = TestDataBuilder.passwordMatches("password123", encoded);
```

## Common Testing Patterns

### 1. Mocking Repository Calls
```java
@Mock
private CourseRepository courseRepository;

@Test
void testGetCourseById_Success() {
    when(courseRepository.findById(1L)).thenReturn(Optional.of(course));
    
    Course result = courseService.getCourseById(1L);
    
    assertThat(result).isNotNull();
    verify(courseRepository, times(1)).findById(1L);
}
```

### 2. Testing Exceptions
```java
@Test
void testGetCourseById_NotFound() {
    when(courseRepository.findById(anyLong())).thenReturn(Optional.empty());
    
    assertThatThrownBy(() -> courseService.getCourseById(999L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid course Id");
}
```

### 3. Testing with MockMvc
```java
@Test
@WithMockUser(username = "john@test.com")
void testShowProfile_Authenticated() throws Exception {
    mockMvc.perform(get("/profile"))
            .andExpect(status().isOk())
            .andExpect(view().name("profile"))
            .andExpect(model().attributeExists("user"));
}
```

### 4. Integration Testing with Real Data
```java
@Test
@Transactional
void testEnrollmentFlow() {
    // Create real entities
    courseService.saveCourse(course, instructor);
    
    // Perform actions
    courseService.enrollStudent(course.getId(), student);
    
    // Verify results with real database
    User updatedStudent = userRepository.findByEmail("student@test.com").get();
    assertThat(updatedStudent.getEnrolledCourses()).contains(course);
}
```

## Best Practices

### 1. Test Naming Convention
Use descriptive names that explain what is being tested:
```java
@Test
void testGetCourseById_Success()
void testGetCourseById_NotFound()
void testSaveCourse_WithNullInstructor_ThrowsException()
```

### 2. Arrange-Act-Assert Pattern
```java
@Test
void testExample() {
    // Arrange - Set up test data
    User student = TestDataBuilder.buildStudent("test@test.com", "Test");
    
    // Act - Perform the action
    User saved = userRepository.save(student);
    
    // Assert - Verify results
    assertThat(saved.getId()).isNotNull();
}
```

### 3. Use Fluent Assertions (AssertJ)
```java
// Good
assertThat(result).isNotNull()
    .hasFieldOrPropertyWithValue("title", "Java Basics")
    .hasFieldOrPropertyWithValue("id", 1L);

// Avoid
assertTrue(result != null);
assertEquals(result.getTitle(), "Java Basics");
```

### 4. Mock External Dependencies Only
- Mock repositories, external services, and mail senders
- Don't mock the class under test
- Test with real objects when possible

### 5. Use @Transactional for Integration Tests
Prevents test data from persisting between test runs:
```java
@SpringBootTest
@Transactional
class IntegrationTest {
    // Test methods here
}
```

## Testing Checklist

- [ ] Create unit tests for all service methods
- [ ] Create unit tests for all controller endpoints
- [ ] Create repository tests for custom queries
- [ ] Create integration tests for complex workflows
- [ ] Test both success and failure scenarios
- [ ] Test edge cases (null values, empty lists, etc.)
- [ ] Use meaningful assertion messages
- [ ] Keep tests isolated and independent
- [ ] Clean up test data with @BeforeEach or @Transactional
- [ ] Document complex test scenarios

## Current Test Coverage

### Service Tests
- ✅ CourseService (7 tests)
- ✅ EmailService (3 tests)
- ✅ UserService (3 tests)

### Controller Tests
- ✅ AuthController (5 tests)
- ✅ ProfileController (4 tests)

### Repository Tests
- ✅ UserRepository (6 tests)
- ✅ CourseRepository (8 tests)

### Integration Tests
- ✅ CourseServiceIntegrationTest (5 tests)
- ✅ UserIntegrationTest (5 tests)

**Total: 46 tests**

## Future Test Additions

Consider adding tests for:
1. InstructorCourseController
2. StudentController
3. AdminController
4. CommentService
5. CategoryService
6. InstructorApplicationService
7. Security/Authentication flows
8. Error handling and exception scenarios
9. Edge cases and boundary conditions

## Troubleshooting

### Issue: Tests fail with "No mock bean available"
**Solution:** Ensure you're using `@Mock` for dependencies and `@InjectMocks` for the class under test.

### Issue: Database state carries between tests
**Solution:** Add `@Transactional` to integration tests or use `@BeforeEach` to clean up.

### Issue: MockMvc returns 401 Unauthorized
**Solution:** Add `@WithMockUser` annotation to test methods that require authentication.

### Issue: Tests pass individually but fail when run together
**Solution:** Check for shared state. Use `@BeforeEach` or `@Transactional` to ensure test isolation.

## References

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [AssertJ Assertions](https://assertj.github.io/assertj-core-features-highlight.html)

