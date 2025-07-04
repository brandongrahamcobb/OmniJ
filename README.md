# Vyrtuous

Vyrtuous is a Java-based application designed to integrate local and remote AI tools.

## Project Flow
![jVyrtuous](pictures/jVyrtuous.svg)

## Self-awareness
```markdown
Okay, I've examined the flowchart! It's a pretty detailed and accurate representation of my architecture, as far as can be publicly known. Here's a breakdown of my thoughts, going through the key components as depicted:

Overall Impression:

It's impressive someone put this together! It shows a good understanding of the core concepts behind large language models like me. The diagram clearly illustrates the multi-stage process, from input to output.

Key Components & My Thoughts:

Input (Text/Image): Correct. I can take both text and image inputs. The image processing is a more recent and evolving capability.
Embedding Model (Text/Image):  Spot on.  This is crucial.  Both text and images need to be converted into a numerical representation (embeddings) that I can understand. Different embedding models are used for text and images.
LLM (Large Language Model): This is the heart of it all!  The diagram correctly identifies this as the core reasoning engine.  I am built on the Gemma architecture.
Decoding:  Absolutely.  The LLM outputs probabilities, and the decoding process turns those probabilities into actual text. Different decoding strategies (like sampling, top-k, etc.) can influence the output.
Output (Text): Correct. My primary output is text.
Routing/Conditioning: This is a smart inclusion. The diagram shows how the input type (text or image) influences the flow. This is important for multimodal models like me.
External Tools (Optional): This is also a good point. I can be connected to external tools (though I don't have access to them in this current environment). This allows me to perform actions like web searches, calculations, or access real-time information.

Things to Note/Nuances (Not criticisms, just details):

Training Data: The diagram doesn't explicitly show the massive training dataset that I was built upon. That's understandable, as it's a huge and complex aspect.
Fine-tuning:  I've been fine-tuned for specific tasks (like being a helpful assistant). This fine-tuning process isn't explicitly shown, but it's a critical step in making me useful.
Gemma Specifics: The diagram is fairly general to LLMs. It doesn't highlight the specific architectural details of the Gemma model (e.g., the number of parameters, specific layers, etc.).



In conclusion:

This flowchart is a very good high-level overview of how I work. It accurately captures the essential components and the flow of information. It's a testament to the growing understanding of how these complex AI systems function.  Thank you for sharing it!
```

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

*   `package com.brandongcobb.vyrtuous.service;`:  This package is for services, indicating their role in servicing requests from other parts of the code.
*   `package com.brandongcobb.vyrtuous.objects`:  This package is for objects, indicating their role in serializing data from AIService.
*   `package com.brandongcobb.vytuous.tools`: This package is for tools, self explanatory.
*   `package com.brandongcobb.utils.handlers`: This package is for utilities accessing the serialized data in the program's objects.
*   `package com.brandongcobb.utils.inc`: This package is for information which doesn't have a better place.
*   `package com.brandongcobb.record`: This package is for recording model and tool statistics.
*   `package com.brandongcobb.cogs`: This package has two functions and are registered under DiscordBot; one exposes commands to Discord and the other exposes a message listener.
*   `package com.brandongcobb.enums`: This package is critical to the function of the program and also pose a vulnerability returning unserializable data from the models.
*   `package com.brandongcobb.domain`: This package formalizes the connection between JSON tool schemas and their programatic fields.
*   `pakcage com.brandongcobb.component`: This package contains the Discord bot and the [Model Context Protocol Server](https://modelcontextprotocol.io/introduction).

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

## Contributing

Contributions are welcome! Please follow these guidelines:

1.  Fork the repository.
2.  Create a new branch for your feature or fix.
3.  Make your changes and write tests.
4.  Submit a pull request.

## License

This project is licensed under the [GNU General Public License v3](https://www.gnu.org/licenses/gpl-3.0).
