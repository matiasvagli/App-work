package com.matiasdev.elecapp.features.electricaltools.domain.diagrams

import com.matiasdev.elecapp.features.electricaltools.domain.ComponentTerminal
import com.matiasdev.elecapp.features.electricaltools.domain.DiagramType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalDiagram
import com.matiasdev.elecapp.features.electricaltools.domain.WiringConnectionStep

val doorbellAndLockDiagram = ElectricalDiagram(
    id = "timbre-cerradura-electrica-12v",
    type = DiagramType.DOORBELL_AND_ELECTRIC_LOCK,
    title = "Timbre y Cerradura Eléctrica (12V MBTS)",
    subtitle = "Campanilla electromagnética, pulsador exterior seguro, destraba-pestillo y cerradura de sobreponer.",
    category = "Seguridad y Comunicación",
    badgeText = "Seguridad 12V",
    requiredVoltage = "Primario 220Vca / Secundario 12Vca (o 12Vcc)",
    safetyVoltageNote = "Pulsadores exteriores e intemperie en Muy Baja Tensión de Seguridad (MBTS) para evitar descargas eléctricas.",
    requiredComponents = listOf(
        "Transformador de seguridad 220Vca / 12Vca (mínimo 15VA a 30VA para timbres o 1A/2A para cerraduras)",
        "Campanilla electromagnética con bobina de 12V (martillo percutor)",
        "Pulsador de timbre exterior normal abierto (NA)",
        "Pestillo eléctrico de embutir (destraba-pestillo) o Cerradura eléctrica de sobreponer",
        "Pulsador de apertura interior o contacto de portero eléctrico / control de acceso",
        "Cables paralelos tipo bipolar (sección 0,75 mm² a 1,5 mm² según distancia)",
    ),
    terminalLegend = listOf(
        ComponentTerminal("Primario 220V", "Alimentación Trafo", "Fase y Neutro de 220V protegidos con termomagnética."),
        ComponentTerminal("Secundario 12Vca", "Salida MBTS", "Bornes de 12Vca libres de potencial peligroso hacia pulsadores y bobinas."),
        ComponentTerminal("Pulsador NA (Exterior)", "Comando de Timbre", "Pulsador de contacto momentáneo en la puerta de calle."),
        ComponentTerminal("Bobina de Campanilla", "Percutor Sonoro", "Electroimán que mueve el martillo contra la campana al recibir 12V."),
        ComponentTerminal("Pestillo / Cerradura", "Solenoide de Destrabe", "Electroimán que libera la traba mecánica al recibir el pulso de 12V."),
    ),
    stepByStepGuide = listOf(
        WiringConnectionStep(
            stepNumber = 1,
            title = "Alimentación del Transformador (220V)",
            wireName = "Fase y Neutro 220V (1,5 mm²)",
            wireColorHex = 0xFF795548,
            fromTerminal = "Salida del circuito del tablero",
            toTerminal = "Bornes primarios (0-220V) del Transformador",
            description = "Conectar los 220V a los terminales de entrada del transformador.",
        ),
        WiringConnectionStep(
            stepNumber = 2,
            title = "Tendido de 12V hacia el Pulsador de Calle",
            wireName = "Línea de 12V al Pulsador",
            wireColorHex = 0xFF9C27B0,
            fromTerminal = "Uno de los bornes secundarios de 12V",
            toTerminal = "Borne de entrada del Pulsador de timbre / calle",
            description = "Llevar un polo de 12V hacia el pulsador exterior.",
        ),
        WiringConnectionStep(
            stepNumber = 3,
            title = "Retorno del Pulsador a la Bobina del Timbre / Pestillo",
            wireName = "Retorno de pulso 12V",
            wireColorHex = 0xFFE65100,
            fromTerminal = "Borne de salida del Pulsador",
            toTerminal = "Borne 1 de la Bobina del Timbre o Cerradura",
            description = "El retorno lleva los 12V cuando la persona presiona el botón.",
        ),
        WiringConnectionStep(
            stepNumber = 4,
            title = "Cierre del Circuito de 12V al Transformador",
            wireName = "Retorno 0V / Común 12V",
            wireColorHex = 0xFF0288D1,
            fromTerminal = "Borne 2 de la Bobina del Timbre o Cerradura",
            toTerminal = "El otro borne secundario de 12V del Transformador",
            description = "Cerrar el circuito directo al transformador para completar la malla de 12Vca.",
        ),
    ),
    practicalTips = listOf(
        "SEGURIDAD OBLIGATORIA (MBTS): Nunca llevar 220V directo a un pulsador de timbre colocado en la intemperie, vereda o reja metálica. La lluvia y humedad pueden transferir tensión al usuario o peatones.",
        "POTENCIA DEL TRAFO PARA CERRADURAS: Las cerraduras eléctricas y pestillos consumen entre 0.8A y 1.5A en el instante de apertura. Si el transformador es chico (ej. solo 5VA), el pestillo vibrará ('zumbido') pero no tendrá fuerza magnética para destrabar.",
        "SECCIÓN DE CABLE POR DISTANCIA: En casas grandes con el timbre o frente a más de 20-30 metros, la baja tensión (12V) sufre caída de tensión severa. Utilizar cable de 1 mm² o 1,5 mm² para evitar que el timbre suene débil o el pestillo no abra.",
        "MODELOS DE CERRADURAS: Pestillo de embutir (se reemplaza en el marco de la puerta existente) y Cerradura de sobreponer (se atornilla sobre la hoja metálica o de madera con botón manual y cilindro exterior).",
    ),
    securityWarning = "Verificar que el transformador cuente con aislamiento galvánico de seguridad (bobinados separados primario/secundario según IRAM 2099 / AEA).",
    aeaReference = "Reglamentación AEA 90364-7-771.18 (Circuitos de muy baja tensión de seguridad MBTS en inmuebles).",
)
