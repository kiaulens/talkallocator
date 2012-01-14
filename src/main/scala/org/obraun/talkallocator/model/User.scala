package org.obraun.talkallocator
package model

import net.liftweb.mapper._
import net.liftweb.common._

/**
 * Der Trait 'MegaProtoUser' definiert einen User, der unter
 * Verwendung des OR-Mappers gespeichert werden kann.
 */
class User extends MegaProtoUser[User] {
  /**
   * einzige abstrakte Methode, die implementiert werden muss
   * muss ein Objekt (den Meta-Server für diese Klasse) zurückgeben, welches
   * Informationen für die Datenbank bereitstellt
   */
  def getSingleton = User
}

object User extends User with MetaMegaProtoUser[User] {
  /**
   * eigener Tabellenname für die Speicherung der Benutzer
   */
  override def dbTableName = "users"
  /**
   * Spezifiziert die Darstellung der Webseite der Benutzerverwaltung
   */
  override def screenWrap = Full(
    <lift:surround with="default" at="content">
      <lift:bind />
    </lift:surround>
  )
  /**
   * neue Bentuzer können sich ohne Email-Validierung registrieren
   */
  override def skipEmailValidation = true
  
  /**
   * Erzeugen zweier Beispiel-User und einfügen in die DB, falls nicht vorhanden.
   */
  def createExampleUsers() {
    if(find(By(email,"admin@obraun.org")).isEmpty) {
      create.email("admin@obraun.org")
            .firstName("Hugo")
            .lastName("Admin")
            .password("talkadmin")
            .superUser(true)
            .validated(true)
            .save
    }
    if(find(By(email,"user@obraun.org")).isEmpty) {
      create.email("user@obraun.org")
            .firstName("Egon")
            .lastName("User")
            .password("talkuser")
            .validated(true)
            .save
    }
  }
}