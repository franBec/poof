package dev.pollito.poof.util;

public class DependencyUtil {
  private DependencyUtil() {}

  public static final String CONSUMER_EXECUTION_TEMPLATE =
      "<execution>\r\n\t\t\t\t\t\t<id>consumer generation - <!--name--></id>\r\n\t\t\t\t\t\t<goals>\r\n\t\t\t\t\t\t\t<goal>generate</goal>\r\n\t\t\t\t\t\t</goals>\r\n\t\t\t\t\t\t<configuration>\r\n\t\t\t\t\t\t\t<inputSpec>${project.basedir}/src/main/resources/openapi/<!--name-->.yaml</inputSpec>\r\n\t\t\t\t\t\t\t<generatorName>java</generatorName>\r\n\t\t\t\t\t\t\t<library>feign</library>\r\n\t\t\t\t\t\t\t<output>${project.build.directory}/generated-sources/openapi/</output>\r\n\t\t\t\t\t\t\t<apiPackage><!--apiPackage--></apiPackage>\r\n\t\t\t\t\t\t\t<modelPackage><!--modelPackage--></modelPackage>\r\n\t\t\t\t\t\t\t<configOptions>\r\n\t\t\t\t\t\t\t\t<feignClient>true</feignClient>\r\n\t\t\t\t\t\t\t\t<interfaceOnly>true</interfaceOnly>\r\n\t\t\t\t\t\t\t\t<useEnumCaseInsensitive>true</useEnumCaseInsensitive>\r\n\t\t\t\t\t\t\t</configOptions>\r\n\t\t\t\t\t\t</configuration>\r\n\t\t\t\t\t</execution>";
}
