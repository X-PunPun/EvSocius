# Microservicio Pokemon - Arquitectura Hexagonal + Apache Camel

Este proyecto es una implementación avanzada de un servicio REST que consume la PokeAPI utilizando Apache Camel para la orquestación y Spring Boot con Arquitectura Hexagonal.

## Requerimientos
* Java 17 o superior
* Maven 3.9+

## Instrucciones de Ejecución

1.  Clonar el repositorio o descomprimir el proyecto.
2.  Abrir una terminal en la carpeta raíz.
3.  Ejecutar el comando:
    bash
    mvn spring-boot:run
    
4.  El servidor iniciará en el puerto *8080*.

## Documentación API (Swagger)
Una vez iniciado, puede ver la documentación interactiva y probar los endpoints aquí:
* http://localhost:8080/swagger-ui.html

## Endpoints Disponibles
* *Por Tipo:* GET /api/v1/pokemon/type/{type}
* *Por Defensa:* GET /api/v1/pokemon/defense?min=50&type=steel
* *Por Peso:* GET /api/v1/pokemon/weight?min=10&max=100&type=normal
* *Por Experiencia:* GET /api/v1/pokemon/exp?min=100&type=psychic
