# Vyrtuous Project README

## Project Overview

Vyrtuous is a project designed to integrate local and remote AI tools, providing a unified interface for interacting with various AI models and services.  It leverages Discord as a primary interaction platform, utilizing bots and cogs to manage commands and events.

## Project Structure

The project is organized under the `com.brandongcobb.vyrtuous` package. Key subpackages include:

*   `bots`: Contains the DiscordBot class, responsible for managing the Discord API and bot interactions.
*   `cogs`:  Houses cogs (extensions) that provide modular functionality and commands.
*   `utils`:  A utility package containing various helper classes and modules.
*   `utils.inc`: Includes configuration files and helper functions.
*   `utils.handlers`: Contains handler classes that manage AI interactions, tool management, and other core functionalities.

## Main Components

*   **Bots:** The `DiscordBot` class manages the Discord connection and handles bot-related tasks.
*   **Cogs:**  Modular extensions that provide specific functionalities, such as `HybridCommands` and event listeners (e.g., `EventListeners`).
*   **Handlers:**  Classes within the `utils.handlers` package manage different aspects of AI interaction, including:
    *   `AIManager`:  Centralized AI management.
    *   `ToolHandler`:  Tool management and execution.
    *   `OpenAIUtils`:  Utilities for interacting with OpenAI APIs.
    *   `LlamaContainer`: Container for Llama models.
    *   `MessageManager`: Handles message processing and routing.

## Entry Flow

1.  The application starts with the `Vyrtuous.java` entry point.
2.  The `DiscordBot` initializes the Discord connection.
3.  Cogs are loaded and registered with the bot.
4.  User commands are processed by the appropriate cogs and handlers.
5.  AI interactions are managed by the `AIManager` and related handler classes.

## Dependencies

*   JDA (Java Discord API) for Discord integration.
*   Various AI model libraries (e.g., OpenAI, Llama) - specific dependencies will be detailed in the project's build file.

## Development Notes

*   The project utilizes a modular design with cogs for easy extensibility.
*   Error handling and logging should be implemented to ensure robustness.
*   Consider adding more comprehensive documentation for each component."
