# Vyrtuous

Vyrtuous is a Java-based application designed to integrate local and remote AI tools.

## Project Description

This project aims to provide a unified interface for interacting with various AI models and services, including local LLMs and cloud-based APIs like OpenAI, OpenRouter, and LM Studio. It supports features like context management, approval workflows, and a REPL interface for experimentation.

## Key Features

*   **Integration with Multiple AI Tools:** Vyrtuous can connect to various AI models and services (e.g., OpenAI, LM Studio, Ollama). 
*   **Context Management:**  Maintains and manages conversation context for more coherent interactions.
*   **Approval Workflows:** Implements approval mechanisms to control AI responses.
*   **REPL Interface:**  Provides a Read-Eval-Print Loop (REPL) for interactive experimentation with AI tools.
*   **Modular Design:** Built with modularity in mind, allowing for easy extension and customization.

## Technologies Used

*   **Java:** The primary programming language.
*   **Discord API (JDA):** For bot integration (if applicable).
*   **Logging:** Utilizes Java's logging framework.
*   **Concurrent Data Structures:** `HashMap`, `ConcurrentHashMap`, etc., for efficient data management.
*   **Asynchronous Programming:** `CompletableFuture` for non-blocking operations.

## Project Structure

*   `src/main/java/com/brandongcobb/vyrtuous/`:  Main source code directory.
    *   `bots/`: Contains bot-related classes (e.g., `DiscordBot`).
    *   `records/`: Classes related to data records.
    *   `tools/`: Contains classes implementing various AI tools and utilities (e.g., `Patch`, `SaveContext`, `ToolResult`).
    *   `enums/`:  Enumerations for configurations and states (e.g., `ModelRegistry`, `ApprovalMode`).
    *   `objects/`: Classes representing data containers and models (e.g., `LlamaContainer`).
    *   `utils/`:  Utility classes and handlers (e.g., `Encryption`, `AIManager`, `MarkdownUtils`).
    *   `Vyrtuous.java`: The main entry point of the application.

## Setting Up the Project

This project is built using [Maven/Gradle - *Please verify and update*].

**Prerequisites:**
*   Java 17 or higher installed and configured.
*   Maven or Gradle installed and configured.

**Installation (Maven):**
1.  Clone the repository.
2.  Navigate to the project directory.
3.  Run `mvn clean install` to build the project and download dependencies.

**Installation (Gradle):**
1.  Clone the repository.
2.  Navigate to the project directory.
3.  Run `./gradlew build` (or `gradlew.bat build` on Windows) to build the project and download dependencies.

**Configuration:**
1.  Configure API keys, model paths, and other settings in the appropriate configuration files (e.g., `application.properties`, `config.yml`).  Refer to the project's documentation for specific details.

## Running Tests

The project includes a suite of tests to ensure functionality and stability.

**Running Tests (Maven):**
Run `mvn test` from the project root directory.

**Running Tests (Gradle):**
Run `./gradlew test` (or `gradlew.bat test` on Windows) from the project root directory.

**Test Coverage:**
Aim for a minimum of 80% test coverage.  Use a code coverage tool (e.g., JaCoCo) to monitor coverage levels.
## Getting Started

1.  Clone the repository.
2.  Set up your environment with the necessary dependencies (likely managed via Maven or Gradle - check the `pom.xml` or `build.gradle` file).
3.  Configure the application with appropriate settings (API keys, model paths, etc.).
4.  Build and run the application.

## Contributing

Contributions are welcome! Please follow these guidelines:

1.  Fork the repository.
2.  Create a new branch for your feature or fix.
3.  Make your changes and write tests.
4.  Submit a pull request.

## Advanced Features

Vyrtuous now includes robust context management and a suite of tools for streamlining AI interactions:

*   **Context Management:** Persistent conversation history across sessions, enabling more natural and coherent AI dialogues.  Context is stored securely and can be reviewed and modified by authorized users.
*   **Tool Integration:**  A growing collection of tools including `patch`, `read_file`, `search_files`, `create_file`, `save_context`, `load_context`, and `refresh_context`.  These tools provide fine-grained control over file system operations, context management, and more.
*   **REPL Improvements:** Enhanced REPL interface with features like code completion and syntax highlighting.
## Future Enhancements

*   Implement advanced context management features.
*   Expand support for additional AI tools and models.
*   Refactor codebase for improved scalability and maintainability.
## License

This project is licensed under the [GNU General Public License v3](https://www.gnu.org/licenses/gpl-3.0).
