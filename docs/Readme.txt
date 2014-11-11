TPE Protocolos de comuncación

El proyecto utiliza maven. Por lo tanto para correrlo ejecutarlo como mvn install. Esto instalará todas las dependencias y compilará el proyecto. Asegurarse de tener todos los permisos necesarios ya que el programa creara la carpeta logs y la carpeta la carpeta config.

Para ejecutar el programa se debe correr en una terminal con el comando java -jar. Deben estar presentes los archivos de configuración l33tTransformation.xml, stats.xml, config.xml y users.xml para que el servidor pueda iniciar. Estos archivos se generaran automáticamente cuando empiece el servidor. En caso de no hacerlo, deberían estar presentes en la carpeta config en el directorio de trabajo actual.


Ejemplos de los archivos de configuración.


<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<params>
	<pop3Port>4545</pop3Port>
	<rcpPort>4546</rcpPort>
	<greeting>Proxy Server ready.</greeting>
	<pop3Host>localhost</pop3Host>
	<rcpHost>localhost</rcpHost>
	<defaultPOP3Server>localhost</defaultPOP3Server>
	<pop3BufferSize>4096</pop3BufferSize>
	<capaList>USER</capaList>
	<password>password</password>
	<multiplexingEnabled>true</multiplexingEnabled>
	<l33tEnabled>true</l33tEnabled>
</params>


users.xml:


<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<users>
	<userMap>
		<entry>
			<key>mike@aol.com</key>
			<value>localhost</value>
		</entry>
		<entry>
			<key>federicotedin@gmail.com</key>
			<value>192.168.1.4</value>
		</entry>
	</userMap>
</users>


l33tTransformations.xml:

<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<l33tTransformations>
	<transformations>
		<entry>
		<key>e</key>
		<value>3</value>
		</entry>
		<entry>
		<key>r</key>
		<value>8</value>
		</entry>
		<entry>
		<key>c</key>
		<value>&lt;</value>
		</entry>
		</transformations>
</l33tTransformations>

stats.xml

<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<stats>
    <bytes>0</bytes>
    <accessCount>0</accessCount>
</stats>
