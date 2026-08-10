package com.matiasdev.elecapp.features.inspections.domain

/**
 * Acción concreta que corresponde a cada hallazgo automático.
 *
 * Vive acá y no en el prompt de la IA a propósito: la recomendación forma parte del
 * informe técnico que firma el electricista, así que la decide el dominio y se puede
 * testear sin depender de un modelo externo. La IA que redacta el informe del cliente
 * solo la traduce a lenguaje llano.
 *
 * Criterio de redacción: infinitivo, una acción por hallazgo, y cuando corresponde una
 * segunda oración con el riesgo que evita. Un hallazgo confirmado lleva la acción; una
 * sugerencia de la app pendiente de validación lleva la verificación, no la acción.
 */
object InspectionFindingRecommendations {

    const val PILLAR_CONDUCTORS =
        "Reemplazar los conductores deteriorados del pilar y la acometida."

    const val PILLAR_PROTECTION_COMPATIBILITY =
        "Adecuar la protección a la sección del conductor: reemplazar la térmica por una de " +
            "corriente acorde al cable instalado, o reemplazar el conductor por uno de sección " +
            "acorde a la protección. Mientras no coincidan, la térmica puede no actuar antes de " +
            "que el cable se sobrecaliente."

    const val PANEL_DIFFERENTIAL_MISSING =
        "Instalar un interruptor diferencial en el tablero principal. Sin ese dispositivo la " +
            "instalación no cuenta con protección contra descargas eléctricas."

    const val PANEL_DIFFERENTIAL_FAILED =
        "Reemplazar el interruptor diferencial del tablero principal. Un diferencial que no " +
            "responde a la prueba manual no está protegiendo: hasta reemplazarlo la instalación " +
            "queda sin protección contra descargas eléctricas."

    const val PANEL_IMPROVISED_CONNECTIONS =
        "Rehacer los empalmes y las conexiones improvisadas con borneras o conectores adecuados."

    const val PANEL_OVERHEATING =
        "Desconectar el circuito afectado y revisar el punto de calentamiento antes de volver a " +
            "usarlo: ajustar o reemplazar bornes y conexiones y verificar la carga del circuito."

    const val PANEL_EXPOSED_PARTS =
        "Cerrar o aislar las partes expuestas y reemplazar los conductores con aislación dañada. " +
            "Hasta hacerlo hay riesgo de contacto eléctrico directo."

    const val PANEL_CONDUCTOR_COLORS =
        "Normalizar los colores de los conductores, para que cualquier intervención posterior se " +
            "haga sobre una instalación identificable."

    const val PANEL_GROUND_BAR_MISSING =
        "Instalar una bornera de tierra en el tablero principal y vincular a ella los conductores " +
            "de protección."

    const val PANEL_NEUTRAL_GROUND_NOT_SEPARATED =
        "Separar neutro y tierra en el tablero principal, cada uno en su propia bornera."

    const val PANEL_PROTECTION_CONDUCTORS_MISSING =
        "Instalar conductores de protección en los circuitos que no los tienen. Sin conductor de " +
            "protección, la puesta a tierra no llega a los artefactos."

    const val SUPPLY_VOLTAGE_OUT_OF_RANGE =
        "Repetir la medición en distintos momentos del día y, si el valor se mantiene fuera de " +
            "rango, reclamar la tensión de suministro al prestador del servicio."

    const val PROTECTION_CONDUCTOR_QUICK_CHECK =
        "Verificar el conductor de protección con una medición específica antes de sacar una " +
            "conclusión sobre su continuidad."

    const val GROUND_RESISTANCE_ABOVE_LIMIT =
        "Mejorar la puesta a tierra (agregar o reemplazar jabalina, revisar conexiones y " +
            "empalmes) y volver a medir la resistencia con telurómetro."

    const val CIRCUIT_CONSUMPTION_ABOVE_BREAKER =
        "Redistribuir las cargas del circuito o adecuar la protección y el conductor a la carga " +
            "real, y verificar que la térmica actúe correctamente."
}
