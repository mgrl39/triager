<div align="center">

<p></p>

<a href="#-introducción">Introducción</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-uso">Uso</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-stack">Stack</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-licencia">Licencia</a>

![Redis Badge](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white&style=flat)
![GitHub stars](https://img.shields.io/github/stars/tu_usuario/triage-system)
![GitHub issues](https://img.shields.io/github/issues/tu_usuario/triage-system)
![GitHub license](https://img.shields.io/github/license/tu_usuario/triage-system)

</div>

## 🧑‍🚀 Introducción

**Triage System** es una aplicación simple desarrollada en **Node.js** que simula un sistema de triaje hospitalario, permitiendo el ingreso de pacientes y asignación de niveles de urgencia según un protocolo predefinido.

### Tecnología de Persistencia Seleccionada: Redis

Para esta prueba de concepto, hemos seleccionado **Redis** como tecnología de persistencia. Redis es una base de datos **NoSQL**, tipo clave-valor, conocida por su alto rendimiento y confiabilidad. A diferencia de las bases de datos relacionales, Redis ofrece almacenamiento en memoria, lo que permite operaciones de lectura y escritura extremadamente rápidas. Además, es un sistema ampliamente utilizado y mantenido desde 2009.

---

## 🌍 Uso

### ➡️ Ingreso de Pacientes:

#### Endpoint:
```http
POST /api/v1/pacientes
```

#### Ejemplo de cuerpo de la solicitud:
```json
{
  "nombre": "Juan Pérez",
  "edad": 45,
  "síntomas": "Dolor en el pecho"
}
```

#### Ejemplo de respuesta:
```json
{
  "mensaje": "Paciente ingresado correctamente",
  "nivel_urgencia": "🔴 Rojo crítico"
}
```

### ➡️ Visualizar lista de pacientes por urgencia:

#### Endpoint:
```http
GET /api/v1/pacientes
```

#### Ejemplo de respuesta:
```json
[
  {
    "nombre": "Juan Pérez",
    "edad": 45,
    "síntomas": "Dolor en el pecho",
    "nivel_urgencia": "🔴 Rojo crítico"
  },
  {
    "nombre": "Ana García",
    "edad": 30,
    "síntomas": "Dolor de cabeza",
    "nivel_urgencia": "🟡 Amarillo urgente"
  }
]
```

---

## 🏗️ Stack

Este proyecto está desarrollado con:

- **Node.js** - Entorno de ejecución para JavaScript.
- **Redis** - Base de datos NoSQL de alto rendimiento utilizada para persistir la información de los pacientes.
- **Express.js** - Framework minimalista para la creación de APIs REST en Node.js.
- **JavaScript** - Lenguaje de programación utilizado para la implementación del sistema.

---

## 🔑 Licencia

Este proyecto está bajo la licencia [MIT](./LICENSE).
