# Vyrtuous

Vyrtuous is a Java-based application designed to integrate local and remote AI tools.

## Project Description

This project aims to provide a unified interface for interacting with various AI models and services, including local LLMs and cloud-based APIs like OpenAI, OpenRouter, and LM Studio. It supports features like context management, approval workflows, and a REPL interface for experimentation.

## Key Features

*   **Integration with Multiple AI Tools:** Vyrtuous integrates with Discord and provides a framework for connecting to various AI models and services. Currently, the codebase demonstrates Discord integration. Support for AI models like OpenAI, LM Studio, and Ollama is planned for future development.
*   **Context Management:** Maintains and manages conversation context for more coherent interactions.
*   **Approval Workflows:** Implements approval mechanisms to control AI responses.
*   **REPL Interface:** Provides a Read-Eval-Print Loop (REPL) for interactive experimentation with AI tools.
*   **Modular Design:** Built with modularity in mind, allowing for easy extension and customization.

## Technologies Used

*   **Java:** The primary programming language.
*   **Discord API (JDA):** For bot integration (if applicable).
*   **Logging:** Utilizes Java's logging framework.
*   **Concurrent Data Structures:** `HashMap`, `ConcurrentHashMap`, etc., for efficient data management.
*   **Asynchronous Programming:** `CompletableFuture` for non-blocking operations.

## MCPServer Class Details

The `MCPServer` class is a core component of Vyrtuous, responsible for managing model contexts and providing a central interface for interacting with various AI tools. It acts as a server that handles requests and orchestrates the execution of different tools.

**Class Structure:**

*   `package com.brandongcobb.vyrtuous.utils.handlers;`:  This class resides within the `handlers` package of the `utils` module, indicating its role in handling requests and managing interactions.
*   `private static final Logger LOGGER = Logger.getLogger(MCPServer.class.getName());`:  A logger is used for debugging and monitoring purposes, providing valuable insights into the server's operation.
*   `private final ObjectMapper mapper = new ObjectMapper();`: The Jackson library's ObjectMapper is used for JSON serialization and deserialization, enabling the server to process structured data.
*   `private final Map<String, ToolWrapper> tools = new ConcurrentHashMap<>();`:  A concurrent hash map stores registered tools, allowing for safe concurrent access and efficient tool lookup.  The `ToolWrapper` class encapsulates the tool's name, description, input schema, and execution logic.
*   `private final ContextManager modelContextManager;` & `private final ContextManager userContextManager;`:  Context managers handle the management of model and user contexts, ensuring coherent interactions across multiple requests.
*   `private boolean initialized = false;`: A flag to track whether the server has been initialized.

**Registered Tools (Managed by MCPServer):**

The `MCPServer` registers and manages a suite of tools that provide a wide range of functionalities. These tools are essential for the Vyrtuous application's capabilities.

*   **`count_file_lines`:**  Counts the number of lines in a file.
*   **`create_file`:** Creates a new file with specified content.
*   **`find_in_file`:** Searches for specified terms within a file, providing context lines around matches.
*   **`load_context`:** Loads a previously saved context snapshot.
*   **`patch`:** Applies patches (replacements, insertions, deletions) to files.
*   **`read_file`:** Reads the contents of a file.
*   **`refresh_context`:** Summarizes the current context, providing a concise overview.
*   **`save_context`:** Saves the current context snapshot for later retrieval.
*   **`search_files`:** Searches for files matching specified criteria.
*   **`search_web`:**  Searches the web for information using the Google Programmable Search API.

**Dependencies:**

This project relies on the following key dependencies (listed in `pom.xml`):

*   Jackson Databind
*   Logging Framework (e.g., java.util.logging)
*   JDA (Java Discord API) - for Discord Integration

## Project Structure

*   `src/main/java/com/brandongcobb/vyrtuous/`: Main source code directory.
    *   `bots/`: Contains bot-related classes (e.g., `DiscordBot`).
    *   `records/`: Classes related to data records.
    *   `tools/`: Contains classes implementing various AI tools and utilities (e.g., `Patch`, `SaveContext`, `ToolResult`).
    *   `enums/`: Enumerations for configurations and states (e.g., `ModelRegistry`, `ApprovalMode`).
    *   `objects/`: Classes representing data containers and models (e.g., `LlamaContainer`).
    *   `utils/`: Utility classes and handlers (e.g., `Encryption`, `AIManager`, `MarkdownUtils`).
    *   `Vyrtuous.java`: The main entry point of the application.

## Setting Up the Project

This project is built using Maven.

**Prerequisites:**

*   Java 17 or higher installed and configured.
*   Maven installed and configured.

**Installation (Maven):**

1.  Clone the repository.
2.  Navigate to the project directory.
3.  Run `mvn clean install` to build the project and download dependencies.

**Configuration:**

1.  Configure API keys, model paths, and other settings in the appropriate configuration files (e.g., `.bashrc`). Refer to the project's documentation for specific details.

## Running Tests

The project includes a suite of tests to ensure functionality and stability.

**Running Tests (Maven):**

Run `mvn test` from the project root directory.

## Getting Started

1.  Clone the repository.
2.  Set up your environment with the necessary dependencies (check the `pom.xml` file).
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

*   **Context Management:** Persistent conversation history across sessions, enabling more natural and coherent AI dialogues. Context is stored securely and can be reviewed and modified by authorized users.
*   **Tool Integration:** A growing collection of tools including `patch`, `read_file`, `search_files`, `create_file`, `save_context`, `load_context`, and `refresh_context`. These tools provide fine-grained control over file system operations, context management, and more.
*   **REPL Improvements:** Enhanced REPL interface with features like code completion and syntax highlighting.

## Future Enhancements

*   Implement advanced context management features.
*   Expand support for additional AI tools and models.
*   Refactor codebase for improved scalability and maintainability.

## License

This project is licensed under the [GNU General Public License v3](https://www.gnu.org/licenses/gpl-3.0).
