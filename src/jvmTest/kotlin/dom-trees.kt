package kotlinx.html.tests

import kotlinx.html.Entities
import kotlinx.html.HtmlBlockTag
import kotlinx.html.HtmlHeadTag
import kotlinx.html.HtmlInlineTag
import kotlinx.html.ScriptType
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.classes
import kotlinx.html.consumers.filter
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.dom.create
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.dom.document
import kotlinx.html.dom.prepend
import kotlinx.html.dom.serialize
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.id
import kotlinx.html.meta
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.svg
import kotlinx.html.unsafe
import org.w3c.dom.Element
import kotlin.test.Test
import kotlin.test.assertEquals

class TestDOMTrees {
    @Test fun `able to create simple tree`() {
        val tree = createHTMLDocument().div {
            id = "test-node"
            +"content"
        }

        assertEquals("div", tree.getElementById("test-node")?.tagName?.lowercase())
    }

    @Test fun `able to create complex tree and render it with pretty print`() {
        val tree = createHTMLDocument().html {
            body {
                h1 {
                    +"header"
                }
                div {
                    +"content"
                    span {
                        +"yo"
                    }
                }
            }
        }

        assertEquals("<!DOCTYPE html>\n<html><body><h1>header</h1><div>content<span>yo</span></div></body></html>", tree.serialize(false))
        assertEquals("""
                <!DOCTYPE html>
                <html>
                  <body>
                    <h1>header</h1>
                    <div>content<span>yo</span>
                    </div>
                  </body>
                </html>""".trimIndent(), tree.serialize(true).trim().replace("\r\n", "\n"))
    }

    @Test fun `vals create and append support`() {
        val document = createHTMLDocument().html {
            body {
                div {
                    id = "content"
                }
            }
        }

        val contentNode = document.getElementById("content")!!
        contentNode.append.p {
            +"p1"
        }

        val p2 = document.create.p {
            +"p2"
        }
        contentNode.appendChild(p2)

        assertEquals("""<!DOCTYPE html>
<html>
  <body>
    <div id="content">
      <p>p1</p>
      <p>p2</p>
    </div>
  </body>
</html>
        """.trim().replace("\r\n", "\n"), document.serialize(true).trim().replace("\r\n", "\n"))
    }

    @Test fun `append function support`() {
        val document = createHTMLDocument().html {
            body {
                div {
                    id = "content"
                }
            }
        }

        val contentNode = document.getElementById("content")!!

        val p1Element: Element
        val nodes = contentNode.append {
            p1Element = p {
                +"p1"
            }
            p {
                +"p2"
                p {
                    +"p3"
                }
            }
        }

        assertEquals("p1", p1Element.textContent)
        assertEquals(2, nodes.size)
        assertEquals("""<!DOCTYPE html>
<html>
  <body>
    <div id="content">
      <p>p1</p>
      <p>p2<p>p3</p>
      </p>
    </div>
  </body>
</html>
        """.trim().replace("\r\n", "\n"), document.serialize(true).trim().replace("\r\n", "\n"))
    }

    @Test fun `should compile wiki example`() {
        println(document {
            append.filter { if (it.tagName == "div") SKIP else PASS }.html {
                body {
                    div {
                        a { +"link1" }
                    }
                    a { +"link2" }
                }
            }
        }.serialize())
    }

    @Test fun `svg should have namespace`() {
        val htmlElement: Element
        val d = document {
            htmlElement = append.html {
                body {
                    svg {
                    }
                }
            }
        }

        assertEquals("", htmlElement.textContent)
        assertEquals("<!DOCTYPE html>\n<html><body><svg xmlns=\"http://www.w3.org/2000/svg\"></svg></body></html>",
                d.serialize(false).trim().replace("\r\n", "\n"))
    }

    @Test fun `generalize tests`() {
        fun <T> T.genericFlow() where T : HtmlBlockTag {
            classes += "aha"
            +"content"
            +Entities.nbsp
            div {
            }
        }

        fun <T> T.genericPhrasing() where T : HtmlInlineTag {
            classes += "aha"
            +"content"
            +Entities.nbsp
            span { }
        }

        fun <T> T.genericMetaData() where T : HtmlHeadTag {
            classes += "aha"
            +"content"
            +Entities.nbsp
            meta("a")
            script(ScriptType.textJavaScript) { }
        }

        document {
            append.html {
                head {
                    genericMetaData()
                }
                body {
                    div {
                        genericFlow()
                        p {
                            genericPhrasing()
                        }
                    }
                }
            }
        }
    }

    @Test fun `script content`() {
        val document = document {
            append.html {
                head {
                    script(ScriptType.textJavaScript) {
                        unsafe {
                            raw("fun f() { return 1; }")
                        }
                    }
                }
            }
        }


        assertEquals("<!DOCTYPE html>\n" +
                "<html><head><META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"><script type=\"text/javascript\">fun f() { return 1; }</script></head></html>",
            document.serialize(false).trim().replace("\r\n", "\n"))
    }

    @Test fun testPrepend() {
        val document = createHTMLDocument().html {
            body {
                a { text("aaa") }
            }
        }

        val okElement: Element
        document.getElementsByTagName("body").item(0).prepend {
            okElement = p {
                text("OK")
            }
        }

        assertEquals("OK", okElement.textContent)
        assertEquals("<!DOCTYPE html>\n" +
                "<html><body><p>OK</p><a>aaa</a></body></html>",
            document.serialize(false).trim().replace("\r\n", "\n"))
    }

    @Test fun testComment() {
        val document = createHTMLDocument().html {
            comment("commented")
        }

        assertEquals("<!DOCTYPE html>\n" +
                "<html><!--commented--></html>",
            document.serialize(false).trim().replace("\r\n", "\n"))
    }
}
