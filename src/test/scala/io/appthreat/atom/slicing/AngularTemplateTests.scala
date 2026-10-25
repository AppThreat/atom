package io.appthreat.atom.slicing

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class AngularTemplateTests extends AnyWordSpec with Matchers:

  private def names(template: String) =
      AngularTemplate.reads(template).map((_, name, members, called) => (name, members, called))

  "AngularTemplate.reads" should {
      "read bare names in interpolations, bindings and events" in {
          names("""<a [href]="links.home" (click)="go($event)">{{ title }}</a>""") shouldBe List(
            ("links", List("home"), false),
            ("go", Nil, true),
            ("title", Nil, false)
          )
      }

      "skip members, pipe names, object keys and names the template declares" in {
          names("""{{ user?.name | titlecase }} <p [ngClass]="{ active: on }"></p>""") shouldBe
              List(("user", List("name"), false), ("on", Nil, false))
          names(
            """<li *ngFor="let item of items; index as i">{{ item.name }} {{ i }}</li>"""
          ) shouldBe
              List(("items", Nil, false))
          names("""<input #box (keyup)="find(box.value)">""") shouldBe List(("find", Nil, true))
      }

      "read the expressions of control flow blocks and @let" in {
          names(
            """@if (user(); as u) { {{ u.name }} } @for (t of tags; track t.id) { {{ t }} }"""
          ) shouldBe
              List(("user", Nil, true), ("tags", Nil, false))
          names("""@let total = price * count;""") shouldBe
              List(("price", Nil, false), ("count", Nil, false))
      }

      "keep a logical or, which is not a pipe" in {
          names("""{{ a || b }}""") shouldBe List(("a", Nil, false), ("b", Nil, false))
      }

      "not read string contents, attribute values or prose" in {
          names(
            """<a href="#coffee">such as coffee</a><i [title]="'star'"></i>{{ "bolt" }}"""
          ) shouldBe
              Nil
      }

      "scope a declared name to where it is declared" in {
          names("""@for (star of stars; track star) { {{ star }} } {{ star }}""").map(_._1) shouldBe
              List("stars", "star")
          names("""<li *ngFor="let t of items">{{ t }}</li>{{ t }}""").map(_._1) shouldBe
              List("items", "t")
          names("""<input #box>{{ box.value }}""") shouldBe Nil
      }

      "read unquoted values, non-null assertions, and only @defer's when conditions" in {
          names("""<i [icon]=star></i>""") shouldBe List(("star", Nil, false))
          names("""{{ fmt!.name }}""") shouldBe List(("fmt", List("name"), false))
          names("""@defer (on timer(5s); when ready) { x }""") shouldBe List(("ready", Nil, false))
      }

      "tell a template statement's writes from its reads" in {
          AngularTemplate.writes("""<b (click)="open = true; count += 1; total++"></b>""") shouldBe
              Set("open", "count", "total")
          names("""<b (click)="open = !open; same == other"></b>""").map(_._1) shouldBe
              List("open", "same", "other")
      }
  }
end AngularTemplateTests
