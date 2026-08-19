package com.matiasdev.elecapp.features.electricaltools.domain.diagrams

import com.matiasdev.elecapp.features.electricaltools.domain.ComponentTerminal
import com.matiasdev.elecapp.features.electricaltools.domain.DiagramType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalDiagram
import com.matiasdev.elecapp.features.electricaltools.domain.WiringConnectionStep

val generalDistributionDiagram = ElectricalDiagram(
    id = "distribucion-general-aea-90364",
    type = DiagramType.GENERAL_DISTRIBUTION,
    title = "Distribución General y Tableros (AEA 90364)",
    subtitle = "Jerarquía de tableros, límites de distribuidora, tipos de cajas y reglas de cañería.",
    category = "Normativa y Arquitectura",
    badgeText = "AEA 90364",
    requiredVoltage = "220V / 380V (Monofásica / Trifásica)",
    safetyVoltageNote = "Delimitación de responsabilidad: Red pública vs Instalación interna del usuario.",
    requiredComponents = listOf(
        "Línea de Alimentación de Distribuidora (RAD / RDD)",
        "Dispositivo de Protección de Línea de Alimentación (DPLA / Fusibles aéreos)",
        "Gabinete de Medidor (M)",
        "Tablero Principal (TP) a menos de 2 metros del medidor",
        "Línea Seccional (CT) con aislamiento reforzado",
        "Tablero Seccional General (TSG) con disyuntor e interruptores de circuitos",
        "Subtableros Seccionales (TS1, TS2, etc. cada uno con su propio disyuntor diferencial)",
        "Jabalina y electrodo de Puesta a Tierra (PE)",
    ),
    terminalLegend = listOf(
        ComponentTerminal("RAD / RDD", "Red de Alimentación / Distribución", "Red pública de baja tensión de la empresa distribuidora (Edenor, Edesur, cooperativa)."),
        ComponentTerminal("DPLA / LAD", "Protección de Acometida", "Fusibles o seccionador de protección de la línea de acometida."),
        ComponentTerminal("Medidor (M)", "Medición de Energía", "Equipo de medición de consumo. Límite de responsabilidad de la distribuidora."),
        ComponentTerminal("Tablero Principal (TP)", "Cabecera del Inmueble", "Primer tablero del usuario, con interruptor termomagnético general a ≤ 2 m del medidor."),
        ComponentTerminal("Tablero Seccional Gral (TSG)", "Distribución Interna", "Tablero seccional principal del inmueble. Distribuye hacia circuitos terminales o subtableros."),
        ComponentTerminal("Subtableros (TS1, TS2)", "Tableros Seccionales", "Tableros derivados (ej. Quincho, Bomba, Planta Alta). Cada tablero debe poseer su propio disyuntor."),
    ),
    stepByStepGuide = listOf(
        WiringConnectionStep(
            stepNumber = 1,
            title = "Acometida desde Red Distribuidora (RAD)",
            wireName = "Acometida (Fases + Neutro)",
            wireColorHex = 0xFF795548,
            fromTerminal = "Red pública (RAD / RDD)",
            toTerminal = "DPLA / LAD -> Medidor (M)",
            description = "Tendido de acometida aérea o subterránea de la distribuidora hasta la caja de toma y medidor.",
        ),
        WiringConnectionStep(
            stepNumber = 2,
            title = "Línea Principal al Tablero Principal (TP)",
            wireName = "Línea Principal (Fases + Neutro)",
            wireColorHex = 0xFF795548,
            fromTerminal = "Bornera de salida del Medidor",
            toTerminal = "Interruptor de cabecera del Tablero Principal (TP)",
            description = "Conexión del medidor al TP. La distancia máxima no debe superar los 2 metros según AEA 90364.",
        ),
        WiringConnectionStep(
            stepNumber = 3,
            title = "Línea Seccional (CT) al Tablero Seccional General (TSG)",
            wireName = "Línea Seccional + Conductor PE",
            wireColorHex = 0xFF4CAF50,
            fromTerminal = "Salida del Tablero Principal (TP)",
            toTerminal = "Entrada del Tablero Seccional General (TSG)",
            description = "Tendido de la línea seccional hacia el interior de la vivienda o local con conductor de puesta a tierra continuo.",
        ),
        WiringConnectionStep(
            stepNumber = 4,
            title = "Distribución a Circuitos y Subtableros",
            wireName = "Líneas de Circuitos Terminales",
            wireColorHex = 0xFF03A9F4,
            fromTerminal = "Salida de disyuntores / térmicas en TSG",
            toTerminal = "Cajas de paso, derivación y bocas (o Subtableros TS1, TS2)",
            description = "Alimentación de circuitos de iluminación (IUG/IUE), tomas (TUG/TUE) y subtableros independientes.",
        ),
    ),
    practicalTips = listOf(
        "TIRADAS HORIZONTALES: Máximo 12 metros de cañería continua entre cajas de paso/derivación para permitir el correcto cableado.",
        "TIRADAS VERTICALES: Máximo 15 metros continuos entre cajas.",
        "MÁXIMO DE CURVAS: No más de 3 curvas de 90° (máx 270° totales) por tramo de cañería entre cajas.",
        "BOCA DE EFECTO: Caja destinada exclusivamente a alojar llaves, interruptores, pulsadores o comandos de control.",
        "BOCA TERMINAL: Caja donde se conecta un receptor de energía (tomacorriente, centro de luz, aplique, extractor, ventilador).",
        "DISYUNTORES EN SUBTABLEROS: Todo subtablero seccional derivado (quincho, bomba, pileta, taller) debe contar con su propio interruptor diferencial para selectividad y seguridad local.",
    ),
    securityWarning = "La frontera de responsabilidad divide lo que corresponde a la distribuidora eléctrica del ámbito reglamentario del instalador matriculado y propietario.",
    aeaReference = "Reglamentación AEA 90364 Parte 7 - Sección 771 (Instalaciones Eléctricas en Inmuebles).",
)
