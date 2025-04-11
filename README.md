<div align="center">

<p></p>

<a href="#-introducción">Introducción</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-funcionalidad">Funcionalidad</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-stack">Stack</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-instalación-y-ejecución">Instalación y Ejecución</a>
<span>&nbsp;&nbsp;❖&nbsp;&nbsp;</span>
<a href="#-licencia">Licencia</a>

<p align="center">
  <img src="src/main/resources/images/triager.png" width="10%" height="10%" alt="Logo de Triager">
</p>


![Redis Badge](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white&style=flat)
![GitHub stars](https://img.shields.io/github/stars/mgrl39/triager)
![GitHub issues](https://img.shields.io/github/issues/mgrl39/triager)
![GitHub license](https://img.shields.io/github/license/mgrl39/triager)

</div>

## 🧑‍🚀 Introducción

**Triager** es una aplicación de escritorio en **Java** que simula un sistema de triaje hospitalario, permitiendo el ingreso de pacientes con su nombre, edad y síntomas. Según un protocolo de urgencias, cada paciente será asignado a un nivel de prioridad y almacenado en una cola de espera. Los pacientes serán atendidos en orden de prioridad y eliminados una vez atendidos.

### Tecnología de Persistencia Seleccionada: Redis

Se ha elegido **Redis** como tecnología de persistencia debido a su alto rendimiento y facilidad de uso como base de datos clave-valor. Redis es ideal para manejar las listas de pacientes y sus niveles de urgencia de manera eficiente.

---

## 🌍 Funcionalidad

- **Ingreso de Pacientes**: Permite ingresar pacientes con su nombre, edad y síntomas, asignándoles un nivel de urgencia (🔴 Rojo crítico, 🟡 Amarillo urgente, 🟢 Verde leve).
  
- **Cola de Pacientes**: Los pacientes son colocados en una cola según su nivel de urgencia. Los pacientes más urgentes serán atendidos primero.

- **Atención de Pacientes**: Los pacientes son atendidos en orden de prioridad y eliminados de la lista de espera una vez atendidos.

---

## 🏗️ Stack

- **Java 21** - Versión requerida del JDK para la ejecución de la aplicación.
- **Spring Boot 3.4.4** - Framework para la construcción de la aplicación.
- **Spring Data Redis** - Integración con Redis a través de Spring.
- **Jedis 5.2.0** - Cliente de Redis en Java (usado internamente por Spring Data Redis).
- **dotenv-java 3.0.0** - Para gestión de variables de entorno.

---

## 🚀 Instalación y Ejecución

### Requisitos Previos
- Java 21 o superior
- Maven 3.6 o superior
- Redis 6.0 o superior (local o remoto)

### Instalación de Redis

Se proporcionan tres scripts para diferentes escenarios de instalación de Redis:

1. **Instalación Directa** (`tools/install_redis_direct.sh`):
   - Instala Redis directamente en tu sistema
   - Configura Redis para aceptar conexiones externas
   - Ideal para desarrollo local

2. **Instalación en Contenedor LXC** (`tools/install_redis_lxc.sh`):
   - Crea un contenedor LXC con Ubuntu 20.04
   - Instala y configura Redis dentro del contenedor
   - Expone el puerto 6379 para conexiones externas
   - Perfecto para entornos de prueba aislados

3. **Instalación de RedisInsight** (`tools/install_redis_viewer.sh`):
   - Instala RedisInsight, una GUI oficial de Redis
   - Permite visualizar y gestionar datos de Redis de forma gráfica
   - Requiere Flatpak instalado en el sistema

Para usar cualquiera de estos scripts:
```bash
# Dar permisos de ejecución
chmod +x tools/install_redis_*.sh

# Ejecutar el script deseado
./tools/install_redis_direct.sh    # Para instalación directa
./tools/install_redis_lxc.sh       # Para instalación en contenedor
./tools/install_redis_viewer.sh    # Para instalar RedisInsight
```

### Compilación e Instalación

Para compilar e instalar todas las dependencias:

```bash
mvn clean install
```

### Ejecución

Para ejecutar la prueba de conexión con Redis:

```bash
mvn exec:java -Dexec.mainClass="net.elpuig.triager.RedisTest"
```

Para iniciar la aplicación completa:

```bash
mvn exec:java -Dexec.mainClass="net.elpuig.triager.TriagerApplication"
```

---

## 📄 Licencia

[MIT](LICENSE)
