# ElecApp

MVP Android nativo offline para gestionar clientes, visitas, sesiones de trabajo, agenda, recordatorios locales, relevamientos eléctricos, presupuestos de mano de obra y listas de materiales de un electricista.

## Requisitos

- Android Studio compatible con AGP 8.13.
- JDK 17 o superior. En este entorno se usó JDK 21.
- Android SDK con `compileSdk` 36.
- Conexión a internet la primera vez que Gradle descargue dependencias.

## Ejecutar

```bash
GRADLE_USER_HOME=/home/matiasdev/elec-app/.gradle ./gradlew :app:assembleDebug
GRADLE_USER_HOME=/home/matiasdev/elec-app/.gradle ./gradlew :app:testDebugUnitTest
GRADLE_USER_HOME=/home/matiasdev/elec-app/.gradle ./gradlew :app:lintDebug
```

## Funcionalidad actual

Clientes:

- Nombre y teléfono obligatorios.
- Email, dirección, localidad y notas opcionales.
- Detalle con acciones WhatsApp, llamada, email y Maps.
- Importar desde contactos mediante selector del sistema.
- Recibir texto compartido como notas de un nuevo cliente.
- Listado, detalle y formulario rediseñados con tarjetas y jerarquía visual Material 3.

Visitas:

- Crear desde Home, Agenda o detalle de cliente.
- Atender ahora desde Home crea una visita urgente/no programada con fecha actual, la deja `IN_PROGRESS` y crea una sesión `RUNNING`.
- El cliente rápido de Atender ahora pide solo nombre obligatorio; teléfono, dirección y localidad son opcionales.
- Si ya hay una sesión `RUNNING`, el flujo pide continuar la visita actual o pausarla antes de iniciar otra.
- El formulario de visita permite buscar y seleccionar cliente por nombre o teléfono.
- Si el cliente no existe, se puede crear sin salir del flujo y volver con ese cliente seleccionado.
- Crear, editar, eliminar y cambiar estado.
- Estados: pendiente, confirmada, realizada y cancelada.
- Estados operativos: pendiente, confirmada, en curso, realizada y cancelada.
- Iniciar visita desde el detalle guarda `startedAt`, cambia a `IN_PROGRESS`, crea una sesión `RUNNING` y cancela recordatorios futuros.
- Una visita en curso se muestra primero en Home con acceso “Continuar visita” y tiempo trabajado aproximado.
- Pausar trabajo cierra la sesión activa como `PAUSED`; reanudar crea una nueva sesión `RUNNING`.
- Finalizar visita cierra la sesión activa, guarda `completedAt`, trabajo realizado y pendientes.
- Finalizar desde el detalle abre un cierre guiado con trabajo, tiempo, importe, cobro y confirmación.
- El cierre guiado puede crear `VisitCompletion`, comprobante interno de servicio y pago inicial en una transacción.
- El registro manual permite agregar una sesión cerrada para trabajo ya realizado, con validación de rango, futuro y superposición.
- Agregar al calendario mediante Intent, sin sincronización.
- Recordatorios locales: ninguno, uno o dos por visita.
- Desde el detalle de visita se puede iniciar, continuar o ver un relevamiento eléctrico.
- Si la visita está `COMPLETED`, el detalle muestra el acceso "Informes" (técnico y para el cliente).
- `VisitFormScreen`, `QuickVisitScreen` y el diálogo de finalización (`CompleteVisitDialog`, con las advertencias de validación integradas) están modernizados a Material 3.

Relevamientos eléctricos:

- Flujo: Visita -> Relevamiento -> Resumen estructurado -> Informe final.
- Estados: `DRAFT` y `COMPLETED`.
- Secciones MVP: datos generales, pilar y acometida, tablero principal, hallazgos, sectores no verificados, observación técnica e informe final del cliente.
- Guarda snapshots mínimos de cliente, dirección, localidad y motivo de visita para mantener consistencia histórica si el cliente cambia después.
- Permite guardar borrador, finalizar, reabrir con confirmación, copiar el resumen estructurado y compartirlo con Android Sharesheet.
- Home tiene acceso al listado de relevamientos con filtros En borrador, Finalizados y Todos.
- El listado permite buscar por cliente, domicilio, localidad o motivo de visita. No permite crear relevamientos sueltos; siempre siguen vinculados a una visita.
- El informe final del cliente se pega o redacta manualmente y se guarda en `finalClientReport`; no reemplaza el comentario técnico original.
- No envía información a internet.
- La barra superior del overview permite salir hacia la visita en curso o hacia el inicio sin tener que terminar el relevamiento primero.
- El botón Siguiente de hallazgos avanza según el alcance del relevamiento: visual va a inspección visual complementaria, el resto a observación técnica.
- Observación técnica y observaciones complementarias son las secciones terminales: tienen barra inferior con Atrás, Inicio y Terminar, y al guardar vuelven al overview limpiando las secciones apiladas. Antes volvían a hallazgos, que era de donde venían, y el flujo quedaba en bucle.
- La tarjeta de hallazgos del overview cuenta los hallazgos automáticos, que se derivan del relevamiento en cada lectura y no están guardados en Room.
- La sección de hallazgos se puede marcar completa sin cargar ninguno (`findings_reviewed_at`, v20): no encontrar nada es un resultado válido, distinto de no haber revisado la sección.
- Circuitos del tablero: además de los destinos existentes, admite `GENERAL` (toda la instalación) y `PARTIAL` (varios sectores, descriptos en el campo de texto libre que también usa "Otro").
- Cada hallazgo automático trae su recomendación desde el dominio (`InspectionFindingRecommendations`), no desde el prompt de la IA: la acción forma parte del informe técnico que firma el electricista. Un hallazgo confirmado lleva la acción concreta (reemplazar, instalar, adecuar); una sugerencia de regla pendiente de validación lleva la verificación. Si el hallazgo ya estaba guardado, se actualiza la recomendación pero se respeta la severidad que haya dejado el técnico.
- Diferencial ausente o con prueba manual fallida se clasifican como `URGENT`: en ambos casos la instalación queda sin protección contra descargas.

Presupuestos:

- Documento separado para mano de obra y servicios.
- Siempre pertenece a un cliente; puede vincularse opcionalmente a visita y relevamiento.
- Estados: `DRAFT`, `READY`, `SENT`, `APPROVED`, `REJECTED`, `EXPIRED` y `CANCELLED`.
- Importes guardados como `Long` en centavos; no se usa `Float` para dinero.
- Descuentos fijos o porcentuales; el porcentaje se persiste como puntos básicos (`10000 = 100%`).
- Número local legible, por ejemplo `PRES-2026-0001`.
- Permite copiar y compartir texto. No genera PDF todavía.
- Los materiales no se cargan como ítems de presupuesto en este MVP.

Economía, comprobantes y cobros:

- Módulo offline-first, sin backend, login, Firebase ni pagos online.
- Distingue importe de trabajos, dinero cobrado y saldos pendientes. No llama “ingreso” a dinero no cobrado.
- `ServiceReceipt` es un comprobante interno de servicio. La UI y texto compartido indican: “No válido como factura”.
- No implementa factura fiscal, AFIP/ARCA, CAE, IVA, notas de crédito ni PDF complejo.
- Numeración local secuencial visible como `CS-000001`, asignada al emitir para evitar huecos por borradores abandonados.
- Los comprobantes guardan snapshot propio de ítems e importes; no modifican presupuestos originales.
- `ServiceReceiptItem` separa mano de obra, materiales y adicionales. Los materiales suministrados por el cliente pueden registrarse como no cobrables.
- `Payment` permite cobros totales o parciales, varios métodos y cobros sin comprobante asociado con etiqueta clara.
- Los pagos cancelados o eliminados no suman al cobrado. Los comprobantes cancelados no entran en estadísticas.
- Por defecto se bloquea el sobrepago; propinas o adicionales deben cargarse primero como ítem adicional.
- El detalle de comprobante permite compartir texto por Android Sharesheet, sin abrir WhatsApp obligatoriamente y sin incluir notas internas.
- El dashboard económico (`FinanceDashboardScreen`) se rediseñó con tarjetas M3, KPIs (trabajos completados, generado, cobrado, pendiente, ticket promedio, importe por hora) y un gráfico de barras dibujado con `Canvas`, sin librerías de charts.
- `ClientDetail` agrega accesos compactos a comprobantes y cobros del cliente.
- Home prioriza visita en curso, Atender ahora, próxima visita y acceso al módulo económico.

Informes de atención:

- Al cerrar una visita con relevamiento, `AttentionReportCoordinator` congela el informe técnico como snapshot en `visit_completions.technical_report_snapshot`. Antes se regeneraba en vivo y un informe ya entregado podía cambiar de conclusión si después se ajustaba un umbral de `electrical_rule_configs`.
- Una atención sin relevamiento no genera snapshot; su registro en la historia clínica queda en los campos de trabajo del cierre. La generación nunca hace fallar el cierre: si falla, la atención igual queda cerrada y el informe se puede regenerar después.
- `AttentionReportStatus` compara `reports_generated_at` contra el `updatedAt` más nuevo de las fuentes (relevamiento, secciones, mediciones, circuitos, hallazgos) y devuelve `NOT_GENERATED`, `UP_TO_DATE` o `STALE`. El informe nunca se regenera solo; `STALE` solo avisa para que el técnico decida.
- Informe para el cliente: el técnico copia o comparte (Sharesheet) una plantilla con el informe técnico congelado más instrucciones (`ClientReportPromptGenerator`), la pega en la IA externa que use (ChatGPT, Gemini, la que sea), y pega la respuesta de vuelta en `AttentionReportsScreen`. La IA corre fuera de la app; ElecApp no llama a ningún servicio de red, solo arma el texto y usa clipboard/Sharesheet como con el resto de la app.
- El prompt prohíbe inventar o completar valores, prohíbe interpretar qué indica una medición (solo explicar qué es un dispositivo) y pide texto plano sin Markdown porque el informe se lee en la app y se manda por WhatsApp. Cierra con un bloque “VALORES UTILIZADOS” para que verificar contra el informe técnico sea comparar diez segundos.
- El DAO expone updates separados para informe técnico e informe del cliente: regenerar el técnico nunca pisa el del cliente, que es el único artefacto que no se puede reconstruir.
- Accesible desde el detalle de visita completada (“Informes: Técnico y para el cliente”) y desde cada atención del historial clínico, que muestra si hay informe técnico y/o de cliente guardados sin cargar el texto en el listado.

Decisiones monetarias:

- Los importes nuevos se guardan como `Long` en centavos ARS.
- No se usa `Double`, `Float` ni tipos binarios como fuente de verdad para dinero nuevo.
- Las cantidades decimales nuevas usan `quantityMillis`: `1000 = 1 unidad`.
- `MoneyParser` tolera `100000`, `100.000`, `100000,50` y `$100.000`.
- `ReceiptCalculator`, `PaymentBalanceCalculator`, `ReceiptStatusResolver` y `FinanceMetricsCalculator` concentran reglas puras y testeables.

Listas de materiales:

- Documento separado del presupuesto.
- Siempre pertenece a un cliente; puede vincularse opcionalmente a visita, relevamiento y presupuesto.
- Puede existir sin presupuesto.
- Estados: `DRAFT`, `READY`, `DELIVERED`, `PURCHASED` y `CANCELLED`.
- Responsable de compra: cliente, electricista o a definir.
- Precios opcionales ocultos por defecto y excluidos del texto salvo indicación explícita.
- Plantillas rápidas editables para materiales comunes.

Herramientas eléctricas:

- Sección accesible desde Home como “Herramientas eléctricas”.
- Primeras herramientas: potencia/corriente/tensión y caída de tensión.
- Historial y detalle de cálculos guardados.
- Cada cálculo puede guardarse suelto o asociarse opcionalmente a cliente, visita y relevamiento.
- El origen se registra y muestra como `MEASURED`, `CALCULATED` o `ESTIMATED`: medido con instrumento/referencia opcional, calculado desde entradas concretas o estimado con supuestos.
- Los resultados guardan entradas y resultados numéricos/enums en JSON estable con `schemaVersion`; los textos compartibles se generan después.
- Los cálculos asociados a un relevamiento aparecen en “Mediciones y cálculos” y se incluyen en el resumen estructurado.
- Desde caída de tensión con clasificación de revisión se puede crear un hallazgo sugerido vinculado al relevamiento, con criterio técnico separado del cálculo original.
- Copiar y compartir usan texto determinístico local. No hay IA, backend, PDF, fotos ni sincronización.
- Herramientas marcadas como “Próximamente”: sección orientativa de conductor, luminotecnia, capacitancia, corrección de factor de potencia, consumo energético, protecciones y tablas técnicas.
- Pantallas de herramientas eléctricas (home, referencia, historial, detalle, potencia/corriente/tensión y caída de tensión) modernizadas a Material 3.

Agenda:

- Hoy: visitas del día, separando próximas y realizadas/vencidas.
- Próximas: grupos Mañana, Próximos 7 días y Posteriores.
- Calendario: mes navegable, días con visitas y detalle del día seleccionado.

## Arquitectura

Arquitectura simple por feature:

- `features/clients`: dominio, Room, repository, UI y ViewModels de clientes.
- `features/visits`: dominio, Room, repository, UI y ViewModels de visitas.
- `features/agenda`: lógica de fechas, agrupación y pantalla de agenda.
- `features/inspections`: dominio, Room, repository, ViewModels, pantallas de relevamiento y generador de resumen.
- `features/quotes`: dominio, Room, repository, ViewModels, pantallas y generador de texto de presupuestos.
- `features/materials`: dominio, Room, repository, ViewModels, pantallas y generador de texto de listas.
- `features/electricaltools`: dominio, Room, repository, calculadoras puras, ViewModels, pantallas, previews y generadores de texto.
- `features/electricalrules`: umbrales técnicos configurables en Room, evaluadores de reglas y generación de hallazgos sugeridos.
- `features/finance`: cierre de visita, comprobantes internos, pagos, calculadoras puras de importes, dashboard económico e informes de atención (`AttentionReportCoordinator`, `AttentionReportStatus`).
- `features/reminders`: entidad Room, reglas testeables, scheduler y receivers.
- `features/settings`: preferencias locales de recordatorios con DataStore.
- `features/home`: pantalla principal que prioriza visita en curso, atender ahora y accesos al resto de los módulos.
- `core/external`: Intents externos, contactos y texto compartido.
- `core/time`: `TimeProvider` inyectable para poder testear lógica que depende del reloj.
- `core/ui`: theme y componentes compartidos.
- `navigation`: rutas, `ElecNavHost` y subgrafos de documentos, economía y herramientas. Las etiquetas de la barra inferior usan una sola línea con elipsis (`labelSmall`, 10.5sp) para que “Herramientas” no rompa el layout.
- `app/AppContainer.kt`: armado manual de dependencias, sin Hilt.

Equivalencias conceptuales con React Native:

- Room Entity: modelo persistido local, similar a una tabla SQLite tipada.
- DAO: interfaz de consultas y escrituras SQL, similar a un módulo de acceso a SQLite.
- Repository: capa que coordina DAOs, reglas de datos y transacciones.
- StateFlow: estado observable frío/caliente para UI, comparable a un store observable.
- ViewModel: mantiene estado de pantalla y sobrevive recomposiciones/cambios simples.
- UI state: data class inmutable que representa lo que la pantalla dibuja.
- One-shot events: `SharedFlow` para snackbar, navegación o compartir, evitando repetir eventos por recomposición.
- Navigation back stack: pila de pantallas de Navigation Compose, equivalente conceptual a un stack navigator.
- Transaction: bloque atómico de Room para que visita, cierre, comprobante y pago no queden a medio guardar.
- Derived state: estado calculado desde entidades, por ejemplo saldo = total - cobrado.
- Money in integer cents: importes como enteros en centavos, no números flotantes.
- Offline-first: toda operación principal funciona con base local, sin backend ni sincronización.

## Room y migraciones

Base local: `elec_app.db`.

Versiones:

- v1: tabla `clients`.
- v2: agrega `clients.address`, `clients.locality` y tabla `visits`.
- v3: agrega tabla `visit_reminders`.
- v4: agrega relevamientos eléctricos.
- v5: agrega campos operativos de cierre a `visits`: `started_at`, `completed_at`, `completion_notes` y `pending_work_notes`.
- v6: agrega presupuestos y listas de materiales.
- v7: agrega `technical_calculations` para herramientas eléctricas.
- v8: agrega `visit_work_sessions` para sesiones reales de trabajo de visitas.
- v9: agrega cierre estructurado, comprobantes internos, ítems, pagos, secuencia de comprobantes y metadatos de atención en visitas.
- v10: agrega `electrical_rule_configs` para umbrales técnicos configurables.
- v11: agrega `electrical_inspections.scope` para distinguir alcance del relevamiento.
- v12: agrega motivo/elemento/tarea al relevamiento y estado de revisión por sección en pilar y tablero.
- v13: amplía `pillar_inspections` y agrega `pillar_measurements`.
- v14: amplía `main_panel_inspections` y agrega `main_panel_measurements` y `main_panel_circuits`.
- v15: rehace `inspection_findings` con origen, regla, estado de revisión e inclusión en informe.
- v16: agrega datos de alimentador al tablero principal.
- v17: agrega `grounding_inspections` y corrige la corriente máxima de cobre 2,5 mm².
- v18: agrega cierre estructurado por campos a `visit_completions`.
- v19: agrega `technical_report_snapshot`, `client_report` y `reports_generated_at` a `visit_completions` para congelar el informe de atención.
- v20: agrega `electrical_inspections.findings_reviewed_at`.

Tablas de v4:

- `electrical_inspections`.
- `pillar_inspections`.
- `main_panel_inspections`.
- `inspection_findings`.
- `inspection_unverified_items`.

Tablas de v6:

- `quotes`.
- `quote_items`.
- `material_lists`.
- `material_items`.

Tabla de v7:

- `technical_calculations`.

Columnas principales: `id`, `type`, `source`, `client_id`, `visit_id`, `inspection_id`, `title`, `description`, `input_data_json`, `result_data_json`, `primary_result_value`, `primary_result_unit`, `classification`, `technician_conclusion`, `technician_notes`, `formula_version`, `created_at`, `updated_at`, `is_deleted`.

Índices: `type`, `client_id`, `visit_id`, `inspection_id`, `created_at`, `classification` e `is_deleted`. No se agregan foreign keys físicas para mantener el patrón de borrado lógico.

Tabla de v8:

- `visit_work_sessions`.

Columnas: `id`, `visit_id`, `started_at`, `ended_at`, `status`, `notes`, `created_at`, `updated_at`, `is_deleted`.

Índices: `visit_id`, `status`, `started_at` e `is_deleted`. No se agregan foreign keys físicas para mantener el patrón de borrado lógico.

Tablas y columnas de v9:

- Columnas nuevas en `visits`: `attention_type` y `parent_visit_id`.
- `visit_completions`: cierre estructurado uno a uno lógico con visita.
- `service_receipts`: comprobantes internos con total generado, descuentos, estado y número local.
- `service_receipt_items`: snapshot de mano de obra, materiales y adicionales.
- `payments`: cobros confirmados o cancelados, con o sin comprobante.
- `receipt_sequence`: próxima numeración local de comprobantes emitidos.

Índices principales: visita de cierre única, número de comprobante único, cliente, visita, presupuesto, fecha de emisión, estado, pagos por cliente/comprobante/fecha/método y borrado lógico.

Tabla de v10:

- `electrical_rule_configs`: umbrales y textos de reglas técnicas configurables.

Columnas: `code` como clave primaria, `name`, `enabled`, `severity`, `numeric_value`, `secondary_numeric_value`, `unit`, `finding_title`, `finding_description_template`, `recommendation_template` y `config_version`.

Los valores por defecto se siembran desde `AppDatabase` en `onCreate` y `onOpen` con inserción que ignora los existentes, para que la edición manual del usuario no se pise al abrir la app.

Columna de v11:

- `electrical_inspections.scope`: `VISUAL_INSPECTION`, `SECTOR_ASSESSMENT` o `GENERAL_ASSESSMENT`.

Los relevamientos anteriores a v11 seguían el flujo general, así que la migración les asigna `GENERAL_ASSESSMENT` como valor por defecto.

Columnas de v12:

- `electrical_inspections`: `review_reason`, `reviewed_element` y `task_description`.
- `pillar_inspections.review_status` y `main_panel_inspections.review_status`: `REVIEWED`, `NOT_APPLICABLE` o `NOT_VERIFIED`, con default `REVIEWED` para no marcar como no verificado lo ya cargado.

Tabla y columnas de v13:

- `pillar_measurements`: mediciones del pilar con `type`, `value`, `unit`, `origin`, `sort_order` y borrado lógico.
- `pillar_inspections` suma tipo de propiedad, tipo de suministro, valores "otro" de protecciones y conductores, datos de diferencial (presencia, corriente, sensibilidad y resultado de test) y notas de compatibilidad de protecciones.

Índices: `inspection_id` e `inspection_id + type`.

Tablas y columnas de v14:

- `main_panel_measurements`: mediciones del tablero, con `section` además de `type`, `value`, `unit` y `origin`.
- `main_panel_circuits`: circuitos individuales con destino, protección, curva, sección y material de conductor, consumo, origen del consumo y notas.
- `main_panel_inspections` suma valores "otro" de diferencial, presencia de conductores de protección, estado de colores, partes expuestas o aislación dañada, notas de riesgos de cableado y resultado de verificación de conductor de protección.

Índices: `inspection_id`, `inspection_id + section` para mediciones e `inspection_id + sort_order` para circuitos.

Cambio de v15:

- `inspection_findings` se rehace para soportar hallazgos generados por reglas además de los manuales.
- Columnas nuevas: `source_type`, `source_section`, `source_entity_id`, `source_value`, `source_unit`, `rule_code`, `review_status`, `include_in_report` y `technician_notes`.
- Los hallazgos existentes se migran como `source_type = 'MANUAL'`, `review_status = 'CONFIRMED'` e `include_in_report = 1`, conservando id, textos, orden y timestamps.

Columnas de v16:

- `main_panel_inspections`: `feeder_distance_meters`, `feeder_conductor_section_mm2`, `feeder_conductor_material` y `feeder_data_origin`, para calcular caída de tensión del alimentador desde el relevamiento.

Tabla y dato de v17:

- `grounding_inspections`: sección de puesta a tierra 1 a 1 con el relevamiento, con presencia de electrodo, accesibilidad de cámara de inspección, conductor de tierra principal, continuidad del conductor de protección, resistencia, origen del valor y notas.
- Corrige la corriente máxima de cobre 2,5 mm² de 20 A a 16 A y sube `config_version` a 2. Solo actualiza las filas que todavía tenían el valor viejo, así que un umbral ya editado por el usuario no se pisa.

Columnas de v18:

- `visit_completions`: `work_type`, `work_sectors`, `work_items`, `work_tests`, `work_observations` y `technical_result`, para reemplazar el cierre de texto libre por un cierre estructurado por campos sin perder los cierres anteriores.

Columnas de v19:

- `visit_completions`: `technical_report_snapshot`, `client_report` y `reports_generated_at`. Hasta v18 el informe técnico se regeneraba en vivo desde datos que podían cambiar (umbrales editables), así que un informe ya entregado podía cambiar de conclusión. Las tres columnas son nullable: las atenciones cerradas antes de v19 quedan sin snapshot y se muestran como “informe no generado”.

Columna de v20:

- `electrical_inspections.findings_reviewed_at`: distingue “no pasé por hallazgos” de “pasé y no encontré nada”, porque hasta v19 la sección solo se marcaba completa si había al menos un hallazgo. Nullable: los relevamientos anteriores a v20 quedan sin revisar y se comportan como antes.

No se usa `fallbackToDestructiveMigration`. La migración `3 -> 4` solo crea tablas e índices nuevos. La migración `4 -> 5` solo agrega columnas nullable a `visits`. La migración `5 -> 6` solo crea tablas e índices nuevos. La migración `6 -> 7` solo crea `technical_calculations` e índices. La migración `7 -> 8` solo crea `visit_work_sessions` e índices. La migración `8 -> 9` agrega columnas nullable y tablas nuevas, por lo que clientes, visitas, recordatorios, relevamientos, presupuestos, materiales, cálculos y sesiones existentes siguen intactos.

De v9 en adelante se mantiene el mismo criterio aditivo: `9 -> 10`, `13 -> 14` y `16 -> 17` solo crean tablas e índices nuevos, y `10 -> 11`, `11 -> 12`, `12 -> 13`, `15 -> 16`, `17 -> 18`, `18 -> 19` y `19 -> 20` solo agregan columnas nullable o con default explícito. Los defaults se eligieron para no cambiar el significado de lo ya cargado: `GENERAL_ASSESSMENT` para el alcance previo al flujo por secciones, `REVIEWED` para secciones que ya se habían completado, y `UNKNOWN` / `NOT_TESTED` / `NOT_VERIFIED` para verificaciones que nunca existieron en esa versión.

Las dos migraciones que no son puramente aditivas son:

- `14 -> 15`: rehace `inspection_findings` con el patrón crear tabla nueva, copiar filas, borrar la vieja y renombrar. SQLite no permite agregar columnas `NOT NULL` sin default a una tabla existente, así que se recrea. Los hallazgos previos se conservan con valores explícitos de compatibilidad.
- `16 -> 17`: además de crear `grounding_inspections`, corrige un valor de `electrical_rule_configs` con un `UPDATE` condicionado al valor anterior.

Cada versión nueva vive en su propio archivo `AppMigrationsV<N>.kt` y se registra en `AppContainer`. Los schemas se exportan a `app/schemas/`.

Las secciones pilar, tablero y puesta a tierra son tablas separadas 1 a 1 con `inspection_id` como clave primaria. Se eligió esa forma porque cada sección tiene campos propios y puede crecer sin agrandar `electrical_inspections` con columnas opcionales. Lo que es lista dentro de una sección (mediciones y circuitos) va en tablas hijas con `sort_order` en vez de columnas numeradas. No se usan foreign keys para no acoplar el modelo a borrado físico; el proyecto usa borrado lógico.

La regla de un relevamiento activo por visita se aplica en el repository con `startOrGetInspection`: si ya existe uno no borrado, se abre en vez de crear otro. Esta decisión deja margen para soportar varios relevamientos por visita en una versión futura agregando un estado/índice más específico.

Estrategia futura de migraciones: seguir usando migraciones explícitas reversibles conceptualmente, crear tablas nuevas para secciones grandes y agregar índices cuando una consulta lo justifique. No borrar datos críticos sin confirmación.

## Resumen estructurado e informe

El resumen estructurado se genera localmente en español. Omite campos vacíos, no inventa valores y conserva exactamente el comentario original del técnico. Si existen cálculos asociados, agrega “MEDICIONES Y CÁLCULOS” diferenciando origen medido, calculado o estimado, supuestos, clasificación orientativa y conclusión técnica cuando exista. Desde el overview se puede:

1. Copiar el resumen estructurado.
2. Compartir resumen con `text/plain`.
3. Pegar luego el informe redactado manualmente.
4. Guardar, copiar o compartir el informe final.

Todo el flujo es offline y el usuario decide manualmente qué hacer con el texto copiado o compartido.

## Fórmulas de herramientas eléctricas

Potencia, corriente y tensión:

- DC: `P = V x I`.
- AC monofásico: `P = V x I x cosφ x η`.
- AC trifásico: `P = √3 x V x I x cosφ x η`.
- Se despejan potencia, corriente o tensión según la variable elegida.
- Unidades base: W, A y V. La UI permite ingresar potencia en W o kW y convierte internamente a W.
- En AC no se asume factor de potencia salvo cálculo estimado con supuesto visible. Eficiencia vacía se muestra y guarda como supuesto de 100%.

Caída de tensión:

- Fórmula resistiva simplificada versionada `voltage-drop-resistive-v1`.
- DC y monofásico: `ΔV = 2 x L x I x ρ / S`.
- Trifásico: `ΔV = √3 x L x I x ρ / S`.
- Porcentaje: `ΔV% = ΔV / Vnominal x 100`.
- En DC/monofásico la longitud ingresada representa distancia de ida y la fórmula contempla ida y vuelta. En trifásico se usa el factor correspondiente sin duplicar recorrido.
- Resistividad a 20 °C: cobre `0.017241 Ω·mm²/m`, aluminio `0.028264 Ω·mm²/m`.
- Corrección opcional por temperatura: `ρT = ρ20 x [1 + α x (T - 20)]`, con α cobre `0.00393` y α aluminio `0.00403`.
- Si la corriente se deriva desde potencia, se reutiliza la calculadora de potencia/corriente/tensión.

Clasificación orientativa:

- Configuración interna versionada `voltage-drop-orientative-thresholds-v1`.
- `ACCEPTABLE` hasta 3%.
- `REQUIRES_REVIEW` hasta 5%.
- `CRITICAL_REVIEW` por encima de 5%.
- La clasificación automática no afirma cumplimiento reglamentario. La conclusión del técnico se guarda aparte como `NOT_REVIEWED`, `CONFIRMED_OK`, `CONFIRMED_REQUIRES_ACTION` o `DISCARDED`.

Limitaciones:

- Los cálculos son herramientas de apoyo.
- No reemplazan mediciones ni certifican una instalación.
- No determinan automáticamente cumplimiento normativo.
- El técnico debe validar datos, supuestos, condiciones reales y conclusiones.
- La caída de tensión inicial no modela reactancia, armónicos, agrupamiento, método de instalación ni otras correcciones que no estén representadas explícitamente.

Previews:

- Las pantallas nuevas separan `Screen` conectada a ViewModel y `Content` presentacional.

## QA manual sugerida para economía

Urgencia:

1. Home -> Atender ahora.
2. Crear cliente rápido.
3. Iniciar visita.
4. Confirmar que aparece el timer en el detalle.
5. Finalizar visita desde cierre guiado.
6. Registrar trabajo, importe y pago total.
7. Abrir comprobante y compartir texto.

Pago parcial:

1. Cerrar trabajo por `$100.000`.
2. Registrar pago inicial `$40.000`.
3. Confirmar saldo `$60.000`.
4. Cerrar y abrir la app.
5. Registrar pago posterior `$60.000`.
6. Confirmar estado pagado y saldo cero.

Materiales:

1. En cierre, cargar materiales cobrables.
2. Para materiales suministrados por cliente, registrarlos como no cobrables en el modelo de ítems cuando se use el editor completo.
3. Confirmar que no suman al total.

Navegación:

1. Probar back del sistema y flecha superior en Atender ahora, Cierre, Comprobantes, Pago y Economía.
2. Al guardar Atender ahora, back desde el detalle no debe volver al formulario rápido.
3. Al cerrar visita, debe volver al detalle o al comprobante generado sin duplicar pantallas.

Limitaciones actuales del módulo económico:

- El cierre guiado implementa carga rápida de mano de obra, materiales, adicionales, descuento fijo y pago inicial.
- La importación selectiva desde presupuesto/lista de materiales y el editor completo de ítems quedan preparados por modelo, pero no están terminados en UI.
- No hay filtros avanzados, recordatorios de saldo, CSV, PDF ni backup económico.
- Las previews exhaustivas pedidas quedan como trabajo pendiente; las pantallas mantienen separación presentacional para agregarlas sin Room.
- Hay previews para home de herramientas, formularios vacíos/con datos, resultados y detalle/historial, sin conectar Room ni `AppContainer`.

## Recordatorios

Cada recordatorio tiene:

- `id`;
- `visitId`;
- `minutesBefore`;
- `enabled`;
- `createdAt`;
- `updatedAt`.

Se eligió una entidad separada `visit_reminders` para evitar campos especiales en `Visit` y dejar el modelo listo para sincronización futura. La UI muestra opciones legibles: sin recordatorio, 15/30 minutos, 1/2 horas, 1 día o personalizado con unidad minutos/horas/días. Internamente siempre se guarda `minutesBefore`.

Al guardar una visita:

- se cancelan recordatorios anteriores;
- se reemplazan los recordatorios guardados;
- se programan los activos que todavía no vencieron.

Al completar, cancelar o eliminar una visita:

- se cancelan sus alarmas futuras.

## Flujos de creación

Nueva visita directa:

1. Desde Home o Agenda tocar “Nueva visita”.
2. Buscar un cliente por nombre o teléfono.
3. Seleccionarlo y completar fecha, hora, motivo, notas y recordatorios.
4. Guardar.

Crear cliente durante una visita:

1. En “Nueva visita”, si no aparece el cliente, tocar “Crear cliente”.
2. Completar nombre y teléfono como obligatorios; email, dirección, localidad y notas son opcionales.
3. Tocar “Guardar y continuar”.
4. La app vuelve al formulario de visita con el cliente nuevo seleccionado y conserva el borrador de visita.

Texto compartido:

1. Desde navegador, notas o WhatsApp, compartir texto plano.
2. Elegir ElecApp.
3. La app abre “Nuevo cliente” con el texto en Notas. No inventa nombre, teléfono ni dirección.
4. Guardar el cliente.
5. Usar “Agendar una visita” para abrir el formulario de visita con ese cliente precargado.

## Flujo operativo de visita

Visita agendada:

1. Crear una visita desde Home, Agenda o cliente.
2. Confirmarla si corresponde.
3. Al llegar al domicilio, abrir el detalle y tocar “Iniciar visita”.
4. La visita pasa a `IN_PROGRESS`, guarda hora de inicio y cancela recordatorios futuros.
5. Se crea una sesión `RUNNING` si no existe una activa para esa visita.

Visita en curso:

1. Home prioriza la tarjeta “Visita en curso”.
2. Desde el detalle se muestran cliente, domicilio, motivo, hora de inicio, sesiones y tiempo trabajado.
3. Si hay sesión `RUNNING`, la acción principal es “Pausar trabajo”.
4. Si la visita sigue `IN_PROGRESS` pero no hay sesión activa, la acción principal es “Reanudar trabajo”.
5. Acciones rápidas: iniciar/continuar/ver relevamiento, crear/ver presupuesto, crear/ver lista de materiales, herramientas, WhatsApp, llamar, Maps y finalizar visita.

Sesiones de trabajo:

1. Una visita puede tener varias sesiones.
2. Una visita no debe tener más de una sesión `RUNNING`.
3. Una sesión `RUNNING` tiene `endedAt = null`.
4. Pausar guarda `endedAt` y deja la sesión como `PAUSED`.
5. Reanudar crea una sesión nueva; no modifica la anterior.
6. Finalizar visita cierra cualquier sesión `RUNNING` como `COMPLETED`.
7. Las notas de sesión se pueden editar, pero los timestamps históricos no se editan en este MVP.
8. El tiempo trabajado se calcula como la suma de sesiones cerradas más la sesión activa hasta `now`.
9. El tiempo transcurrido se calcula desde `Visit.startedAt` hasta `completedAt` o `now`.
10. El tiempo pausado es `transcurrido - trabajado`, nunca negativo.
11. El tiempo total queda expuesto como `totalWorkedDuration` y `totalWorkedMinutes` para futuros reportes o presupuestos por hora, sin tarifas todavía.

Persistencia y temporizador:

- El tiempo no depende de un contador en memoria.
- La fuente de verdad son timestamps persistidos en Room.
- No se usa `Service`, `ForegroundService`, `AlarmManager` ni `WorkManager` para mostrar el cronómetro.
- El detalle actualiza la visualización cada segundo solo mientras la pantalla está visible.
- Al cerrar o reabrir la app, el tiempo se recalcula desde Room.
- Las operaciones inicio, pausa, reanudación y finalización se ejecutan con transacciones Room desde el repositorio de sesiones.

Registro manual:

1. En el menú del detalle tocar “Registrar tiempo manual”.
2. Ingresar inicio y fin con formato `dd/MM/yyyy HH:mm`.
3. El fin debe ser posterior al inicio.
4. No se guardan sesiones futuras ni superpuestas en esta primera versión.
5. La sesión manual queda guardada como `COMPLETED`.

Relevamiento desde la visita:

1. Si no existe, “Iniciar relevamiento” crea uno vinculado a la visita y copia snapshots.
2. Si existe en borrador, “Continuar relevamiento” abre el existente.
3. Si está finalizado, “Ver relevamiento” lo abre sin crear duplicados.

Listado de relevamientos:

1. Desde Home tocar “Relevamientos”.
2. Filtrar por En borrador, Finalizados o Todos.
3. Buscar por cliente, domicilio, localidad o motivo.
4. Continuar o ver un relevamiento existente.

Finalizar visita:

1. En visita en curso tocar “Finalizar visita”.
2. Revisar el estado del relevamiento, presupuesto y lista de materiales.
3. Completar “Trabajo realizado” y “Trabajos o verificaciones pendientes”.
4. Confirmar. La visita pasa a `COMPLETED`, guarda cierre y cancela recordatorios.
5. Si había sesión activa, queda cerrada automáticamente y el resumen muestra inicio, fin, tiempo trabajado, pausas y sesiones.

Presupuesto desde distintos puntos:

1. Home -> Presupuestos -> Nuevo presupuesto.
2. Cliente -> Nuevo presupuesto.
3. Visita o visita en curso -> Crear presupuesto.
4. Relevamiento -> Crear presupuesto.
5. Hallazgo -> Agregar al presupuesto abre el flujo vinculado al relevamiento, sin precio automático.

Lista de materiales desde distintos puntos:

1. Home -> Materiales -> Nueva lista.
2. Cliente -> Nueva lista.
3. Visita o visita en curso -> Crear lista de materiales.
4. Relevamiento -> Crear lista de materiales.
5. Presupuesto -> Crear lista de materiales vinculada.

## Notificaciones y AlarmManager

La app crea el canal “Recordatorios de visitas”.

En Android 13+ se solicita `POST_NOTIFICATIONS` al activar recordatorios o desde Configuración, no al abrir la app. Si el permiso no está concedido, la visita se puede guardar igual, pero Android no mostrará la notificación.

Se usa `AlarmManager.setAndAllowWhileIdle` con alarmas locales no exactas. Android puede entregar algunos recordatorios con demora por ahorro de batería. No se solicita `SCHEDULE_EXACT_ALARM`.

Tras reiniciar el teléfono, `BOOT_COMPLETED` reprograma recordatorios futuros de visitas no completadas, no canceladas y no eliminadas.

## Probar en emulador

Recordatorio rápido:

1. Crear una visita para dentro de 3 a 5 minutos.
2. Activar recordatorio personalizado de 1 minuto.
3. Aceptar permiso de notificaciones si Android lo solicita.
4. Dejar la app en segundo plano y esperar.

Cambiar hora del emulador:

- Desde Settings del emulador, desactivar hora automática y ajustar hora manualmente.
- También se puede usar Android Studio Device Manager según la imagen del emulador.

Reinicio:

- Reiniciar el emulador y esperar `BOOT_COMPLETED`.
- Algunos emuladores pueden demorar o limitar receivers de boot si la app nunca fue abierta después de instalarse.

## Equivalencias React Native

- AlarmManager -> librerías de local notifications.
- BroadcastReceiver -> handler nativo disparado por el sistema.
- PendingIntent -> intención pendiente que Android ejecuta después.
- Notification channel -> canal obligatorio para notificaciones Android 8+.
- Runtime permissions -> permisos solicitados en tiempo de uso.
- Android Intent -> `Linking`.
- Room + Flow -> almacenamiento local reactivo.
- Room entity -> modelo persistente local.
- DAO -> interfaz de queries locales.
- Repository -> capa de acceso a datos consumida por ViewModels.
- ViewModel + StateFlow -> store/hooks con ciclo de vida Android.
- Timestamps persistidos -> fuente de verdad para timers que sobreviven cierre o rotación.
- LaunchedEffect -> timer visual mientras la pantalla está visible.
- Lifecycle-aware collect -> observar Flow sin consumir recursos fuera de la pantalla.
- Room transaction -> operación atómica equivalente a una mutación local consistente.
- SavedStateHandle -> parámetros persistidos del destino de navegación.
- ClipboardManager -> clipboard nativo.
- Android Sharesheet -> compartir `text/plain`.
- Relaciones opcionales Room -> referencias por id nullable y consultas con `JOIN`/`LEFT JOIN`.
- Cálculo derivado -> lógica pura testeable antes de persistir snapshots.
- Formularios Compose -> estado en ViewModel con `StateFlow`.
- Navegación con resultados -> `SavedStateHandle` para volver desde creación de cliente.

## Limitaciones actuales

- Sin backend, login, Firebase, Google Drive, backups ni sincronización.
- Sin integración con servicios externos de red; el resumen solo se copia o comparte. El flujo de informe para el cliente (ver "Informes de atención") no es una excepción: la IA corre en una app externa, ElecApp solo arma texto y usa clipboard/Sharesheet, igual que el resto de la app.
- Sin Google Calendar API; solo Intent de creación de evento.
- Sin servicio de cronómetro en segundo plano; el tiempo se deriva de timestamps.
- Sin tarifa horaria ni facturación por tiempo trabajado.
- Sin alarmas exactas garantizadas.
- Sin notificaciones con acciones rápidas; tocar la notificación abre el detalle de visita.
- Sin tests instrumented de migración; los schemas Room quedan exportados.
- Sin PDF, fotos, facturación fiscal, señas, caja, backup, backend, login ni sincronización.
- Comprobantes y cobros existen, pero son internos: no hay factura fiscal, AFIP/ARCA, CAE, IVA ni notas de crédito.
- Presupuestos: no incluyen materiales como ítems y no manejan cobros.
- Materiales: sin catálogo editable ni historial de precios.
- Relevamientos: sin ambientes, fotos ni PDF. Circuitos, mediciones de pilar y tablero y puesta a tierra ya están, pero sin mediciones avanzadas.

## Próximos pasos

- Ambientes.
- Fotografías locales.
- Exportación PDF.
- Datos profesionales y logo.
- Backup/export manual.
- Catálogo editable.
- Historial de precios.
- Señas y caja.
- Tarifa horaria a partir del tiempo trabajado.
- Backend y sincronización futura.
- Tests instrumented de migraciones.
