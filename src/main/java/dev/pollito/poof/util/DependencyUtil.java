package dev.pollito.poof.util;

public class DependencyUtil {
    private DependencyUtil(){}
    public static final String CONSUMER_EXECUTION_TEMPLATE = """
            <execution>
              <id>consumer generation - <!--name--></id>
              <goals>
                <goal>generate</goal>
              </goals>
              <configuration>
                <inputSpec>${project.basedir}/src/main/resources/openapi/<!--name-->.yaml</inputSpec>
                <generatorName>java</generatorName>
                <library>feign</library>
                <output>${project.build.directory}/generated-sources/openapi/</output>
                <apiPackage><!--apiPackage--></apiPackage>
                <modelPackage><!--modelPackage--></modelPackage>
                <configOptions>
                  <feignClient>true</feignClient>
                  <interfaceOnly>true</interfaceOnly>
                  <useEnumCaseInsensitive>true</useEnumCaseInsensitive>
                </configOptions>
              </configuration>
            </execution>
            """;
}
