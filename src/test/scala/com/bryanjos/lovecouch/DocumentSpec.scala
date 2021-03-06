package com.bryanjos.lovecouch

import scala.concurrent.duration._
import scala.concurrent.Await
import org.scalatest._
import play.api.libs.json.Json
import akka.actor.ActorSystem

class DocumentSpec extends FunSpec with BeforeAndAfterAll {
  implicit val system = ActorSystem()
  implicit val context = system.dispatcher
  val couchDB = CouchDb()
  val db = Database(name = "documentspec", couchDB.url)

  case class Guy(_id: Option[String] = None,
                 _rev: Option[String] = None,
                 name: String, age: Long)

  implicit val guyFmt = Json.format[Guy]
  var id = ""
  var revs = List[String]()

  info("CouchDB Document Methods")

  override def beforeAll() {
    Await.result(couchDB.createDatabase("documentspec"), 5 seconds)
  }

  describe("Create a new document") {
    it("should be created") {
      val data = Guy(name = "Alf", age = 23)
      val result = db.createDocument[Guy](data) map {
        value =>
          id = value.id
          revs = revs ++ List[String](value.rev)
          assert(value.ok)
      }
      Await.result(result, 5 seconds)
    }
  }


  describe("Returns the latest revision of the document") {
    it("should be return the wanted document") {
      val result = db.getDocument[Guy](id) map {
        value =>
          assert(value.age == 23)
          assert(value.name == "Alf")
          assert(value._id.get == id)
      }
      Await.result(result, 5 seconds)
    }


  }


  describe("Inserts a new document, or new version of an existing document") {
    it("should be updated") {
      val data = Guy(_id = Some(id), _rev = Some(revs.last), name = "Alf", age = 24)

      val result = db.updateDocument[Guy](data, id) map {
        value =>
          revs = revs ++ List[String](value.rev)
          assert(value.ok)
      }
      Await.result(result, 5 seconds)
    }


  }


  describe("Adds an attachment of a document") {
    it("should be added") {
      val result = db.addAttachment(id,
        revs.last,
        "README.md",
        new java.io.File("/Users/bryanjos/Projects/Personal/lovecouch/README.md"),
        "text/plain") map {
        value =>
          revs = revs ++ List[String](value.rev)
          assert(value.ok)
      }
      Await.result(result, 5 seconds)
    }
  }


  describe("Gets the attachment of a document") {
    it("should be a byte array with non zero bytes") {
      val result = db.getAttachment(id, "README.md") map {
        value =>
          assert(!value.isEmpty)
      }

      Await.result(result, 5 seconds)
    }


  }


  describe("Returns the a revision of the document") {
    it("should be a previous revision of the document") {
      val result = db.getDocument[Guy](id, Some(revs.head)) map {
        value =>
          assert(value.age == 23)
          assert(value.name == "Alf")
          assert(value._id.get == id)
      }

      Await.result(result, 5 seconds)
    }


  }


  describe("Deletes the document") {
    it("should be deleted") {
      val result = db.deleteDocument(id, revs.last) map {
        value =>
          assert(value.ok)
      }

      Await.result(result, 5 seconds)
    }


  }

  override def afterAll() {
    Await.result(couchDB.deleteDatabase("documentspec"), 5 seconds)
  }
}
