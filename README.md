# ElecApp

MVP Android nativo offline para gestionar clientes, visitas, agenda, recordatorios locales, relevamientos eléctricos, presupuestos de mano de obra y listas de materiales de un electricista.

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
- El formulario de visita permite buscar y seleccionar cliente por nombre o teléfono.
- Si el cliente no existe, se puede crear sin salir del flujo y volver con ese cliente seleccionado.
- Crear, editar, eliminar y cambiar estado.
- Estados: pendiente, confirmada, realizada y cancelada.
- Estados operativos: pendiente, confirmada, en curso, realizada y cancelada.
- Iniciar visita desde el detalle guarda `startedAt`, cambia a `IN_PROGRESS` y cancela recordatorios futuros.
- Una visita en curso se muestra primero en Home con acceso “Continuar visita”.
- Finalizar visita guarda `completedAt`, trabajo realizado y pendientes.
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

Listas de materiales:

- Documento separado del presupuesto.
- Siempre pertenece a un cliente; puede vincularse opcionalmente a visita, relevamiento y presupuesto.
- Puede existir sin presupuesto.
- Estados: `DRAFT`, `READY`, `DELIVERED`, `PURCHASED` y `CANCELLED`.
- Responsable de compra: cliente, electricista o a definir.
- Precios opcionales ocultos por defecto y excluidos del texto salvo indicación explícita.
- Plantillas rápidas editables para materiales comunes.

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
- `features/reminders`: entidad Room, reglas testeables, scheduler y receivers.
- `features/settings`: preferencias locales de recordatorios con DataStore.
- `core/external`: Intents externos, contactos y texto compartido.
- `app/AppContainer.kt`: armado manual de dependencias, sin Hilt.

## Room y migraciones

Base local: `elec_app.db`.

Versiones:

- v1: tabla `clients`.
- v2: agrega `clients.address`, `clients.locality` y tabla `visits`.
- v3: agrega tabla `visit_reminders`.
- v4: agrega relevamientos eléctricos.
- v5: agrega campos operativos de cierre a `visits`: `started_at`, `completed_at`, `completion_notes` y `pending_work_notes`.
- v6: agrega presupuestos y listas de materiales.

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

No se usa `fallbackToDestructiveMigration`. La migración `3 -> 4` solo crea tablas e índices nuevos. La migración `4 -> 5` solo agrega columnas nullable a `visits`. La migración `5 -> 6` solo crea tablas e índices nuevos, por lo que clientes, visitas, recordatorios y relevamientos existentes siguen intactos.

Las secciones pilar y tablero son tablas separadas 1 a 1 con `inspection_id` como clave primaria. Se eligió esa forma porque cada sección tiene campos propios y puede crecer sin agrandar `electrical_inspections` con columnas opcionales. No se usan foreign keys para no acoplar el modelo a borrado físico; el proyecto usa borrado lógico.

La regla de un relevamiento activo por visita se aplica en el repository con `startOrGetInspection`: si ya existe uno no borrado, se abre en vez de crear otro. Esta decisión deja margen para soportar varios relevamientos por visita en una versión futura agregando un estado/índice más específico.

Estrategia futura de migraciones: seguir usando migraciones explícitas reversibles conceptualmente, crear tablas nuevas para secciones grandes y agregar índices cuando una consulta lo justifique. No borrar datos críticos sin confirmación.

## Resumen para ChatGPT e informe

El resumen estructurado se genera localmente en español. Omite campos vacíos, no inventa valores y conserva exactamente el comentario original del técnico. Desde el overview se puede:

1. Copiar para ChatGPT.
2. Compartir resumen con `text/plain`.
3. Pegar luego el informe redactado manualmente.
4. Guardar, copiar o compartir el informe final.

Todo el flujo es offline. ChatGPT se abre fuera de la app solo si el usuario lo decide manualmente.

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

Visita en curso:

1. Home prioriza la tarjeta “Visita en curso”.
2. Desde el detalle se muestran cliente, domicilio, motivo, hora de inicio y tiempo transcurrido aproximado.
3. Acciones rápidas: iniciar/continuar/ver relevamiento, crear/ver presupuesto, crear/ver lista de materiales, WhatsApp, llamar, Maps y finalizar visita.

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
