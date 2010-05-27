package com.flowfact

import org.specs._
import com.osinka.mongodb.Preamble._
import runner.JUnitSuiteRunner
import org.junit.runner.RunWith
import com.osinka.mongodb.{MongoCollection, DBObjectCollection}
import com.osinka.mongodb.shape.ShapedCollection
import com.mongodb.{DBObject, DB, Mongo}

@RunWith(classOf[JUnitSuiteRunner])
class WebshopSpec extends SpecificationWithJUnit {
  "The webshop application" should {
    val mongo = new Mongo()
    val db = mongo.getDB("webshop")
    val dbCollection = db.getCollection("customer")

    doBefore {
      dbCollection.remove(Map())
    }

    val customerCollection: DBObjectCollection = dbCollection.asScala

    val customers: ShapedCollection[Customer] = dbCollection of Customer
    val customerOttoNormal = new Customer
    customerOttoNormal.id = "1234"
    customerOttoNormal.name = "Otto Normal"
    customerOttoNormal.city = "Berlin"
    customerOttoNormal.numberOfOrders = 4

    val bankData = new BankData("9876543210", "30020011", "Otto Normal")
    customerOttoNormal.bankData = bankData

    "work with MongoDB" in {
      customerCollection << Map("id" -> "4711",
        "name" -> "Max Mustermann",
        "city" -> "Cologne",
        "numberOfOrders" -> 3)
      customerCollection must exist {_.get("id") == "4711"} // {dbObject : DBObject => dbObject.get("id") == "4711"}
    }

    "find a customer by city" in {
      customers << customerOttoNormal
      val result = Customer where {Customer.city eq_? "Berlin"} take 1 in customers
      result must haveSize(1)
      val savedCustomer = result.toList(0)
      savedCustomer.id mustEqual "1234"
      savedCustomer.bankData.bankCode mustEqual "30020011"
    }

    "increment the number of orders if the customer place an order" in {
      customers << customerOttoNormal
      customers.update(Customer.id eq_? "1234", Customer.numberOfOrders inc 1)

      val foundCustomers = Customer where {Customer.numberOfOrders is_> 4} take 1 in customers
      foundCustomers.firstOption must beSome[Customer].which {_.numberOfOrders == 5}
    }
  }


}
