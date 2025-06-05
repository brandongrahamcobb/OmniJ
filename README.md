Here's a cleaned-up and **merged README** for your **Vyrtuous** project, with all escape slashes removed and the content integrated logically:

---

# Vyrtuous

**Vyrtuous** is a modular, Java-based platform for integrating and managing local and remote AI tools—complete with Discord bot integration (via JDA), REPL support, and extensive extensibility through cogs, tools, and models. Whether you're experimenting with LLMs, building a custom toolchain, or deploying in a multi-user environment, Vyrtuous provides a solid base.

---

## Table of Contents

* [Project Structure & Scope](#project-structure--scope)
* [Extending/Manipulating the Project](#extendingmanipulating-the-project)
* [Prerequisites](#prerequisites)
* [Obtaining API Keys](#obtaining-api-keys)
* [Cloning the Repository](#cloning-the-repository)
* [Building the Project](#building-the-project)
* [Running the Application](#running-the-application)
* [Setting Up Docker](#setting-up-docker)
* [Notes](#notes)
* [License](#license)

---

## Project Structure & Scope

* **Core Integration**:
  `Vyrtuous.java` orchestrates the platform, integrating local/remote AI tools through a central extensible API. The `AIManager` mediates ML model requests and tools.

* **Discord Bot**:
  Managed via `DiscordBot.java`, handling the lifecycle and API integration for Discord. Supports both slash and text commands via cogs.

* **Cogs / Extensions**:

  * `Cog.java`: Registers and loads bot extensions.
  * `HybridCommands.java`: Sample cog with slash + text commands.
  * `EventListeners.java`: Hooks into event triggers from Discord or other platforms.

* **Model & Context Handling**:

  * `ModelInfo.java`, `ModelRegistry.java`: Define/query model metadata. Add new models by extending these.
  * `ContextEntry.java`, `ContextManager.java`: Maintain conversation context for users and models.
  * `StructuredOutput.java`, `SchemaMerger.java`: For handling JSON or schema-based outputs.

* **Tool Management**:

  * `ToolHandler.java`, `ToolUtils.java`, `ToolContainer.java`: Support for managing pluggable tools.

* **LLM Integration**:

  * `LlamaContainer.java`, `OllamaContainer.java`, `OpenAIContainer.java`: Integrate AI endpoints.
  * Corresponding utility classes (e.g., `OpenAIUtils.java`) streamline usage.

* **Moderation & Messaging**:

  * `ModerationManager.java`: Filters for explicit content.
  * `MessageManager.java`: Unified output handler, e.g., for Discord.

* **REPL & Utilities**:

  * `REPLManager.java`: CLI-based interface for automation/testing.
  * Misc helpers: `Helpers.java`, `Maps.java`, `EnvironmentPaths.java`.

---

## Extending/Manipulating the Project

* **Add new models**:
  Implement new `ModelInfo` records and register them in `ModelRegistry`.

* **Add commands**:
  Create cogs under `cogs/` and register them via `Cog.java`.

* **Integrate new endpoints**:
  Extend `*Container.java` classes as templates for new APIs.

* **Add tools**:
  Use `ToolHandler` and `ToolUtils` as base structures.

---

## Prerequisites

Ensure the following are installed:

* [Java JDK 11+](https://jdk.java.net/)
* [Maven](https://maven.apache.org/)
* [Docker](https://www.docker.com/get-started)

---

## Obtaining API Keys

### OpenAI

1. Go to [OpenAI API Keys](https://platform.openai.com/api-keys)
2. Sign in and generate a key.
3. Save it for later use.

### Discord

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications)
2. Create or select an app.
3. Under "Bot", click “Add Bot”.
4. Copy the bot token.

---

## Cloning the Repository

```bash
git clone https://github.com/yourusername/vyrtuous.git
cd vyrtuous
```

---

## Building the Project

```bash
mvn clean package
```

This will generate `Vyrtuous-<version>.jar` inside the `target/` directory.

To move and rename it:

```bash
cp target/Vyrtuous-*.jar Vyrtuous.jar
```

---

## Running the Application

Set API keys as environment variables:

```bash
export OPENAI_API_KEY="your-openai-api-key"
export DISCORD_API_KEY="your-discord-api-key"
```

Then run:

```bash
java -jar Vyrtuous.jar
```

Or pass the keys as arguments:

```bash
java -jar Vyrtuous.jar --openai-key="your-openai-api-key" --discord-key="your-discord-api-key"
```

---

## Setting Up Docker

### Start Docker

* **Linux (Systemd)**:

```bash
sudo systemctl start docker.service
```

* **macOS / Windows**:
  Start Docker Desktop from the launcher.

### Build Docker Image

```bash
sudo docker build -t vyrtuous .
```

### Run the Docker Container

```bash
sudo docker run \
  -e OPENAI_API_KEY="your-openai-api-key" \
  -e DISCORD_API_KEY="your-discord-api-key" \
  vyrtuous
```

---

## Notes

* `EnvironmentPaths.java` controls your data/model paths. Customize as needed.
* Supports both interactive (REPL) and non-interactive (bot) operation modes.
* Ideal for advanced automation, experimentation, and educational tools.

---

## License

(C) 2025 [github.com/brandongrahamcobb](https://github.com/brandongrahamcobb)
Licensed under the **GNU GPL v3+**. See [LICENSE](LICENSE) for details.

---

Let me know if you'd like this exported to `README.md`, or want badges, usage examples, or diagrams added.
