# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Proyecto

ElecApp: app Android nativa **offline-first** (Kotlin + Jetpack Compose + Room) para un electricista: clientes, visitas, sesiones de trabajo, agenda, recordatorios locales, relevamientos eléctricos, presupuestos, listas de materiales, herramientas de cálculo y módulo económico.

**Sin backend, sin login, sin red.** Toda operación funciona contra la base local. Compartir = clipboard + Android Sharesheet (`text/plain`). No agregar dependencias de red, Firebase, analytics ni sincronización sin pedirlo explícitamente.

Idioma: todo el texto de UI, mensajes de error y contenido generado está en **español (es-AR)**. El código (clases, funciones, variables) está en inglés.

## Comandos

El proyecto usa un `GRADLE_USER_HOME` local; usarlo siempre para no descargar dependencias de nuevo:

```bash
GRADLE_USER_HOME=/home/matiasdev/elec-app/.gradle ./gradlew :app:assembleDebug
GRADLE_USER_HOME=/home/matiasdev/elec-app/.gradle ./gradlew :app:testDebugUnitTest
GRADLE_USER_HOME=/home/matiasdev/elec-app/.gradle ./gradlew :app:lintDebug

# Un solo test / una sola clase
GRADLE_USER_HOME=/home/matiasdev/elec-app/.gradle ./gradlew :app:testDebugUnitTest \
  --tests "com.matiasdev.elecapp.features.quotes.domain.QuoteCalculatorTest"
GRADLE_USER_HOME=/home/matiasdev/elec-app/.gradle ./gradlew :app:testDebugUnitTest \
  --tests "*QuoteCalculatorTest.aplica descuento porcentual*"
```

JDK 17+ (se usó 21), AGP 8.13, `compileSdk` 36, `minSdk` 26. `local.properties` (`sdk.dir`) no se versiona.

Solo hay tests JVM (`app/src/test`). No hay tests instrumented; los schemas Room se exportan a `app/schemas/` pero no se testean migraciones.

## Arquitectura

Un solo módulo Gradle (`:app`), organizado **por feature** bajo `com.matiasdev.elecapp.features.<feature>`, cada una con el mismo esqueleto:

```
domain/   modelos + reglas puras (sin Android, sin Room) — es lo que se testea
data/     Entity + Dao + Mapper + interfaz Repository + RoomXRepository
ui/       Screen (Compose) + UiState (data class) + ViewModel + ViewModelFactory
summary/  generadores de texto determinístico para copiar/compartir (algunas features)
```

Features: `clients`, `visits`, `agenda`, `inspections`, `quotes`, `materials`, `finance`, `electricaltools`, `electricalrules`, `reminders`, `settings`, `home`.
Transversales: `core/external` (Intents, contactos, texto compartido), `core/time` (`TimeProvider`), `core/ui` (theme + componentes), `navigation`, `app/AppContainer.kt`.

### Reglas estructurales que hay que respetar

- **DI manual, sin Hilt.** `app/AppContainer.kt` construye la DB y todos los repositorios; `ElecNavHost` los recibe como parámetros y los pasa a cada `ViewModelFactory`. Un repositorio nuevo se agrega ahí y se propaga por firma — no hay service locator ni singletons ocultos.
- **Interfaz + implementación Room.** Cada feature expone `XRepository` (interfaz en `data/`, tipos de dominio, `Flow` para lectura, `suspend` para escritura) y `RoomXRepository`. Los ViewModels dependen solo de la interfaz; los tests usan `Fake*Repository` en `app/src/test/.../data/` o `.../ui/`.
- **La lógica de negocio vive en `domain/` como funciones/objects puros** (`QuoteCalculator`, `ReceiptCalculator`, `PaymentBalanceCalculator`, `FinanceMetricsCalculator`, `VisitWorkActions`, `ReminderRules`, evaluadores de `electricalrules`). Los ViewModels orquestan; no calculan. Si algo es difícil de testear sin Android, está en la capa equivocada.
- **ViewModel + `StateFlow<XUiState>`**, un `UiState` inmutable por pantalla, en su propio archivo. Eventos one-shot (snackbar, navegación, compartir) por `SharedFlow`, nunca dentro del state.
- **Pantallas nuevas separan `XScreen` (conectada al ViewModel) de `XContent` (presentacional)** para poder hacer `@Preview` sin Room ni `AppContainer`.
- **`TimeProvider` en vez de `Instant.now()`** en dominio y ViewModels, para poder testear tiempo.
- IDs: `UUID.randomUUID().toString()` generado en la capa que crea la entidad.

### Persistencia

- Base `elec_app.db`, `AppDatabase` en `features/clients/data/` (por historia, no por pertenencia), **versión 18**, `exportSchema = true`.
- **Migraciones explícitas siempre**; nunca `fallbackToDestructiveMigration`. Cada versión nueva vive en su propio archivo `AppMigrationsV<N>.kt` y se registra en `AppContainer`. El patrón vigente es aditivo: crear tablas/índices nuevos o agregar columnas *nullable*; no romper datos existentes.
- **Borrado lógico en todo el modelo** (`is_deleted`). Por eso **no hay foreign keys físicas**: las relaciones son ids nullable + `JOIN`/`LEFT JOIN`, y todas las queries filtran `is_deleted = 0`.
- Índices por `client_id`, `visit_id`, `inspection_id`, `status`, fechas y `is_deleted`.

### Dinero y cantidades

- Importes: `Long` en **centavos** ARS. Nunca `Float`/`Double`/`BigDecimal` como fuente de verdad.
- Porcentajes: puntos básicos (`10000 = 100%`).
- Cantidades decimales: `quantityMillis` (`1000 = 1 unidad`).
- Parseo/formato en `features/quotes/domain/MoneyFormatter.kt` (tolera `$100.000`, `100000,50`).
- Distinguir siempre **importe generado** vs **cobrado** vs **pendiente**. Dinero no cobrado no es ingreso.
- `ServiceReceipt` es comprobante **interno**; la UI y el texto compartido deben decir "No válido como factura". No hay AFIP/ARCA, CAE, IVA ni PDF.

### Tiempo de trabajo

El cronómetro de visitas **no** usa `Service`, `WorkManager` ni contadores en memoria: la fuente de verdad son los timestamps de `visit_work_sessions` en Room, y la UI solo recalcula mientras la pantalla está visible. Invariante: como máximo una sesión `RUNNING` por visita (`endedAt = null`). Inicio/pausa/reanudación/cierre se ejecutan en transacciones Room desde `RoomVisitWorkSessionRepository`.

### Navegación

Navigation Compose con rutas string en `navigation/AppRoutes.kt` (params opcionales por query string). `ElecNavHost` es el host + bottom bar; los subgrafos grandes se extraen como extensiones de `NavGraphBuilder` (`DocumentNavGraph`, `FinanceNavGraph`, `ElectricalToolsNavGraph`) — seguir ese patrón antes de agrandar `ElecNavHost.kt` (ya ~570 líneas). Devolver resultados entre pantallas (ej. crear cliente desde el formulario de visita) usa `SavedStateHandle` del back stack.

El contrato de navegación/back stack ya validado está en `docs/qa-navigation-layout-checklist.md`; revisarlo al tocar flujos de listado -> detalle -> edición.

### Cálculos eléctricos

`electricaltools/calculators` contiene fórmulas puras **versionadas** (`voltage-drop-resistive-v1`, umbrales `voltage-drop-orientative-thresholds-v1`). Entradas y resultados se persisten como JSON con `schemaVersion`; el texto compartible se genera después, nunca se guarda. `electricalrules` guarda umbrales configurables en Room con defaults sembrados en el callback de `AppDatabase`.

Regla de producto: la clasificación automática es **orientativa** y no afirma cumplimiento reglamentario; la conclusión del técnico se guarda aparte. No suavizar ese lenguaje en la UI ni en los textos generados.

## Convenciones del repo (`AGENTS.md`)

- Mantener archivos nuevos bajo ~400 líneas; es guía de mantenibilidad, no regla dura. No partir archivos grandes que ya funcionan solo por el número. Al tocar uno que ya supera 400, no agrandarlo mucho más: extraer helper/modelo/componente.
- Commits chicos y descriptivos después de cada cambio validado. **No agregar líneas `Co-authored-by`** (esto pisa el default global de Claude Code). No tocar la config de Git. No pushear sin autorización explícita.

## README

`README.md` es la documentación funcional detallada (flujos, estados, fórmulas, QA manual, limitaciones) y está al día con el esquema v18. Al agregar una migración, documentarla ahí en "Room y migraciones": versión en la lista, tablas/columnas nuevas con sus índices, y el criterio del default elegido si la columna no es nullable. Mantener también "Limitaciones actuales" y "Próximos pasos" cuando una feature deja de ser pendiente.
