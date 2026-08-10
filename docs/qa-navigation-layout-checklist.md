# QA navegación y layouts

Fecha base: 2026-08-05.

## Contrato aplicado

- Listado -> detalle: abrir con `launchSingleTop`; volver usa `navigateUp`.
- Detalle -> editar: guardar elimina el formulario actual del back stack y reusa el detalle.
- Crear documento: guardar elimina el formulario y abre el detalle; atrás vuelve al origen anterior.
- Cambios de estado en documentos: permanecen en detalle salvo cancelación, que vuelve al listado.
- Cancelación de presupuestos/materiales: acción secundaria con confirmación.
- Herramientas desde visita: abre una sola ruta asociada, sin intercalar destinos duplicados.

## Checklist manual

Clientes:

- Lista -> detalle -> atrás: vuelve a lista.
- Detalle -> editar -> guardar: vuelve a detalle actualizado.
- Detalle -> eliminar: vuelve a lista, sin pantalla apuntando al cliente eliminado.

Visitas:

- Agenda -> detalle -> atrás: vuelve a agenda.
- Detalle -> iniciar: permanece en detalle actualizado.
- Detalle -> iniciar con doble toque: debe existir una sola sesión `RUNNING`.
- Detalle en curso con sesión activa: muestra “Pausar trabajo” y “Finalizar visita”.
- Pausar trabajo: permanece en detalle, muestra snackbar y la acción cambia a “Reanudar trabajo”.
- Esperar con visita pausada: el tiempo trabajado no debe crecer.
- Reanudar trabajo: crea una nueva sesión `RUNNING` y vuelve a mostrar “Pausar trabajo”.
- Finalizar visita con sesión activa: cierra la sesión, guarda cierre y no muestra iniciar/pausar/reanudar.
- Cerrar app y abrir durante sesión activa: el timer se recalcula desde Room.
- Girar pantalla: el timer no se reinicia desde cero.
- Bloquear/desbloquear emulador: el tiempo se recalcula desde timestamps persistidos.
- Registrar tiempo manual inválido o superpuesto: muestra error y no guarda.
- Registrar tiempo manual válido: vuelve al detalle y aparece en “Registro de trabajo”.
- Detalle -> editar -> guardar: vuelve a detalle.
- Finalizar visita: permanece en flujo de detalle con estado actualizado.

Relevamientos:

- Listado -> overview -> atrás: vuelve a listado.
- Visita -> relevamiento -> atrás: vuelve a visita.
- Sección -> guardar -> overview: vuelve al overview.
- Hallazgos -> Siguiente -> observaciones: la sección de observaciones tiene barra inferior con Atrás, Inicio y Terminar.
- Observaciones -> Terminar o Guardar: vuelve al overview, nunca a hallazgos.
- Overview -> recorrer secciones -> volver: una sola pulsación de atrás sale del relevamiento, sin overviews apilados.
- Overview: la tarjeta de hallazgos muestra la misma cantidad que la pantalla de hallazgos, incluidos los automáticos.

Presupuestos:

- Listado -> detalle -> atrás: vuelve a listado.
- Detalle -> editar -> guardar: vuelve a detalle, no al formulario.
- DRAFT -> READY: permanece en detalle y muestra solo siguiente acción.
- READY -> SENT: permanece en detalle y muestra solo acciones válidas.
- SENT -> APPROVED/REJECTED: permanece en detalle y cierra acciones de avance.
- Cancelar: confirma y vuelve al listado.

Materiales:

- Listado -> detalle -> atrás: vuelve a listado.
- Detalle -> editar -> guardar: vuelve a detalle, no al formulario.
- DRAFT -> READY: permanece en detalle.
- READY -> DELIVERED: permanece en detalle.
- DELIVERED -> PURCHASED: permanece en detalle y muestra “Volver a materiales”.
- Cancelar: confirma y vuelve al listado.
- Compartir/copiar: no desborda en ancho de 320 dp.

Herramientas:

- Herramientas -> calculadora -> atrás: vuelve a herramientas.
- Visita -> herramientas: abre cálculo asociado y atrás vuelve a visita.
- Relevamiento -> agregar cálculo -> atrás: vuelve al relevamiento.
- Historial -> detalle -> atrás: vuelve al historial.

Layout mínimo:

- Revisar ancho 320 dp en detalle de materiales.
- Revisar ancho 320 dp en detalle de visita con acciones y sesiones.
- Ningún botón debe quedar vertical.
- Filtros y chips deben envolver línea.
- TopAppBar no debe invadir status bar.
- Contenido debe quedar debajo de top bar y ser desplazable.
