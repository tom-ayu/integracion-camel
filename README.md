# EduConnect – Progreso 1 (Parte II)

Aplicación Apache Camel que lee las matrículas exportadas por el Sistema Académico (SA), valida cada registro, aplica las reglas de negocio y genera los archivos de integración para el Sistema Financiero (SF) y el reporte de errores.

Estilo de integración: **transferencia de archivos** (CSV por lotes). El SA y el SF no se modifican; ambos quedan desacoplados porque solo comparten el formato del archivo.

## Requisitos

- Java 17 o superior
- Maven 3.9 o superior

## Ejecución

```bash
mvn compile exec:java
```

La ruta procesa todo `input/*.csv` y la aplicación termina sola tras 5 segundos sin archivos nuevos.

## Entrada y salidas

| Archivo | Contenido |
|---|---|
| `input/matriculas.csv` | Entrada del SA. No se modifica ni se borra (`noop=true`) |
| `output/obligaciones_financieras.csv` | Registros válidos para el SF |
| `output/errores_matriculas.csv` | Registros rechazados con su `motivo_error` |
| `logs/` | Log con nombre de archivo, registros leídos, válidos y rechazados |

## Reglas implementadas

- **Validación** (`MatriculaValidator`): se informa el primer error en este orden: `ID_INVALIDO`, `NOMBRE_VACIO`, `CARRERA_VACIA`, `VALOR_INVALIDO`, `BECA_INVALIDA`, `ESTADO_INVALIDO`, `ESTUDIANTE_RETIRADO`.
- **Duplicados**: la primera aparición de un ID válido lo reserva, aunque la fila tenga otro error. Cualquier aparición posterior se rechaza como `ID_DUPLICADO`. Un ID inválido no reserva nada.
- **Cálculo** (`ObligacionFinanciera`): la beca `S` aplica un 15 % de descuento y la beca `N`, ninguno. Se usa `BigDecimal` con redondeo `HALF_UP` a dos decimales.
- **Continuidad**: un registro inválido no detiene el procesamiento del resto. Se conserva el orden de entrada en ambas salidas.

## Estructura

```
src/main/java/tomas/ontaneda/integracion/
  Application.java          arranque de Camel Main
  MatriculasRoute.java      ruta: lectura, validación, cálculo y escritura
  MatriculaCsvParser.java   CSV -> registros
  MatriculaValidator.java   validaciones y duplicados
  ObligacionFinanciera.java regla financiera
  Matricula.java, MatriculaRechazada.java, MotivoError.java
src/main/resources/logback.xml
```
