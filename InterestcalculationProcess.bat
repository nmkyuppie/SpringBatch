@ECHO OFF
TITLE Interest Calculation Processing Batch Application start %cd%
SET version=1.0

start /b java -Xms1G -Xmx2G -XX:MaxMetaspaceSize=512M -jar -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=9000,suspend=n -jar  -Dspring.config.location=props\ -Dlogging.config=props\logback-spring.xml  .\target\interestcalculation-%version%.jar interestCalculationEvent

