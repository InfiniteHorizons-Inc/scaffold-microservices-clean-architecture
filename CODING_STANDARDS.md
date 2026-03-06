# Estándares de Ingeniería y Arquitectura Java (Enterprise)

Este documento define las reglas estrictas de codificación, diseño y arquitectura para el proyecto. Los revisores automáticos (ej. CodeRabbit) utilizarán estas directivas para evaluar cada Pull Request. El incumplimiento de estas reglas es motivo de bloqueo del PR.

## 1. Arquitectura y Principios de Diseño
- **Interfaces sobre Implementaciones:** Programa siempre orientándote a interfaces. Las clases concretas no deben depender de otras clases concretas.
- **Desacoplamiento y Extensibilidad:** Diseña los componentes para que sean cerrados a la modificación pero abiertos a la extensión (Open/Closed Principle). Para módulos extensibles o sistemas de plugins, utiliza la Inyección de Dependencias o la API `java.util.ServiceLoader` (SPI) para cargar implementaciones dinámicamente.
- **Inmutabilidad por Defecto:** Las clases de dominio y transferencia de datos deben ser inmutables. Utiliza `record` de Java (Java 25) para DTOs y objetos de valor (Value Objects). Si usas clases tradicionales, haz los campos `private final` y no proveas *setters*.
- **Principio de Responsabilidad Única (SRP):** Una clase debe tener una, y solo una, razón para cambiar. Si una clase maneja lógica de negocio, acceso a datos y formateo, debe ser refactorizada inmediatamente.

## 2. Convenciones de Código y Limpieza (KISS & DRY)
- **Nombres Descriptivos:** No uses abreviaturas (`usr`, `ctx`, `mgr`). Usa nombres completos que revelen la intención (`user`, `context`, `manager`).
- **Límites de Complejidad:**
    - Los métodos no deben superar las 30 líneas de código (Long Method).
    - Las clases no deben tener más de 4 dependencias inyectadas. Si tienen más, probablemente violan SRP.
- **Prohibición de "Magic Numbers/Strings":** Cualquier valor literal (ej. `int maxRetries = 3;` o `if (status.equals("ACTIVE"))`) debe ser extraído a una constante `static final` o a un `Enum`.
- **Colecciones:** Devuelve siempre colecciones inmutables (`List.copyOf()`, `Collections.unmodifiableList()`) desde los *getters* para evitar mutaciones externas indeseadas.

## 3. Manejo de Errores y Excepciones
- **Prohibido Silenciar Excepciones:** Nunca dejes un bloque `catch` vacío ni hagas un simple `e.printStackTrace()`. Maneja el error, envuélvelo en una excepción personalizada de negocio, o regístralo a través de un *Logger*.
- **Excepciones Específicas:** No lances `Exception` o `RuntimeException` genéricas. Crea y lanza excepciones orientadas al dominio (ej. `ResourceNotFoundException`, `InvalidPluginConfigurationException`).
- **No uses Excepciones para el Flujo de Control:** Las excepciones son para casos excepcionales, no para controlar la lógica `if/else` del negocio.

## 4. Rendimiento y Concurrencia
- **Uso Correcto de Streams:** Utiliza el API de Streams para transformaciones de datos complejas. Sin embargo, para iteraciones críticas de ultra-bajo nivel, prefiere los bucles `for` tradicionales si el profiling demuestra una mejora significativa de rendimiento.
- **Thread-Safety:** Las clases que se comparten entre múltiples hilos (como los servicios singleton) no deben tener estado mutable. Si el estado es necesario, utiliza clases de `java.util.concurrent.atomic` o estructuras concurrentes (`ConcurrentHashMap`), nunca colecciones estándar no sincronizadas.
- **Cierre de Recursos:** Todo recurso que implemente `AutoCloseable` (conexiones, streams de archivos, sockets) debe ser instanciado dentro de un bloque `try-with-resources`.

## 5. Seguridad
- **Validación de Entradas (Fail-Fast):** Valida todos los parámetros de entrada al comienzo de los métodos públicos. Usa `Objects.requireNonNull()` o aserciones de validación. Lanza `IllegalArgumentException` inmediatamente si los datos son inválidos.
- **Loggings Seguros:** Nunca registres (log) información sensible como contraseñas, tokens JWT, PII (Personal Identifiable Information) o números de tarjetas.
- **Prohibido System.out/err:** El uso de `System.out.println` o `System.err.println` está estrictamente prohibido en código de producción. Utiliza siempre el framework de logging estándar del proyecto (ej. SLF4J).

## 6. Pruebas Unitarias
- **Patrón Arrange-Act-Assert (AAA):** Todos los tests deben seguir esta estructura visual, separando la preparación de datos, la ejecución del método y la verificación de los resultados.
- **Pruebas de Casos Extremos:** No pruebes solo el "Happy Path". Es obligatorio incluir pruebas para valores nulos, colecciones vacías y límites numéricos.