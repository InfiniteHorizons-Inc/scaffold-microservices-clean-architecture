package co.infinitehorizons.scaffold.microservices;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ScaffoldPluginFunctionalTest {

    @TempDir
    File testProjectDir;

    @Test
    void testScaffoldInfoTask() throws IOException {
        File buildFile = new File(testProjectDir, "build.gradle");

        try (FileWriter writer = new FileWriter(buildFile)) {
            writer.write("plugins { id 'com.infinitehorizons.scaffold.microservices' }\n");

            writer.write("scaffoldMicroServices {\n");
            writer.write("    packageName = 'com.infinitehorizons.demo'\n");
            writer.write("    companyName = 'Infinite Horizons Inc.'\n");
            writer.write("    projectName = 'Demo Ecosystem'\n");
            writer.write("}\n");
        }

        BuildResult result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withArguments("scaffoldInfo", "--info")
                .withPluginClasspath()
                .build();

        String output = result.getOutput();
        System.out.println("LOGS DEL TEST:\n" + output);

        assertTrue(output.contains("Infinite Horizons Inc."), "Debe mostrar el nombre de la empresa");
        assertTrue(output.contains("com.infinitehorizons.demo"), "Debe mostrar el paquete configurado");
    }
}