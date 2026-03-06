# Engineering Standards: Scaffolder Gradle Plugin (Java 25)

This document governs the development of the Gradle orchestrator plugin. The goal of this code is to generate microservice ecosystems (Spring Boot/WebFlux) using Clean Architecture and Git Submodules. All pull requests will be evaluated by CodeRabbit under these unbreakable rules to ensure maintainability (ISO/IEC 25010), build-time performance, and scaffolding security.

## 1. Plugin Architecture (Clean Architecture applied to the Scaffolder)

The plugin itself MUST follow Clean Architecture principles. Do not mix Gradle logic with build logic.

- **Domain:** Abstract ecosystem models (`MicroserviceBlueprint`, `RootWorkspace`, `DependencyCatalog`). Zero coupling with the Gradle API or the FileSystem. These must be immutable Java 25 records.
- **Application Use Cases:** Logic orchestrators (e.g., `GenerateReactiveMicroserviceUseCase`, `LinkGitSubmoduleUseCase`).
- **Infrastructure:** - **I/O Adapters:** Classes that implement actual file writing (`FileSystemAdapter`).
- **Git Adapters:** Wrappers for executing terminal commands (`GitCliAdapter`) with timeout control.
- **Gradle Layer:** Classes that extend `DefaultTask` or `Plugin<Project>` are simple controllers. They only capture user input (`Extension`) and delegate it to Use Cases.

## 2. Specific Gradle API Rules

- **Configuration Cache Ready:** The plugin MUST be compatible with Gradle's configuration cache.
- The use of global state or access to the `Project` object during the Execution Phase is prohibited.
- Only pass necessary data to tasks using `@Input` or `@Internal`.
- **Lazy Configuration:** Strictly use the `Property<T>` and `Provider<T>` APIs. Never use direct primitive types (`String`, `boolean`) in plugin extensions.
- **`afterEvaluate` is prohibited:** The use of `project.afterEvaluate {}` is an antipattern in modern Gradle. Model task dependencies correctly using Task Outputs/Inputs.

## 3. Code and Template Generation (Scaffolding)

- **String concatenation is prohibited for code:** Never generate `.java` or `.gradle` files by concatenating strings. Use Java Text Blocks (`"""`) in Java 15+ for simple templates, or a structured template engine (FreeMarker, Mustache) for complex files (e.g., generated `build.gradle.kts`).
- **Mandatory Idempotence:** All generation tasks must be safe to run multiple times.
- Before writing, check if the file exists.
- If it exists and has user modifications, DO NOT overwrite it unless there is an explicit `--force` flag.
- **Strict Paradigm Injection:** Generated code templates must respect the paradigm chosen by the user. If `REACTIVE` is chosen, the generated template must use `Mono/Flux` and prohibit blocking dependencies (such as `spring-boot-starter-web` or `spring-boot-starter-data-jpa`, forcing `R2DBC`).

## 4. Orchestration and Git Submodules

- **Secure Process Execution (Submodules):** When the plugin executes `git submodule add` commands, NEVER use `Runtime.getRuntime().exec()`. Use the modern `ProcessBuilder` API, properly handling output (stdout) and error (stderr) streams to avoid Gradle's main thread deadlocks.
- **Path Traversal Sanitization:** Microservice names provided by the user in the plugin configuration MUST be validated with regular expressions (e.g., `^[a-z0-9-]+$`). This prevents directory traversal attacks or errors when creating physical submodule paths.

## 5. Error Handling and Developer Experience (DX)

- **Fail-Fast Configuration:** Validates user properties (e.g., base package name, target Java version) during the Gradle configuration phase. `GradleException` with clear and actionable messages if crucial data is missing.
- **Level Logs:** - Use `project.getLogger()`.
- Log the start and end of submodule creation at the `LIFECYCLE` level.
- Log individual file I/O operations at the `DEBUG` or `INFO` level.

## 6. Plugin Testing (TestKit and AST Verification)

The quality of a generator is measured by the quality of what it generates.

- **Unit Tests:** For routing logic and naming (100% coverage).
- **Gradle TestKit (Functional Tests):** Every main flow must have an integration test using `GradleRunner`. The test must:

1. Create a temporary directory (`@TempDir`).
2. Run the plugin's scaffolding task.
3. Verify that the folder structure (Domain, Infrastructure, Application) exists.
4. **Compile the result:** The test must run an in-memory compiler on the generated Java code to ensure that the templates did not introduce syntax errors. - **Isolation:** Tests that interact with Git should initialize local and temporary `git init` repositories, never affecting the global `~/.gitconfig` configuration of the CI/CD machine.