package de.mosimtech.common.jpa.converter

import de.mosimtech.common.core.unit.MeasureUnits
import jakarta.persistence.Converter

/** [QuantityConverter] für Spalten mit `Quantity<Mass>`; lehnt Größen anderer Art ab. */
@Converter(autoApply = false)
class MassQuantityConverter : QuantityConverter(MeasureUnits.KILOGRAM)
