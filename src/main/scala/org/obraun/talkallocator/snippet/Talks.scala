package org.obraun.talkallocator
package snippet

import scala.xml._
import net.liftweb._
import mapper._
import util.Helpers._
import common._
import http.S._
import http.SHtml._
import model._

object Talks {
  def available = talksAsTable(true)
  def allocated = talksAsTable(false)
  
  def talksAsTable(available: Boolean) = {
    
    /**
     * Zum Ausgeben des Sprechers.
     * 
     * @param speakerID Fremdschlüssel des Sprechers in [[model.Talk]].
     */
    def speaker(speakerID: MappedLong[Talk]) = {
      // Ermitteln des User-Objektes zur speaker id
      val speaker = User.find(By(User.id,speakerID)).get
      // Erzeugen eines Objektes vom Typ 'scala.xml.Text'
      Text(speaker.firstName+" "+speaker.lastName)
    }
    
    /**
     * Berechnung der anzuzeigenden Talks.
     * Freie Talks haben in der Datenbank den Wert 'Null' für 'speaker'.
     */
    val talks = Talk.findAll(
      // der 'QueryParam' 'NullRef' ermittelt alle freien Talks
      if(available) NullRef(Talk.speaker)
      // findet alle vergebenen Talks
      else NotNullRef(Talk.speaker)
    )
    
    // Formulierung des Ergebniswertes: eine 'NodeSeq' als Tabelle
    <table>
    { talks.map{
        talk =>
          <tr>
            <th>{talk.title}</th>
            { if(!available)
              <th width="20%">
                ({speaker(talk.speaker)})
              </th>
            }
          </tr>
      }
    }
    </table>
  }
  
  def choose = {
    /**
     * Der eingeloggte Benutzer.
     */
    val user = User.currentUser.open_!
    /**
     * Liste der ausgewählten Talks. Nur ein Talk kann ausgewählt sein.
     */
    val chosen = Talk.findAll(By(Talk.speaker,user.id))
    /**
     * Liste der verfügbaren Talks.
     */
    val available = Talk.findAll(NullRef(Talk.speaker))
    /**
     * Eventuell ausgewählter Titel.
     */
    var newTitle: Option[String] = None
    
    def chooseTalk(maybeTitle: Option[String]) = {
      val hasOld = !chosen.isEmpty
      
      maybeTitle match {
        // der eventuell vorher bereits gewählte Talk wird zurückgesetzt
        case None if hasOld	=> chosen.head.speaker(Empty).save
        // Talk wird ermittelt
        case Some(title)	=> Talk.find(By(Talk.title,title)) match {
          case Full(talk) => if(hasOld) {
            val old = chosen.head
            if(old.title != talk.title) {
              old.speaker(Empty).save
              talk.speaker(user.id).save
            }
          } else talk.speaker(user.id).save
          case _ => error("Talk "+title+"not found")
        }
        case _ =>
      }
      // auf Startseite umleiten
      redirectTo("/") // Methode des 'S'-Objektes, das den aktuellen Zustand des HTTP-Request und -Response repräsentiert
    }
    
    /**
     * Erzeugen einer Gruppe von Radio-Buttons mit der Methode 'radio'
     * des SHtml-Objektes.
     * 
     * Parameter:
     * 1. die verschiedenen Optionen
     * 2. die vorausgewählte Option als Box (Empty = keine Vorauswahl)
     * 3. Funktion, die mit der gewählten Option ausgeführt wird
     */
    val talks = radio(
      (chosen:::available).map{_.title.toString},
      if(chosen.isEmpty)
        Empty
      else
        Full(chosen.head.title),
      title => newTitle = Some(title)
    ).toForm // umwandlung in eine 'NodeSeq'
    
    /**
     * Button mit der Methode 'submit' mit Label und Funktion zum ausführen
     * nach dem Klicken.
     */
    val choose = submit(
      "Auswählen",
      () => chooseTalk(newTitle)
    )
    
    val chooseNone = submit(
      "Keinen Talk übernehmen",
      () => chooseTalk(None)
    )
    
    talks :+ choose :+ chooseNone
  }
  
  def add(html: NodeSeq) = {
    var title = ""
    
    /**
     * Hinzufügen eines Talks in die Datenbank, wenn der
     * Titel nicht der leere String und nicht schon in der DB
     * vorhanden ist.
     */
    def addTalk(title: String) = {
      if(title!="" && Talk.find(By(Talk.title,title)).isEmpty)
        Talk.create.title(title).save
    }
    
    // Erzeugen der 'NodeSeq' mit 'bind' des Objektes 'Helpers' aus dem Package 'net.liftweb.util'
    // Hierbei werden Teile einer bestehenden 'NodeSeq' geändert.
    // Argumente:
    // 1. Namespace (siehe auch 'add.html'), tags mit entprechendem Namespace werden geändert
    // 2. 'NodeSeq', in der ersetzt werden soll
    // 3. alle weiteren Argumente sind vom Typ 'BindParam'
    bind("talk",html,
      // Ersetzen des Tags 'title' im Namespace 'talk' durch ein Textfeld mit 'SHtml.text'
      "title"	-> text("",t => title = t.trim),
      // Ersetzen von des Tags 'talk:add' durch einen Button der die Funktion 'addTalk' ausführt
      "add"		-> submit("Hinzufügen", () => addTalk(title))
    )
  }
  
  def delete = {
    import scala.collection.mutable.Set
    /**
     * veränderbares Set für die Talks, die gelöscht werden sollen
     */
    val toDelete = Set[Talk]()
    val talks = Talk.findAll()
    
    /**
     * löscht alle übergebenen Talks
     */
    def deleteTalks(toDelete: Set[Talk]) {
      toDelete.foreach {
        talk => if(!talk.delete_!)
          error("Could not delete: "+talk.toString)
      }
    }
    
    val checkboxes = talks.flatMap(talk =>
      checkbox(
        false,
        if (_) toDelete += talk
      ) :+ Text(talk.title) :+ <br />
    )
    
    val delete = submit(
      "Löschen",
      () => deleteTalks(toDelete)
    )
    checkboxes ++ delete
  }
}