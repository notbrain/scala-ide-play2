package org.scalaide.play2.templateeditor.lexical

import scala.tools.eclipse.testsetup.SDTTestUtils
import scala.tools.eclipse.testsetup.TestProjectSetup

import org.eclipse.jdt.internal.core.util.SimpleDocument
import org.junit.AfterClass
import org.junit.Assert._
import org.junit.Test
import org.scalaide.play2.templateeditor.TemplateCompilationUnit

object TemplateCompilationUnitTest extends TestProjectSetup("aProject", bundleName = "org.scala-ide.play2.tests") {
  @AfterClass
  def projectCleanUp() {
    SDTTestUtils.deleteProjects(project)
  }
}

class TemplateCompilationUnitTest {
  import TemplateCompilationUnitTest._

  /**
   * Test the following sequence:
   *   - content1 -> generated1
   *   - content2 -> generated2
   *   - content1 -> generated1 (used to return generated2, see ticket #30)
   */
  @Test
  def cachedContentIsCorrectlyUpdated() {
    val indexFile = file("app/views/index.scala.html")
    val templateCU = TemplateCompilationUnit(indexFile)

    val content1 = SDTTestUtils.slurpAndClose(indexFile.getContents())

    val document = new TestDocument(content1)
    templateCU.connect(document)

    val generated1 = templateCU.generatedSource.get.content

    val content2 = "@(messages: String)\n<html><body>@messages</body></html>\n"
    document.set(content2)

    val generated2 = templateCU.generatedSource.get.content

    assertFalse("generated1 and generated2 should be different", generated1 == generated2)

    document.set(content1)

    val generated1_2 = templateCU.generatedSource.get.content

    assertEquals("Same content should return the same generated code", generated1, generated1_2)

  }

  @Test
  def no_scala_source_is_generated_when_there_are_template_parse_errors() {
    val tFile = file("app/views/template_parse_error.scala.html")
    val tu = TemplateCompilationUnit(tFile)
    assertTrue(tu.generatedSource.isFailure)
  }
  
  @Test
  def scala_source_is_generated_when_there_are_scala_compiler__errors() {
    val tFile = file("app/views/scala_compiler_error.scala.html")
    val tu = TemplateCompilationUnit(tFile)
    assertTrue(tu.generatedSource.isSuccess)
  }
}

/**
 * Minimal Document supporting getting and setting content
 */
class TestDocument(var content: String) extends SimpleDocument("") {
  
  override def get(): String = content
  
  override def set(c: String) {
    content= c
  }
  
}