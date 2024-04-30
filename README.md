<div style="display: flex; flex-direction: column; align-items: center">
    <h1>Memify</h1>
    <img alt="Logo de Memify" width="256" height="256" src="./src/main/resources/static/images/MemifyLogo.jpg" />
    <h4>Crear y viralizar memes nunca ha sido tán fácil.</h4>
</div>

## Manual de desarrollador
### Terminal

Para comenzar, asegúrate de tener instalados en tu sistema operativo el gestor de paquetes Maven y JDK 17+ (puedes encontrar los enlaces de descarga en la bibliografía). Al terminar, sigue estos pasos:

1. Clona el repositorio de GitHub: `git clone https://github.com/adrgarcha/memify`.
2. Abre un terminal en el directorio raíz del proyecto y ejecuta el comando `mvn clean package`.
3. Después de la construcción del proyecto, ejecuta el comando `java -jar ./target/memify-1.0.0.jar` en el terminal para iniciar la aplicación.

### IDE

Para esta alternativa también será necesario tener instalado Maven y JDK 17+. Después debe seguir los siguientes pasos:

1. Clonar el repositorio de GitHub: https://github.com/adrgarcha/memify
2. Haz clic derecho en el archivo **MemifyApplication.java** y selecciona **Run** para ejecutar la aplicación.

### Docker

En este procedimiento, no es necesario contar con ninguna instalación previa. Sin embargo, es indispensable tener Docker instalado para ejecutar los comandos necesarios en el terminal.

1. Clonar el repositorio de GitHub: https://github.com/adrgarcha/memify
2. Ejecutar el siguiente comando desde el directorio raíz del proyecto: `docker build -t memify .`.
3. Ejecutar el siguiente comando desde el mismo directorio: `docker run -p 8282:8282 memify`.

## Manual de despliegue

Para desplegar la aplicación en tu servicio de hosting preferido, se ha creado un archivo Dockerfile, que permite ejecutar la aplicación de forma sencilla en un entorno aislado y sin problemas de dependencias. Suponiendo que el servicio de alojamiento elegido admita la integración con repositorios de GitHub (una característica común en la mayoría de los servicios de hosting), podemos configurarlo para que la aplicación se despliegue automáticamente cuando se realicen cambios en la rama seleccionada.

Los pasos o la configuración a seguir en el servicio de hosting serían los siguientes:

1. Indicar en el servicio de hosting seleccionado que se va a utilizar un **Dockerfile** y la ruta donde se encuentra dicho archivo.
2. Ejecutar el siguiente comando para construir el contenedor de Docker: `docker build -t memify .`.
3. Ejecutar el siguiente comando para iniciar el contenedor: `docker run -p 8282:8282 memify`.

Sin embargo, en algunos servicios de hosting, la configuración puede ser tan simple como indicar el Dockerfile que se utilizará y su ubicación para tener el proyecto completamente configurado.
