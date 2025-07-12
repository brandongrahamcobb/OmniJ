# Vyrtuous

Vyrtuous is a Java-based application designed to integrate local and remote AI tools.

## Project Flow
![jVyrtuous](pictures/jVyrtuous.svg)

## Project Description

This project aims to provide a unified interface for interacting with various AI models and services, including local LLMs and cloud-based APIs like OpenAI, OpenRouter, and LM Studio. It supports features like context management, approval workflows, and a REPL interface for experimentation.

## Key Features

*   **Integration with Multiple AI Tools:** Vyrtuous integrates with Discord and provides a framework for connecting to various AI models and services. Currently, the codebase demonstrates Discord integration. 
*   **Context Management:** Maintains and manages conversation context for more coherent interactions.
*   **Approval Workflows:** Implements approval mechanisms to control AI responses.
*   **REPL Interface:** Provides a Read-Eval-Print Loop (REPL) for interactive experimentation with AI tools.
*   **Modular Design:** Built with modularity in mind, allowing for easy extension and customization.

## Technologies Used

*   **Asynchronous Programming:** `CompletableFuture` for non-blocking operations.
*   **Discord API (JDA):** For bot integration.
*   **Java:** The primary programming language.
*   **Logging:** Utilizes Java's logging framework.
*   **Maven:** The primary package manager (Gradle support upcoming).
*   **[Model Context Protocol Server](https://modelcontextprotocol.io/introduction):** A backend component to reveal context to the LLM's. 
*   **Spring-Boot-AI:** Autowires beans, component, services and the main SpringBootApplication.

## Setting Up the Project

This project is built using Maven.

**Prerequisites:**

*   Java 17 or higher installed and configured.
*   Maven installed and configured.

**Installation (Maven):**


1.  Clone the repository.
2.  Run `mvn clean install` to build the project and download dependencies.
3.  Copy Vyrtuous-0.1.jar to the root directory of the project as Vyrtuous.jar.
4.  Configure the application with appropriate settings in .env and save it to ~/.env or add the fields to your .env file.
5. **Linux:** docker build --platform=linux/arm64/v8 -t vyrtuous .
5. **Mac:** docker buildx build --platform=linux/arm64/v8 -t vyrtuous --load .
6. docker run -it --env-file ~/.env --rm vyrtuous

**Configuration:**

1.  Configure API keys, model paths, and other settings in the appropriate configuration files (e.g., `.bashrc`). Refer to the project's documentation for specific details.

## Description 
**Dependencies:**

This project relies on the following key dependencies (listed in `pom.xml`):

*   Apache Http Client
*   Jackson Databind
*   JDA
*   [Metadata](https://github.com/brandongrahamcobb/Metadata)
*   Spring-Boot

** Package Structure:**

*   `package com.brandongcobb.omnij.service;`:  This package is for services, indicating their role in servicing requests from other parts of the code.
*   `package com.brandongcobb.omnij.objects`:  This package is for objects, indicating their role in serializing data from AIService.
*   `package com.brandongcobb.vytuous.tools`: This package is for tools, self explanatory.
*   `package com.brandongcobb.omnij.utils.handlers`: This package is for utilities accessing the serialized data in the program's objects.
*   `package com.brandongcobb.omnij.utils.inc`: This package is for information which doesn't have a better place.
*   `package com.brandongcobb.omnij.record`: This package is for recording model and tool statistics.
*   `package com.brandongcobb.omnij.cogs`: This package has two functions and are registered under DiscordBot; one exposes commands to Discord and the other exposes a message listener.
*   `package com.brandongcobb.omnij.enums`: This package is critical to the function of the program and also pose a vulnerability returning unserializable data from the models.
*   `package com.brandongcobb.omnij.domain`: This package formalizes the connection between JSON tool schemas and their programatic fields.
*   `package com.brandongcobb.omnij.component`: This package contains the Discord bot and the [Model Context Protocol Server](https://modelcontextprotocol.io/introduction).

**[Model Context Protocol Server](https://modelcontextprotocol.io/introduction)**

The server is a core component of Vyrtuous, responsible for managing model contexts and providing a central interface for interacting with various AI tools.
It acts as a server that handles requests and orchestrates the execution of different tools.

*   **`count_file_lines`:**  Counts the number of lines in a file.
*   **`create_file`:** Creates a new file with specified content.
*   **`find_in_file`:** Searches for specified terms within a file, providing context lines around matches.
*   **`patch`:** Applies patches (replacements, insertions, deletions) to files.
*   **`read_file`:** Reads the contents of a file.
*   **`search_files`:** Searches for files matching specified criteria.
*   **`search_web`:**  Searches the web for information using the Google Programmable Search API.

## Advanced Features

Vyrtuous now includes a [Model Context Protocol Server](https://modelcontextprotocol.io/introduction) and cloud access to OpenAI:

*   **OpenAI:** The leader in global AI can be accessed with a single API key and personal funding.
*   **[Model Context Protocol Server](https://modelcontextprotocol.io/introduction):** The opportunities are vast and the tools only reflect a small portion of useful actions for agentic capabilities. 
*   **Discord:** Local LLM's with streaming is highly recommended.

## Future Enhancements

*   Further implementation of Spring-Boot AI
*   Native support for tool calling in local models.
*   Refactor codebase for improved scalability and maintainability.

## License

This project is licensed under the [GNU General Public License v3](https://www.gnu.org/licenses/gpl-3.0).
