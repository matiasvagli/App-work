package com.matiasdev.elecapp.features.electricaltools.domain.diagrams

import com.matiasdev.elecapp.features.electricaltools.domain.ComponentTerminal
import com.matiasdev.elecapp.features.electricaltools.domain.DiagramType
import com.matiasdev.elecapp.features.electricaltools.domain.ElectricalDiagram
import com.matiasdev.elecapp.features.electricaltools.domain.WiringConnectionStep

val protectionsDiagram = ElectricalDiagram(
    id = "termomagneticas-y-disyuntores",
    type = DiagramType.CIRCUIT_BREAKERS_AND_RCD,
    title = "Termomagnéticas y Disyuntores (Tipos AC, A, F, B)",
    subtitle = "Funcionamiento térmico/magnético, curvas B-C-D, ecuaciones de disparo y tipos de disyuntores diferenciales.",
    category = "Protecciones y Seguridad",
    badgeText = "IEC 60898 / 61008",
    requiredVoltage = "220Vca / 380Vca (Monofásica y Trifásica)",
    safetyVoltageNote = "Coordinación obligatoria: Ib ≤ In ≤ Iz (Carga ≤ Térmica ≤ Cable) y Disyuntor diferencial de cabecera.",
    requiredComponents = listOf(
        "Interruptor Termomagnético (PIA / Térmica) Curvas B, C o D",
        "Interruptor Diferencial (Disyuntor) Tipo AC, Tipo A, Tipo F o Tipo B de 30 mA",
        "Barra colectora tipo peine (bipolar o tetrapolar)",
        "Gabinete normalizado DIN con riel simétrico",
    ),
    terminalLegend = listOf(
        ComponentTerminal("Bimetal (Térmico)", "Protección por Sobrecarga", "Efecto Joule calienta el bimetal produciendo su flexión. Disparo temporizado según 1,13 In a 1,45 In."),
        ComponentTerminal("Bobina (Magnético)", "Protección por Cortocircuito", "Electroimán que atrae el percutor en milisegundos ante corrientes elevadas (5 a 10 In en Curva C)."),
        ComponentTerminal("Disyuntor Tipo AC", "Fuga Alterna Pura", "Detecta solo corrientes senoidales alternas puras 50/60 Hz."),
        ComponentTerminal("Disyuntor Tipo A", "Alterna + Pulsante", "Detecta alterna senoidal y corriente continua pulsante (viviendas con electrónica y lavarropas)."),
        ComponentTerminal("Disyuntor Tipo F", "Frecuencia Variable", "Detecta además corrientes de frecuencia variable monofásica hasta 1 kHz (motores Inverter)."),
        ComponentTerminal("Disyuntor Tipo B", "Universal / DC Pura", "Detecta todo tipo de fugas: alterna, pulsante, continua pura DC y alta frecuencia (EV y Solar)."),
    ),
    stepByStepGuide = listOf(
        WiringConnectionStep(
            stepNumber = 1,
            title = "Llegada de Acometida al Interruptor de Cabecera",
            wireName = "Línea Principal (Fase + Neutro)",
            wireColorHex = 0xFF795548,
            fromTerminal = "Línea Seccional desde el Medidor",
            toTerminal = "Bornes superiores (1 y N) de la Termomagnética General",
            description = "Conectar la alimentación a los bornes superiores de entrada de la protección general.",
        ),
        WiringConnectionStep(
            stepNumber = 2,
            title = "Puente de la Térmica al Disyuntor Diferencial",
            wireName = "Puente de potencia (4 a 10 mm²)",
            wireColorHex = 0xFF212121,
            fromTerminal = "Bornes inferiores (2 y N) de la Térmica General",
            toTerminal = "Bornes superiores (1 y N) del Disyuntor Diferencial",
            description = "El disyuntor debe quedar siempre protegido aguas arriba por la termomagnética o tener corriente nominal igual o mayor (ej. Disyuntor 40A protegido con térmica de 32A o 25A).",
        ),
        WiringConnectionStep(
            stepNumber = 3,
            title = "Distribución desde el Disyuntor a Térmicas de Circuitos",
            wireName = "Peine de distribución / Peines DIN",
            wireColorHex = 0xFF0288D1,
            fromTerminal = "Bornes inferiores del Disyuntor Diferencial",
            toTerminal = "Bornes superiores de las Térmicas de circuitos (IUG, TUG, TUE)",
            description = "Repartir la fase y el neutro hacia las termomagnéticas individuales de cada circuito terminal.",
        ),
    ),
    practicalTips = listOf(
        "LÍMITES DE CORRIENTE CURVA C: A 1,13 In (18A para 16A) NO debe disparar en 1 hora. A 1,45 In (23,2A para 16A) DEBE disparar en menos de 1 hora. A 2,55 In (40,8A) dispara entre 1 y 60 seg. A 5 In (80A) el disparo es instantáneo.",
        "DISYUNTORES EN CASAS MODERNAS: Con el auge de fuentes conmutadas, lavarropas electrónicos, microondas e inducción, la AEA recomienda interruptores diferenciales TIPO A en lugar de Tipo AC.",
        "EQUIPOS INVERTER: Los aires acondicionados y heladeras Inverter generan corrientes residuales compuestas de alta frecuencia que pueden cegar o hacer saltar intempestivamente un disyuntor Tipo AC común. Para ellos se utiliza TIPO F.",
        "CARGADORES DE AUTOS ELÉCTRICOS Y SOLAR: Los cargadores EV y los inversores solares requieren obligatoriamente TIPO B para detectar corrientes de fuga en corriente continua pura (DC) que saturan los núcleos toroidales convencionales.",
    ),
    securityWarning = "Pulsar el botón 'TEST' mensual del disyuntor diferencial para asegurar el correcto funcionamiento mecánico del mecanismo de desenganche.",
    aeaReference = "AEA 90364-7-771.19 / Normas IEC 60898-1 (Termomagnéticas) e IEC 61008-1 / 62423 (Diferenciales).",
)
