package com.flowfact

import com.mongodb.DBObject
import com.osinka.mongodb.shape._
import com.osinka.mongodb.MongoObject

class Customer extends MongoObject {
  var id : String = _
  var name : String = _
  var city : String = _
  var numberOfOrders : Int = _
  var bankData : BankData = _
}

class BankData(val accountNumber : String,
        val bankCode : String,
        val accountHolder : String) {
}

object Customer extends ObjectShape[Customer] {
  lazy val id = Field.scalar("id", (c : Customer) => c.id, (x: Customer, v: String) => x.id = v)
  lazy val name = Field.scalar("name", _.name, (x: Customer, v: String) => x.name = v)
  lazy val city = Field.scalar("city", _.city, (x: Customer, v: String) => x.city = v)
  lazy val numberOfOrders = Field.scalar("numberOfOrders", _.numberOfOrders, (x: Customer, v: Int) => x.numberOfOrders = v)

  object bankData extends EmbeddedField[BankData]("bankData", _.bankData, Some((x: Customer, v: BankData) => x.bankData = v)) with BankDataIn[Customer]
  override lazy val * = List(id, name, city, numberOfOrders, bankData)
  override def factory(dbo: DBObject) = Some(new Customer)
  
}

trait BankDataIn[T] extends ObjectIn[BankData, T] {
  lazy val accountNumber = Field.scalar("accountNumber", _.accountNumber)
  lazy val bankCode = Field.scalar("bankCode",_.bankCode)
  lazy val accountHolder = Field.scalar("accountHolder", _.accountHolder)

  override lazy val * = accountNumber :: bankCode :: accountHolder :: Nil
  override def factory(dbo: DBObject) =
    for{accountNumber(n) <- Some(dbo)
        bankCode(c) <- Some(dbo)
        accountHolder(h) <- Some(dbo)}
    yield new BankData(n, c, h)
}



