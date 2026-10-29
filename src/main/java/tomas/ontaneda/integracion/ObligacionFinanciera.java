package tomas.ontaneda.integracion;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record ObligacionFinanciera(
        String estudianteId,
        String nombre,
        String concepto,
        BigDecimal valorOriginal,
        BigDecimal descuento,
        BigDecimal total) {

    private static final BigDecimal PORCENTAJE_BECA = new BigDecimal("0.15");

    //beca S aplica 15% de descuento, beca N aplica 0, redondeo half_up a dos decimales
    public static ObligacionFinanciera desde(Matricula m) {
        BigDecimal valor = new BigDecimal(m.valorMatricula().trim()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal descuento = m.beca().trim().equals("S")
                ? valor.multiply(PORCENTAJE_BECA).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        return new ObligacionFinanciera(
                m.idEstudiante().trim(),
                m.nombre().trim(),
                "MATRICULA",
                valor,
                descuento,
                valor.subtract(descuento));
    }

    public String toCsv() {
        return String.join(",", estudianteId, nombre, concepto,
                valorOriginal.toPlainString(), descuento.toPlainString(), total.toPlainString());
    }
}
