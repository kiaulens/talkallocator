package org.obraun.talkallocator
package model

import net.liftweb.mapper._

/**
 * Ein Mapper-Trait wird in die Klasse Talk hineingemixt um den
 * OR-Mapper nutzen zu können.
 * 'LongKeyedMapper'-Trait - verwendet einen Long-Wert als Primary-Key in der DB
 * 'IdPK'-Trait - Implementierung für den Primary-Key: jedes Talk-Objekt erhält
 *                einen eindeutigen Schlüssel mit dem Bezeichner 'id'
 */
class Talk extends LongKeyedMapper[Talk] with IdPK {
  /**
   * analog zu [[User]], gibt die getSingleton Methode das Companion-Objekt 'Talk' zurück
   */
  def getSingleton = Talk
  /**
   * Definition der Felder als Objekte für den OR-Mapper.
   * Der Typ der Spalte aus der Datenbank ergibt sich aus dem verwendeten Trait.
   * Speicherung des Titels als String mit maximal 100 Zeichen.
   */
  object title extends MappedString(this,100)
  /**
   * Speicherung des Vortragenden als Long-Wert, der als Fremdschlüssel genutzt wird
   */
  object speaker extends MappedLongForeignKey(this,User)
}

/**
 * 'LongKeyedMetaMapper' fügt die benötigte Funktionalität hinzu, um den
 * OR-Mapper nutzen zu können. 
 */
object Talk extends Talk with LongKeyedMetaMapper[Talk] {
  /**
   * Eigener Name für die Tabelle in der Talks gespeichert werden.
   */
  override def dbTableName = "talks"
    
  /**
   * Erzeugen zweier Beispiel-Talks, falls nicht vorhanden.
   */
  def createExampleTalks() = {
    List(
      "Scala 2.8.0 - Was gibt's Neues?",
      "Scala - OSGi-Bundles from Outer (Java) Space"
    ).foreach {
      talk => if(find(By(title,talk)).isEmpty)
        create.title(talk).save
    }
  }
}