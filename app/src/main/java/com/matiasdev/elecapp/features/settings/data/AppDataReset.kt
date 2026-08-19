package com.matiasdev.elecapp.features.settings.data

import com.matiasdev.elecapp.features.clients.data.AppDatabase
import com.matiasdev.elecapp.features.electricalrules.data.insertDefaultElectricalRuleConfigsIgnoringExisting
import com.matiasdev.elecapp.features.referencedocs.data.ReferenceDocumentStorage
import com.matiasdev.elecapp.features.reminders.data.VisitReminderRepository
import com.matiasdev.elecapp.features.reminders.scheduling.VisitReminderScheduler

/**
 * Borrado total de los datos de la app, para dejarla como recién instalada.
 *
 * Es deliberadamente todo o nada. Marcar qué fila es "de prueba" exigiría una columna en más de
 * veinte tablas y se rompería igual apenas alguien edite un dato de ejemplo, así que la app no
 * distingue: se borra todo y se arranca limpio.
 */
class AppDataReset(
    private val database: AppDatabase,
    private val reminderRepository: VisitReminderRepository,
    private val scheduler: VisitReminderScheduler,
    private val referenceDocumentStorage: ReferenceDocumentStorage,
) {
    /**
     * El orden importa: las alarmas se cancelan **antes** de vaciar las tablas, porque después ya
     * no se sabe cuáles había y quedarían `PendingIntent` vivos apuntando a visitas inexistentes.
     */
    suspend fun wipeAll() {
        scheduler.cancelAll(reminderRepository.findAllEnabled())

        database.clearAllTables()

        // `clearAllTables` también vacía los umbrales técnicos, que son semilla y no dato del
        // usuario. El callback de `AppContainer` solo corre al abrir la base, así que sin esto la
        // app quedaría sin criterios hasta el próximo arranque.
        database.openHelper.writableDatabase.insertDefaultElectricalRuleConfigsIgnoringExisting()

        referenceDocumentStorage.deleteAll()
    }
}
