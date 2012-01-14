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

object Users {
  def admin = usersAsTable(true)
  def registered = usersAsTable(false)
  
  def usersAsTable(admin: Boolean) = {
    
    def userOut(user: User) = {
      // Erzeugen eines Objektes vom Typ 'scala.xml.Text'
      Text(user.firstName+" "+user.lastName)
    }
    
    /**
     * Berechnung der anzuzeigenden Benutzer.
     */
    val users = User.findAll(By(User.superUser,admin))
    
    // Formulierung des Ergebniswertes: eine 'NodeSeq' als Tabelle
    <table>
    { users.map{
        user =>
          <tr>
            <th>{userOut(user)}</th>
          </tr>
      }
    }
    </table>
  }
}