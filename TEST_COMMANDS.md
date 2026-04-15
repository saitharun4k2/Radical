# Quick Test Commands Reference for MiniUdemy

## Windows PowerShell Commands

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CourseServiceTest
mvn test -Dtest=UserRepositoryTest
mvn test -Dtest=ProfileControllerTest

# Run specific test method
mvn test -Dtest=CourseServiceTest#testGetCourseById_Success

# Run tests matching a pattern
mvn test -Dtest=*ServiceTest
mvn test -Dtest=*RepositoryTest

# Run tests with coverage report
mvn clean test jacoco:report

# Run only integration tests
mvn test -Dtest=*IntegrationTest

# Run tests with detailed output
mvn test -X

# View test report in browser
start target\surefire-reports\index.html

# View coverage report
start target\site\jacoco\index.html

# Clean and run tests
mvn clean test

# Run tests and skip build
mvn test -DskipBuild

# Run tests in parallel (speeds up execution)
mvn test -DthreadCount=4 -DthreadCountSuites=1

# Skip tests during build
mvn clean install -DskipTests

# Run tests from IDE shortcuts (IntelliJ IDEA):
# - Ctrl+Shift+F10  : Run focused test
# - Ctrl+Shift+F9   : Debug focused test
# - Right-click test file → Run Tests

## Test File Locations

Service Tests:
  - src/test/java/com/online/MiniUdemy/service/CourseServiceTest.java
  - src/test/java/com/online/MiniUdemy/service/EmailServiceTest.java
  - src/test/java/com/online/MiniUdemy/service/UserServiceTest.java

Controller Tests:
  - src/test/java/com/online/MiniUdemy/controller/AuthControllerTest.java
  - src/test/java/com/online/MiniUdemy/controller/ProfileControllerTest.java

Repository Tests:
  - src/test/java/com/online/MiniUdemy/repository/UserRepositoryTest.java
  - src/test/java/com/online/MiniUdemy/repository/CourseRepositoryTest.java

Integration Tests:
  - src/test/java/com/online/MiniUdemy/CourseServiceIntegrationTest.java
  - src/test/java/com/online/MiniUdemy/UserIntegrationTest.java

Exception Tests:
  - src/test/java/com/online/MiniUdemy/exception/GlobalExceptionHandlerTest.java

Utilities:
  - src/test/java/com/online/MiniUdemy/utils/TestDataBuilder.java
  - src/test/java/com/online/MiniUdemy/TestConfig.java

Test Resources:
  - src/test/resources/application.properties

## Most Common Commands

# Quick test run
mvn test

# Test a single class quickly
mvn test -Dtest=CourseServiceTest

# Full clean build with tests
mvn clean install

# Run tests without recompiling
mvn surefire:test

# Run tests in CI/CD pipeline
mvn clean test verify

# Generate test report
mvn test site:site

# Quick IDE test without Maven
# In IntelliJ: Right-click test class → Run 'ClassName'

