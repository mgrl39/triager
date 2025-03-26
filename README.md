<div align="center">

<p></p>

<a href="#-introducción">Introducción</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-funcionalidad">Funcionalidad</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-stack">Stack</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-licencia">Licencia</a>

![Redis Badge](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white&style=flat)
![GitHub stars](https://img.shields.io/github/stars/mgrl39/triager)
![GitHub issues](https://img.shields.io/github/issues/mgrl39/triager)
![GitHub license](https://img.shields.io/github/license/mgrl39/triager)

</div>

## 🧑‍🚀 Introducción

**Triage System** es una aplicación de escritorio en **Java** que simula un sistema de triaje hospitalario, permitiendo el ingreso de pacientes con su nombre, edad y síntomas. Según un protocolo de urgencias, cada paciente será asignado a un nivel de prioridad y almacenado en una cola de espera. Los pacientes serán atendidos en orden de prioridad y eliminados una vez atendidos.

### Tecnología de Persistencia Seleccionada: Redis

Se ha elegido **Redis** como tecnología de persistencia debido a su alto rendimiento y facilidad de uso como base de datos clave-valor. Redis es ideal para manejar las listas de pacientes y sus niveles de urgencia de manera eficiente.

---

## 🌍 Funcionalidad

- **Ingreso de Pacientes**: Permite ingresar pacientes con su nombre, edad y síntomas, asignándoles un nivel de urgencia (🔴 Rojo crítico, 🟡 Amarillo urgente, 🟢 Verde leve).
  
- **Cola de Pacientes**: Los pacientes son colocados en una cola según su nivel de urgencia. Los pacientes más urgentes serán atendidos primero.

- **Atención de Pacientes**: Los pacientes son atendidos en orden de prioridad y eliminados de la lista de espera una vez atendidos.

---

## 🏗️ Stack

- **Java** - Lenguaje de programación utilizado para la implementación de la aplicación.
- **Redis** - Base de datos NoSQL utilizada para almacenar y gestionar las listas de pacientes y niveles de urgencia.
- **Jedis** - Cliente de Redis en Java utilizado para interactuar con la base de datos.

---

## 🔑 Licencia

Este proyecto está bajo la licencia [MIT](./LICENSE).
<p></p>

<a href="#-introducción">Introducción</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-funcionalidad">Funcionalidad</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-stack">Stack</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-licencia">Licencia</a>

![Redis Badge](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white&style=flat)
![GitHub stars](https://img.shields.io/github/stars/mgrl39/triager)
![GitHub issues](https://img.shields.io/github/issues/mgrl39/triager)
![GitHub license](https://img.shields.io/github/license/mgrl39/triager)

</div>

## 🧑‍🚀 Introducción

**Triage System** es una aplicación de escritorio en **Java** que simula un sistema de triaje hospitalario, permitiendo el ingreso de pacientes con su nombre, edad y síntomas. Según un protocolo de urgencias, cada paciente será asignado a un nivel de prioridad y almacenado en una cola de espera. Los pacientes serán atendidos en orden de prioridad y eliminados una vez atendidos.

### Tecnología de Persistencia Seleccionada: Redis

Se ha elegido **Redis** como tecnología de persistencia debido a su alto rendimiento y facilidad de uso como base de datos clave-valor. Redis es ideal para manejar las listas de pacientes y sus niveles de urgencia de manera eficiente.

---

## 🌍 Funcionalidad

- **Ingreso de Pacientes**: Permite ingresar pacientes con su nombre, edad y síntomas, asignándoles un nivel de urgencia (🔴 Rojo crítico, 🟡 Amarillo urgente, 🟢 Verde leve).
  
- **Cola de Pacientes**: Los pacientes son colocados en una cola según su nivel de urgencia. Los pacientes más urgentes serán atendidos primero.

- **Atención de Pacientes**: Los pacientes son atendidos en orden de prioridad y eliminados de la lista de espera una vez atendidos.

---

## 🏗️ Stack

- **Java** - Lenguaje de programación utilizado para la implementación de la aplicación.
- **Redis** - Base de datos NoSQL utilizada para almacenar y gestionar las listas de pacientes y niveles de urgencia.
- **Jedis** - Cliente de Redis en Java utilizado para interactuar con la base de datos.

---

## 🔑 Licencia

Este proyecto está bajo la licencia [MIT](./LICENSE).

