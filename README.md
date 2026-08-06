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

Relevamientos eléctricos:

- Flujo: Visita -> Relevamiento -> Resumen estructurado -> Informe final.
- Estados: `DRAFT` y `COMPLETED`.
- Secciones MVP: datos generales, pilar y acometida, tablero principal, hallazgos, sectores no verificados, observación técnica e informe final del cliente.
- Guarda snapshots mínimos de cliente, dirección, localidad y motivo de visita para mantener consistencia histórica si el cliente cambia después.
- Permite guardar borrador, finalizar, reabrir con confirmación, copiar resumen para ChatGPT y compartirlo con Android Sharesheet.
- Home tiene acceso al listado de relevamientos con filtros En borrador, Finalizados y Todos.
- El listado permite buscar por cliente, domicilio, localidad o motivo de visita. No permite crear relevamientos sueltos; siempre siguen vinculados a una visita.
- El informe final del cliente se pega o redacta manualmente y se guarda en `finalClientReport`; no reemplaza el comentario técnico original.
- No usa OpenAI API ni envía información a internet.

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
- El dashboard económico muestra período, trabajos completados, importe generado, cobrado, pendiente, ticket promedio e importe por hora.
- `ClientDetail` agrega accesos compactos a comprobantes y cobros del cliente.
- Home prioriza visita en curso, Atender ahora, próxima visita y acceso al módulo económico.

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
- `features/reminders`: entidad Room, reglas testeables, scheduler y receivers.
- `features/settings`: preferencias locales de recordatorios con DataStore.
- `core/external`: Intents externos, contactos y texto compartido.
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

No se usa `fallbackToDestructiveMigration`. La migración `3 -> 4` solo crea tablas e índices nuevos. La migración `4 -> 5` solo agrega columnas nullable a `visits`. La migración `5 -> 6` solo crea tablas e índices nuevos. La migración `6 -> 7` solo crea `technical_calculations` e índices. La migración `7 -> 8` solo crea `visit_work_sessions` e índices. La migración `8 -> 9` agrega columnas nullable y tablas nuevas, por lo que clientes, visitas, recordatorios, relevamientos, presupuestos, materiales, cálculos y sesiones existentes siguen intactos.

Las secciones pilar y tablero son tablas separadas 1 a 1 con `inspection_id` como clave primaria. Se eligió esa forma porque cada sección tiene campos propios y puede crecer sin agrandar `electrical_inspections` con columnas opcionales. No se usan foreign keys para no acoplar el modelo a borrado físico; el proyecto usa borrado lógico.

La regla de un relevamiento activo por visita se aplica en el repository con `startOrGetInspection`: si ya existe uno no borrado, se abre en vez de crear otro. Esta decisión deja margen para soportar varios relevamientos por visita en una versión futura agregando un estado/índice más específico.

Estrategia futura de migraciones: seguir usando migraciones explícitas reversibles conceptualmente, crear tablas nuevas para secciones grandes y agregar índices cuando una consulta lo justifique. No borrar datos críticos sin confirmación.

## Resumen para ChatGPT e informe

El resumen estructurado se genera localmente en español. Omite campos vacíos, no inventa valores y conserva exactamente el comentario original del técnico. Si existen cálculos asociados, agrega “MEDICIONES Y CÁLCULOS” diferenciando origen medido, calculado o estimado, supuestos, clasificación orientativa y conclusión técnica cuando exista. Desde el overview se puede:

1. Copiar para ChatGPT.
2. Compartir resumen con `text/plain`.
3. Pegar luego el informe redactado manualmente.
4. Guardar, copiar o compartir el informe final.

Todo el flujo es offline. ChatGPT se abre fuera de la app solo si el usuario lo decide manualmente.

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
- Sin API de OpenAI; el resumen solo se copia o comparte.
- Sin Google Calendar API; solo Intent de creación de evento.
- Sin servicio de cronómetro en segundo plano; el tiempo se deriva de timestamps.
- Sin tarifa horaria ni facturación por tiempo trabajado.
- Sin alarmas exactas garantizadas.
- Sin notificaciones con acciones rápidas; tocar la notificación abre el detalle de visita.
- Sin tests instrumented de migración; los schemas Room quedan exportados.
- Sin PDF, fotos, comprobantes, facturación, pagos, señas, caja, backup, backend, login ni sincronización.
- Presupuestos: no incluyen materiales como ítems y no manejan cobros.
- Materiales: sin catálogo editable ni historial de precios.
- Relevamientos: sin circuitos individuales, mediciones avanzadas, puesta a tierra detallada, ambientes, fotos ni PDF.

## Próximos pasos

- Circuitos individuales.
- Mediciones.
- Puesta a tierra.
- Ambientes.
- Fotografías locales.
- Exportación PDF.
- Datos profesionales y logo.
- Backup/export manual.
- Catálogo editable.
- Historial de precios.
- Cobros y señas.
- Backend y sincronización futura.
- Tests instrumented de migraciones.
