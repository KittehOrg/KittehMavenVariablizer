/*
 * * Copyright (C) 2016 Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.versionizer;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.IOException;

@Mojo(name = "versionizer", defaultPhase = LifecyclePhase.PROCESS_CLASSES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class Versionizer extends AbstractMojo {
    @Parameter(required = true)
    private String className;

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final String daFolder = this.project.getBuild().getOutputDirectory();
        final ClassPool classPool = new ClassPool(ClassPool.getDefault());
        try {
            classPool.appendClassPath(daFolder);
        } catch (NotFoundException e) {
            this.getLog().error("Could not play with classpath!", e);
            return;
        }
        classPool.importPackage(this.className);
        try {
            final CtClass daClass = classPool.get(this.className);
            final CtField oldField = daClass.getDeclaredField("version");
            daClass.removeField(oldField);
            final CtField newField = CtField.make("private String version=\"" + this.project.getVersion() + "\";", daClass);
            daClass.addField(newField);
            daClass.writeFile(daFolder);
        } catch (NotFoundException | IOException | CannotCompileException e) {
            this.getLog().error("Failed!", e);
            return;
        }
        this.getLog().info("Set version to " + this.project.getVersion() + "!");
    }
}
